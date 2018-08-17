package com.ss.localchat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;


public class LocationStateBroadcastReceiver extends BroadcastReceiver{

    public static final String LOCATION_ACTION = "android.location.PROVIDERS_CHANGED";

    private OnLocationStateChangedListener mListener;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(LOCATION_ACTION)){

            LocationManager locationManager = (LocationManager)context
                    .getSystemService(Context.LOCATION_SERVICE);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Toast.makeText(context, "enabled", Toast.LENGTH_SHORT).show();
            } else {
                mListener.onLocationStateDisabled();

                Toast.makeText(context, "disabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setOnLocationStateChangedListener(OnLocationStateChangedListener listener) {
        mListener = listener;
    }

    public interface OnLocationStateChangedListener {
        void onLocationStateDisabled();
    }
}
