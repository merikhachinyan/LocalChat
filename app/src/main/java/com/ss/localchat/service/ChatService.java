package com.ss.localchat.service;

import android.app.IntentService;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.databinding.ObservableArrayMap;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.ss.localchat.activity.ChatActivity;
import com.ss.localchat.activity.GroupChatActivity;
import com.ss.localchat.db.GroupRepository;
import com.ss.localchat.db.MessageRepository;
import com.ss.localchat.db.UserRepository;
import com.ss.localchat.db.entity.Group;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.helper.BitmapHelper;
import com.ss.localchat.helper.NotificationHelper;
import com.ss.localchat.model.ConnectionState;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.receiver.ConnectionsBroadcastReceiver;
import com.ss.localchat.viewmodel.UserViewModel;
import com.ss.localchat.viewmodel.MessageViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class ChatService extends IntentService {

    public static final String NOTIFICATION_TITLE = "Local Chat";

    public static final String NOTIFICATION_CONTENT = "Advertising...";

    public static final String MESSAGE_TYPE = "message";

    public static final String FILENAME_TYPE = "filename";

    public static final String GROUP_TYPE = "group";

    public static final String GROUP_LEAVE_TYPE = "group.leave";

    public static final String READ_TYPE = "read";

    public static final String PHOTO_MESSAGE_TYPE = "photo";

    public static final String PHOTO_TEXT_MESSAGE_TYPE = "photo.text";


    public static final int FOREGROUND_NOTIFICATION_ID = 1;

    public static final Strategy STRATEGY = Strategy.P2P_CLUSTER;


    protected ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String id, @NonNull ConnectionInfo connectionInfo) {
            mEndpoints.put(id, ConnectionState.CONNECTING);

            mConnectionsClient.acceptConnection(id, mPayloadCallback);

            String name = connectionInfo.getEndpointName().split(":")[0];
            String uuidString = connectionInfo.getEndpointName().split(":")[1];

            User user = new User();
            user.setId(UUID.fromString(uuidString));
            user.setName(name);
            user.setEndpointId(id);

            mUserRepository.insert(user);

            Log.v("____", "Connected to " + connectionInfo.getEndpointName());
        }

        @Override
        public void onConnectionResult(@NonNull String id, @NonNull ConnectionResolution connectionResolution) {
            if (connectionResolution.getStatus().isSuccess()) {
                mEndpoints.put(id, ConnectionState.CONNECTED);
                sendProfilePicture(id);
            }
        }

        @Override
        public void onDisconnected(@NonNull String id) {
            mEndpoints.put(id, ConnectionState.DISCONNECTED);
            Log.v("____", "Disconnected from " + id);
        }
    };

    protected PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull
                                              String s, @NonNull Payload payload) {

            if (payload.getType() == Payload.Type.BYTES) {

                try {
                    String payloadText = new String(payload.asBytes(), StandardCharsets.UTF_8);

                    JSONObject jsonObject = new JSONObject(payloadText);
                    String type = jsonObject.getString("type");

                    if (MESSAGE_TYPE.equals(type)) {
                        UUID senderId = UUID.fromString(jsonObject.getString("id"));
                        String messageText = jsonObject.getString("message");
                        boolean isGroup = jsonObject.getBoolean("group");
                        String sender = jsonObject.getString("sender");

                        UUID myUserId = Preferences.getUserId(getApplicationContext());

                        Message message = new Message();
                        message.setText(messageText);
                        message.setRead(false);

                        message.setReceiverId(myUserId);
                        message.setSenderId(senderId);
                        message.setSenderName(sender);

                        if (isGroup) {
                            message.setGroup(true);
                            showGroupMessageNotification(senderId, message);
                        } else {
                            message.setGroup(false);
                            showMessageNotification(senderId, message);
                        }

                        mMessageRepository.insert(message);
                        markAsRead(senderId, s);
                    } else if (FILENAME_TYPE.equals(type)) {
                        String senderId = jsonObject.getString("id");
                        String payloadId = jsonObject.getString("payload_id");
                        String imageExtension = jsonObject.getString("extension");

                        String fileName = "photo-".concat(senderId).concat(imageExtension);

                        filePayloadFilenames.put(Long.parseLong(payloadId), fileName);

                    } else if (READ_TYPE.equals(type)) {
                        UUID senderId = UUID.fromString(jsonObject.getString("id"));

                        markAsRead(senderId);

                    } else if (PHOTO_TEXT_MESSAGE_TYPE.equals(type)) {
                        UUID myUserId = Preferences.getUserId(getApplicationContext());

                        UUID senderId = UUID.fromString(jsonObject.getString("id"));
                        String payloadId = jsonObject.getString("payload_id");
                        String imageExtension = jsonObject.getString("extension");
                        boolean isTextMessage = jsonObject.getBoolean("is_text_message");
                        boolean isGroup = jsonObject.getBoolean("group");

                        String messageText = null;

                        if (isTextMessage) {
                            messageText = jsonObject.getString("message_text");
                        }

                        mMessage = new Message();
                        mMessage.setText(messageText);
                        mMessage.setRead(false);
                        mMessage.setReceiverId(myUserId);
                        mMessage.setSenderId(senderId);

                        if (isGroup) {
                            mMessage.setGroup(true);
                            showGroupMessageNotification(senderId, mMessage);
                        } else {
                            mMessage.setGroup(false);
                            showMessageNotification(senderId, mMessage);
                        }

                        String fileName = "picture-".concat(payloadId).concat(imageExtension);

                        filePayloadFilenames.put(Long.parseLong(payloadId), fileName);

                    } else if (PHOTO_MESSAGE_TYPE.equals(type)) {
                        UUID myUserId = Preferences.getUserId(getApplicationContext());

                        UUID senderId = UUID.fromString(jsonObject.getString("id"));
                        String payloadId = jsonObject.getString("payload_id");
                        String imageExtension = jsonObject.getString("extension");

                        mMessage = new Message();
                        mMessage.setRead(false);
                        mMessage.setReceiverId(myUserId);
                        mMessage.setSenderId(senderId);

                        String fileName = "picture-".concat(payloadId).concat(imageExtension);

                        filePayloadFilenames.put(Long.parseLong(payloadId), fileName);
                    } else if (GROUP_TYPE.equals(type)) {
                        String groupId = jsonObject.getString("id");
                        String groupName = jsonObject.getString("name");
                        String groupMembers = jsonObject.getString("members");

                        Group group = new Group();
                        group.setId(UUID.fromString(groupId));
                        group.setName(groupName);
                        group.setMembers(groupMembers);

                        mGroupRepository.insert(group);
                    } else if (GROUP_LEAVE_TYPE.equals(type)) {
                        UUID senderId = UUID.fromString(jsonObject.getString("id"));
                        String sender = jsonObject.getString("sender");

                        UUID myUserId = Preferences.getUserId(getApplicationContext());

                        Message message = new Message();
                        message.setRead(false);
                        message.setReceiverId(myUserId);
                        message.setSenderId(senderId);
                        message.setSenderName(sender);
                        message.setGroup(true);
                        mMessageRepository.insert(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (payload.getType() == Payload.Type.FILE) {
                incomingPayloads.put(payload.getId(), payload);
            }
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull final String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
            if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                Long payloadId = payloadTransferUpdate.getPayloadId();
                Payload payload = incomingPayloads.remove(payloadId);
                if (payload != null && payload.getType() == Payload.Type.FILE) {
                    String fileName = filePayloadFilenames.remove(payloadId);
                    File payloadFile = payload.asFile().asJavaFile();
                    File newFile = new File(payloadFile.getParentFile(), fileName);
                    boolean isRenamed = payloadFile.renameTo(newFile);
                    Log.v("____", "Receive and renamed: " + isRenamed);
                    mUserRepository.updatePhoto(s, Uri.fromFile(newFile).toString());

                    if (fileName.contains("photo")) {
                        mUserRepository.updatePhoto(s, Uri.fromFile(newFile).toString());

                    } else if (fileName.contains("picture")) {
                        mMessage.setPhoto(Uri.fromFile(newFile).toString());
                        mMessage.setDate(new Date());
                        mMessageRepository.insert(mMessage);

                        showMessageNotification(mMessage.getSenderId(), mMessage);

                        markAsRead(mMessage.getSenderId(), s);
                    }
                }
            }
        }
    };

    protected EndpointDiscoveryCallback mEndpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String id, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {

            Log.v("____", id + " " + discoveredEndpointInfo.getEndpointName() + " " + discoveredEndpointInfo.getServiceId());
            if (discoveredEndpointInfo.getEndpointName().length() < 3)
                return;

            String myUserOwner = Preferences.getUserName(getApplicationContext()) + ":" + Preferences.getUserId(getApplicationContext());
            mConnectionsClient.requestConnection(myUserOwner, id, mConnectionLifecycleCallback);

            if (!mEndpoints.containsKey(id) || mEndpoints.get(id) != ConnectionState.CONNECTED) {
                mEndpoints.put(id, ConnectionState.CONNECTING);
            }

            String name = discoveredEndpointInfo.getEndpointName().split(":")[0];
            String uuidString = discoveredEndpointInfo.getEndpointName().split(":")[1];

            User user = new User();
            user.setId(UUID.fromString(uuidString));
            user.setName(name);
            user.setEndpointId(id);

            mDiscoverUsersListener.OnUserFound(user);
        }

        @Override
        public void onEndpointLost(@NonNull String id) {
            mDiscoverUsersListener.onUserLost(id);
            Log.v("____", "Lost");
        }
    };

    private ConnectionsBroadcastReceiver.OnConnectionsStateChangedListener mListener =
            new ConnectionsBroadcastReceiver.OnConnectionsStateChangedListener() {
                @Override
                public void onBluetoothDisabled(boolean flag) {
                    if (flag) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (isLocationDisabled) {
                                mConnectionsClient.stopDiscovery();
                            }
                        } else if (isWifiDisabled) {
                            if (isRunningService) {
                                stopAdvertising();
                            }
                        } else {
                            isBluetoothDisabled = true;
                        }
                    } else {
                        isBluetoothDisabled = false;
                    }
                }

                @Override
                public void onWifiDisabled(boolean flag) {
                    if (flag) {
                        if (isBluetoothDisabled) {
                            if (isRunningService) {
                                stopAdvertising();
                            }
                        } else if (isLocationDisabled) {
                            mConnectionsClient.stopDiscovery();
                        } else {
                            isWifiDisabled = true;
                        }
                    } else {
                        isWifiDisabled = false;
                    }
                }

                @Override
                public void onLocationStateDisabled(boolean flag) {
                    if (flag) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (isBluetoothDisabled) {
                                mConnectionsClient.stopDiscovery();
                            }
                        } else {
                            isLocationDisabled = true;
                        }
                    } else {
                        isLocationDisabled = false;
                    }
                }
            };


    private static boolean isRunningService;

    private static boolean isRunningDiscovery;

    private ConnectionsClient mConnectionsClient;

    private MessageRepository mMessageRepository;

    private UserRepository mUserRepository;

    private GroupRepository mGroupRepository;

    private ServiceBinder mServiceBinder;

    private OnDiscoverUsersListener mDiscoverUsersListener;

    private ConnectionsBroadcastReceiver mConnectionsBroadcastReceiver;

    private IntentFilter mIntentFilter;


    private boolean isBluetoothDisabled;

    private boolean isWifiDisabled;

    private boolean isLocationDisabled;


    private ObservableArrayMap<String, ConnectionState> mEndpoints;

    private OnMapChangedListener mMapChangedListener;


    private final SimpleArrayMap<Long, Payload> incomingPayloads = new SimpleArrayMap<>();

    private final SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();

    private Message mMessage;


    public ChatService() {
        super("Service");
    }

    public ChatService(String name) {
        super(name);

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        startAdvertising();

        startForegroundAdvertiseService();

        isRunningService = true;

        return START_NOT_STICKY;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        init();

        registerReceiver(mConnectionsBroadcastReceiver, mIntentFilter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mServiceBinder == null) {
            mServiceBinder = new ServiceBinder();
        }

        return mServiceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mConnectionsBroadcastReceiver);
    }

    private void init() {
        mConnectionsClient = Nearby.getConnectionsClient(this);

        mMessageRepository = new MessageRepository(getApplication());
        mUserRepository = new UserRepository(getApplication());
        mGroupRepository = new GroupRepository(getApplication());

        NotificationHelper.createNotificationChannel(this);

        mConnectionsBroadcastReceiver = new ConnectionsBroadcastReceiver();

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ConnectionsBroadcastReceiver.LOCATION_ACTION);

        mConnectionsBroadcastReceiver.setOnConnectionsStateChangedListener(mListener);

        mEndpoints = new ObservableArrayMap<>();

        mEndpoints.addOnMapChangedCallback(new android.databinding.ObservableMap.OnMapChangedCallback<android.databinding.ObservableMap<String, ConnectionState>, String, ConnectionState>() {
            @Override
            public void onMapChanged(android.databinding.ObservableMap<String, ConnectionState> sender, String key) {
                if (mMapChangedListener != null) {
                    mMapChangedListener.onMapChanged(sender);
                }
            }
        });
    }

    private void markAsRead(UUID uuid) {
        final MessageViewModel model = new MessageViewModel(getApplication());

                final LiveData<List<Message>> liveData = model.getReceiverUnreadMessages(uuid, false);
                liveData.observeForever(new Observer<List<Message>>() {
                    @Override
                    public void onChanged(@Nullable List<Message> messages) {
                        if (messages != null && messages.size() != 0) {
                            for (Message message : messages) {
                                message.setReadReceiver(true);
                            }
                            model.update(messages.toArray(new Message[messages.size()]));
                        }

                        liveData.removeObserver(this);
                    }
                });
            }

    private void startAdvertising() {
        String myUserName = Preferences.getUserName(getApplicationContext());
        UUID myUserId = Preferences.getUserId(getApplicationContext());

        String ownerName = myUserName + ":" + myUserId.toString();
        Log.v("____", "Advertising: " + ownerName);
        mConnectionsClient.startAdvertising(ownerName, getPackageName(), mConnectionLifecycleCallback, new AdvertisingOptions.Builder()
                .setStrategy(STRATEGY)
                .build());
    }

    private void discover() {
        mConnectionsClient.startDiscovery(getPackageName(), mEndpointDiscoveryCallback,
                new DiscoveryOptions.Builder()
                        .setStrategy(STRATEGY)
                        .build());

        isRunningDiscovery = true;
    }

    private void connect(String id) {
        String myUserOwner = Preferences.getUserName(getApplicationContext()) + ":" + Preferences.getUserId(getApplicationContext());
        mConnectionsClient.requestConnection(myUserOwner, id, mConnectionLifecycleCallback);
        Log.v("____", "Connected to " + id);
    }

    private void disconnect(String id) {
        mConnectionsClient.disconnectFromEndpoint(id);
        mEndpoints.put(id, ConnectionState.DISCONNECTED);
        Log.v("____", "Disconnected from " + id);
    }

    private void sendMessage(String id, String messageText) {
        try {
            UUID myUserId = Preferences.getUserId(getApplicationContext());
            String myUserName = Preferences.getUserName(getApplicationContext());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", MESSAGE_TYPE);
            jsonObject.put("id", myUserId.toString());
            jsonObject.put("message", messageText);
            jsonObject.put("group", false);
            jsonObject.put("sender", myUserName);

            mConnectionsClient.sendPayload(id, Payload.fromBytes(jsonObject.toString().getBytes(StandardCharsets.UTF_8)));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void readMessage(String id, String readMessage) {
        try {
            UUID userId = Preferences.getUserId(getApplicationContext());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", userId.toString());
            jsonObject.put("type", readMessage);
            mConnectionsClient.sendPayload(id, Payload.fromBytes(jsonObject.toString().getBytes(StandardCharsets.UTF_8)));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendGroupMessage(List<String> endpointList, String messageText, Group group) {
        try {
            String myUserName = Preferences.getUserName(getApplicationContext());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", MESSAGE_TYPE);
            jsonObject.put("id", group.getId().toString());
            jsonObject.put("message", messageText);
            jsonObject.put("group", true);
            jsonObject.put("sender", myUserName);

            mConnectionsClient.sendPayload(endpointList, Payload.fromBytes(jsonObject.toString().getBytes(StandardCharsets.UTF_8)));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendGroup(List<String> endpointList, Group group) {
        try {
            JSONObject jsonGroup = new JSONObject();
            jsonGroup.put("type", GROUP_TYPE);
            jsonGroup.put("id", group.getId().toString());
            jsonGroup.put("name", group.getName());
            jsonGroup.put("members", group.getMembers());
            mConnectionsClient.sendPayload(endpointList, Payload.fromBytes(jsonGroup.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendGroupLeaveMessage(List<String> endpointList, Group group) {
        try {
            String myUserName = Preferences.getUserName(getApplicationContext());
            String sender = myUserName + " leaves the group";

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", MESSAGE_TYPE);
            jsonObject.put("id", group.getId().toString());
            jsonObject.put("sender", myUserName);
            mConnectionsClient.sendPayload(endpointList, Payload.fromBytes(jsonObject.toString().getBytes(StandardCharsets.UTF_8)));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void sendProfilePicture(final String id) {
        UUID myUserId = Preferences.getUserId(getApplicationContext());
        Uri myUserPhotoUri = Preferences.getUserPhoto(getApplicationContext());

        if (myUserPhotoUri != null) {
            Log.v("____", "Sending picture, uri: " + myUserPhotoUri);
            try {
                File file = new File(myUserPhotoUri.toString());

                String photoUri = myUserPhotoUri.toString();
                String imageExtension = photoUri.substring(photoUri.lastIndexOf("."));

                Payload filePayload = Payload.fromFile(file);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", FILENAME_TYPE);
                jsonObject.put("id", myUserId.toString());
                jsonObject.put("payload_id", filePayload.getId());
                jsonObject.put("extension", imageExtension);

                mConnectionsClient.sendPayload(id, Payload.fromBytes(jsonObject.toString().getBytes(StandardCharsets.UTF_8)));
                mConnectionsClient.sendPayload(id, filePayload);

            } catch (FileNotFoundException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendPictureWithTextMessage(String id, Uri uri, String messageText) {
        UUID myUserId = Preferences.getUserId(getApplicationContext());
        String myUserName = Preferences.getUserName(getApplicationContext());

        try {
            Bitmap bitmap = BitmapHelper.getResizedBitmap(BitmapHelper.uriToBitmap(this, uri), 640);

            String fileExtension = getFileExtension(uri);
            Payload filePayload = Payload.fromFile(BitmapHelper.bitmapToFile(this, bitmap, fileExtension));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", PHOTO_TEXT_MESSAGE_TYPE);
            jsonObject.put("id", myUserId.toString());
            jsonObject.put("payload_id", filePayload.getId());
            jsonObject.put("extension", fileExtension);
            jsonObject.put("sender", myUserName);
            jsonObject.put("group", false);

            if (messageText == null) {
                jsonObject.put("is_text_message", false);
            } else {
                jsonObject.put("is_text_message", true);
                jsonObject.put("message_text", messageText);
            }

            mConnectionsClient.sendPayload(id, Payload.fromBytes(jsonObject.toString().getBytes(StandardCharsets.UTF_8)));
            mConnectionsClient.sendPayload(id, filePayload);

        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendPictureWithTextMessageToGroup(List<String> endpointList, Uri uri, String messageText, Group group) {

        try {
            Bitmap bitmap = BitmapHelper.getResizedBitmap(BitmapHelper.uriToBitmap(this, uri), 640);

            String fileExtension = getFileExtension(uri);
            Payload filePayload = Payload.fromFile(BitmapHelper.bitmapToFile(this, bitmap, fileExtension));

            String myUserName = Preferences.getUserName(getApplicationContext());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", PHOTO_TEXT_MESSAGE_TYPE);
            jsonObject.put("id", group.getId().toString());
            jsonObject.put("payload_id", filePayload.getId());
            jsonObject.put("group", true);
            jsonObject.put("extension", fileExtension);
            jsonObject.put("sender", myUserName);

            if (messageText == null) {
                jsonObject.put("is_text_message", false);
            } else {
                jsonObject.put("is_text_message", true);
                jsonObject.put("message_text", messageText);
            }

            mConnectionsClient.sendPayload(endpointList, Payload.fromBytes(jsonObject.toString().getBytes(StandardCharsets.UTF_8)));
            mConnectionsClient.sendPayload(endpointList, filePayload);

        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }
    }

    public String getFileExtension(Uri uri) {
        String filePath = null;
        String[] filePathColumn = {MediaStore.Images.Media.DISPLAY_NAME};

        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        if (cursor == null)
            return null;

        if (cursor.moveToFirst()) {
            String fileName = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            filePath = fileName.substring(fileName.lastIndexOf("."));
        }
        cursor.close();
        return filePath;
    }

    public void startForegroundAdvertiseService() {
        startForeground(FOREGROUND_NOTIFICATION_ID, NotificationHelper.createAdvertiseNotification(this, NOTIFICATION_TITLE, NOTIFICATION_CONTENT));
    }

    private void stopAdvertising() {
        mConnectionsClient.stopAdvertising();
        mConnectionsClient.stopDiscovery();
        mConnectionsClient.stopAllEndpoints();

        stopForeground(true);
        stopSelf();

        isRunningService = false;
        Log.v("____", "Stop");

        for (Map.Entry<String, ConnectionState> entry : mEndpoints.entrySet()) {
            mEndpoints.put(entry.getKey(), ConnectionState.DISCONNECTED);
        }
    }

    private void showMessageNotification(final UUID id, final Message message) {

        mUserRepository.getUserById(id).observeForever(new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if (user != null) {
                    if (!ChatActivity.isCurrentlyRunning || (ChatActivity.isCurrentlyRunning && !ChatActivity.currentUserId.equals(user.getId()))) {
                        NotificationHelper.showNotification(getApplicationContext(), user, message);
                    }
                }
                mUserRepository.getUserById(id).removeObserver(this);
            }
        });
    }

    private void showGroupMessageNotification(final UUID id, final Message message) {

        mGroupRepository.getGroupById(id).observeForever(new Observer<Group>() {
            @Override
            public void onChanged(@Nullable Group group) {
                if (group != null) {
                    if (!GroupChatActivity.isCurrentlyRunning || (GroupChatActivity.isCurrentlyRunning && !GroupChatActivity.currentGroupId.equals(group.getId()))) {
                        NotificationHelper.showNotification(getApplicationContext(), group, message);
                    }
                }
                mGroupRepository.getGroupById(id).removeObserver(this);
            }
        });
    }

    private void markAsRead(UUID senderId, String endpointId) {
        if (ChatActivity.isCurrentlyRunning && senderId.equals(ChatActivity.currentUserId)) {
            if (mEndpoints != null) {
                if (mEndpoints.containsKey(endpointId) && mEndpoints.get(endpointId).equals(ConnectionState.CONNECTED)) {
                    readMessage(endpointId, ChatActivity.READ_MESSAGE);
                }
            }
        }
    }

    public class ServiceBinder extends Binder {
        public void startDiscovery() {
            discover();
        }

        public void stopDiscovery() {
            mConnectionsClient.stopDiscovery();

            isRunningDiscovery = false;
        }

        public void connectTo(String id) {
            connect(id);
        }

        public void disconnectFrom(String id) {
            disconnect(id);
        }

        public void sendMessageTo(String id, String messageText) {
            sendMessage(id, messageText);
        }

        public void sendGroupMessageTo(List<String> endpointList, String messageText, Group group) {
            sendGroupMessage(endpointList, messageText, group);
        }

        public void sendGroupTo(List<String> endpointList, Group group) {
            sendGroup(endpointList, group);
        }

        public void sendGroupLeaveMessageTo(List<String> endpointList, Group group) {
            sendGroupLeaveMessage(endpointList, group);
        }

        public void sendPhotoWithTextMessage(String id, Uri uri, String messageText) {
            sendPictureWithTextMessage(id, uri, messageText);
        }

        public void sendPhotoWithTextMessageToGroup(List<String> endpointList, Uri uri, String messageText, Group group) {
            sendPictureWithTextMessageToGroup(endpointList, uri, messageText, group);
        }

        public void markMessageAsRead(String id, String readMessage) {
            readMessage(id, readMessage);
        }

        public void stopService() {
            stopAdvertising();

            stopDiscovery();
        }

        public boolean isRunningService() {
            return isRunningService;
        }

        public boolean isRunningDiscovery() {
            return isRunningDiscovery;
        }

        public ObservableArrayMap<String, ConnectionState> getEndpoints() {
            return mEndpoints;
        }

        public void setOnDiscoverUsersListener(OnDiscoverUsersListener listener) {
            mDiscoverUsersListener = listener;
        }

        public void setOnMapChangedListener(OnMapChangedListener listener) {
            mMapChangedListener = listener;
        }
    }

    public interface OnDiscoverUsersListener {
        void OnUserFound(User user);

        void onUserLost(String id);
    }

    public interface OnMapChangedListener {
        void onMapChanged(android.databinding.ObservableMap<String, ConnectionState> map);
    }
}