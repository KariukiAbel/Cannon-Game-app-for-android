package com.nabesh.cannongameapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
    private double pierceLength;
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
        super(context, attrs);
        activity = (Activity) context;
    }

    public void alignCannon(MotionEvent event) {
    }

    public void releaseResources() {
    }

    public void stopGame() {
    }

    public void fireCannonBall(MotionEvent e) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
