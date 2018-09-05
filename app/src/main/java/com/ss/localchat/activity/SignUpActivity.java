package com.ss.localchat.activity;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.ss.localchat.helper.BitmapHelper;
import com.ss.localchat.preferences.Preferences;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 1;

    private static final int REQUEST_CODE_SELECT_PICTURE = 1;

    private ImageView mImageView;

    private EditText mFirstName;

    private EditText mLastName;

    private Uri mUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mImageView = findViewById(R.id.imageViewLogin);

        Glide.with(this)
                .load(R.drawable.no_user_image)
                .apply(RequestOptions.circleCropTransform())
                .into(mImageView);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnClick(v);
            }
        });

        mFirstName = findViewById(R.id.editTextFirstName);
        mLastName = findViewById(R.id.editTextLastName);

        Button loginNoInternet = findViewById(R.id.loginNoInternet);
        loginNoInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFirstName.getText().toString().length() <= 0) {
                    mFirstName.setError("Enter FirstName");
                }
                if (mLastName.getText().toString().length() <= 0) {
                    mLastName.setError("Enter LastName");
                } else {
                    mFirstName.setError(null);
                    mLastName.setError(null);

                    User user = new User();
                    user.setName(mFirstName.getText().toString() + " " + mLastName.getText().toString());
                    user.setPhotoUrl(mUri == null ? null : mUri.toString());

                    new UserRepository(getApplication()).insert(user);
                    Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_ID_KEY, user.getId().toString());
                    Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_NAME_KEY, user.getName());

                    String path = saveToInternalStorage(Uri.parse(user.getPhotoUrl()), user.getId());
                    Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_PHOTO_KEY, path);

                }

                startMainActivity();
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

    private String saveToInternalStorage(Uri mUri, UUID uuid){
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
            } else if (extension.equals(".png")){
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }

            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        String path = directory.getAbsolutePath() + "/" + file.getName();

        return path;
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
            mUri = data.getData();

            Glide.with(this)
                    .load(mUri)
                    .apply(RequestOptions.circleCropTransform().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .error(Glide.with(this).load(R.drawable.no_user_image).apply(RequestOptions.circleCropTransform()))
                    .into(mImageView);
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
}
