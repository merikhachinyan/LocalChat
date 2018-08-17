package com.ss.localchat.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
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
import com.ss.localchat.activity.ChatActivity;
import com.ss.localchat.activity.MainActivity;
import com.ss.localchat.model.User;
import com.ss.localchat.receiver.NotificationBroadcastReceiver;

public abstract class BaseService extends IntentService {

    private static final String CHANNEL_ID = "send.message.service";
    public static final String KEY_TEXT_REPLY = "key.text.reply";
    public static final String REPLY_ACTION = "reply.action";
    public static final String USER_EXTRA = "user.extra";

    public static final int NOTIFICATION_ID = 100;

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
                    mConnectionsClient.requestConnection("User", id, mConnectionLifecycleCallback);
                }
            };

    protected PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {

                    //TODO get user name by id from db
                    mUser = new User(s, "User", null);
                    showNotification(mUser.getName(), new String(payload.asBytes()));
                  
                    Toast.makeText(getApplicationContext(),
                                new String(payload.asBytes()), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                }
            };

    protected ConnectionsClient mConnectionsClient;

    private NotificationManager mManager;

    private User mUser;


    public BaseService(String name) {
        super(name);

        createNotificationChannel(CHANNEL_ID);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mConnectionsClient = Nearby.getConnectionsClient(this);
    }

    private void showNotification(String title, String message) {
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

    protected Notification createNotification(String title, String message){
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.USER_EXTRA, mUser);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntentWithParentStack(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(15, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setSmallIcon(R.drawable.ic_launcher_background)
                .addAction(createReplyButton());

        return builder.build();
    }

    private NotificationCompat.Action createReplyButton(){
        String replyLabel = "Reply";
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel(replyLabel)
                .build();

        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(R.drawable.ic_launcher_background,
                replyLabel, getReplyPendingIntent())
                .addRemoteInput(remoteInput)
                .setAllowGeneratedReplies(true)
                .build();

        return replyAction;
    }

    private PendingIntent getReplyPendingIntent(){
        Intent intent;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            intent = new Intent(this, NotificationBroadcastReceiver.class);
            intent.setAction(REPLY_ACTION);
            intent.putExtra(USER_EXTRA, mUser);

            return PendingIntent.getBroadcast(getApplicationContext(), 100, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            intent = new Intent(this, ChatActivity.class);
            intent.setAction(REPLY_ACTION);

            return PendingIntent.getActivity(getApplicationContext(), 100, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }
}