package com.ss.localchat.activity;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.ss.localchat.R;
import com.ss.localchat.db.UserRepository;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.fragment.ShowPhotoFragment;
import com.ss.localchat.helper.BitmapHelper;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.viewmodel.UserViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    public static final String FRAGMENT_TAG = "show.photo";

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private final static String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE


            };

    private static final int SELECT_FILE = 1;

    private static final int REQUEST_CAMERA = 2;


    private ImageView mImageView;

    private EditText mNameEditText;

    private Uri mUri;

    private UserViewModel mUserViewModel;

    private CallbackManager mCallbackManager;

    private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {
        private ProfileTracker mProfileTracker;

        @Override
        public void onSuccess(LoginResult loginResult) {
            Profile profile = Profile.getCurrentProfile();
            final User user = new User();
            if (profile == null) {
                mProfileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                        user.setName(currentProfile.getName());
                        user.setPhotoUrl(currentProfile.getProfilePictureUri(400, 400).toString());

                        mProfileTracker.stopTracking();
                        new UserRepository(getApplication()).insert(user);
                        Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_ID_KEY, user.getId().toString());
                        Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_NAME_KEY, user.getName());

                        startMainActivity();
                    }


                };

            } else {
                user.setName(profile.getName());
                user.setPhotoUrl(profile.getProfilePictureUri(400, 400).toString());

                new UserRepository(getApplication()).insert(user);
                Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_ID_KEY, user.getId().toString());
                Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_NAME_KEY, user.getName());

                startMainActivity();
            }

        }

        @Override
        public void onCancel() {
            Toast.makeText(getApplicationContext(), "cancel_login", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(FacebookException error) {
            Toast.makeText(getApplicationContext(), "error_login", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
    }

    private void init() {
        mCallbackManager = CallbackManager.Factory.create();
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        LoginButton loginButton = findViewById(R.id.facebook_login_button);
        loginButton.registerCallback(mCallbackManager, mFacebookCallback);


        FloatingActionButton floatingActionButton = findViewById(R.id.loginCamera);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage();

            }
        });

        mImageView = findViewById(R.id.imageViewLogin);
        Glide.with(this)
                .load(R.drawable.no_user_image)
                .apply(RequestOptions.circleCropTransform())
                .into(mImageView);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri != null) {
                    showPhoto(mUri.toString());
                } else {
                    addImage();
                }
            }
        });

        mNameEditText = findViewById(R.id.editTextFirstName);
        mNameEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        Button loginNoInternet = findViewById(R.id.sign_up_button);
        loginNoInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNameEditText.getText().toString().trim().length() == 0) {
                    mNameEditText.setError("Enter name");
                } else {
                    mNameEditText.setError(null);

                    User user = new User();
                    user.setName(mNameEditText.getText().toString());
                    user.setPhotoUrl(mUri == null ? null : mUri.toString());

                    mUserViewModel.insert(user);
                    Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_ID_KEY, user.getId().toString());
                    Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_NAME_KEY, user.getName());

                    if (user.getPhotoUrl() != null) {
                        String path = saveToInternalStorage(Uri.parse(user.getPhotoUrl()), user.getId());
                        Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_PHOTO_KEY, path);
                    }

                    startMainActivity();
                }
            }
        });
    }

    public void addImage() {
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        } else {
            selectImage();
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

    private String saveToInternalStorage(Uri mUri, UUID uuid) {
        Bitmap bitmap = BitmapHelper.getResizedBitmap(BitmapHelper.uriToBitmap(this, mUri), 480);

        String extension = getFileExtension(mUri);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());

        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File file = new File(directory, uuid + extension);

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);

            if (extension.equals(".jpg")) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            } else if (extension.equals(".png")) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }

            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return directory.getAbsolutePath() + "/" + file.getName();
    }

    private void selectImage() {

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

                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);

                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private void showPhoto(String photoUri) {
        ShowPhotoFragment fragment = ShowPhotoFragment.newInstance(photoUri);
        fragment.show(getSupportFragmentManager(), FRAGMENT_TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode != RESULT_OK)
                return;

            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");
            mUri = getImageUri(getApplicationContext(), bitmap);
            Glide.with(this)
                    .load(mUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mImageView);

        } else if (requestCode == SELECT_FILE) {
            if (resultCode != RESULT_OK)
                return;

            mUri = data.getData();
            Glide.with(this)
                    .load(mUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mImageView);
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
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

        selectImage();
    }

    public String getFileExtension(Uri mUri) {
        String filePath = null;
        String[] filePathColumn = {MediaStore.Images.Media.DISPLAY_NAME};

        Cursor cursor = getContentResolver().query(mUri, filePathColumn, null, null, null);
        if (cursor == null)
            return null;

        if (cursor.moveToFirst()) {
            String fileName = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            filePath = fileName.substring(fileName.lastIndexOf("."));
        }
        cursor.close();
        return filePath;
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}