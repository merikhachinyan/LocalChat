package com.ss.localchat.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.util.Log;


public class ConnectionsBroadcastReceiver extends BroadcastReceiver{

    public static final String LOCATION_ACTION = "android.location.PROVIDERS_CHANGED";

    private OnConnectionsStateChangedListener mListener;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        int state;

        if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){

            state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state){
                case BluetoothAdapter.STATE_OFF:
                    Log.v("___", "State off");
                    mListener.onBluetoothDisabled(true);
                    break;
                case BluetoothAdapter.STATE_ON:
                    mListener.onBluetoothDisabled(false);
                    break;
            }
        } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {

            state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

            switch (state) {
                case WifiManager.WIFI_STATE_DISABLED:
                    mListener.onWifiDisabled(true);
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    mListener.onWifiDisabled(false);
                    break;
            }
        } else if (action.equals(LOCATION_ACTION)) {

            LocationManager locationManager = (LocationManager)context
                    .getSystemService(Context.LOCATION_SERVICE);

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                mListener.onLocationStateDisabled(true);
            } else {
                mListener.onLocationStateDisabled(false);
            }
        }
    }

    public void setOnConnectionsStateChangedListener(OnConnectionsStateChangedListener  listener) {
        mListener = listener;
    }

    public interface OnConnectionsStateChangedListener {
        void onBluetoothDisabled(boolean flag);

        void onWifiDisabled(boolean flag);

        void onLocationStateDisabled(boolean flag);
    }
}
