package com.ss.localchat.activity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.ss.localchat.R;
import com.ss.localchat.db.UserRepository;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.fragment.ShowPhotoFragment;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.service.ChatService;
import com.ss.localchat.viewmodel.GroupViewModel;
import com.ss.localchat.viewmodel.MessageViewModel;
import com.ss.localchat.viewmodel.UserViewModel;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static com.ss.localchat.preferences.Preferences.INTRODUCE_APP_KEY;
import static com.ss.localchat.preferences.Preferences.USER_ID_KEY;
import static com.ss.localchat.preferences.Preferences.USER_NAME_KEY;

public class SettingsActivity extends AppCompatActivity {

    public static final String ENABLE_ADVERTISING = "Enable Advertising";

    public static final String DISABLE_ADVERTISING = "Disable Advertising";

    public static final String FRAGMENT_TAG = "show.photo";

    private UserRepository userRepository;

    private ImageView imageView;

    private UserViewModel mUserViewModel;

    private MessageViewModel mMessageViewModel;

    private GroupViewModel mGroupViewModel;

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private final static String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE


            };

    private static final int SELECT_FILE = 1;

    private static final int REQUEST_CAMERA = 2;

    private Uri mUri;

    private TextView textViewMyProfileName;

    private TextView mAdvertiseTextView;

    private User mUser;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAdvertiseBinder = (ChatService.ServiceBinder) service;

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


    private Switch mAdvertisingSwitch;

    private ChatService.ServiceBinder mAdvertiseBinder;

    private boolean isBound;
    private boolean isEditable;

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
        userRepository = new UserRepository(getApplication());
        mMessageViewModel=new MessageViewModel(getApplication());
        mGroupViewModel=new GroupViewModel(getApplication());
        textViewMyProfileName = findViewById(R.id.myprofile_name);
        imageView = findViewById(R.id.myprofile_imageView);
        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButtonCamera);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage();

            }
        });

        Button button = findViewById(R.id.btnEditName);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndDisplayDialog();
            }
        });

        final Intent intent = new Intent(this, ChatService.class);

        mAdvertisingSwitch = findViewById(R.id.turn_on_off_advertising_switch);

        mAdvertiseTextView = findViewById(R.id.advertising_text);

        mAdvertisingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (isBound) {
                        if (!mAdvertiseBinder.isRunningService()) {
                            startService(intent);
                            mAdvertiseTextView.setText(DISABLE_ADVERTISING);
                        }
                    }
                } else {
                    mAdvertiseBinder.stopService();
                    mAdvertiseTextView.setText(ENABLE_ADVERTISING);
                }
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUser.getPhotoUrl() != null) {
                    showPhoto(mUser.getPhotoUrl());
                } else
                    addImage();
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

    @Override
    protected void onStart() {
        super.onStart();

        if (!isEditable) {
            mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
            final UUID myid = Preferences.getUserId(getApplicationContext());
            mUserViewModel.getUserById(myid).observe(this, new Observer<User>() {
                @Override
                public void onChanged(@Nullable User user) {

                    mUser = user;
                    if (mUser != null) {
                        textViewMyProfileName.setText(mUser.getName());
                        if (mUser.getPhotoUrl() != null) {
                            Glide.with(getApplicationContext())
                                    .load(mUser.getPhotoUrl())
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(imageView);

                        } else {
                            Glide.with(getApplicationContext())
                                    .load(R.drawable.no_user_image)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(imageView);

                        }
                        if (mUser.getName() == null) {
                            textViewMyProfileName.setText("User Name");
                        }
                        CardView cardView = findViewById(R.id.cardViewLogOut);
                        cardView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
                                alert.setMessage("Are you sure?")
                                        .setPositiveButton("Logout", new DialogInterface.OnClickListener()                 {

                                            public void onClick(DialogInterface dialog, int which) {

                                                logout();
                                            }
                                        }).setNegativeButton("Cancel", null);

                                AlertDialog alert1 = alert.create();
                                alert1.show();


                            }
                        });
                        mUserViewModel.getUserById(myid).removeObserver(this);
                    }
                }
            });
        }
    }

    private void logout(){
        if (AccessToken.getCurrentAccessToken() == null) {
            goIntroduceActivity();
        } else {
            LoginManager.getInstance().logOut();
            goIntroduceActivity();
        }
    }

    private void goIntroduceActivity() {
        Intent intent = new Intent(this, IntroduceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        Preferences.removeUser(getApplicationContext());

        userRepository.delete(mUser.getId());
        mMessageViewModel.deleteAllMessages();
        mGroupViewModel.deleteAllGroups();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();

        mAdvertiseBinder.stopService();

        startActivity(intent);

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    public void addImage() {
        isEditable = true;
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        } else {
            SelectImage();
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

    private void SelectImage() {

        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Image");

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);

                } else if (items[i].equals("Gallery")) {

                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Select File"), SELECT_FILE);

                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();

    }

    private void showPhoto(String photoUri) {
        ShowPhotoFragment fragment = ShowPhotoFragment.newInstance(photoUri);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_layout, fragment, FRAGMENT_TAG)
                .addToBackStack(FRAGMENT_TAG)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_CAMERA) {

                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                mUri = getImageUri(getApplicationContext(), bitmap);
                Glide.with(this)
                        .load(mUri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageView);

                mUser.setPhotoUrl(mUri.toString());

            } else if (requestCode == SELECT_FILE) {

                mUri = data.getData();
                Glide.with(this)
                        .load(mUri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageView);
                mUser.setPhotoUrl(mUri.toString());
            }
            userRepository.update(mUser);

        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS)
            return;

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Application can't upload image without required permission", Toast.LENGTH_LONG).show();
                return;
            }
        }
        SelectImage();
    }

    private void createAndDisplayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        TextView tvMessage = new TextView(this);
        final EditText etInput = new EditText(this);
        etInput.setText(textViewMyProfileName.getText());
        tvMessage.setText("Enter name:");
        tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        etInput.setSingleLine();
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(tvMessage);
        layout.addView(etInput);
        layout.setPadding(50, 40, 50, 10);

        builder.setView(layout);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String name = etInput.getText().toString();
                textViewMyProfileName.setText(name);
                mUser.setName(name);

                userRepository.update(mUser);
            }}).setNegativeButton("Cancel", null);

        builder.create().show();
    }
}