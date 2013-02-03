/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.alexjf.tmm.R;
import net.alexjf.tmm.adapters.ImmediateTransactionAdapter;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.fragments.DateIntervalBarFragment;
import net.alexjf.tmm.fragments.DateIntervalBarFragment.OnDateIntervalChangedListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MoneyNodeDetailsActivity extends SherlockFragmentActivity 
    implements OnDateIntervalChangedListener {
    private static final int REQCODE_ADD = 0;
    private static final int REQCODE_EDIT = 1;

    private MoneyNode currentMoneyNode;
    private ImmediateTransactionAdapter adapter;
    private Date startDate;
    private Date endDate;

    private TextView balanceTextView;
    private TextView totalTransactionsTextView;
    private ListView transactionsListView;

    public MoneyNodeDetailsActivity() {
        currentMoneyNode = null;
        balanceTextView = null;
        transactionsListView = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moneynode_details);

        Intent intent = getIntent();
        currentMoneyNode = (MoneyNode) intent.getParcelableExtra(
                MoneyNode.KEY_MONEYNODE);

        setTitle(currentMoneyNode.getName());

        balanceTextView = (TextView) findViewById(R.id.balance_value);
        transactionsListView = (ListView) findViewById(R.id.transaction_list);
        totalTransactionsTextView = (TextView) findViewById(R.id.total_transactions_value);
        DateIntervalBarFragment dateBar = (DateIntervalBarFragment) 
            getSupportFragmentManager().findFragmentById(R.id.dateinterval_bar);
        startDate = dateBar.getStartDate();
        endDate = dateBar.getEndDate();

        View emptyView = findViewById(R.id.transaction_list_empty);
        adapter = new ImmediateTransactionAdapter(this);

        transactionsListView.setEmptyView(emptyView);
        transactionsListView.setAdapter(adapter);
        transactionsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, 
                int position, long id) {
                Log.d("TTM", "Item selected at position " + position);
            }
        });

        registerForContextMenu(transactionsListView);
        updateDetailsPanel();
    }

    @Override
    protected void onStop() {
        super.onStop();
        DatabaseHelper.getInstance().close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        android.view.MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_immedtransaction_list, menu);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        ImmediateTransaction transaction = adapter.getItem(info.position);
        switch (item.getItemId()) {
            case R.id.menu_remove:
                try {
                    transaction.getMoneyNode().removeImmediateTransaction(transaction);
                    adapter.remove(transaction);
                } catch (DatabaseException e) {
                    Log.e("TMM", "Unable to remove immediate transaction", e);
                }
                return true;
            case R.id.menu_edit:
                Intent intent = new Intent(this, 
                    ImmedTransactionEditActivity.class);
                intent.putExtra(ImmediateTransaction.KEY_TRANSACTION, transaction);
                startActivityForResult(intent, REQCODE_EDIT);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
            Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQCODE_ADD:
                    updateTransactionList();
                    break;
                case REQCODE_EDIT:
                    updateTransactionList();
                    break;
            }
        }
    }

    public void onDateIntervalChanged(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        if (startDate != null) {
            Log.d("TMM", "Selected new date interval: " + startDate.toString() + "-" + endDate.toString());
        } else {
            Log.d("TMM", "Selected new date interval: all time");
        }
        updateTransactionList();
    }

    private void updateTransactionList() {
        List<ImmediateTransaction> immediateTransactions;

        try {
            immediateTransactions = currentMoneyNode.getImmediateTransactions(
                    startDate, endDate);
        } catch (DatabaseException e) {
            Log.e("TMM", "Unable to get immediate transactions", e);
            immediateTransactions = new LinkedList<ImmediateTransaction>();
        }

        adapter.clear();
        for (ImmediateTransaction transaction : immediateTransactions) {
            adapter.add(transaction);
        }

        updateDetailsPanel();
    }

    private void updateDetailsPanel() {
        balanceTextView.setText(currentMoneyNode.getBalance().toString() + " " +
                currentMoneyNode.getCurrency());
        totalTransactionsTextView.setText(Integer.toString(adapter.getCount()));
    }
}
