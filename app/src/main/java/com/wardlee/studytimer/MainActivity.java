package com.wardlee.studytimer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    // Tag for debugging
    private static final String TAG = "MainActivity";

    // Set up a variable to store the number of seconds
    private int seconds = 0;

    // Set up variables to store running states
    private boolean running;
    private boolean wasRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load saved time and update message
        loadSavedTime();

        // Make sure Start/Pause button shows the correct state
        updateStartButton();

        // Initialise the timer (it will run when running is set to true)
        runTimer();

        //Log.d(TAG, "onCreate run");
    }


    /**
     * Save the instance state, so we can restore it on device reorientation
     * @param savedInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("seconds", seconds);
        savedInstanceState.putBoolean("running", running);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
    }


    /**
     * Restore instance state when the device is rotated
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null) {
            seconds = savedInstanceState.getInt("seconds", seconds);
            running = savedInstanceState.getBoolean("running", running);
            wasRunning = savedInstanceState.getBoolean("wasRunning", wasRunning);
        }
    }


    /**
     * Method to save the time to shared preferences
     */
    public void saveTime() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("seconds", seconds);
        editor.putBoolean("running", running);
        editor.putBoolean("wasRunning", wasRunning);
        editor.commit();
    }


    /**
     * Method to load saved time from shared preferences
     */
    public void loadSavedTime() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        seconds = sharedPref.getInt("seconds", seconds);
        running = sharedPref.getBoolean("running", running);
        wasRunning = sharedPref.getBoolean("wasRunning", wasRunning);

        // Update the message with the saved time
        updateMessage();
    }


    /**
     * When the activity is started, check for saved values and show them
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Check for saved values
        loadSavedTime();

        // Ensure start button colour and label is correct
        updateStartButton();

        // If the timer wasn't running/paused, set seconds to 0
        if(!running) {
            seconds = 0;
        }

        //Log.d(TAG, "onStart run");
    }


    /**
     * When the activity is paused (e.g. by navigating away), pause the timer
     */
    @Override
    protected void onPause() {
       super.onPause();
       wasRunning = running;

       //Log.d(TAG, "onPause run");
    }


    /**
     * When the activity is resumed, start the timer again if it was running
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (wasRunning) {
            running = true;
        }

        // Make sure the button is correct
        updateStartButton();

        //Log.d(TAG, "onResume run");
    }


    /**
     * When the activity is stopped,
     * pause the timer and save the time to shared preferences
     */
    @Override
    protected void onStop() {
        super.onStop();

        // Stop the timer
        running = false;

        //Log.d(TAG, "onStop run");
    }


    /**
     * If the app is exited/destroyed by the system, save the time
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            saveTime();
        } else {
            //It's an orientation change, don't do anything
        }

       //Log.d(TAG, "onDestroy run");
    }


    /**
     * Method to set the start/pause button according to the current running state
     */
    public void updateStartButton() {
        Button startButton = findViewById(R.id.button_start);

        if(running) {
            startButton.setText("Pause");
            startButton.setBackgroundColor(getColor(R.color.colorPause));
            startButton.setTextColor(getColor(R.color.colorDark));
        }
        else {
            startButton.setText("Start");
            startButton.setBackgroundColor(getColor(R.color.colorStart));
            startButton.setTextColor(getColor(R.color.colorLight));
        }
    }


    /**
     * Start/pause button
     */
    public void startTimer(View view) {

        // Swap the running state
        if(running) {
            running = false;
        }
        else {
            running = true;
        }

        // Update the button
        updateStartButton();
    }


    /**
     * Stop button
     */
    public void stopTimer(View view) {

        // Update the message
        updateMessage();

        // Stop the activity
        running = false;
        wasRunning = false;

        // Save the current time
        saveTime();

        // Reset seconds to 0
        seconds = 0;

        // Change the start/pause button back
        updateStartButton();
    }


    /**
     * Method to build a string of the time
     */
    public String createTimeString(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
        return time;
    }


    /**
     * Method to update the previous session message
     * This gets whatever the current value of "seconds" is and uses it to set the message
     * e.g. when run directly after loadSavedTime() it will show the saved time,
     *      when run when the timer is stopped it will show the current time
     */
    public void updateMessage() {
        // The field
        TextView savedMessage = findViewById(R.id.textView_savedTime);

        // The time
        String time = createTimeString(seconds);

        // Set the message
        savedMessage.setText("You spent " + time + " studying last time");
    }


    /**
     * Timer method
     */
    private void runTimer() {

        // Get the textView that holds the time
        final TextView clock = findViewById(R.id.textView_timer);

        // Create a new Handler
        final Handler timeHandler = new Handler();

        // Use the post() method with a new runnable
        // The post() method runs code without a delay
        timeHandler.post(new Runnable() {
            @Override
            public void run() {

                // Get and format the time
                String time = createTimeString(seconds);

                // Set the text view text
                clock.setText(time);

                // If running is true, increment the seconds
                if(running) {
                    seconds++;
                }

                // Post the code again with a delay of 1 second.
                timeHandler.postDelayed(this, 1000);
            }
        });
    }
}
