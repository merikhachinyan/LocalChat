package com.ss.localchat.activity;

import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ss.localchat.R;
import com.ss.localchat.adapter.ViewPagerFragmentAdapter;
import com.ss.localchat.fragment.ChatListFragment;
import com.ss.localchat.fragment.DiscoveredUsersFragment;

import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.service.ChatService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 1;

    private final static String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private DiscoveredUsersFragment.OnStartDiscoveryListener mDiscoveryListener = new DiscoveredUsersFragment.OnStartDiscoveryListener() {
        @Override
        public void OnStartDiscovery(boolean flag) {
            if (flag) {
                mFab.setImageResource(R.drawable.ic_stop_black_24dp);
            } else {
                mFab.setImageResource(R.drawable.ic_bluetooth_searching_black_24dp);
            }
        }
    };


    private List<Fragment> mFragmentList = new ArrayList<>();

    private static boolean isAdvertising;

    private OnButtonClickListener mListener;

    private FloatingActionButton mFab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (!Preferences.contain(getApplicationContext(), Preferences.USER_ID_KEY)) {

            if (!Preferences.contain(getApplicationContext(), Preferences.INTRODUCE_APP_KEY)) {
                Intent intent = new Intent(this, IntroduceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return;
            }
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
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

        if (!isAdvertising) {
            startService(new Intent(this, ChatService.class));
            isAdvertising = true;
        }
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
    }

    private void init() {
        mFragmentList.add(ChatListFragment.newInstance());
        mFragmentList.add(DiscoveredUsersFragment.newInstance());

        ViewPagerFragmentAdapter adapter =
                new ViewPagerFragmentAdapter(getSupportFragmentManager(), mFragmentList);

        final ViewPager viewPager = findViewById(R.id.view_pager_content_main);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        TabLayout tableLayout = findViewById(R.id.tab_layout_content_main);
        tableLayout.setupWithViewPager(viewPager);

        mFab = findViewById(R.id.floating_action_button);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() == 1) {
                    mListener.onDiscoveryButtonClick(mFab);
                }
            }
        });

        ((DiscoveredUsersFragment)mFragmentList.get(1)).setOnStartDisocveryListener(mDiscoveryListener);
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
                startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            isAdvertising = true;
        }
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        mListener = listener;
    }

    public interface OnButtonClickListener {
        void onDiscoveryButtonClick(FloatingActionButton fab);
    }
}