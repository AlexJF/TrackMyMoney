/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import net.alexjf.tmm.R;
import net.alexjf.tmm.adapters.ImmediateTransactionAdapter;
import net.alexjf.tmm.adapters.TabAdapter;
import net.alexjf.tmm.adapters.TabAdapter.OnTabFragmentCreateListener;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.exceptions.ExitingException;
import net.alexjf.tmm.fragments.DateIntervalBarFragment.OnDateIntervalChangedListener;
import net.alexjf.tmm.fragments.ImmedTransactionEditorFragment.ImmedTransactionEditOldInfo;
import net.alexjf.tmm.fragments.ImmedTransactionListFragment;
import net.alexjf.tmm.fragments.ImmedTransactionListFragment.OnImmedTransactionActionListener;
import net.alexjf.tmm.fragments.ImmedTransactionStatsFragment;
import net.alexjf.tmm.interfaces.IWithAdapter;
import net.alexjf.tmm.utils.AsyncTaskWithProgressDialog;
import net.alexjf.tmm.utils.AsyncTaskWithProgressDialog.AsyncTaskResultListener;
import net.alexjf.tmm.utils.DrawableResolver;
import net.alexjf.tmm.utils.Utils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class MoneyNodeDetailsActivity extends BaseActionBarActivity
		implements OnDateIntervalChangedListener, OnImmedTransactionActionListener,
		AsyncTaskResultListener {
	private static String KEY_IMMEDIATETRANSACTIONS = "immediateTransactions";
	private static String KEY_CURRENTSTARTDATE = "startDate";
	private static String KEY_CURRENTENDDATE = "endDate";

	private static String TASK_TRANSACTIONS = "transactions";

	private static final int REQCODE_ADD = 0;
	private static final int REQCODE_PREFS = 1;
	private static final int REQCODE_DETAILS = 2;

	private MoneyNode currentMoneyNode;
	private ImmediateTransactionAdapter immedTransAdapter;
	private boolean dateIntervalSet;
	private Date startDate;
	private Date endDate;
	private Money income;
	private Money expense;
	private CurrencyUnit currency;

	private static DateFormat dateTimeFormat = DateFormat.getDateTimeInstance();
	private static AsyncTaskWithProgressDialog<Date> transactionTask;

	private ViewGroup balancePanel;
	private TextView balanceTextView;
	private TextView totalTransactionsTextView;
	private ImageView moreBalanceButton;
	private ViewGroup balanceDetailsPanel;
	private ViewGroup initialBalanceRow;
	private TextView initialBalanceTextView;
	private TextView incomeTextView;
	private TextView expenseTextView;
	private ViewPager viewPager;
	private TabAdapter tabAdapter;

	public MoneyNodeDetailsActivity() {
		dateIntervalSet = false;
	}

	@Override
	protected void onCreateInternal(Bundle savedInstanceState) throws ExitingException {
		super.onCreateInternal(savedInstanceState);
		setContentView(R.layout.activity_moneynode_details);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayUseLogoEnabled(true);

		balancePanel = (ViewGroup) findViewById(R.id.balance_panel);
		balanceTextView = (TextView) findViewById(R.id.balance_value);
		balanceDetailsPanel = (ViewGroup) findViewById(R.id.balance_details_panel);
		initialBalanceRow = (ViewGroup) findViewById(R.id.initial_balance_line);
		initialBalanceTextView = (TextView) findViewById(R.id.initial_balance_value);
		moreBalanceButton = (ImageView) findViewById(R.id.balance_more_button);
		incomeTextView = (TextView) findViewById(R.id.income_value);
		expenseTextView = (TextView) findViewById(R.id.expense_value);
		totalTransactionsTextView = (TextView)
				findViewById(R.id.total_transactions_value);
		viewPager = (ViewPager) findViewById(R.id.view_pager);

		immedTransAdapter = new ImmediateTransactionAdapter(this);

		tabAdapter = new TabAdapter(this, viewPager);
		tabAdapter.addTab(actionBar.newTab().setText(R.string.list),
				ImmedTransactionListFragment.class);
		tabAdapter.addTab(actionBar.newTab().setText(R.string.stats),
				ImmedTransactionStatsFragment.class);

		tabAdapter.setOnTabFragmentCreateListener(new OnTabFragmentCreateListener() {
			public void onTabFragmentCreated(Fragment fragment, int position) {
				Log.d("TMM", "Tab fragment created");
				IWithAdapter fragmentWithAdapter =
						(IWithAdapter) fragment;
				fragmentWithAdapter.setAdapter(immedTransAdapter);

				if (fragment instanceof ImmedTransactionListFragment) {
					if (isDatabaseReady()) {
						((ImmedTransactionListFragment) fragment).onDatabaseReady();
					}
				}
			}
		});

		balancePanel.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				LayoutParams detailsLayoutParams =
						balanceDetailsPanel.getLayoutParams();
				if (detailsLayoutParams.height == 0) {
					detailsLayoutParams.height = LayoutParams.WRAP_CONTENT;
					balanceDetailsPanel.setLayoutParams(detailsLayoutParams);
					moreBalanceButton.setImageResource(
							DrawableResolver.getInstance().getDrawableId("toggle_up"));
				} else {
					detailsLayoutParams.height = 0;
					balanceDetailsPanel.setLayoutParams(detailsLayoutParams);
					moreBalanceButton.setImageResource(
							DrawableResolver.getInstance().getDrawableId("toggle_down"));
				}
			}

			;
		});

		if (transactionTask != null) {
			transactionTask.setContext(this);
			transactionTask.setResultListener(this);
		}
	}

	@Override
	protected void onDatabaseReady(Bundle savedInstanceState) {
		super.onDatabaseReady(savedInstanceState);

		Intent intent = getIntent();
		currentMoneyNode = (MoneyNode) intent.getParcelableExtra(
				MoneyNode.KEY_MONEYNODE);
		try {
			currentMoneyNode.load();
		} catch (DatabaseException e) {
			Log.e("TMM", e.getMessage(), e);
		}
		currency = currentMoneyNode.getCurrency();

		income = Money.zero(currency);
		expense = Money.zero(currency);

		setTitle(currentMoneyNode.getName());

		ImmedTransactionListFragment transactionListFragment =
				(ImmedTransactionListFragment) tabAdapter.getFragment(0);

		if (transactionListFragment != null) {
			transactionListFragment.onDatabaseReady();
		}

		updateData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_moneynode_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_addTransaction:
				Intent intent = new Intent(this,
						ImmedTransactionEditActivity.class);
				intent.putExtra(MoneyNode.KEY_MONEYNODE, currentMoneyNode);
				startActivityForResult(intent, REQCODE_ADD);
				return true;
			case R.id.menu_preferences:
				intent = new Intent(this,
						PreferencesActivity.class);
				startActivityForResult(intent, REQCODE_PREFS);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			ImmediateTransaction transaction;
			switch (requestCode) {
				case REQCODE_ADD:
					transaction = (ImmediateTransaction)
							data.getParcelableExtra(
									ImmediateTransaction.KEY_TRANSACTION);

					onImmedTransactionAdded(transaction);
					break;
				case REQCODE_PREFS:
					if (data.getBooleanExtra(
							PreferencesActivity.KEY_FORCEDATAREFRESH,
							false)) {
						updateData();
					}
					break;
				case REQCODE_DETAILS:
					transaction = (ImmediateTransaction)
							data.getParcelableExtra(
									ImmediateTransaction.KEY_TRANSACTION);
					ImmedTransactionEditOldInfo oldInfo =
							(ImmedTransactionEditOldInfo) data.getParcelableExtra(
									ImmedTransactionEditOldInfo.KEY_OLDINFO);

					if (oldInfo != null) {
						onImmedTransactionEdited(transaction, oldInfo);
					}
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void onDateIntervalChanged(Date startDate, Date endDate) {
		dateIntervalSet = true;
		this.startDate = startDate;
		this.endDate = endDate;

		if (startDate != null) {
			Log.d("TMM", "Selected new date interval: " + startDate.toString() + "-" + endDate.toString());
		} else {
			Log.d("TMM", "Selected new date interval: all time");
		}

		updateData();
	}

	// TODO: Use lazy loading instead of getting all transactions in the period
	private void updateData() {
		// If database not ready, do nothing
		if (!isDatabaseReady()) {
			return;
		}

		// If no date interval set yet, skip. Initially, because of
		// onDatabaseReady, updateData might get called before the DateIntervalBarFragment
		// has had time to set the correct interval. No point getting data at this
		// point because we'll get it again once the interval is set correctly.
		if (!dateIntervalSet) {
			return;
		}

		// If task is already running, do nothing
		if (transactionTask != null) {
			return;
		}

		Utils.preventOrientationChanges(this);

		String strLoading = getResources().getString(
				R.string.loading);

		transactionTask =
				new AsyncTaskWithProgressDialog<Date>
						(this, TASK_TRANSACTIONS, strLoading) {
					@Override
					protected Bundle doInBackground(Date... args) {
						try {
							List<ImmediateTransaction> immediateTransactions =
									currentMoneyNode.getImmediateTransactions(
											args[0], args[1]);
							Bundle bundle = new Bundle();
							bundle.putParcelableArray(
									KEY_IMMEDIATETRANSACTIONS,
									immediateTransactions.toArray(
											new ImmediateTransaction[immediateTransactions.size()]));
							return bundle;
						} catch (DatabaseException e) {
							setThrowable(e);
							return null;
						}
					}

					;
				};

		transactionTask.setResultListener(this);
		transactionTask.execute(startDate, endDate);
	}

	private void updateGui() {
		if (!isDatabaseReady()) {
			return;
		}

		immedTransAdapter.sort(new ImmediateTransaction.DateComparator(true));
		immedTransAdapter.notifyDataSetChanged();
		updateDetailsPanel();
	}

	private void updateDetailsPanel() {
		Money balance = income.plus(expense);

		// If we are not limiting the start date or it is less than or equal to
		// money node creationDate, add initialBalance
		if (startDate == null ||
				startDate.compareTo(currentMoneyNode.getCreationDate()) <= 0) {
			Money initialBalance = currentMoneyNode.getInitialBalance();
			balance = balance.plus(initialBalance);
			balanceTextView.setText(balance.toString());
			initialBalanceTextView.setText(initialBalance.toString());
			initialBalanceRow.setVisibility(View.VISIBLE);
		} else {
			balanceTextView.setText(balance.toString());
			initialBalanceRow.setVisibility(View.GONE);
		}

		totalTransactionsTextView.setText(Integer.toString(immedTransAdapter.getCount()));
		incomeTextView.setText(income.toString());
		expenseTextView.setText(expense.toString());
	}

	@Override
	public void onImmedTransactionAdded(ImmediateTransaction transaction) {
		try {
			transaction.load();
			if (transaction.getMoneyNode() == currentMoneyNode) {
				if (Utils.dateBetween(transaction.getExecutionDate(),
						startDate, endDate)) {
					immedTransAdapter.add(transaction);
					Money value = transaction.getValue();

					if (value.isPositive()) {
						income = income.plus(value);
					} else if (value.isNegative()) {
						expense = expense.plus(value);
					}
				}
			}
			updateGui();
		} catch (DatabaseException e) {
			Log.e("TMM", "Error loading transaction after adding.", e);
		}
	}

	@Override
	public void onImmedTransactionRemoved(ImmediateTransaction transaction) {
		Money value = transaction.getValue();

		if (value.isPositive()) {
			income = income.minus(value);
		} else if (value.isNegative()) {
			expense = expense.minus(value);
		}
		updateGui();
	}

	@Override
	public void onImmedTransactionEdited(ImmediateTransaction transaction,
			ImmedTransactionEditOldInfo oldInfo) {
		try {
			transaction.load();
		} catch (DatabaseException e) {
			Log.e("TMM", "Error loading transaction after editing.", e);
			return;
		}

		if (transaction.getMoneyNode() != currentMoneyNode) {
			return;
		}

		Money originalValue = oldInfo.getValue();
		Money newValue = transaction.getValue();
		Money delta = newValue.minus(originalValue);
		Money incomeDelta = Money.zero(currency);
		Money expenseDelta = Money.zero(currency);

		// If immediate transaction is no longer on the period being considered,
		// remove it
		if (!Utils.dateBetween(transaction.getExecutionDate(),
				startDate, endDate)) {
			immedTransAdapter.remove(transaction);
			onImmedTransactionRemoved(transaction);
			return;
		}

		// If values didn't change, quit
		if (delta.isZero()) {
			return;
		}

		// If the 2 values share the same signal, we only need to modify either
		// the income or expense
		if (originalValue.isPositive() && newValue.isPositive()) {
			incomeDelta = incomeDelta.plus(delta);
		} else if (originalValue.isNegative() && newValue.isNegative()) {
			expenseDelta = expenseDelta.plus(delta);
		}
		// Else we need to modify both income and expense
		else {
			if (originalValue.isPositive()) {
				incomeDelta = incomeDelta.minus(originalValue);
				expenseDelta = expenseDelta.plus(newValue);
			} else if (originalValue.isNegative()) {
				expenseDelta = expenseDelta.minus(originalValue);
				incomeDelta = incomeDelta.plus(newValue);
			}
		}

		income = income.plus(incomeDelta);
		expense = expense.plus(expenseDelta);
		updateGui();
	}

	@Override
	public void onImmedTransactionSelected(ImmediateTransaction transaction) {
		Intent intent = new Intent(this,
				ImmedTransactionDetailsActivity.class);
		intent.putExtra(ImmediateTransaction.KEY_TRANSACTION, transaction);
		startActivityForResult(intent, REQCODE_DETAILS);
	}

	@Override
	protected void onResumeInternal() throws ExitingException {
		super.onResumeInternal();
		// If there's not a database refresh ongoing, refresh gui
		if (transactionTask == null) {
			updateGui();
		}
		tabAdapter.refreshPreferences();
	}

	@Override
	public void onAsyncTaskResultSuccess(String taskId, Bundle result) {
		immedTransAdapter.setNotifyOnChange(false);
		immedTransAdapter.clear();

		income = Money.zero(currency);
		expense = Money.zero(currency);
		ImmediateTransaction[] immediateTransactions =
				(ImmediateTransaction[])
						result.getParcelableArray(KEY_IMMEDIATETRANSACTIONS);
		for (ImmediateTransaction transaction : immediateTransactions) {
			try {
				transaction.load();
			} catch (DatabaseException e) {
				Log.e("TMM", "Error loading transaction " + transaction.getId(), e);
				continue;
			}

			Money value = transaction.getValue();

			if (value.isPositive()) {
				income = income.plus(value);
			} else if (value.isNegative()) {
				expense = expense.plus(value);
			}

			immedTransAdapter.add(transaction);
		}

		updateGui();
		transactionTask = null;
		Utils.allowOrientationChanges(this);
	}

	@Override
	public void onAsyncTaskResultFailure(String taskId, Throwable e) {
		String strError = getResources().getString(
				R.string.error_moneynode_load_transactions);
		Toast.makeText(this,
				String.format(strError, e.getMessage()), Toast.LENGTH_LONG).show();
		Log.e("TMM", e.getMessage(), e);
		transactionTask = null;
		Utils.allowOrientationChanges(this);
	}

	@Override
	public void onAsyncTaskResultCanceled(String taskId) {
		transactionTask = null;
		Utils.allowOrientationChanges(this);
	}

	@Override
	protected void onDestroy() {
		if (transactionTask != null) {
			transactionTask.setContext(null);
		}
		super.onDestroy();
	}
}
