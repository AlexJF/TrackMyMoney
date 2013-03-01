/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.preferences;

import net.alexjf.tmm.R;
import net.alexjf.tmm.activities.PreferencesActivity;
import net.alexjf.tmm.activities.PreferencesActivity.OnFileChosenListener;
import net.alexjf.tmm.exceptions.ImportException;
import net.alexjf.tmm.importexport.CSVImportExport;
import net.alexjf.tmm.utils.AsyncTaskWithProgressDialog;

import android.content.Context;
import android.net.Uri;
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
    implements OnFileChosenListener {
    private static final int RES_DIALOGLAYOUT = R.layout.prefdiag_import_data;

    private PreferencesActivity activity;

    private EditText locationEditText;
    private Button locationChooserButton;
    private CheckBox replaceCheckBox;

    public ImportDataPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setPositiveButtonText(R.string.import_text);
    }

    public ImportDataPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImportDataPreference(Context context) {
        this(context, null, 0);
    }

    @Override
    protected View onCreateDialogView() {
        activity = (PreferencesActivity) getContext();

        LayoutInflater vi = (LayoutInflater) this.getContext().
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

        AsyncTaskWithProgressDialog<String, Void, Throwable> asyncTask = 
            new AsyncTaskWithProgressDialog<String, Void, Throwable> 
            (getContext(), "Importing...") {
                @Override
                protected Throwable doInBackground(String... args) {
                    CSVImportExport importer = new CSVImportExport();
                    String path = args[0];
                    boolean replace = Boolean.parseBoolean(args[1]);
                    try {
                        importer.importData(path, replace);
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
                            "Import successful!", 3).show();
                        activity.setForceDataRefresh(true);
                    } else {
                        Toast.makeText(getContext(), 
                            "Import error! (" + e.getMessage() + ")", 3).show();
                        Log.e("TMM", e.getMessage(), e);
                    }
                };
            };

        asyncTask.execute(locationEditText.getText().toString(), 
                Boolean.toString(replaceCheckBox.isChecked()));
    }

    @Override
    public void onFileChosen(Uri uri) {
        locationEditText.setText(uri.getPath());
    }
}
