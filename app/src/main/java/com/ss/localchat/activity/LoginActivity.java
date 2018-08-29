package com.ss.localchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.ss.localchat.R;
import com.ss.localchat.db.UserRepository;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.preferences.Preferences;

public class LoginActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mCallbackManager = CallbackManager.Factory.create();


        Button login = findViewById(R.id.btnLogin);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
            }
        });

        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Profile profile = Profile.getCurrentProfile();
                User user = new User();
                user.setName(profile.getName());
                user.setPhotoUrl(profile.getProfilePictureUri(400, 400));

                new UserRepository(getApplication()).insert(user);
                Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_ID_KEY, user.getId().toString());
                Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_NAME_KEY, user.getName());
                Preferences.putStringToPreferences(getApplicationContext(), Preferences.USER_PHOTO_KEY,
                        user.getPhotoUrl() == null ? null : user.getPhotoUrl().toString());

                startMainActivity();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "cancel_login", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "error_login", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}