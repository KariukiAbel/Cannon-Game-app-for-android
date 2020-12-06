package com.nabesh.cannongameapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.HashMap;
import java.util.Map;


public class CannonView extends SurfaceView implements SurfaceHolder.Callback {
    private CannonThread cannonThread;
    private Activity activity; //to display Game over dialog in GUI thread
    private boolean dialogIsDisplayed = false;

    //constants for the game play
    public static final int TARGET_PIECES = 7;
    public static final int MISS_PENALTY = 2;
    public static final int HIT_REWARD = 3;

    //variables for the game loop and tracking statistics
    private boolean gameOver;
    private double timeLeft;
    private int shotsFired;
    private double totalTimeElapsed;

    //variables for the blocker and target
    private Line blocker;
    private int blockerDistance;
    private int blockerBeginning;
    private int blockerEnd;
    private int initialBlockerVelocity;
    private float blockerVelocity;

    private Line target;
    private int targetDistance;
    private int targetBeginning;
    private double pieceLength;
    private int targetEnd;
    private int initialTargetVelocity;
    private float targetVelocity;

    private int lineWidth;
    private boolean[] hitStates;
    private int targetPiecesHit;

    //variables for the cannon and cannonball
    private Point cannonball;
    private int cannonballVelocityX;
    private int cannonballVelocityY;
    private boolean cannonballOnScreen;
    private int cannonballRadius;
    private int cannonballSpeed;
    private int cannonBaseRadius;
    private int cannonLength;
    private Point barrelEnd;
    private int screenWidth;
    private int screenHeight;

    //constants and variables for managing sounds
    private static final int TARGET_SOUND_ID = 0;
    private static final int CANNON_SOUND_ID = 1;
    private static final int BLOCKER_SOUND_ID = 2;
    private SoundPool soundPool;    //plays sound effects
    private Map<Integer, Integer> soundMap; //maps IDs toSoundPool

    //Paint variables used when drawing each item on the screen
    private Paint textPaint;
    private Paint cannonballPaint;
    private Paint cannonPaint;
    private Paint blockerPaint;
    private Paint targetPaint;
    private Paint backgroundPaint;


    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs); //calls super's constructor
        activity = (Activity) context;

        //register SurfaceHolder.Callback listener
        getHolder().addCallback(this);

        //initialize other classes
        blocker = new Line(); //create the blocker as a line
        target = new Line(); //create the target as a line
        cannonball = new Point(); //create cannon ball as a point

        //initialize the hitStates as a boolean array
        hitStates = new boolean[TARGET_PIECES];

        //initialize the SoundPool to play the three sound effects
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        //create Map of sounds and preload sounds
        soundMap = new HashMap<Integer, Integer>(); //creates a new HashMap
