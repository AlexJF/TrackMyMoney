/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;
import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.exceptions.ExitingException;
import net.alexjf.tmm.fragments.ImmedTransactionEditorFragment;
import net.alexjf.tmm.fragments.ImmedTransactionEditorFragment.ImmedTransactionEditOldInfo;
import net.alexjf.tmm.fragments.ImmedTransactionEditorFragment.OnImmediateTransactionEditListener;

public class ImmedTransactionEditActivity extends BaseActionBarActivity
		implements OnImmediateTransactionEditListener {

	public static final String KEY_FORCE_ADD = "forceAdd";

	@Override
	protected void onCreateInternal(Bundle savedInstanceState) throws ExitingException {
		super.onCreateInternal(savedInstanceState);
		setContentView(R.layout.activity_immedtransaction_edit);
	}

	@Override
	protected void onDatabaseReady(Bundle savedInstanceState) {
		super.onDatabaseReady(savedInstanceState);

		Intent intent = getIntent();
		MoneyNode moneyNode = (MoneyNode)
				intent.getParcelableExtra(MoneyNode.KEY_MONEYNODE);
		ImmediateTransaction transaction = (ImmediateTransaction)
				intent.getParcelableExtra(ImmediateTransaction.KEY_TRANSACTION);

		ImmedTransactionEditorFragment editor = (ImmedTransactionEditorFragment)
				getSupportFragmentManager().findFragmentById(R.id.immedtransaction_editor);
		editor.onDatabaseReady();
		editor.setCurrentMoneyNode(moneyNode);
		editor.setTransaction(transaction);

		if (transaction == null) {
			setTitle(R.string.title_activity_immedtrans_add);
		} else {
			setTitle(R.string.title_activity_immedtrans_edit);
		}
	}

	public void onImmediateTransactionEdited(ImmediateTransaction trans,
			ImmedTransactionEditOldInfo oldInfo) {
		try {
			Intent data = new Intent();
			Log.d("TMM", "Editing immediate transaction " + trans.getId());
			trans.save();
			// If this is a transfer transaction, save other one
			if (trans.getTransferTransaction() != null) {
				trans.getTransferTransaction().save();
				// Resave the original transaction to get the updated id
				// of the other one
				trans.save();
			}
			// If this is not a transfer transaction but was one before,
			// remove the other one
			else if (oldInfo.getTransferTransaction() != null) {
				ImmediateTransaction otherTransaction =
						oldInfo.getTransferTransaction();
				MoneyNode otherNode = otherTransaction.getMoneyNode();
				otherNode.removeImmediateTransaction(otherTransaction);
			}
			data.putExtra(ImmediateTransaction.KEY_TRANSACTION, trans);
			data.putExtra(ImmedTransactionEditOldInfo.KEY_OLDINFO, oldInfo);
			setResult(ActionBarActivity.RESULT_OK, data);
			finish();
		} catch (DatabaseException e) {
			Log.e("TMM", "Failure editing immediate transaction", e);
			String strError =
					getResources().getString(R.string.error_trans_edit);
			Toast.makeText(ImmedTransactionEditActivity.this,
					String.format(strError, e.getMessage()), Toast.LENGTH_LONG).show();
		}
	}

	public void onImmediateTransactionCreated(ImmediateTransaction trans) {
		try {
			Intent data = new Intent();
			Log.d("TMM", "Adding immediate transaction " + trans.getId());
			trans.save();
			// If this is a transfer transaction, save other one
			if (trans.getTransferTransaction() != null) {
				trans.getTransferTransaction().save();
				// Resave the original transaction to get the updated id
				// of the other one.
				trans.save();
			}
			data.putExtra(ImmediateTransaction.KEY_TRANSACTION, trans);
			setResult(ActionBarActivity.RESULT_OK, data);
			finish();
		} catch (DatabaseException e) {
			Log.e("TMM", "Failure adding immediate transaction", e);
			String strError =
					getResources().getString(R.string.error_trans_add);
			Toast.makeText(ImmedTransactionEditActivity.this,
					String.format(strError, e.getMessage()), Toast.LENGTH_LONG).show();
		}
	}
}
