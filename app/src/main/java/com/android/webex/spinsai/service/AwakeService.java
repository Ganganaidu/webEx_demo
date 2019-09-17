package com.android.webex.spinsai.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.android.webex.spinsai.R;

public class AwakeService extends Service {
    private PowerManager.WakeLock wl;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (null != pm){
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SpinSci:AwakeServiceTag");
            wl.acquire(3000);
        }

        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (null != notifyManager) {
            Notification notification;
            String channelId = "SpinSci_channel_id";
            String channelName = "SpinSci_channel_name";
            String title = "SpinSci Telehealth";
            String text = "Calling";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                notifyManager.createNotificationChannel(mChannel);
                notification = new Notification.Builder(this, channelId)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setOngoing(true)
                        .setNumber(1)
                        .setWhen(System.currentTimeMillis())
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .build();
            } else {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setOngoing(true)
                        .setNumber(1)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setWhen(System.currentTimeMillis())
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                notification = builder.build();
            }
            startForeground(0x101, notification);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != wl)
            wl.release();
        stopForeground(true);
    }
}
