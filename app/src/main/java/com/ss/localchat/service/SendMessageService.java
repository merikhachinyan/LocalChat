package com.ss.localchat.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.android.gms.nearby.connection.Payload;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class SendMessageService extends BaseService {

    private SendMessageBinder mSendMessageBinder;

    public SendMessageService() {
        super("Send Message Service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mSendMessageBinder == null) {
            mSendMessageBinder = new SendMessageBinder();
        }
        return mSendMessageBinder;
    }

    private void sendMessage(String id, String messageText) {
        try {
            UUID userId = UUID.fromString(PreferenceManager.getDefaultSharedPreferences(getApplication()).getString("id", ""));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", userId.toString());
            jsonObject.put("message", messageText);
            mConnectionsClient.sendPayload(id, Payload.fromBytes(jsonObject.toString().getBytes(StandardCharsets.UTF_8)));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class SendMessageBinder extends Binder {
        public void send(String id, String messageText) {
            sendMessage(id, messageText);
        }
    }
}