/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import java.util.HashSet;
import java.util.Set;

import net.alexjf.tmm.R;
import net.alexjf.tmm.utils.PreferenceManager;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.ipaulpro.afilechooser.utils.FileUtils;

public class PreferencesActivity extends PreferenceActivity {
    public static final String KEY_FORCEDATAREFRESH = "forceDataRefresh";

    private static final int REQCODE_FILECHOOSE = 0;

    private Set<OnStopListener> stopListeners = 
        new HashSet<OnStopListener>();
    private Set<OnSaveInstanceListener> saveInstanceListeners = 
        new HashSet<OnSaveInstanceListener>();
    private Set<OnRestoreInstanceListener> restoreInstanceListeners = 
        new HashSet<OnRestoreInstanceListener>();

    private OnFileChosenListener currentFileChoiceListener;
    private Intent result;

    public interface OnStopListener {
        public void onStop();
    }

    public interface OnSaveInstanceListener {
        public void onSaveInstance(Bundle bundle);
    }

    public interface OnRestoreInstanceListener {
        public void onRestoreInstance(Bundle bundle);
    }

    public void registerOnStopListener(OnStopListener listener) {
        stopListeners.add(listener);
    }

    public void unregisterOnStopListener(OnStopListener listener) {
        stopListeners.remove(listener);
    }

    public void registerOnSaveInstanceListener(OnSaveInstanceListener listener) {
        saveInstanceListeners.add(listener);
    }

    public void unregisterOnSaveInstanceListener(OnSaveInstanceListener listener) {
        saveInstanceListeners.remove(listener);
    }

    public void registerOnRestoreInstanceListener(OnRestoreInstanceListener listener) {
        restoreInstanceListeners.add(listener);
    }

    public void unregisterOnRestoreInstanceListener(OnRestoreInstanceListener listener) {
        restoreInstanceListeners.remove(listener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {    
        super.onCreate(savedInstanceState);       
        result = new Intent();
        setResult(RESULT_OK, result);
        PreferenceManager prefManager = PreferenceManager.getInstance();
        Log.d("TMM", prefManager.getCurrentUserPreferencesName());
        getPreferenceManager().setSharedPreferencesName(
                prefManager.getCurrentUserPreferencesName());
        addPreferencesFromResource(R.xml.preferences);       
    }

    @Override
    protected void onStop() {
        for (OnStopListener listener : stopListeners) {
            listener.onStop();
        }
       super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        for (OnSaveInstanceListener listener : saveInstanceListeners) {
            listener.onSaveInstance(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        for (OnRestoreInstanceListener listener : restoreInstanceListeners) {
            listener.onRestoreInstance(state);
        }
    }

    public void requestFileChooser(OnFileChosenListener requester) {
        currentFileChoiceListener = requester;
        Intent target = FileUtils.createGetContentIntent();
        Intent intent = Intent.createChooser(target, "Select a file");

        try {
            startActivityForResult(intent, REQCODE_FILECHOOSE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    public void setForceDataRefresh(boolean force) {
        result.putExtra(KEY_FORCEDATAREFRESH, force);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQCODE_FILECHOOSE:  
                if (resultCode == RESULT_OK &&
                    currentFileChoiceListener != null) {  
                    currentFileChoiceListener.onFileChosen(data.getData());
                }
        }
    }

    public interface OnFileChosenListener {
        public void onFileChosen(Uri uri);
    }
}
