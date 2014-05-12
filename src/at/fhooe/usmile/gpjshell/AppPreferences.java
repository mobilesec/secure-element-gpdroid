/*******************************************************************************
 * Copyright (c) 2014 Michael Hölzl <mihoelzl@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Hölzl <mihoelzl@gmail.com> - initial implementation
 *     Thomas Sigmund - data base, key set, channel set selection and GET DATA integration
 ******************************************************************************/
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
