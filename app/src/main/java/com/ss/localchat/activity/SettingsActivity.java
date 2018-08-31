package com.ss.localchat.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.ss.localchat.R;
import com.ss.localchat.service.ChatService;

public class SettingsActivity extends AppCompatActivity {

    public static final String ENABLE_ADVERTISING = "Enable Advertising";

    public static final String DISABLE_ADVERTISING = "Disable Advertising";


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAdvertiseBinder = (ChatService.ServiceBinder)service;

            isBound = true;

            if (mAdvertiseBinder.isRunningService()) {
                mAdvertiseTextView.setText(DISABLE_ADVERTISING);

                mAdvertisingSwitch.setChecked(true);
            } else {
                mAdvertiseTextView.setText(ENABLE_ADVERTISING);

                mAdvertisingSwitch.setChecked(false);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAdvertiseBinder = null;

            isBound = false;
        }
    };

    private TextView mAdvertiseTextView;

    private Switch mAdvertisingSwitch;

    private ChatService.ServiceBinder mAdvertiseBinder;

    private boolean isBound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        bindService(new Intent(this, ChatService.class), mServiceConnection, Context.BIND_AUTO_CREATE);

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isBound) {
            unbindService(mServiceConnection);

            Log.v("___", "unbind settings");
        }
    }

    private void init() {
        final Intent intent = new Intent(this, ChatService.class);

        mAdvertisingSwitch = findViewById(R.id.turn_on_off_advertising_switch);

        mAdvertiseTextView = findViewById(R.id.advertising_text);

        mAdvertisingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!mAdvertiseBinder.isRunningService()){
                        startService(intent);
                        mAdvertiseTextView.setText(DISABLE_ADVERTISING);

                    }
                } else {
                    mAdvertiseBinder.stopService();
                    mAdvertiseTextView.setText(ENABLE_ADVERTISING);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        sendResult();
    }

    private void sendResult() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}