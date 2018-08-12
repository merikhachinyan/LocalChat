package com.ss.localchat.service;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.ss.localchat.model.Endpoint;


public class DiscoverService extends BaseService {

    protected EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(@NonNull String id, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
//                    mConnectionsClient.requestConnection(Preferences.getName(getApplicationContext()),
//                            id, mConnectionLifecycleCallback);
                    mOnEndpointFoundListener.onEndpointFound(new Endpoint(id, discoveredEndpointInfo.getEndpointName()));
                }

                @Override
                public void onEndpointLost(@NonNull String id) {
                    mOnEndpointFoundListener.onEndpointLost(id);
                }
            };

//    private ConnectionsBroadcastReceiver.OnConnectionsStateChangedListener mOnConnectionsStateChangedListener =
//            new ConnectionsBroadcastReceiver.OnConnectionsStateChangedListener() {
//                @Override
//                public void onConnectionLost() {
//                    stopSelf();
//                }
//
//                @Override
//                public void onConnectionFound() {
//
//                }
//            };

//    private ConnectionsBroadcastReceiver mConnectionsBroadcastReceiver;
    private IntentFilter mConnectionsIntentFilter;
    private DiscoverBinder mDiscoverBinder;
    private OnEndpointFoundListener mOnEndpointFoundListener;

    public DiscoverService() {
        super("Discover Service");

//        mConnectionsBroadcastReceiver = new ConnectionsBroadcastReceiver();
//        mConnectionsBroadcastReceiver.setOnConnectionsStateChangedListener(mOnConnectionsStateChangedListener);
        mConnectionsIntentFilter = new IntentFilter();
        mConnectionsIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        discover();
//        startDiscoverForegroundService();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mDiscoverBinder == null){
            mDiscoverBinder = new DiscoverBinder();
        }
        return mDiscoverBinder;
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
//        registerReceiver(mConnectionsBroadcastReceiver, mConnectionsIntentFilter);
    }

    @Override
    public boolean stopService(Intent name) {
        mConnectionsClient.stopDiscovery();
        return super.stopService(name);
    }

    private void discover(){
        mConnectionsClient.startDiscovery(getPackageName(), mEndpointDiscoveryCallback,
                new DiscoveryOptions.Builder()
                        .setStrategy(STRATEGY)
                        .build());
    }

    private void startDiscoverForegroundService(){
        createNotificationChannel(AdvertiseService.CHANNEL_ID);
        startForeground(2, createNotification("Local chat", "Discovery..."));
    }

    public class DiscoverBinder extends Binder {
        public void startDiscovery(){
            discover();
        }

        public void setOnEndpointFoundListener(OnEndpointFoundListener onEndpointFoundListener){
            mOnEndpointFoundListener = onEndpointFoundListener;
        }
    }

    public interface OnEndpointFoundListener{
        void onEndpointFound(Endpoint endpoint);
        void onEndpointLost(String id);
    }
}