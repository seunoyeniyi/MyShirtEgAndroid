package com.fatima.fabric.handler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class UserSession {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Context context;

    // Shared pref mode
    int PRIVATE_MODE = 0;
    // shared file name
    private static final String PREF_NAME = "user_session";

    public boolean LOGGED;
    public String userID;

    @SuppressLint("CommitPrefEdits")
    public UserSession(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        this.editor = preferences.edit();

        LOGGED = preferences.getBoolean("logged", false);
        userID = preferences.getString("userID", "0");
    }

    public void createLoginSession(String userID, String username, String email, boolean logged) {
            editor.putBoolean("logged", logged);
            editor.putString("userID", userID);
            editor.putString("username", username);
            editor.putString("email", email);
            editor.commit();
    }
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        user.put("userID", preferences.getString("userID", null));
        user.put("username", preferences.getString("username", null));
        user.put("email", preferences.getString("email", null));
        // return user
        return user;
    }
    public boolean logged() {
        return  preferences.getBoolean("logged", false);
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }

}
