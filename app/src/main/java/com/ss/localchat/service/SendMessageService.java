package com.ss.localchat.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.nearby.connection.Payload;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SendMessageService extends BaseService{

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
        if (mSendMessageBinder == null){
            mSendMessageBinder = new SendMessageBinder();
        }
        return mSendMessageBinder;
    }

//    private void sendMessage(String id, String message){
//        mConnectionsClient.sendPayload(id, Payload.fromBytes(message.getBytes(StandardCharsets.UTF_8)));
//    }

    private void sendMessage(ArrayList<String> ids, String message){
        for (String id : ids){
            mConnectionsClient.sendPayload(id, Payload.fromBytes(message.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public class SendMessageBinder extends Binder {
        public void send(ArrayList<String> ids, String message){
            sendMessage(ids, message);
        }
    }
}
