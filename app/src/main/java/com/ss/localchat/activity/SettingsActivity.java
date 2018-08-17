package com.ss.localchat.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.ss.localchat.R;
import com.ss.localchat.service.AdvertiseService;

public class SettingsActivity extends AppCompatActivity {

    private Intent mAdvertiseIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        init();
    }

    private void init() {
        mAdvertiseIntent = new Intent(SettingsActivity.this, AdvertiseService.class);

        Switch advertisingSwitch = findViewById(R.id.turn_on_off_advertising_switch);

        advertisingSwitch.setChecked(isAdvertiseServiceRunning(AdvertiseService.class));

        advertisingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startAdvertiseService(isChecked);
            }
        });


    }

    private void startAdvertiseService(boolean flag) {
        if (flag) {
            startService(mAdvertiseIntent);
        } else {
            stopService(mAdvertiseIntent);
        }
    }

    private boolean isAdvertiseServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
