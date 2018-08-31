package com.ss.localchat.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.Visibility;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;
import com.ss.localchat.R;
import com.ss.localchat.db.UserRepository;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.service.AdvertiseService;
import com.ss.localchat.util.CircularTransformation;
import com.ss.localchat.viewmodel.UserViewModel;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import static com.ss.localchat.preferences.Preferences.INTRODUCE_APP_KEY;
import static com.ss.localchat.preferences.Preferences.USER_ID_KEY;
import static com.ss.localchat.preferences.Preferences.USER_NAME_KEY;
import static com.ss.localchat.preferences.Preferences.getUserId;

public class SettingsActivity extends AppCompatActivity {
    private UserRepository userRepository;
    private ImageView imageView;
    private UserViewModel mUserViewModel;
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
    private final static String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE


            };

    private static final int SELECT_FILE = 1;
    private static final int REQUEST_CAMERA = 1;
    public static final String START_ADVERTISING = "Start Advertising";

    public static final String STOP_ADVERTISING = "Stop Advertising";
    private Uri uri;
    private TextView textViewMyProfileName;
    private TextView mAdvertiseTextView;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_settings);
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
        init();
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
                            Picasso.get()
                                    .load(mUser.getPhotoUrl())
                                    .transform(new CircularTransformation())
                                    .into(imageView);
                        } else {
                            Picasso.get()
                                    .load(R.drawable.no_user_image)
                                    .transform(new CircularTransformation())
                                    .into(imageView);
                        }
                        if (mUser.getName() == null) {
                            textViewMyProfileName.setText("User Name");
                        }
                        CardView cardView = findViewById(R.id.cardViewLogOut);
                        cardView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (AccessToken.getCurrentAccessToken() == null) {
                                    goIntroduceActivity();
                                } else {
                                    LoginManager.getInstance().logOut();
                                    goIntroduceActivity();
                                }

                            }
                        });
                        mUserViewModel.getUserById(myid).removeObserver(this);
                    }
                }
            });

        }

    }


    private void init() {
        userRepository = new UserRepository(getApplication());

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

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUser.getPhotoUrl() != null) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                    String mime = "*/*";
                    MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                    if (mimeTypeMap.hasExtension(
                            mimeTypeMap.getFileExtensionFromUrl(mUser.getPhotoUrl().toString())))
                        mime = mimeTypeMap.getMimeTypeFromExtension(
                                mimeTypeMap.getFileExtensionFromUrl(mUser.getPhotoUrl().toString()));
                    intent.setDataAndType(mUser.getPhotoUrl(), mime);
                    startActivity(intent);
                } else
                    addImage();
            }
        });

    }

    private boolean isEditable;


    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();

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


    private void goIntroduceActivity() {
        Intent intent = new Intent(this, IntroduceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.edit().remove(USER_ID_KEY).commit();
        sharedPreferences.edit().remove(USER_NAME_KEY).commit();
        sharedPreferences.edit().remove(INTRODUCE_APP_KEY).commit();

        userRepository.delete(mUser.getId());
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
        }
        SelectImage();
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
                    //  startActivityForResult(intent, SELECT_FILE);

                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_CAMERA) {

                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                uri = getImageUri(getApplicationContext(), bitmap);
                Picasso.get()
                        .load(uri)
                        .transform(new CircularTransformation())
                        .into(imageView);
                mUser.setPhotoUrl(uri);

            } else if (requestCode == SELECT_FILE) {

                uri = data.getData();
                Picasso.get()
                        .load(uri)
                        .transform(new CircularTransformation())
                        .into(imageView);
                mUser.setPhotoUrl(uri);
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

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
        });

        builder.setPositiveButton("Done", (dialog, which) -> {
            String name = etInput.getText().toString();
            textViewMyProfileName.setText(name);
            mUser.setName(name);

            userRepository.update(mUser);

        });

        builder.create().show();
    }
}