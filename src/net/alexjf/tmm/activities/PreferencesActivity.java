/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.alexjf.tmm.R;
import net.alexjf.tmm.importexport.CSVImportExport;
import net.alexjf.tmm.utils.AsyncTaskWithProgressDialog;
import net.alexjf.tmm.utils.AsyncTaskWithProgressDialog.AsyncTaskResultListener;
import net.alexjf.tmm.utils.PreferenceManager;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

public class PreferencesActivity extends PreferenceActivity 
        implements AsyncTaskResultListener {
    public static final String KEY_FORCEDATAREFRESH = "forceDataRefresh";

    private static final int REQCODE_FILECHOOSE = 0;

    private static final String TASK_EXPORT = "export";

    private static AsyncTaskWithProgressDialog<Void> 
        exportTask;

    private OnFileChosenListener currentFileChoiceListener;
    private Intent result;

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

        if (exportTask != null) {
            exportTask.setContext(this);
            exportTask.setResultListener(this);
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

    // TODO: Move this to a pref dialog like import with date filtering
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference.getKey().equals("pref_key_export_data")) {
            if (exportTask == null) {
                exportTask = new AsyncTaskWithProgressDialog<Void> 
                (this, TASK_EXPORT, "Exporting...") {
                    @Override
                    protected Bundle doInBackground(Void... args) {
                        CSVImportExport exporter = new CSVImportExport();
                        DateFormat dateFormat = new SimpleDateFormat("'TMM_'yyyy_MM_dd_HH_mm_ss'.cvs'");
                        File extDir = Environment.getExternalStorageDirectory();
                        File tmmDir = new File(extDir, "TMM");
                        File exportPath = new File(tmmDir, dateFormat.format(new Date()));

                        try {
                            tmmDir.mkdirs();
                            exporter.exportData(exportPath.getPath());
                        } catch (Exception e) {
                            setThrowable(e);
                        }

                        return null;
                    }
                };

                exportTask.setResultListener(this);
                exportTask.ensureDatabaseOpen(true);
                exportTask.execute();
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onAsyncTaskResultSuccess(String taskId, Bundle resultData) {
        Toast.makeText(this, "Export successful!", 3).show();
        exportTask = null;
    }

    @Override
    public void onAsyncTaskResultCanceled(String taskId) {
        exportTask = null;
    }

    @Override
    public void onAsyncTaskResultFailure(String taskId, Throwable e) {
        Toast.makeText(this, "Import error! (" + e.getMessage() + ")", 3).show();
        Log.e("TMM", e.getMessage(), e);
        exportTask = null;
    }

    @Override
    protected void onDestroy() {
        if (exportTask != null) {
            exportTask.setContext(null);
        }
        super.onDestroy();
    }

    public interface OnFileChosenListener {
        public void onFileChosen(Uri uri);
    }
}