//        soundMap.put(TARGET_SOUND_ID, soundPool.load(context, R.raw.target_hit, 1));
//        soundMap.put(CANNON_SOUND_ID, soundPool.load(context, R.raw.cannon_fire, 1));
//        soundMap.put(BLOCKER_SOUND_ID, soundPool.load(context, R.raw.blocker_hit, 1));

        //Constructs for the paint
        textPaint = new Paint();
        cannonballPaint = new Paint();
        cannonPaint = new Paint();
        blockerPaint = new Paint();
        targetPaint = new Paint();
        backgroundPaint = new Paint();
    }

    //called when the size of this view changes --including when this view is first added to the view hierarchy
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;

        cannonBaseRadius = h / 18;
        cannonLength = w / 8;

        cannonballRadius = w / 36;
        cannonballSpeed = w * 3 / 2;

        lineWidth = w / 24;

        blockerDistance = w *5 / 8;
        blockerBeginning = h / 8;
        blockerEnd = h * 3 / 8;
        initialBlockerVelocity = h / 2;
        blocker.start = new Point(blockerDistance, blockerBeginning);
        blocker.end = new Point(blockerDistance, blockerEnd);

        targetDistance = w * 7 / 8;
        targetBeginning = h / 8;
        targetEnd = h * 7 / 8;
        pieceLength = (targetEnd - targetBeginning) / TARGET_PIECES;
        initialTargetVelocity = -h / 4;
        target.start = new Point(targetDistance, targetBeginning);
        target.end = new Point(targetDistance, targetEnd);

        barrelEnd = new Point(cannonLength, h / 2);

        textPaint.setTextSize(w / 20);
        textPaint.setAntiAlias(true);
        cannonballPaint.setStrokeWidth(lineWidth * 1.5f);
        blockerPaint.setStrokeWidth(lineWidth);
        targetPaint.setStrokeWidth(lineWidth);
        backgroundPaint.setColor(Color.WHITE);
        
        newGame();

    }

    //reset all the screen elements and start a new game
    public void newGame() {
        for (int i = 0; i < TARGET_PIECES; ++i) {
            hitStates[i] = false;
        }

        targetPiecesHit = 0;
        blockerVelocity = initialBlockerVelocity;
        targetVelocity = initialTargetVelocity;
        timeLeft = 10;
        cannonballOnScreen = false;
        shotsFired = 0;
        totalTimeElapsed = 0.0;
        blocker.start.set(blockerDistance, blockerBeginning);
        blocker.end.set(blockerDistance, blockerEnd);
        target.start.set(targetDistance, targetBeginning);
        target.end.set(targetDistance, targetEnd);

        if (gameOver){
            gameOver = false;
            cannonThread = new CannonThread(getHolder());
            cannonThread.start();
        }
    }

    //called repeatedly by the CannonThread to update game elements
    private void updatePositions(double elapsedTimeMS){
        double interval = elapsedTimeMS / 100; //converts to seconds
        if (cannonballOnScreen){ //if there is currently a shot fired
            //update cannon ball position
            cannonball.x += interval * cannonballVelocityX;
            cannonball.y += interval * cannonballVelocityY;

            //check for collision with the blocker
            if (cannonball.x + cannonballRadius > blockerDistance &&
                    cannonball.x - cannonballRadius < blockerDistance &&
                    cannonball.y + cannonballRadius > blocker.start.y &&
                    cannonball.y - cannonballRadius < blocker.end.y){
                cannonballVelocityX *= -1; //reverse cannonball's direction
                timeLeft -= MISS_PENALTY; //penalize the user

                //play blocker sound
                soundPool.play(soundMap.get(BLOCKER_SOUND_ID), 1, 1,1,0, 1f);
            }

            //check for collision with left and right walls
            else if (cannonball.x + cannonballRadius > screenWidth || cannonball.x - cannonballRadius < 0){
                cannonballOnScreen = false; //remove cannonball from the screen
            }

            //check for collision with the top and bottom walls
            else if (cannonball.y + cannonballRadius > screenHeight || cannonball.y - cannonballRadius < 0){
                cannonballOnScreen = false;
            }

            //check for cannonball collision with the target
            else if (cannonball.x + cannonballRadius > targetDistance &&
                    cannonball.x - cannonballRadius < targetDistance &&
                    cannonball.y + cannonballRadius > target.start.y &&
                    cannonball.x - cannonballRadius < target.end.y){
                //determine target section number(0 is the top)
                int section = (int) ((cannonball.y - target.start.y) / pieceLength);

                //check if the piece hasn't been hit yet
                if ((section >= 0 && section < TARGET_PIECES) && !hitStates[section]){
                    hitStates[section] = true; //section was hit
                    cannonballOnScreen = false; //remove cannonBall
                    timeLeft += HIT_REWARD;

                    //play target hit sound
                    soundPool.play(soundMap.get(TARGET_SOUND_ID), 1, 1, 1, 0, 1f);

                    //if all pieces have been hit
                    if (++targetPiecesHit == TARGET_PIECES)
                        cannonThread.setRunning(false);
                        showGameOverDialog(R.string.win); //shows winning dialog
                        gameOver = true;
                }
            }
        }

        //update blocker position
        double blockerUpdate = interval * blockerVelocity;
        blocker.start.y += blockerUpdate;
        blocker.end.y += blockerUpdate;

        //update the target's position
        double targetUpdate  = interval * targetVelocity;
        target.start.y  += targetUpdate;
        target.end.y += targetUpdate;

        //if the blocker hit the top or bottom, reverse direction
        if (blocker.start.y < 0 || blocker.end.y > screenHeight){
            blockerVelocity *= -1;
        }

        //if the target hit top or bottom, reverse direction
        if (target.start.y < 0 || target.end.y > screenHeight){
            targetVelocity *= -1;
        }
        timeLeft -= interval;

        //if the timer gets to zero
        if (timeLeft <= 0){
            timeLeft = 0.0;
            gameOver = true;
            cannonThread.setRunning(false);
            showGameOverDialog(R.string.lose);
        }
    }

    private void showGameOverDialog(int win) {
    }

    public void alignCannon(MotionEvent event) {
    }

    public void releaseResources() {
        soundPool.release();
        soundPool = null;
    }

    public void stopGame() {
        if (cannonThread != null){
            cannonThread.setRunning(false);
        }
    }

    public void fireCannonBall(MotionEvent e) {
        if (cannonballOnScreen)
            return;
        double angle = alignCannon(e); //get the cannonbarrel's angle

        //move the cannonball inside the cannon
        cannonball.x = cannonballRadius;
        cannonball.y = screenHeight / 2;

        //getting the x component of total velocity
        cannonballVelocityX = (int) (cannonballSpeed * Math.sin(angle));

        //getting the Y component of the total velocity
        cannonballVelocityY = (int) (-cannonballSpeed * Math.cos(angle));
        cannonballOnScreen = true;
        ++shotsFired;

        //play cannon fired sound
        soundPool.play(soundMap.get(CANNON_SOUND_ID), 1, 1, 1, 0, 1f);
    }

    public void drawGameElements(Canvas canvas) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        cannonThread = new CannonThread(surfaceHolder);
        cannonThread.setRunning(true);
        cannonThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //ensure the thread terminates correctly
        boolean retry = true;
        cannonThread.setRunning(false);

        while(retry){
            try {
                cannonThread.join();
                retry = false;
            }catch (InterruptedException e){
            }
        }
    }
}
