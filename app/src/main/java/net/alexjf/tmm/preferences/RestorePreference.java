/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import net.alexjf.tmm.R;
import net.alexjf.tmm.activities.PreferencesActivity;
import net.alexjf.tmm.activities.PreferencesActivity.OnFileChosenListener;
import net.alexjf.tmm.database.DatabaseManager;
import net.alexjf.tmm.domain.User;
import net.alexjf.tmm.utils.AsyncTaskWithProgressDialog;
import net.alexjf.tmm.utils.PreferenceManager;
import net.alexjf.tmm.utils.Utils;

import java.io.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RestorePreference
		extends DialogWithAsyncTaskProgressPreference<String>
		implements OnFileChosenListener {
	private static final int RES_DIALOGLAYOUT = R.layout.prefdiag_restore;
	private static final String TASK_RESTORE = "restore";

	protected EditText locationEditText;
	protected Button locationChooserButton;

	public RestorePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

	public RestorePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	protected void initialize(Context context) {
		super.initialize(context);
		setPositiveButtonText(R.string.restore);
	}

	@Override
	protected View onCreateDialogView() {
		LayoutInflater vi = (LayoutInflater) getActivity().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = vi.inflate(RES_DIALOGLAYOUT, null);

		locationEditText = (EditText) view.findViewById(R.id.filepath_text);
		locationChooserButton = (Button) view.findViewById(R.id.filepath_chooser);

		locationChooserButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				getActivity().requestFileChooser(RestorePreference.this);
			}
		});

		return view;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected AsyncTaskWithProgressDialog<String> createTask() {
		String strLoading = getActivity().getResources().getString(
				R.string.restoring);
		return new AsyncTaskWithProgressDialog<String>
				(TASK_RESTORE, strLoading) {
			@Override
			protected Bundle doInBackground(String... args) {
				File tmpBackupFile = null;

				try {
					Uri fileUri = Uri.parse(args[0]);

					ParcelFileDescriptor parcelFileDescriptor =
							getContext().getContentResolver().openFileDescriptor(fileUri, "r");

					FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
					FileOutputStream fileOutputStream = null;

					// Copy file specified by URI to a local temporary file (file might not be available for
					// direct read using the File API - Google Drive files for instance).
					try {
						tmpBackupFile = File.createTempFile("backup", null, getContext().getCacheDir());
						fileOutputStream = new FileOutputStream(tmpBackupFile);

						byte[] buffer = new byte[1024];
						int length;

						while ((length = fileInputStream.read(buffer)) > 0) {
							fileOutputStream.write(buffer, 0, length);
						}

						fileOutputStream.close();
						fileOutputStream = null;
					} finally {
							fileInputStream.close();

						if (fileOutputStream != null) {
							fileOutputStream.close();
						}
					}

					ZipFile backupZip = new ZipFile(tmpBackupFile);

					ZipEntry dbEntry = backupZip.getEntry("database");
					InputStream is = backupZip.getInputStream(dbEntry);
					DataInputStream dis = new DataInputStream(is);
					// Close the database for we will replace it (and clean cache).
					DatabaseManager.getInstance().closeDatabase(true);
					User currentUser = Utils.getCurrentUser();
					File filesDir = getActivity().getFilesDir();
					File dbFile = new File(filesDir, "../databases/" +
							currentUser.getName() + ".db");
					FileOutputStream fos = new FileOutputStream(dbFile);

					try {
						byte[] buffer = new byte[1024];
						int bytesRead = 0;
						while ((bytesRead = dis.read(buffer)) != -1) {
							fos.write(buffer, 0, bytesRead);
						}
					} finally {
						dis.close();
						fos.close();
					}

					ZipEntry prefEntry = backupZip.getEntry("prefs");
					is = backupZip.getInputStream(prefEntry);
					ObjectInputStream ois = new ObjectInputStream(is);

					PreferenceManager prefManager =
							PreferenceManager.getInstance();
					SharedPreferences.Editor prefEdit = prefManager.
							getCurrentUserPreferences().edit();
					prefEdit.clear();

					try {
						Map<String, ?> entries = (Map<String, ?>) ois.readObject();

						for (Entry<String, ?> entry : entries.entrySet()) {
							Object v = entry.getValue();
							String key = entry.getKey();

							if (v instanceof Boolean)
								prefEdit.putBoolean(key, (Boolean) v);
							else if (v instanceof Float)
								prefEdit.putFloat(key, (Float) v);
							else if (v instanceof Integer)
								prefEdit.putInt(key, (Integer) v);
							else if (v instanceof Long)
								prefEdit.putLong(key, (Long) v);
							else if (v instanceof String)
								prefEdit.putString(key, ((String) v));
						}
						prefEdit.commit();
					} finally {
						ois.close();
						backupZip.close();
					}
				} catch (Exception e) {
					setThrowable(e);
				} finally {
					if (tmpBackupFile != null) {
						tmpBackupFile.delete();
					}
				}

				return null;
			}
		};
	}

	@Override
	protected void runTask(AsyncTaskWithProgressDialog<String> task) {
		task.execute(locationEditText.getText().toString());
	}

	@Override
	public void onFileChosen(Uri uri) {
		locationEditText.setText(uri.toString());
	}

	@Override
	public void onAsyncTaskResultSuccess(String taskId, Bundle resultData) {
		super.onAsyncTaskResultSuccess(taskId, resultData);
		PreferencesActivity activity = getActivity();
		Toast.makeText(activity,
				getActivity().getResources().getString(R.string.restore_success),
				Toast.LENGTH_LONG).show();
		activity.setLogout(true);
		activity.refreshPreferenceScreen();
	}

	@Override
	public void onAsyncTaskResultFailure(String taskId, Throwable e) {
		super.onAsyncTaskResultFailure(taskId, e);
		String strError = getActivity().getResources().getString(
				R.string.error_restore);
		Toast.makeText(getActivity(),
				String.format(strError, e.getMessage()), Toast.LENGTH_LONG).show();
		Log.e("TMM", e.getMessage(), e);
	}
}
