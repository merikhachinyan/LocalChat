package com.ss.localchat.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class BluetoothStateBroadcastReceiver extends BroadcastReceiver{

    private OnBluetoothStateChangedListener mListener;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state){
                case BluetoothAdapter.STATE_OFF:
                    Log.v("___", "State off");

                    mListener.onBluetoothDisabled();
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.v("_____", "State on");
                    break;
            }
        }
    }

    public void setOnBluetoothStateChangedListener(OnBluetoothStateChangedListener listener) {
        mListener = listener;
    }

    public interface OnBluetoothStateChangedListener {
        void onBluetoothDisabled();
    }
}
