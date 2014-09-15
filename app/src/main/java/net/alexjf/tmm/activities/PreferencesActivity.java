/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.ipaulpro.afilechooser.utils.FileUtils;
import net.alexjf.tmm.R;
import net.alexjf.tmm.utils.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class PreferencesActivity extends PreferenceActivity {
	public static final String KEY_FORCEDATAREFRESH = "forceDataRefresh";
	public static final String KEY_LOGOUT = "logout";

	private static final int REQCODE_FILECHOOSE = 0;

	private Set<OnStopListener> stopListeners =
			new HashSet<OnStopListener>();
	private Set<OnDestroyListener> destroyListeners =
			new HashSet<OnDestroyListener>();
	private Set<OnSaveInstanceListener> saveInstanceListeners =
			new HashSet<OnSaveInstanceListener>();
	private Set<OnRestoreInstanceListener> restoreInstanceListeners =
			new HashSet<OnRestoreInstanceListener>();

	private OnFileChosenListener currentFileChoiceListener;
	private boolean forceDataRefresh;
	private boolean logout;

	@Override
	public void finish() {
		if (logout) {

			Intent intent = new Intent(this, UserListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
					.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

			startActivity(intent);
		} else {
			Intent result = new Intent();
			result.putExtra(KEY_FORCEDATAREFRESH, forceDataRefresh);
			setResult(RESULT_OK, result);
			super.finish();
		}
	}

	public interface OnStopListener {
		public void onStop();
	}

	public interface OnDestroyListener {
		public void onDestroy();
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

	public void registerOnDestroyListener(OnDestroyListener listener) {
		destroyListeners.add(listener);
	}

	public void unregisterOnDestroyListener(OnDestroyListener listener) {
		destroyListeners.remove(listener);
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
		PreferenceManager prefManager = PreferenceManager.getInstance();
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
	protected void onDestroy() {
		for (OnDestroyListener listener : destroyListeners) {
			listener.onDestroy();
		}
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		for (OnSaveInstanceListener listener : saveInstanceListeners) {
			listener.onSaveInstance(outState);
		}
		outState.putBoolean(KEY_FORCEDATAREFRESH, forceDataRefresh);
		outState.putBoolean(KEY_LOGOUT, logout);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);

		if (state != null) {
			forceDataRefresh = state.getBoolean(KEY_FORCEDATAREFRESH);
			logout = state.getBoolean(KEY_LOGOUT);
		}

		for (OnRestoreInstanceListener listener : restoreInstanceListeners) {
			listener.onRestoreInstance(state);
		}
	}

	public void requestFileChooser(OnFileChosenListener requester) {
		currentFileChoiceListener = requester;
		Intent target = FileUtils.createGetContentIntent();
		Intent intent = Intent.createChooser(target,
				getResources().getString(R.string.file_select));

		try {
			startActivityForResult(intent, REQCODE_FILECHOOSE);
		} catch (ActivityNotFoundException e) {
			// The reason for the existence of aFileChooser
		}
	}

	public void setForceDataRefresh(boolean force) {
		forceDataRefresh = force;
	}

	public void setLogout(boolean logout) {
		this.logout = logout;
	}

	public void refreshPreferenceScreen() {
		setPreferenceScreen(null);
		addPreferencesFromResource(R.xml.preferences);
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
