package com.myshirt.eg.handler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.myshirt.eg.R;

public class SiteInfo {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "site_info";

    public String name;

    @SuppressLint("CommitPrefEdits")
    public SiteInfo(Context context) {

        this.preferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        this.editor = preferences.edit();

        name = preferences.getString("name", context.getString(R.string.app_name));
    }

    public void set(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }
    public void set(String key, Boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean is_banner_enabled(String banner) {
        return preferences.getBoolean("banner_" + banner, (banner.equals("slide"))); //default is true for slide banner
    }

    public void set_banner_enable_value(String banner, boolean value) {
        editor.putBoolean("banner_" + banner, value);
        editor.commit();
    }

    public void enable_banner(String banner) {
        editor.putBoolean("banner_" + banner, true);
        editor.commit();
    }
    public void disable_banner(String banner) {
        editor.putBoolean("banner_" + banner, false);
        editor.commit();
    }

    public void set_last_check(long last) {
        editor.putLong("last_check", last);
        editor.commit();
    }

    public long get_last_check() {
        return preferences.getLong("last_check", 0);
    }


}
