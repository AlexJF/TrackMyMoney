/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.utils;

import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.User;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class acts as a helper to access the global/user shared preferences.
 */
public class PreferenceManager {
    public static String GLOBAL_PREFERENCES = "global";

    private static PreferenceManager instance = null;

    private Context context;
    private SharedPreferences globalPreferences;
    private User currentUser;
    private SharedPreferences currentUserPreferences;

    public static PreferenceManager initialize(Application app) {
        if (instance == null) {
            instance = new PreferenceManager(app);
        }

        return instance;
    }

    public static  PreferenceManager getInstance() {
        return instance;
    }

    private PreferenceManager(Application app) {
        context = app;
    }

    public String readUserStringPreference(String key, String def) {
        SharedPreferences preferences = getCurrentUserPreferences();
        return preferences.getString(key, def);
    }

    public void writeUserStringPreference(String key, String value) {
        SharedPreferences.Editor editor = getCurrentUserPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void removeUserPreference(String key) {
        SharedPreferences.Editor editor = getCurrentUserPreferences().edit();
        editor.remove(key);
        editor.commit();
    }

    public String readGlobalStringPreference(String key, String def) {
        SharedPreferences preferences = getGlobalPreferences();
        return preferences.getString(key, def);
    }

    public void writeGlobalStringPreference(String key, String value) {
        SharedPreferences.Editor editor = getGlobalPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void removeGlobalPreference(String key) {
        SharedPreferences.Editor editor = getGlobalPreferences().edit();
        editor.remove(key);
        editor.commit();
    }

    public SharedPreferences getGlobalPreferences() {
        if (globalPreferences == null) {
            globalPreferences = getSharedPreferences(GLOBAL_PREFERENCES);
        }
        
        return globalPreferences;
    }

    public SharedPreferences getCurrentUserPreferences() {
        User currentUser = DatabaseHelper.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            return null;
        }

        if (currentUserPreferences == null ||
            currentUser != this.currentUser) {
            this.currentUser = currentUser;
            currentUserPreferences = getSharedPreferences(currentUser.getName());
        }

        return currentUserPreferences;
    }

    public String getCurrentUserPreferencesName() {
        User currentUser = DatabaseHelper.getInstance().getCurrentUser();

        if (currentUser == null) {
            return null;
        }

        return currentUser.getName();
    }

    private SharedPreferences getSharedPreferences(String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }
}
