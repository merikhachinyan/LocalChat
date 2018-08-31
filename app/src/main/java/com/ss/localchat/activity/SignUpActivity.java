package com.ss.localchat.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ss.localchat.R;
import com.ss.localchat.db.UserRepository;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.preferences.Preferences;

public class SignUpActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 1;

    private static final int REQUEST_CODE_SELECT_PICTURE = 1;

    private ImageView imageView;

    private EditText firstName;

    private EditText lastName;

    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        imageView = findViewById(R.id.imageViewLogin);

        Glide.with(this)
                .load(R.drawable.no_user_image)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnClick(v);
            }
        });

        firstName = findViewById(R.id.editTextFirstName);
        lastName = findViewById(R.id.editTextLastName);

        Button loginNoInternet = findViewById(R.id.loginNoInternet);
        loginNoInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstName.getText().toString().length() <= 0) {
                    firstName.setError("Enter FirstName");
                }
                if (lastName.getText().toString().length() <= 0) {
                    lastName.setError("Enter LastName");
                } else {
                    firstName.setError(null);
                    lastName.setError(null);

                    User user = new User();
                    user.setName(firstName.getText().toString() + " " + lastName.getText().toString());
                    user.setPhotoUrl(uri == null ? null : uri.toString());

                    new UserRepository(getApplication()).insert(user);
                    Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_ID_KEY, user.getId().toString());
                    Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_NAME_KEY, user.getName());
                    Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_PHOTO_KEY, user.getPhotoUrl());

                    startMainActivity();
                }
            }
        });
    }

    public void btnClick(View v) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION);
            return;
        }
        pickImage();
    }

    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_PICTURE) {
            uri = data.getData();

            Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .error(Glide.with(this).load(R.drawable.no_user_image).apply(RequestOptions.circleCropTransform()))
                    .into(imageView);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION)
            return;

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Application can't upload image without required permission", Toast.LENGTH_LONG).show();
                return;
            }
        }
        pickImage();
    }


    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
    }
}
