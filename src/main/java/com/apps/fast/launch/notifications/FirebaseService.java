package com.apps.fast.launch.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
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
    private static final String CHANNEL_ID = "default";

    private static final long[] VIBRATE = new long[]{ 0, 2000, 400, 400, 400, 400, 400, 400 };

    @Override
    public void onNewToken(String token) { }

    private static boolean IsSDK26OrHigher()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        LaunchLog.Log(SERVICES, "AlertService", "Raising notification.");
        //SharedPreferences sharedPreferences = this.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.drawable.notification);

        if(!IsSDK26OrHigher())
        {
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
            notificationBuilder.setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.airraid));
            notificationBuilder.setVibrate(VIBRATE);
        }

        notificationBuilder.setContentTitle(this.getString(R.string.app_name));

        notificationBuilder.setContentText(this.getString(R.string.notification_text));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE));
        }
        else
        {
            notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
        }

        notificationBuilder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }
}