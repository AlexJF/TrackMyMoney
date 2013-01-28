/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import java.util.List;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.User;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MoneyNodeDetailsActivity extends SherlockActivity {
    private DatabaseHelper dbHelper;
    private User currentUser;
    private List<ImmediateTransaction> immediateTransactions;
    //private MoneyNodeAdapter adapter;

    public MoneyNodeDetailsActivity() {
        dbHelper = null;
        currentUser = null;
        immediateTransactions = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moneynode_details);

        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra(
                User.EXTRA_CURRENTUSER);
        dbHelper = new DatabaseHelper(getApplicationContext(), 
                currentUser);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                Intent intent = new Intent(this, 
                    MoneyNodeAddActivity.class);
                intent.putExtra(User.EXTRA_CURRENTUSER, currentUser);
                startActivityForResult(intent, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
