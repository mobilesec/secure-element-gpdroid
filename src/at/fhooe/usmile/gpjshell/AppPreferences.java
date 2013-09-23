package at.fhooe.usmile.gpjshell;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AppPreferences {
    public static final String KEY_PREFS_SELECTED_CAP = "applet_selected_cap";
    private static final String APP_SHARED_PREFS = AppPreferences.class.getSimpleName(); //  Name of the file -.xml
    private SharedPreferences mSharedPrefs;
    private Editor mPrefsEditor;

    public AppPreferences(Context context) {
        this.mSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.mPrefsEditor = mSharedPrefs.edit();
    }

    public String getSelectedCap() {
        return mSharedPrefs.getString(KEY_PREFS_SELECTED_CAP, ""); // Get our string from prefs or return an empty string
    }

    public void saveSelectedCap(String text) {
        mPrefsEditor.putString(KEY_PREFS_SELECTED_CAP, text);
        mPrefsEditor.commit();
    }
}
