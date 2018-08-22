package com.ss.localchat.service;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.preferences.Preferences;

import java.util.UUID;


public class DiscoverService extends BaseService {

    protected EndpointDiscoveryCallback mEndpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String id, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {

            Log.v("____", id + " " + discoveredEndpointInfo.getEndpointName() + " " + discoveredEndpointInfo.getServiceId());
            if (discoveredEndpointInfo.getEndpointName().isEmpty())
                return;

            //Todo request user name from shared preferences& user photo is null
            String myUserOwner = Preferences.getUserName(getApplicationContext()) + ":" + Preferences.getUserId(getApplicationContext());
            mConnectionsClient.requestConnection(myUserOwner, id, mConnectionLifecycleCallback);

            String name = discoveredEndpointInfo.getEndpointName().split(":")[0];
            String uuidString = discoveredEndpointInfo.getEndpointName().split(":")[1];

            User user = new User();
            user.setId(UUID.fromString(uuidString));
            user.setName(name);
            user.setEndpointId(id);

            mUserRepository.insert(user);
            mOnDiscoverUsersListener.OnUserFound(user);
        }

        @Override
        public void onEndpointLost(@NonNull String id) {
            mOnDiscoverUsersListener.onUserLost(id);
            Log.v("____", "Lost");
        }
    };


    private IntentFilter mConnectionsIntentFilter;

    private DiscoverBinder mDiscoverBinder;

    private OnDiscoverUsersListener mOnDiscoverUsersListener;

    public DiscoverService() {
        super("Discover Service");

        mConnectionsIntentFilter = new IntentFilter();
        mConnectionsIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        discover();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mDiscoverBinder == null) {
            mDiscoverBinder = new DiscoverBinder();
        }
        return mDiscoverBinder;
    }


    @Override
    public boolean stopService(Intent name) {
        mConnectionsClient.stopDiscovery();
        return super.stopService(name);
    }

    private void discover() {
        mConnectionsClient.startDiscovery(getPackageName(), mEndpointDiscoveryCallback,
                new DiscoveryOptions.Builder()
                        .setStrategy(STRATEGY)
                        .build());
    }

    public class DiscoverBinder extends Binder {
        public void startDiscovery() {
            discover();
        }

        public void setOnDiscoverUsersListener(OnDiscoverUsersListener OnDiscoverUsersListener) {
            mOnDiscoverUsersListener = OnDiscoverUsersListener;
        }
    }

    public interface OnDiscoverUsersListener {
        void OnUserFound(User user);

        void onUserLost(String id);
    }
}