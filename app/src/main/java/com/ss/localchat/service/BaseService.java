package com.ss.localchat.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.ss.localchat.R;

public abstract class BaseService extends IntentService {

    private static final String CHANNEL_ID = "send_message_service";

    public static final int NOTIFICATION_ID = 1;

    public static final Strategy STRATEGY = Strategy.P2P_CLUSTER;

    protected ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NonNull String id, @NonNull ConnectionInfo connectionInfo) {
                    mConnectionsClient.acceptConnection(id, mPayloadCallback);
                    Log.v("____", "Connected");
                }

                @Override
                public void onConnectionResult(@NonNull String id, @NonNull ConnectionResolution connectionResolution) {
                }

                @Override
                public void onDisconnected(@NonNull String id) {
                }
            };

    protected PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                    createNotificationChannel(CHANNEL_ID);
                    showNotification("Message", new String(payload.asBytes()));
                    Toast.makeText(getApplicationContext(),
                            new String(payload.asBytes()), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                }
            };

    protected ConnectionsClient mConnectionsClient;
    private NotificationManager mManager;

    public BaseService(String name) {
        super(name);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
//        mConnectionsClient = Nearby.getConnectionsClient(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mConnectionsClient = Nearby.getConnectionsClient(this);
    }

    protected Notification createNotification(String title, String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_background);

        return builder.build();
    }

    private void showNotification(String title, String message){
        getManager().notify(NOTIFICATION_ID, createNotification(title, message));
    }

    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    protected void createNotificationChannel(String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ServiceChannel";
            String description = "ServiceChannelDescription";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getManager();
            notificationManager.createNotificationChannel(channel);
        }
    }
}