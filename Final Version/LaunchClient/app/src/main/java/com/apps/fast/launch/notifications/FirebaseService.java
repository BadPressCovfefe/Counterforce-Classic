package com.apps.fast.launch.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.ClientDefs;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import launch.utilities.LaunchLog;

import static launch.utilities.LaunchLog.LogType.SERVICES;

import androidx.core.app.NotificationCompat;

public class FirebaseService extends FirebaseMessagingService
{
    private static final int TICK_RATE_ALERT_SERVICE = 100;
    private static final int TIMEOUT = 5000;
    private static final int MESSAGE_BUFFER_SIZE = 10240;

    private static final String TAG = "LAUNCH_ATTACK_ALERTS";
    private static final String CHANNEL_ID = "cf_default_channel";

    private static final long[] VIBRATE = new long[]{ 0, 2000, 400, 400, 400, 400, 400, 400 };

    @Override
    public void onNewToken(String token) { }

    private static boolean IsSDK26OrHigher()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        LaunchLog.Log(SERVICES, "AlertService", "Raising notification.");

        Log.d("FCM", "Full RemoteMessage: " + remoteMessage);
        Log.d("FCM", "Notification payload: " + remoteMessage.getNotification());
        Log.d("FCM", "Data payload: " + remoteMessage.getData());

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        final Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.airraid);

        // Create notification channel if needed (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID);

            if (existingChannel == null) {
                // Channel doesn't exist, create it with custom sound
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();

                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "CF Default Channel",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setSound(soundUri, audioAttributes);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 2000, 400, 400, 400, 400, 400, 400});
                notificationManager.createNotificationChannel(channel);
            }
            // else: channel exists, Android uses existing channel settings (including sound)
        }

        // Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 2000, 400, 400, 400, 400, 400, 400});

        // For pre-Oreo, set sound on builder
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setSound(soundUri);
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        // Extract title/body from notification or data payload
        String title = null;
        String body = null;

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            title = notification.getTitle();
            body = notification.getBody();
        }
        if (title == null) {
            title = remoteMessage.getData().get("title");
        }
        if (body == null) {
            body = remoteMessage.getData().get("body");
        }
        if (title == null) {
            title = getString(R.string.app_name);
        }
        if (body == null) {
            body = getString(R.string.notification_text);
        }

        Log.d("FCM", "Parsed title: " + title);
        Log.d("FCM", "Parsed body: " + body);

        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(body);

        // Intent for notification tap action
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );
        notificationBuilder.setContentIntent(pendingIntent);

        // Show notification
        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }
}