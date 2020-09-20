package com.nabesh.cannongameapp;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.net.rtp.AudioStream;
import android.os.Bundle;
import android.view.GestureDetector;

public class CannonGame extends AppCompatActivity {
    private GestureDetector gestureDetector; //listens to double tap on the screen
    private CannonView cannonView; //custom view to display the game

    //called when the app first launches
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //inflates the layout

        //get the cannon view
        cannonView = findViewById(R.id.cannonView);

        //initialize the GestureDetector
        gestureDetector = new GestureDetector(this,gestureListener);

        //allow volume keys to set game volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    //when the app is pushed to the background, pause it
    @Override
    public void onPause() {
        super.onPause(); //call the super method
        cannonView.stopGame(); //terminates the game
    }

    //release resources
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cannonView.releaseResources();
    }
}
