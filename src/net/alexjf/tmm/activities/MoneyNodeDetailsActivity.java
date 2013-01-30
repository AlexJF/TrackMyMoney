/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import java.util.LinkedList;
import java.util.List;

import net.alexjf.tmm.R;
import net.alexjf.tmm.adapters.ImmediateTransactionAdapter;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.domain.User;
import net.alexjf.tmm.exceptions.DatabaseException;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MoneyNodeDetailsActivity extends SherlockActivity {
    private DatabaseHelper dbHelper;
    private User currentUser;
    private MoneyNode currentMoneyNode;
    private List<ImmediateTransaction> immediateTransactions;
    private ImmediateTransactionAdapter adapter;

    private TextView balanceTextView;
    private TextView totalTransactionsTextView;
    private ListView transactionsListView;

    public MoneyNodeDetailsActivity() {
        dbHelper = null;
        currentUser = null;
        currentMoneyNode = null;
        immediateTransactions = null;

        balanceTextView = null;
        transactionsListView = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moneynode_details);

        Intent intent = getIntent();
        currentUser = (User) intent.getParcelableExtra(
                User.EXTRA_CURRENTUSER);
        dbHelper = new DatabaseHelper(getApplicationContext(), 
                currentUser);
        currentMoneyNode = (MoneyNode) intent.getParcelableExtra(
                MoneyNode.EXTRA_CURRENTMONEYNODE);

        setTitle(currentMoneyNode.getName());

        balanceTextView = (TextView) findViewById(R.id.balance_value);
        balanceTextView.setText(currentMoneyNode.getBalance().toString() + " " +
                currentMoneyNode.getCurrency());

        transactionsListView = (ListView) findViewById(R.id.transaction_list);

        try {
            immediateTransactions = currentMoneyNode.getImmediateTransactions();
        } catch (DatabaseException e) {
            immediateTransactions = new LinkedList<ImmediateTransaction>();
        }

        totalTransactionsTextView = (TextView) findViewById(R.id.total_transactions_value);
        totalTransactionsTextView.setText(Integer.toString(immediateTransactions.size()));

        View emptyView = findViewById(R.id.transaction_list_empty);
        adapter = new ImmediateTransactionAdapter(this, dbHelper, 
                immediateTransactions);

        transactionsListView.setEmptyView(emptyView);
        transactionsListView.setAdapter(adapter);
        transactionsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, 
                int position, long id) {
                Log.d("TTM", "Item selected at position " + position);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.close();
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
                /*Intent intent = new Intent(this, 
                    MoneyNodeAddActivity.class);
                intent.putExtra(User.EXTRA_CURRENTUSER, currentUser);
                startActivityForResult(intent, 0);*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
