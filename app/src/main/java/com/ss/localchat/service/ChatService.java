package com.ss.localchat.service;

import android.app.IntentService;
import android.arch.lifecycle.Observer;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.databinding.ObservableArrayMap;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
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
import com.ss.localchat.db.MessageRepository;
import com.ss.localchat.db.UserRepository;
import com.ss.localchat.db.entity.Message;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.helper.NotificationHelper;
import com.ss.localchat.model.ConnectionState;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.receiver.ConnectionsBroadcastReceiver;
import com.ss.localchat.viewmodel.UserViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;


public class ChatService extends IntentService {

    public static final String NOTIFICATION_TITLE = "Local Chat";

    public static final String NOTIFICATION_CONTENT = "Advertising...";

    public static final int FOREGROUND_NOTIFICATION_ID = 1;

    public static final Strategy STRATEGY = Strategy.P2P_CLUSTER;

    public static final String MESSAGE_TYPE = "message";

    public static final String FILENAME_TYPE = "filename";


    protected ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String id, @NonNull ConnectionInfo connectionInfo) {
            mConnectionsClient.acceptConnection(id, mPayloadCallback);

            mEndpoints.put(id, ConnectionState.CONNECTING);

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

            String myUserOwner = Preferences.getUserName(getApplicationContext()) + ":" + Preferences.getUserId(getApplicationContext());
            mConnectionsClient.requestConnection(myUserOwner, id, mConnectionLifecycleCallback);

        }
    };

    protected PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {

            if (payload.getType() == Payload.Type.BYTES) {
                try {
                    String payloadText = new String(payload.asBytes(), StandardCharsets.UTF_8);

                    JSONObject jsonObject = new JSONObject(payloadText);
                    String type = jsonObject.getString("type");

                    if (MESSAGE_TYPE.equals(type)) {
                        UUID senderId = UUID.fromString(jsonObject.getString("id"));
                        String messageText = jsonObject.getString("message");

                        UUID myUserId = Preferences.getUserId(getApplicationContext());

                        Message message = new Message();
                        message.setText(messageText);
                        message.setRead(false);
                        message.setReceiverId(myUserId);
                        message.setSenderId(senderId);
                        message.setDate(new Date());
                        mMessageRepository.insert(message);


                        showMessageNotification(s, messageText);
                    } else if (FILENAME_TYPE.equals(type)) {
                        String senderId = jsonObject.getString("id");
                        String payloadId = jsonObject.getString("payload_id");
                        String imageExtension = jsonObject.getString("extension");

                        String fileName = "photo-".concat(senderId).concat(imageExtension);

                        filePayloadFilenames.put(Long.parseLong(payloadId), fileName);
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
                    Log.v("____", "Receive and renamed(old file): " + payloadFile.getAbsolutePath());
                    Log.v("____", "Receive and renamed(new file): " + newFile.getAbsolutePath());
                    mUserRepository.updatePhoto(s, Uri.fromFile(newFile).toString());
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

            mEndpoints.put(id, ConnectionState.CONNECTING);

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

    private ConnectionsClient mConnectionsClient;

    private MessageRepository mMessageRepository;

    private UserRepository mUserRepository;

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
    }

    private void sendMessage(String id, String messageText) {
        try {
            UUID myUserId = Preferences.getUserId(getApplicationContext());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", MESSAGE_TYPE);
            jsonObject.put("id", myUserId.toString());
            jsonObject.put("message", messageText);
            mConnectionsClient.sendPayload(id, Payload.fromBytes(jsonObject.toString().getBytes(StandardCharsets.UTF_8)));

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
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(myUserPhotoUri, "r");
                Payload filePayload = Payload.fromFile(pfd);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", FILENAME_TYPE);
                jsonObject.put("id", myUserId.toString());
                jsonObject.put("payload_id", filePayload.getId());
                jsonObject.put("extension", getFileExtension(myUserPhotoUri));

                mConnectionsClient.sendPayload(id, Payload.fromBytes(jsonObject.toString().getBytes(StandardCharsets.UTF_8)));
                mConnectionsClient.sendPayload(id, filePayload);

            } catch (FileNotFoundException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension(Uri uri) {
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

    private void showMessageNotification(final String endpointId, final String messageText) {

        mUserRepository.getUserByEndpointId(endpointId).observeForever(new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if (user != null) {
                    if (!ChatActivity.isCurrentlyRunning || (ChatActivity.isCurrentlyRunning && !ChatActivity.currentUserId.equals(user.getId()))) {
                        NotificationHelper.showNotification(getApplicationContext(), user, messageText);
                    }
                }
                mUserRepository.getUserByEndpointId(endpointId).removeObserver(this);
            }
        });
    }

    public class ServiceBinder extends Binder {
        public void startDiscovery() {
            discover();
        }

        public void stopDiscovery() {
            mConnectionsClient.stopDiscovery();
        }

        public void sendMessageTo(String id, String messageText) {
            sendMessage(id, messageText);
        }

        public void stopService() {
            stopAdvertising();
        }

        public boolean isRunningService() {
            return isRunningService;
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