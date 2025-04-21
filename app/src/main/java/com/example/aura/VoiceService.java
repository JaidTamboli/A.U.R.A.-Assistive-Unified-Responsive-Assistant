package com.example.aura;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

public class VoiceService extends Service {

    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private static final String HOTWORD = "hey aura";
    private static final String CHANNEL_ID = "voice_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize speech recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListenerAdapter() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    for (String result : matches) {
                        if (result.toLowerCase().contains(HOTWORD)) {
                            Intent launchIntent = new Intent(VoiceService.this, MainActivity.class);
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);
                            break;
                        }
                    }
                }
                restartListening(); // keep listening
            }

            @Override
            public void onError(int error) {
                restartListening(); // retry on error
            }
        });

        // Recognizer intent
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());

        // Start foreground service
        startForeground(1, buildNotification());
        restartListening();
    }

    private void restartListening() {
        try {
            speechRecognizer.stopListening();
            speechRecognizer.startListening(recognizerIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Voice Activation",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("AURA is listening...")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now) // Safe built-in icon
                .setOngoing(true)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
