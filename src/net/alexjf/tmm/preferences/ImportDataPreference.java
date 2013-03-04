/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.preferences;

import net.alexjf.tmm.R;
import net.alexjf.tmm.activities.PreferencesActivity;
import net.alexjf.tmm.activities.PreferencesActivity.OnDestroyListener;
import net.alexjf.tmm.activities.PreferencesActivity.OnFileChosenListener;
import net.alexjf.tmm.activities.PreferencesActivity.OnRestoreInstanceListener;
import net.alexjf.tmm.importexport.CSVImportExport;
import net.alexjf.tmm.utils.AsyncTaskWithProgressDialog;
import net.alexjf.tmm.utils.AsyncTaskWithProgressDialog.AsyncTaskResultListener;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class ImportDataPreference extends DialogPreference 
    implements OnFileChosenListener, AsyncTaskResultListener,
           OnDestroyListener, 
           OnRestoreInstanceListener {
    private static final int RES_DIALOGLAYOUT = R.layout.prefdiag_import_data;

    private static AsyncTaskWithProgressDialog<String> importTask;

    private static final String TASK_IMPORT = "import";

    private PreferencesActivity activity;

    private EditText locationEditText;
    private Button locationChooserButton;
    private CheckBox replaceCheckBox;

    public ImportDataPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    public ImportDataPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    protected void initialize(Context context) {
        activity = (PreferencesActivity) context;
        setPositiveButtonText(R.string.import_text);
        activity.registerOnDestroyListener(this);
        activity.registerOnRestoreInstanceListener(this);
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater vi = (LayoutInflater) activity.
            getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(RES_DIALOGLAYOUT, null);

        locationEditText = (EditText) view.findViewById(R.id.filepath_text);
        locationChooserButton = (Button) view.findViewById(R.id.filepath_chooser);
        replaceCheckBox = (CheckBox) view.findViewById(R.id.replace_check);

        locationChooserButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                activity.requestFileChooser(ImportDataPreference.this);
            }
        });

        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (!positiveResult) {
            return;
        }

        if (importTask != null) {
            return;
        }

        importTask = 
            new AsyncTaskWithProgressDialog<String> 
            (activity, TASK_IMPORT, "Importing...") {
                @Override
                protected Bundle doInBackground(String... args) {
                    CSVImportExport importer = new CSVImportExport();
                    String path = args[0];

                    boolean replace = Boolean.parseBoolean(args[1]);
                    try {
                        importer.importData(path, replace);
                    } catch (Exception e) {
                        setThrowable(e);
                    }

                    return null;
                }
            };

        importTask.setResultListener(this);
        importTask.ensureDatabaseOpen(true);
        importTask.execute(locationEditText.getText().toString(), 
                Boolean.toString(replaceCheckBox.isChecked()));
    }

    @Override
    public void onFileChosen(Uri uri) {
        locationEditText.setText(uri.getPath());
    }

    @Override
    public void onAsyncTaskResultSuccess(String taskId, Bundle resultData) {
        Toast.makeText(activity, 
            "Import successful!", 3).show();
        activity.setForceDataRefresh(true);
        importTask = null;
    }

    @Override
    public void onAsyncTaskResultCanceled(String taskId) {
        importTask = null;
    }

    @Override
    public void onAsyncTaskResultFailure(String taskId, Throwable e) {
        Toast.makeText(activity, 
            "Import error! (" + e.getMessage() + ")", 3).show();
        Log.e("TMM", e.getMessage(), e);
        importTask = null;
    }

    @Override
    public void onRestoreInstance(Bundle state) {
        Log.d("TMM", "Activity restore instance");
        if (importTask != null) {
            Log.d("TMM", "Resetting context");
            importTask.setContext(activity);
            importTask.setResultListener(this);
        }
    }

    @Override
    public void onDestroy() {
        Log.d("TMM", "Activity destroy");
        if (importTask != null) {
            Log.d("TMM", "Nulling context");
            importTask.setContext(null);
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.d("TMM", "Dialog restore instance");
        super.onRestoreInstanceState(state);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.d("TMM", "Dialog save instance");
        return super.onSaveInstanceState();
    }

    @Override
    protected void showDialog(Bundle state) {
        Log.d("TMM", "Dialog show");
        super.showDialog(state);
    }
}
