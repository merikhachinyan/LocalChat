package com.ss.localchat.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.ss.localchat.R;
import com.ss.localchat.service.AdvertiseService;

public class SettingsActivity extends AppCompatActivity {

    public static final String START_ADVERTISING = "Start Advertising";

    public static final String STOP_ADVERTISING = "Stop Advertising";


//    private ServiceConnection mAdvertiseServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            mAdvertiseBinder = (AdvertiseService.AdvertiseBinder)service;
//
//
//            //mAdvertiseBinder.advertise();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            mAdvertiseBinder = null;
//        }
//    };

    private TextView mAdvertiseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        init();
    }

    private void init() {
        final Intent intent = new Intent(this, AdvertiseService.class);

        Switch advertisingSwitch = findViewById(R.id.turn_on_off_advertising_switch);

        mAdvertiseTextView = findViewById(R.id.advertising_text);


        advertisingSwitch.setChecked(isRunningAdvertiseService(AdvertiseService.class));

        advertisingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(intent);
                    mAdvertiseTextView.setText(STOP_ADVERTISING);
                } else {
                    stopService(intent);
                    mAdvertiseTextView.setText(START_ADVERTISING);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isRunningAdvertiseService(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                mAdvertiseTextView.setText(STOP_ADVERTISING);
                return true;
            }
        }
        mAdvertiseTextView.setText(START_ADVERTISING);
        return false;
    }
}