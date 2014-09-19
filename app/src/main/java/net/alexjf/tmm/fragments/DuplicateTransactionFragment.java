/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import net.alexjf.tmm.R;
import net.alexjf.tmm.activities.MoneyNodeListActivity;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.utils.DrawableResolver;
import net.alexjf.tmm.views.SelectorButton;

public class DuplicateTransactionFragment extends DialogFragment {
	private static String KEY_TRANSACTION = "transaction";
	private static String KEY_MONEYNODE = "moneyNode";

	private static final int REQCODE_MONEYNODECHOOSE = 1;

	private ImmediateTransaction transaction;
	private MoneyNode target;
	private DuplicateTransactionDialogListener listener;

	private SelectorButton targetMoneyNodeButton;

	private Bundle savedInstanceState;
	private boolean databaseReady;

	public interface DuplicateTransactionDialogListener {
		void onDuplicateTransaction(ImmediateTransaction srcTransaction,
				ImmediateTransaction dstTransaction);
	}

	public DuplicateTransactionFragment() {
		this.transaction = null;
		this.target = null;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.d("TMM", "DuplicateTransactionFragment - " + this + " - onCreateDialog - " + databaseReady);

		LayoutInflater vi = (LayoutInflater) getActivity().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = vi.inflate(R.layout.fragment_duplicate_transaction, null);

		targetMoneyNodeButton = (SelectorButton) view.findViewById(
				R.id.duplicate_trans_moneynode_button);

		targetMoneyNodeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(view.getContext(),
						MoneyNodeListActivity.class);
				intent.putExtra(MoneyNodeListActivity.KEY_INTENTION,
						MoneyNodeListActivity.INTENTION_SELECT);

				startActivityForResult(intent, REQCODE_MONEYNODECHOOSE);
			}
		});

		if (databaseReady) {
			readDataFromState(savedInstanceState);
		} else {
			this.savedInstanceState = savedInstanceState;
		}

		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.title_fragment_duplicate_trans)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								doDuplication(transaction, target);
							}
						}
				)
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dismiss();
							}
						}
				)
				.setView(view)
				.create();
	}

	public void onDatabaseReady() {
		databaseReady = true;
		Log.d("TMM", "DuplicateTransactionFragment - " + this + " - onDatabaseReady - " + savedInstanceState);

		readDataFromState(savedInstanceState);
	}

	private void readDataFromState(Bundle savedInstanceState) {
		Log.d("TMM", "DuplicateTransactionFragment - " + this + " - readDataFromState - " + savedInstanceState);

		if (savedInstanceState != null) {
			transaction = savedInstanceState.getParcelable(
					KEY_TRANSACTION);
			Log.d("TMM", "Set transaction to " + transaction);
			target = savedInstanceState.getParcelable(
					KEY_MONEYNODE);
			Log.d("TMM", "Set target to " + target);

		}

		updateTarget();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d("TMM", "DuplicateTransactionFragment - " + this + " - onSaveInstanceState - " + transaction + " - " + target);
		outState.putParcelable(KEY_TRANSACTION, transaction);
		outState.putParcelable(KEY_MONEYNODE, target);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQCODE_MONEYNODECHOOSE) {
				target = (MoneyNode)
						data.getParcelableExtra(MoneyNode.KEY_MONEYNODE);
				updateTarget();
			}
		}
	}

	/**
	 * @param transaction the transaction to set
	 */
	public void setTransaction(ImmediateTransaction transaction) {
		Log.d("TMM", "DuplicateTransactionFragment - " + this + " - setTransaction - " + transaction);
		this.transaction = transaction;
		this.target = null;
		updateTarget();
	}

	/**
	 * @param listener the caller to set
	 */
	public void setListener(DuplicateTransactionDialogListener listener) {
		this.listener = listener;
	}

	private void updateTarget() {
		if (transaction == null) {
			Log.d("TMM", "updateTarget - Leaving cause transaction is null");
			return;
		}

		if (targetMoneyNodeButton == null) {
			Log.d("TMM", "updateTarget - Leaving cause targetMoneyNodeButton is null");
			return;
		}

		try {
			Log.d("TMM", "updateTarget - target=" + target);
			if (target == null) {
				transaction.load();
				target = transaction.getMoneyNode();
				Log.d("TMM", "updateTarget - default target=" + target);
			}

			target.load();

			targetMoneyNodeButton.setText(
					target.getName());
			int drawableId = DrawableResolver.getInstance().getDrawableId(
					target.getIcon());
			targetMoneyNodeButton.setDrawableId(drawableId);
			targetMoneyNodeButton.setError(false);
		} catch (DatabaseException e) {
			Log.e("TMM", "Error updating target", e);
		}
	}

	private void doDuplication(ImmediateTransaction transaction,
			MoneyNode target) {
		try {
			ImmediateTransaction duplicate = ImmediateTransaction.copy(transaction);
			duplicate.setMoneyNode(target);
			listener.onDuplicateTransaction(transaction, duplicate);
		} catch (DatabaseException e) {
			Toast.makeText(this.getActivity(), R.string.error_trans_duplicate, Toast.LENGTH_LONG);
			Log.e("TMM", "Error doing duplication", e);
		}
	}
}

