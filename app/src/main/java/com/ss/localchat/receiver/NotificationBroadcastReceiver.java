package com.ss.localchat.receiver;

import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;


import com.ss.localchat.db.entity.User;
import com.ss.localchat.service.BaseService;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private OnSendDirectMessageListener mListener;

    private Context mContext;

    private User mUser;

    private CharSequence mMessageText;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BaseService.REPLY_ACTION.equals(intent.getAction())){
            mContext = context;

            mUser = (User) intent.getSerializableExtra(BaseService.USER_EXTRA);
            mMessageText = getReplyMessage(intent);

            mListener.onSendDirectMessage(mUser, mMessageText.toString());

            Toast.makeText(context, mMessageText, Toast.LENGTH_SHORT).show();
        }
    }

    private CharSequence getReplyMessage(Intent intent){
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null){
            return remoteInput.getCharSequence(BaseService.KEY_TEXT_REPLY);
        }
        return null;
    }

    public void setOnSendDirectMessageListener(OnSendDirectMessageListener listener) {
        mListener = listener;
    }

    public interface OnSendDirectMessageListener {
        void onSendDirectMessage(User user, String message);
    }
}
