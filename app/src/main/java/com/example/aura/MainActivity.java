package com.example.aura;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.aura.activities.CallActivity;
import com.example.aura.activities.MessageActivity;
import com.example.aura.activities.EnvironmentActivity;
import com.example.aura.activities.LocationActivity;
import com.example.aura.activities.BatteryActivity;
import com.example.aura.activities.MusicActivity;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    private static final int REQUEST_PERMISSION_CODE = 101;

    private CardView cardCall, cardMessage, cardEnvironment, cardLocation, cardBattery, cardMusic;
    private Button btnVoice;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Layout with ConstraintLayout + NestedScrollView


        // Initialize UI components
        cardCall = findViewById(R.id.cardCall);
        cardMessage = findViewById(R.id.cardMessage);
        cardEnvironment = findViewById(R.id.cardEnvironment);
        cardLocation = findViewById(R.id.cardLocation);
        cardBattery = findViewById(R.id.cardBattery);
        cardMusic = findViewById(R.id.cardMusic);
        btnVoice = findViewById(R.id.btnVoice);

        // Initialize Text-to-Speech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        // Card click listeners - open respective activities and play sound
        cardCall.setOnClickListener(v -> {
            speak("Calling feature activated");
            openActivity(CallActivity.class);
        });

        cardMessage.setOnClickListener(v -> {
            speak("Messaging feature activated");
            openActivity(MessageActivity.class);
        });

        cardEnvironment.setOnClickListener(v -> {
            speak("Environment feature activated");
            openActivity(EnvironmentActivity.class);
        });

        cardLocation.setOnClickListener(v -> {
            speak("Location feature activated");
            openActivity(LocationActivity.class);
        });

        cardBattery.setOnClickListener(v -> {
            speak("Battery status feature activated");
            openActivity(BatteryActivity.class);
        });

        cardMusic.setOnClickListener(v -> {
            speak("Music player activated");
            openActivity(MusicActivity.class);
        });

        // Voice command button
        btnVoice.setOnClickListener(v -> startVoiceRecognition());
    }

    private void startVoiceService() {
        // Start foreground service for hotword detection (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent serviceIntent = new Intent(this, VoiceService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        } else {
            Intent serviceIntent = new Intent(this, VoiceService.class);
            startService(serviceIntent);
        }
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
    }

    private void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show(); // Optional
    }


    private void playSound(String soundName) {
        int soundResourceId = getResources().getIdentifier(soundName, "raw", getPackageName());
        if (soundResourceId != 0) {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, soundResourceId);
            mediaPlayer.start();
        } else {
            speak("Sound for " + soundName + " not found.");
        }
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a command like 'Call', 'Message', 'Music'...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Voice recognition not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String command = result.get(0).toLowerCase(Locale.ROOT);
                handleVoiceCommand(command);
            }
        }
    }

    private void handleVoiceCommand(String command) {
        if (command.contains("call")) {
            cardCall.performClick();
        } else if (command.contains("message")) {
            cardMessage.performClick();
        } else if (command.contains("environment")) {
            cardEnvironment.performClick();
        } else if (command.contains("location")) {
            cardLocation.performClick();
        } else if (command.contains("battery")) {
            cardBattery.performClick();
        } else if (command.contains("music")) {
            cardMusic.performClick();
        } else {
            speak("Sorry, I didn't understand the command.");
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, start the service
                startVoiceService();
            } else {
                // Permissions denied, handle accordingly
                Toast.makeText(this, "Permissions denied, features may not work properly", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
