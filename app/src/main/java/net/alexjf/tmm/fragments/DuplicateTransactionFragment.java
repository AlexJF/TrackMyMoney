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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import net.alexjf.tmm.R;
import net.alexjf.tmm.activities.MoneyNodeListActivity;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;
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
		if (savedInstanceState != null) {
			transaction = savedInstanceState.getParcelable(
					ImmediateTransaction.KEY_TRANSACTION);
			target = savedInstanceState.getParcelable(
					MoneyNode.KEY_MONEYNODE);
		}

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

		updateTarget();

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

	@Override
	public void onSaveInstanceState(Bundle outState) {
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
		this.transaction = transaction;
		this.target = null;
		updateTarget();
	}

	/**
	 * @param caller the caller to set
	 */
	public void setListener(DuplicateTransactionDialogListener listener) {
		this.listener = listener;
	}

	private void updateTarget() {
		if (targetMoneyNodeButton == null) {
			return;
		}

		if (target == null) {
			this.target = transaction.getMoneyNode();
		}

		targetMoneyNodeButton.setText(
				target.getName());
		int drawableId = DrawableResolver.getInstance().getDrawableId(
				target.getIcon());
		targetMoneyNodeButton.setDrawableId(drawableId);
		targetMoneyNodeButton.setError(false);
	}

	private void doDuplication(ImmediateTransaction transaction,
			MoneyNode target) {
		ImmediateTransaction duplicate = new ImmediateTransaction(transaction);
		duplicate.setMoneyNode(target);
		listener.onDuplicateTransaction(transaction, duplicate);
	}
}

