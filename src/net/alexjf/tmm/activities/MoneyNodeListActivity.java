package net.alexjf.tmm.activities;

import java.util.LinkedList;
import java.util.List;

import net.alexjf.tmm.R;
import net.alexjf.tmm.adapters.MoneyNodeAdapter;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.domain.User;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MoneyNodeListActivity extends SherlockActivity {
    private DatabaseHelper dbHelper;
    private User currentUser;
    private List<MoneyNode> moneyNodes;
    private MoneyNodeAdapter adapter;

    public MoneyNodeListActivity() {
        dbHelper = null;
        currentUser = null;
        moneyNodes = null;
        adapter = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moneynode_list);

        Intent intent = getIntent();
        currentUser = (User) intent.getParcelableExtra(
                User.EXTRA_CURRENTUSER);
        dbHelper = new DatabaseHelper(getApplicationContext(), 
                currentUser);

        try {
            moneyNodes = dbHelper.getMoneyNodes();
        } catch (Exception e) {
            Log.e("TMM", "Failed to get money nodes: " + e.getMessage() + 
                    "\n" + e.getStackTrace());
            moneyNodes = new LinkedList<MoneyNode>();
        }

        adapter = new MoneyNodeAdapter(this, dbHelper, moneyNodes);

        ListView moneyNodesListView = (ListView) findViewById(
                R.id.moneynode_list);

        View emptyView = findViewById(R.id.moneynode_list_empty);

        moneyNodesListView.setEmptyView(emptyView);
        moneyNodesListView.setAdapter(adapter);
        moneyNodesListView.setOnItemClickListener(new OnItemClickListener() {
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
        inflater.inflate(R.menu.activity_moneynode_list, menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
            Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            MoneyNode node = (MoneyNode) data.getParcelableExtra(
                    MoneyNodeAddActivity.EXTRA_NEWMONEYNODE);

            moneyNodes.add(node);
            adapter.notifyDataSetChanged();
        }
    }
}
