/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.exceptions.ExitingException;
import net.alexjf.tmm.fragments.ImmedTransactionDetailsFragment;
import net.alexjf.tmm.fragments.ImmedTransactionEditorFragment.ImmedTransactionEditOldInfo;

public class ImmedTransactionDetailsActivity extends BaseActionBarActivity {
	private static final int REQCODE_EDIT = 0;

	private static final String KEY_OLDTRANSACTIONINFO = "oldTransInfo";

	private ImmedTransactionDetailsFragment detailsFragment;

	private ImmediateTransaction currentImmedTransaction;
	private ImmedTransactionEditOldInfo oldTransactionInfo;

	@Override
	protected void onCreateInternal(Bundle savedInstanceState) throws ExitingException {
		super.onCreateInternal(savedInstanceState);
		setContentView(R.layout.activity_immedtransaction_details);
		setTitle(R.string.title_activity_immedtrans_details);
	}

	@Override
	protected void onDatabaseReady(Bundle savedInstanceState) {
		super.onDatabaseReady(savedInstanceState);

		Intent intent = getIntent();
		currentImmedTransaction = (ImmediateTransaction)
				intent.getParcelableExtra(ImmediateTransaction.KEY_TRANSACTION);
		try {
			currentImmedTransaction.load();
		} catch (DatabaseException e) {
			Log.e("TMM", e.getMessage(), e);
		}

		detailsFragment = (ImmedTransactionDetailsFragment)
				getSupportFragmentManager().findFragmentById(
						R.id.immedtransaction_details);
		detailsFragment.setTransaction(currentImmedTransaction);

		if (savedInstanceState != null) {
			oldTransactionInfo = (ImmedTransactionEditOldInfo)
					savedInstanceState.getParcelable(KEY_OLDTRANSACTIONINFO);
		}
	}

	@Override
	public void finish() {
		Intent result = new Intent();
		result.putExtra(ImmediateTransaction.KEY_TRANSACTION,
				currentImmedTransaction);
		result.putExtra(ImmedTransactionEditOldInfo.KEY_OLDINFO,
				oldTransactionInfo);
		setResult(RESULT_OK, result);
		super.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_immedtrans_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_editTransaction:
				Intent intent = new Intent(this,
						ImmedTransactionEditActivity.class);
				intent.putExtra(ImmediateTransaction.KEY_TRANSACTION,
						currentImmedTransaction);
				startActivityForResult(intent, REQCODE_EDIT);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case REQCODE_EDIT:
					oldTransactionInfo = (ImmedTransactionEditOldInfo)
							data.getParcelableExtra(
									ImmedTransactionEditOldInfo.KEY_OLDINFO);
					detailsFragment.updateDetails();
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (oldTransactionInfo != null) {
			outState.putParcelable(KEY_OLDTRANSACTIONINFO,
					oldTransactionInfo);
		}
		super.onSaveInstanceState(outState);
	}
}
