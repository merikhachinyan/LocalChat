package com.ss.localchat.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.ss.localchat.R;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.preferences.Preferences;
import com.ss.localchat.util.CircularTransformation;

public class LoginNoInternet extends AppCompatActivity {
    private static final int SELECTED_PICTURE = 1;
    private ImageView imageView;
    private EditText firstName;
    private EditText lastName;
    private Button loginNoInternet;
    private User user;
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_no_internet);

        imageView = findViewById(R.id.imageViewLogin);

        Picasso.get()
                .load(R.drawable.no_user_image)
                .transform(new CircularTransformation())
                .into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnClick(v);
            }
        });

        firstName = findViewById(R.id.editTextFirstName);
        firstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        lastName = findViewById(R.id.editTextLastName);
        loginNoInternet = findViewById(R.id.loginNoInternet);
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
                    goMainScreen();
                }
            }
        });
    }

    public void btnClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SELECTED_PICTURE);
            } else {
                pickImage();
            }

        } else {
            pickImage();
        }


    }

    public void pickImage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECTED_PICTURE);
    }

    private Uri uri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case SELECTED_PICTURE:
                if (resultCode == RESULT_OK) {
                    uri = data.getData();

                    Picasso.get()
                            .load(uri)
                            .transform(new CircularTransformation())
                            .into(imageView);

                }

                break;

            default:
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    Toast.makeText(this, "Please give your permission.", Toast.LENGTH_LONG).show();
                }
                pickImage();
                break;
            }
        }
    }


    private void goMainScreen() {
        user = new User();
        user.setName(firstName.getText().toString());
        if (uri != null) {
            user.setPhotoUrl(uri.toString());
        }
        preferences.putStringToPreferences(getApplicationContext(), "user.id", user.getId().toString());
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }
}
