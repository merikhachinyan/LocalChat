package com.ss.localchat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;


public class WifiStateBroadcastReceiver extends BroadcastReceiver{

    private OnWifiStateChangedListener mListener;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
            final int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            switch (state){
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.v("___", "Enabled");
                    //context.stopService(new Intent(context, AdvertiseService.class));
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    Log.v("____", "Disabled");

                    mListener.onWifiDisabled();

                    break;
            }
        }
    }

    public void setOnWifiStateChangedListener(OnWifiStateChangedListener listener) {
        mListener = listener;
    }

    public interface OnWifiStateChangedListener {
        void onWifiDisabled();
    }
}
