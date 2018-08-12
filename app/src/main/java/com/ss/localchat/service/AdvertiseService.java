package com.ss.localchat.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.nearby.connection.AdvertisingOptions;


public class AdvertiseService extends BaseService {

    public static final String CHANNEL_ID = "advertise_service";

    private AdvertiseBinder mAdvertiseBinder;

    public AdvertiseService() {
        super("Advertise Service");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
//        advertising();
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
        mConnectionsClient.startAdvertising("Name", getPackageName(),
                mConnectionLifecycleCallback, new AdvertisingOptions.Builder()
                        .setStrategy(STRATEGY)
                        .build());
    }


    public void startForegroundAdvertiseService(){
        createNotificationChannel(CHANNEL_ID);
        startForeground(1, createNotification("Local chat", "Advertising..."));
    }

    public class AdvertiseBinder extends Binder {
        public void startAdvertising() {
            advertising();
        }
    }
}