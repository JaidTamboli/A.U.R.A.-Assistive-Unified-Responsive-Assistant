package com.example.aura.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aura.MainActivity;
import com.example.aura.R;
import com.example.aura.activities.environment.CameraActivity;

import java.util.Locale;

public class EnvironmentActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private int volumePressCount = 0;
    private long lastVolumePressTime = 0;
    private static final int TIME_INTERVAL = 1500; // 1.5 seconds between volume presses

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environment); // Make sure you have this layout

        // Initialize TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US); // Default
                speakInstructions();
            } else {
                Toast.makeText(this, "Text to Speech Initialization Failed!", Toast.LENGTH_SHORT).show();
            }
        });

        // Tap screen to capture and start CameraActivity
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Toast.makeText(this, "Screen tapped! Starting camera...", Toast.LENGTH_SHORT).show();
                // Start CameraActivity
                Intent intent = new Intent(this, CameraActivity.class);
                startActivity(intent);

                // Announce that the camera is starting
                if (tts != null) {
                    tts.speak("Camera is started", TextToSpeech.QUEUE_FLUSH, null, null);
                }
                return true;
            }
            return false;
        });
    }

    private void speakInstructions() {
        String msg = "For language selection, press volume button one time for Marathi, two times for Hindi, and three times for English. Tap on screen to capture image. Press Back to return to dashboard.";
        if (tts != null) {
            tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    // Detect volume button presses
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastVolumePressTime < TIME_INTERVAL) {
                volumePressCount++;
            } else {
                volumePressCount = 1;
            }
            lastVolumePressTime = currentTime;

            handleLanguageSelection(volumePressCount);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void handleLanguageSelection(int count) {
        if (tts != null) {
            switch (count) {
                case 1:
                    tts.setLanguage(new Locale("mr")); // Marathi
                    tts.speak("You selected Marathi", TextToSpeech.QUEUE_FLUSH, null, null);
                    break;
                case 2:
                    tts.setLanguage(new Locale("hi")); // Hindi
                    tts.speak("You selected Hindi", TextToSpeech.QUEUE_FLUSH, null, null);
                    break;
                case 3:
                    tts.setLanguage(Locale.ENGLISH);
                    tts.speak("You selected English", TextToSpeech.QUEUE_FLUSH, null, null);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (tts != null) {
            tts.speak("Dashboard is opened", TextToSpeech.QUEUE_FLUSH, null, null);
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
