/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.preferences;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import net.alexjf.tmm.R;
import net.alexjf.tmm.activities.PreferencesActivity;
import net.alexjf.tmm.activities.PreferencesActivity.OnFileChosenListener;
import net.alexjf.tmm.importexport.CSVImportExport;
import net.alexjf.tmm.utils.AsyncTaskWithProgressDialog;

public class ImportDataPreference
		extends DialogWithAsyncTaskProgressPreference<String>
		implements OnFileChosenListener {
	private static final int RES_DIALOGLAYOUT = R.layout.prefdiag_import_data;

	private static final String TASK_IMPORT = "import";

	protected EditText locationEditText;
	protected Button locationChooserButton;
	protected CheckBox replaceCheckBox;

	public ImportDataPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

	public ImportDataPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	protected void initialize(Context context) {
		super.initialize(context);
		setPositiveButtonText(R.string.import_text);
	}

	@Override
	protected View onCreateDialogView() {
		LayoutInflater vi = (LayoutInflater) getActivity().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = vi.inflate(RES_DIALOGLAYOUT, null);

		locationEditText = (EditText) view.findViewById(R.id.filepath_text);
		locationChooserButton = (Button) view.findViewById(R.id.filepath_chooser);
		replaceCheckBox = (CheckBox) view.findViewById(R.id.replace_check);

		locationChooserButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				getActivity().requestFileChooser(ImportDataPreference.this);
			}
		});

		return view;
	}

	@Override
	protected AsyncTaskWithProgressDialog<String> createTask() {
		String strLoading = getActivity().getResources().getString(
				R.string.importing);
		return new AsyncTaskWithProgressDialog<String>
				(TASK_IMPORT, strLoading) {
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
	}

	@Override
	protected void runTask(AsyncTaskWithProgressDialog<String> task) {
		task.execute(locationEditText.getText().toString(),
				Boolean.toString(replaceCheckBox.isChecked()));
	}

	@Override
	public void onFileChosen(Uri uri) {
		locationEditText.setText(uri.getPath());
	}

	@Override
	public void onAsyncTaskResultSuccess(String taskId, Bundle resultData) {
		super.onAsyncTaskResultSuccess(taskId, resultData);
		PreferencesActivity activity = getActivity();
		Toast.makeText(activity,
				getActivity().getResources().getString(R.string.import_success),
				3).show();
		activity.setForceDataRefresh(true);
	}

	@Override
	public void onAsyncTaskResultFailure(String taskId, Throwable e) {
		super.onAsyncTaskResultFailure(taskId, e);
		String strError = getActivity().getResources().getString(
				R.string.error_import);
		Toast.makeText(getActivity(),
				String.format(strError, e.getMessage()), 3).show();
		Log.e("TMM", e.getMessage(), e);
	}
}
