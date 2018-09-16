package com.ss.localchat.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.ss.localchat.R;
import com.ss.localchat.activity.ChatActivity;
import com.ss.localchat.activity.GroupChatActivity;
import com.ss.localchat.activity.MainActivity;
import com.ss.localchat.activity.SettingsActivity;
import com.ss.localchat.db.entity.Group;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.db.entity.User;

public class NotificationHelper {

    private static final String CHANNEL_ID = "send.message.service";

    private static final int REQUEST_CODE = 1;

    public static final int MESSAGE_NOTIFICATION_ID = 2;

    private static NotificationManager mManager;


    public static NotificationManager getManager(Context context) {
        if (mManager == null) {
            mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ServiceChannel";
            String description = "ServiceChannelDescription";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getManager(context);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static Notification createAdvertiseNotification(Context context, String title, String message) {
        Intent intent = new Intent(context, SettingsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntentWithParentStack(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(REQUEST_CODE, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_status_bar);

        return builder.build();
    }

    private static Notification createNotification(Context context, Intent intent, String name, Message message, User user) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntentWithParentStack(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(15, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(name)
                .setContentIntent(pendingIntent)
                .setColor(context.getResources().getColor(R.color.colorAccent))
                .setSmallIcon(R.drawable.ic_status_bar)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (user != null && user.getPhotoUrl() != null) {
            builder.setLargeIcon(BitmapHelper.uriToBitmap(context, Uri.parse(user.getPhotoUrl())));
        }

        if (message.getText() != null && message.getPhoto() != null) {
            builder.setContentText(message.getText())
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message.getText()))
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(BitmapHelper.uriToBitmap(context, Uri.parse(message.getPhoto()))));

        } else if (message.getPhoto() == null) {
            builder.setContentText(message.getText())
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message.getText()));

        } else {
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(BitmapHelper.uriToBitmap(context, Uri.parse(message.getPhoto()))));
        }

        return builder.build();
    }

    public static void showNotification(Context context, User user, Message message) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatActivity.USER_EXTRA, user);

        getManager(context).notify(user.getId().toString(),
                MESSAGE_NOTIFICATION_ID,
                createNotification(context, intent, user.getName(), message, user));
    }

    public static void showNotification(Context context, Group group, Message message) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        intent.putExtra(GroupChatActivity.GROUP_EXTRA, group);

        getManager(context).notify(group.getId().toString(),
                MESSAGE_NOTIFICATION_ID,
                createNotification(context, intent, group.getName(), message, null));
    }
}
