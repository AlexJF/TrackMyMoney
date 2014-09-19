package net.alexjf.tmm.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import net.alexjf.tmm.database.DatabaseManager;
import net.alexjf.tmm.exceptions.ExitingException;
import net.alexjf.tmm.utils.Utils;
import net.sqlcipher.database.SQLiteDatabase;

/**
 * Created by alex on 18/09/14.
 */
public class BaseActionBarActivity extends ActionBarActivity {
	protected final static int REQCODE_RELOGIN = 99;

	private boolean requiresDatabase;
	private boolean onDatabaseReadyCalled;
	private boolean onCreateFinished;
	private boolean waitingForRelogin;
	private Bundle savedInstanceStateTemp;

	public BaseActionBarActivity() {
		requiresDatabase = true;
		onDatabaseReadyCalled = false;
		onCreateFinished = false;
		savedInstanceStateTemp = null;
		waitingForRelogin = false;
	}

	@Override
	final public void onCreate(Bundle savedInstanceState) {
		onCreateFinished = false;
		waitingForRelogin = false;
		onDatabaseReadyCalled = false;
		savedInstanceStateTemp = savedInstanceState;

		try {
			onCreateInternal(savedInstanceState);
			ensureDatabaseReady();
		} catch (ExitingException e) {
			finish();
		}

		onCreateFinished = true;
	}

	protected void ensureDatabaseReady() {
		if (onDatabaseReadyCalled || waitingForRelogin) {
			return;
		}

		if (requiresDatabase()) {
			SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();

			// If db doesn't exist or is not open, ask for relogin
			if (db == null || !db.isOpen()) {
				// If we got this far, database does not exist or is not open.
				// We need to recreate it and open it. We do so in the UserListActivity
				Intent intent = new Intent(this, UserListActivity.class);
				intent.putExtra(UserListActivity.KEY_INTENTION, UserListActivity.INTENTION_RELOGIN);

				waitingForRelogin = true;
				this.startActivityForResult(intent, REQCODE_RELOGIN);

				return;
			}
		}

		onDatabaseReady(savedInstanceStateTemp);
	}

	protected void onCreateInternal(Bundle savedInstanceState) throws ExitingException {
		super.onCreate(savedInstanceState);

		// Finish and continue exiting if we have a whole app exit event and activity was not in memory.
		Utils.checkIfExiting();
	}

	protected void onDatabaseReady(Bundle savedInstanceState) {
		// Prevent multiple calls
		if (onDatabaseReadyCalled) {
			return;
		}

		onDatabaseReadyCalled = true;
	}

	@Override
	public void finish() {
		if (isTaskRoot()) {
			Utils.cancelExiting();
		}
		super.finish();
	}

	@Override
	final protected void onResume() {
		try {
			onResumeInternal();
		} catch (ExitingException e) {
			finish();
		}
	}

	protected void onResumeInternal() throws ExitingException {
		super.onResume();

		// Finish and continue exiting if we have a whole app exit event and activity was still in memory.
		Utils.checkIfExiting();
	}

	public boolean requiresDatabase() {
		return requiresDatabase;
	}

	public void setRequiresDatabase(boolean requiresDatabase) {
		this.requiresDatabase = requiresDatabase;

		// If we updated this value to true but have already left onCreate and
		// not waiting for a relogin, try to prepare the database
		if (requiresDatabase && onCreateFinished && !waitingForRelogin) {
			ensureDatabaseReady();
		}
	}

	public boolean isDatabaseReady() {
		return onDatabaseReadyCalled;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("TMM", "BaseActionBarActivity - onActivityResult - " + requestCode + " - " + resultCode + " - " + data);
		Log.d("TMM", "BaseActionBarActivity - waitingForRelogin=" + waitingForRelogin);
		if (resultCode == Activity.RESULT_OK &&
				requestCode == REQCODE_RELOGIN &&
				waitingForRelogin) {
			waitingForRelogin = false;
			onDatabaseReady(savedInstanceStateTemp);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
