package com.ss.localchat.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

public class Preferences {

    public static final String USER_ID_KEY = "user.id";

    public static final String USER_NAME_KEY = "user.name";

    public static final String INTRODUCE_APP_KEY = "introduce.app";



    public static UUID getUserId(Context context) {
        return UUID.fromString(PreferenceManager.getDefaultSharedPreferences(context).getString(USER_ID_KEY, null));
    }

    public static String getUserName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(USER_NAME_KEY, null);
    }

    public static void putStringToPreferences(Context context, String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(key, value);
        editor.apply();
    }

    public static boolean contain(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).contains(key);
    }
}