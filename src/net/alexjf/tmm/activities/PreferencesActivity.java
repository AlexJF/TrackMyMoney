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

public class PreferencesActivity extends PreferenceActivity {
    public static final String KEY_FORCEDATAREFRESH = "forceDataRefresh";

    private static final int REQCODE_FILECHOOSE = 0;

    private OnFileChosenListener currentFileChoiceListener;
    private Intent result;

    @Override
    public void onCreate(Bundle savedInstanceState) {    
        super.onCreate(savedInstanceState);       
        result = new Intent();
        addPreferencesFromResource(R.xml.preferences);       
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
            AsyncTaskWithProgressDialog<Void, Void, Throwable> asyncTask = 
                new AsyncTaskWithProgressDialog<Void, Void, Throwable> 
                (this, "Exporting...") {
                    @Override
                    protected Throwable doInBackground(Void... args) {
                        try {
                            CSVImportExport exporter = new CSVImportExport();
                            DateFormat dateFormat = new SimpleDateFormat("'TMM_'yyyy_MM_dd_HH_mm_ss'.cvs'");
                            File extDir = Environment.getExternalStorageDirectory();
                            File tmmDir = new File(extDir, "TMM");
                            tmmDir.mkdirs();
                            File exportPath = new File(tmmDir, dateFormat.format(new Date()));
                            exporter.exportData(exportPath.getPath());
                            return null;
                        } catch (Exception e) {
                            return e;
                        }
                    };

                    @Override
                    protected void onPostExecute(Throwable e) {
                        super.onPostExecute(e);

                        if (e == null) {
                            Toast.makeText(getContext(), 
                                "Export successful!", 3).show();
                        } else {
                            Toast.makeText(getContext(), 
                                "Import error! (" + e.getMessage() + ")", 3).show();
                            Log.e("TMM", e.getMessage(), e);
                        }
                    };
                };

            asyncTask.execute();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public interface OnFileChosenListener {
        public void onFileChosen(Uri uri);
    }
}
