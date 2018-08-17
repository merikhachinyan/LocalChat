package com.ss.localchat.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.ss.localchat.R;
import com.ss.localchat.db.entity.User;
import com.ss.localchat.viewmodel.UserViewModel;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = sharedPref.edit();

        final EditText inputNameEditText = findViewById(R.id.input_name_edit_text_login_activity);

        findViewById(R.id.login_button_login_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User();
                user.setName(inputNameEditText.getText().toString().trim());
                editor.putString("id", user.getId().toString());
                editor.putString("name", user.getName());
                editor.apply();
                userViewModel.insert(user);

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}
