package com.ss.localchat.activity;

import android.Manifest;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ss.localchat.R;
import com.ss.localchat.adapter.ViewPagerFragmentAdapter;
import com.ss.localchat.fragment.ChatListFragment;
import com.ss.localchat.fragment.DiscoveredUsersFragment;

import com.ss.localchat.receiver.BluetoothStateBroadcastReceiver;
import com.ss.localchat.receiver.LocationStateBroadcastReceiver;
import com.ss.localchat.receiver.WifiStateBroadcastReceiver;
import com.ss.localchat.service.AdvertiseService;
import com.ss.localchat.service.DiscoverService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

  
    protected WifiStateBroadcastReceiver.OnWifiStateChangedListener mWifiStateChangedListener =
            new WifiStateBroadcastReceiver.OnWifiStateChangedListener() {
                @Override
                public void onWifiDisabled() {
                    if (isBluetoothDisabled) {
                        stopService(new Intent(MainActivity.this, AdvertiseService.class));
                        stopService(new Intent(MainActivity.this, DiscoverService.class));
                    } else {
                        isWifiDisabled = true;
                    }
                }
            };

    private BluetoothStateBroadcastReceiver.OnBluetoothStateChangedListener mBluetoothStateChangedListener =
            new BluetoothStateBroadcastReceiver.OnBluetoothStateChangedListener() {
                @Override
                public void onBluetoothDisabled() {
                    //Todo For location check which api
                    if (isWifiDisabled || isLocationDisabled) {
                        stopService(new Intent(MainActivity.this, AdvertiseService.class));
                        stopService(new Intent(MainActivity.this, DiscoverService.class));
                    } else {
                        isBluetoothDisabled = true;
                    }
                }
            };

    private LocationStateBroadcastReceiver.OnLocationStateChangedListener mLocationStateChangedListener =
            new LocationStateBroadcastReceiver.OnLocationStateChangedListener() {
                @Override
                public void onLocationStateDisabled() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        if (isBluetoothDisabled) {
                           stopService(new Intent(MainActivity.this, AdvertiseService.class));
                        } else {
                            isLocationDisabled = true;
                        }
                    }
                }
            };


    private List<Fragment> mFragmentList = new ArrayList<>();

    private WifiStateBroadcastReceiver mWifiStateBroadcastReceiver;
    private BluetoothStateBroadcastReceiver mBluetoothStateBroadcastReceiver;
    private LocationStateBroadcastReceiver mLocationStateBroadcastReceiver;

    private IntentFilter mWifiIntentFilter;
    private IntentFilter mBluetoothIntentFilter;
    private IntentFilter mLocationIntentFilter;

    private boolean isWifiDisabled;
    private boolean isBluetoothDisabled;
    private boolean isLocationDisabled;


    private List<Fragment> mFragmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!PreferenceManager.getDefaultSharedPreferences(this).contains("id")) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }

        registerReceivers();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceivers();
    }

    private boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS)
            return;

        for (int granResult : grantResults) {
            if (granResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Application can't start without required permissions", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }

    private void init() {
        mFragmentList.add(ChatListFragment.newInstance());
        mFragmentList.add(DiscoveredUsersFragment.newInstance());

        ViewPagerFragmentAdapter adapter =
                new ViewPagerFragmentAdapter(getSupportFragmentManager(), mFragmentList);

        ViewPager viewPager = findViewById(R.id.view_pager_content_main);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        TabLayout tableLayout = findViewById(R.id.tab_layout_content_main);
        tableLayout.setupWithViewPager(viewPager);

        mWifiStateBroadcastReceiver = new WifiStateBroadcastReceiver();
        mBluetoothStateBroadcastReceiver = new BluetoothStateBroadcastReceiver();
        mLocationStateBroadcastReceiver = new LocationStateBroadcastReceiver();

        mWifiIntentFilter = new IntentFilter();
        mWifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        mBluetoothIntentFilter = new IntentFilter();
        mBluetoothIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        mLocationIntentFilter = new IntentFilter();
        mLocationIntentFilter.addAction(LocationStateBroadcastReceiver.LOCATION_ACTION);

        mWifiStateBroadcastReceiver.setOnWifiStateChangedListener(mWifiStateChangedListener);
        mBluetoothStateBroadcastReceiver.setOnBluetoothStateChangedListener(mBluetoothStateChangedListener);
        mLocationStateBroadcastReceiver.setOnLocationStateChangedListener(mLocationStateChangedListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void registerReceivers(){
        registerReceiver(mWifiStateBroadcastReceiver, mWifiIntentFilter);
        registerReceiver(mBluetoothStateBroadcastReceiver, mBluetoothIntentFilter);
        registerReceiver(mLocationStateBroadcastReceiver, mLocationIntentFilter);
    }

    private void unregisterReceivers(){
        unregisterReceiver(mWifiStateBroadcastReceiver);
        unregisterReceiver(mBluetoothStateBroadcastReceiver);
        unregisterReceiver(mLocationStateBroadcastReceiver);
        }
    }
}
