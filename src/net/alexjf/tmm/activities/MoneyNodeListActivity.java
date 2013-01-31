/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import java.util.ArrayList;
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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MoneyNodeListActivity extends SherlockActivity {
    private static final int REQCODE_ADD = 0;
    private static final int REQCODE_EDIT = 1;
    private static final String KEY_MONEYNODES = "moneyNodes";

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
                User.KEY_USER);
        dbHelper = new DatabaseHelper(getApplicationContext(), 
                currentUser);

        if (savedInstanceState == null) {
            try {
                moneyNodes = dbHelper.getMoneyNodes();
            } catch (Exception e) {
                Log.e("TMM", "Failed to get money nodes: " + e.getMessage() + 
                        "\n" + e.getStackTrace());
                moneyNodes = new LinkedList<MoneyNode>();
            }
        } else {
            moneyNodes = savedInstanceState.getParcelableArrayList(KEY_MONEYNODES);
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
                MoneyNode selectedNode = adapter.getItem(position);
                Intent intent = new Intent(MoneyNodeListActivity.this, 
                    MoneyNodeDetailsActivity.class);
                intent.putExtra(User.KEY_USER, currentUser);
                intent.putExtra(MoneyNode.KEY_MONEYNODE, selectedNode);
                startActivity(intent);
            }
        });

        registerForContextMenu(moneyNodesListView);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_moneynode_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                Intent intent = new Intent(this, 
                    MoneyNodeEditActivity.class);
                intent.putExtra(User.KEY_USER, currentUser);
                startActivityForResult(intent, REQCODE_ADD);
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
                case REQCODE_ADD:
                    MoneyNode node = (MoneyNode) data.getParcelableExtra(
                        MoneyNode.KEY_MONEYNODE);
                    moneyNodes.add(node);
                    adapter.notifyDataSetChanged();
                    break;
                case REQCODE_EDIT:
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_MONEYNODES, new ArrayList<MoneyNode>(moneyNodes));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        android.view.MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_moneynode_list, menu);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        MoneyNode node = adapter.getItem(info.position);
        switch (item.getItemId()) {
            case R.id.menu_remove:
                dbHelper.deleteMoneyNode(node);
                moneyNodes.remove(node);
                adapter.notifyDataSetChanged();
                return true;
            case R.id.menu_edit:
                Intent intent = new Intent(this, 
                    MoneyNodeEditActivity.class);
                intent.putExtra(User.KEY_USER, currentUser);
                intent.putExtra(MoneyNode.KEY_MONEYNODE, node);
                startActivityForResult(intent, REQCODE_EDIT);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
