package com.nabesh.cannongameapp;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

//Thread subclass to control the game loop
class CannonThread  extends Thread{

    private SurfaceHolder surfaceHolder;
    CannonView cannonView;
    private boolean threadIsRunning = true;

    public CannonThread(SurfaceHolder holder) {
        surfaceHolder = holder;
        setName("CannonThread");
    }

    public void setRunning(boolean running){
        threadIsRunning = running;
    }

    @Override
    public void run() {
        Canvas canvas = null;
        long previousFrameTime = System.currentTimeMillis();

        while (threadIsRunning){
            try {
                canvas = surfaceHolder.lockCanvas();

                //lock the surfaceHolder for drawing
                synchronized (surfaceHolder){
                    long currentTime = System.currentTimeMillis();
                    double elapsedTimeMS = currentTime - previousFrameTime;
                    double totalElapsedTime = elapsedTimeMS / 1000.0;
                    cannonView.updatePositions();
                    cannonView.drawGameElements(canvas);
                    previousFrameTime = currentTime;
                }
            }finally {
                if (canvas != null){
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}
