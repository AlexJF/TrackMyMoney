/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.preferences;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import net.alexjf.tmm.R;
import net.alexjf.tmm.activities.PreferencesActivity;
import net.alexjf.tmm.database.DatabaseManager;
import net.alexjf.tmm.utils.AsyncTaskWithProgressDialog;
import net.alexjf.tmm.utils.PreferenceManager;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupPreference
		extends DialogWithAsyncTaskProgressPreference<Void> {
	private static final int RES_DIALOGLAYOUT = R.layout.prefdiag_backup;

	private static final String KEY_FILE = "file";
	private static final String KEY_SENDTO = "sendTo";

	private static final String TASK_BACKUP = "backup";

	private CheckBox shareCheckBox;

	public BackupPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

	public BackupPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	protected void initialize(Context context) {
		super.initialize(context);
		setPositiveButtonText(R.string.backup);
	}

	@Override
	protected View onCreateDialogView() {
		LayoutInflater vi = (LayoutInflater) getActivity().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = vi.inflate(RES_DIALOGLAYOUT, null);

		shareCheckBox = (CheckBox) view.findViewById(R.id.share_check);

		return view;
	}

	@Override
	protected AsyncTaskWithProgressDialog<Void> createTask() {
		String strLoading = getActivity().getResources().getString(
				R.string.backingup);
		return new AsyncTaskWithProgressDialog<Void>
				(TASK_BACKUP, strLoading) {
			private boolean sendToOnSuccess = shareCheckBox.isChecked();

			@Override
			protected Bundle doInBackground(Void... args) {
				DateFormat dateFormat = new SimpleDateFormat("'TMM_'yyyy_MM_dd_HH_mm_ss'.bak'");
				File extDir = Environment.getExternalStorageDirectory();
				File tmmDir = new File(extDir, "TMM");
				File exportPath = new File(tmmDir, dateFormat.format(new Date()));

				OutputStream os = null;
				ZipOutputStream zos = null;

				try {
					tmmDir.mkdirs();
					os = new FileOutputStream(exportPath);
					zos = new ZipOutputStream(new BufferedOutputStream(os));

					DatabaseManager dbManager = DatabaseManager.getInstance();
					File dbFile = new File(
							dbManager.getDatabase().getPath());
					byte[] dbData = new byte[(int) dbFile.length()];

					DataInputStream dis = new DataInputStream(
							new FileInputStream(dbFile));
					try {
						dis.readFully(dbData);
					} finally {
						dis.close();
					}

					ZipEntry dbEntry = new ZipEntry("database");
					zos.putNextEntry(dbEntry);
					zos.write(dbData);
					zos.closeEntry();

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(bos);

					try {
						PreferenceManager prefManager =
								PreferenceManager.getInstance();
						oos.writeObject(
								prefManager.getCurrentUserPreferences().getAll());
					} finally {
						oos.close();
					}

					ZipEntry prefsEntry = new ZipEntry("prefs");
					zos.putNextEntry(prefsEntry);
					zos.write(bos.toByteArray());
					zos.closeEntry();

					Bundle bundle = new Bundle();
					bundle.putSerializable(KEY_FILE, exportPath);
					bundle.putBoolean(KEY_SENDTO, sendToOnSuccess);
					return bundle;
				} catch (Exception e) {
					setThrowable(e);
					return null;
				} finally {
					if (zos != null) {
						try {
							zos.close();
						} catch (Exception e) {
							Log.e("TMM", e.getMessage(), e);
						}
					}
				}
			}
		};
	}

	@Override
	protected void runTask(AsyncTaskWithProgressDialog<Void> task) {
		task.execute();
	}

	@Override
	public void onAsyncTaskResultSuccess(String taskId, Bundle resultData) {
		super.onAsyncTaskResultSuccess(taskId, resultData);
		PreferencesActivity activity = getActivity();
		if (resultData.getBoolean(KEY_SENDTO)) {
			File file = (File) resultData.getSerializable(KEY_FILE);
			Intent i = new Intent(Intent.ACTION_SEND);
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
			i.setType("application/zip");
			String title = activity.getResources().getString(
					R.string.export_share_title);
			activity.startActivity(Intent.createChooser(i, title));
		}
		Toast.makeText(activity,
				activity.getResources().getString(R.string.backup_success),
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onAsyncTaskResultFailure(String taskId, Throwable e) {
		super.onAsyncTaskResultFailure(taskId, e);
		String strError = getActivity().getResources().getString(
				R.string.error_backup);
		Toast.makeText(getActivity(),
				String.format(strError, e.getMessage()), Toast.LENGTH_LONG).show();
		Log.e("TMM", e.getMessage(), e);
	}
}

