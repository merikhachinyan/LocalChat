package com.ss.localchat.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.ss.localchat.R;
import com.ss.localchat.activity.MainActivity;
import com.ss.localchat.activity.SettingsActivity;

public class AdvertiseService extends BaseService {

    public static final String CHANNEL_ID = "advertise.service";

    public static final String NOTIFICATION_TITLE = "Local Chat";

    public static final String NOTIFICATION_CONTENT = "Advertising...";

    public static final int REQUEST_CODE = 1;

    private AdvertiseBinder mAdvertiseBinder;

    public AdvertiseService() {
        super("Advertise Service");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        advertising();
        startForegroundAdvertiseService();
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mAdvertiseBinder == null) {
            mAdvertiseBinder = new AdvertiseBinder();
        }
        return mAdvertiseBinder;
    }

    @Override
    public boolean stopService(Intent name) {
        mConnectionsClient.stopAdvertising();
        return super.stopService(name);
    }

    private void advertising() {
        String name = PreferenceManager.getDefaultSharedPreferences(getApplication()).getString("name", "");
        String id = PreferenceManager.getDefaultSharedPreferences(getApplication()).getString("id", "");
        String ownerName = name + ":" + id;
        mConnectionsClient.startAdvertising(ownerName, getPackageName(), mConnectionLifecycleCallback, new AdvertisingOptions.Builder()
                .setStrategy(STRATEGY)
                .build());
    }

    public void startForegroundAdvertiseService() {
        createNotificationChannel(CHANNEL_ID);
        startForeground(1, createAdvertiseNotification(NOTIFICATION_TITLE, NOTIFICATION_CONTENT));
    }

    protected Notification createAdvertiseNotification(String title, String message){
        Intent intent = new Intent(this, SettingsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntentWithParentStack(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(REQUEST_CODE, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_background);

        return builder.build();
    }

    protected Notification createNotification(String title, String message) {
        Intent intent = new Intent(this, SettingsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_background);

        return builder.build();
    }

    public class AdvertiseBinder extends Binder {
        public void startAdvertising() {
            advertising();
        }
    }
}