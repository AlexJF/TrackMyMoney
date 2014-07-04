/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import java.util.LinkedList;
import java.util.List;

import net.alexjf.tmm.R;
import net.alexjf.tmm.adapters.MoneyNodeAdapter;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.domain.UserList;
import net.alexjf.tmm.exceptions.DatabaseException;

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
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MoneyNodeListActivity extends SherlockActivity {
    private static final int REQCODE_ADD = 0;
    private static final int REQCODE_EDIT = 1;
    private static final int REQCODE_PREF = 2;

    public static final String KEY_INTENTION = "intention";
    public static final String KEY_EXCLUDE = "exclude";
    public static final String INTENTION_MANAGE = "manage";
    public static final String INTENTION_SELECT = "select";

    private MoneyNodeAdapter adapter;

    private String intention;
    private List<MoneyNode> excludedMoneyNodes;

    public MoneyNodeListActivity() {
        adapter = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moneynode_list);

        Intent intent = getIntent();

        intention = intent.getStringExtra(KEY_INTENTION);

        if (intention == null) {
            intention = INTENTION_MANAGE;
        }

        excludedMoneyNodes = intent.getParcelableArrayListExtra(KEY_EXCLUDE);

        if (excludedMoneyNodes == null) {
            excludedMoneyNodes = new LinkedList<MoneyNode>();
        }

        adapter = new MoneyNodeAdapter(this);

        ListView moneyNodesListView = (ListView) findViewById(
                R.id.moneynode_list);

        View emptyView = findViewById(R.id.moneynode_list_empty);

        moneyNodesListView.setEmptyView(emptyView);
        moneyNodesListView.setAdapter(adapter);
        moneyNodesListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, 
                int position, long id) {
                MoneyNode selectedNode = adapter.getItem(position);

                if (intention.equals(INTENTION_MANAGE)) {
                    Intent intent = new Intent(MoneyNodeListActivity.this, 
                        MoneyNodeDetailsActivity.class);
                    intent.putExtra(MoneyNode.KEY_MONEYNODE, selectedNode);
                    startActivity(intent);
                } else {
                    Intent data = new Intent();
                    data.putExtra(MoneyNode.KEY_MONEYNODE, selectedNode);
                    setResult(SherlockActivity.RESULT_OK, data);
                    finish();
                }
            }
        });

        registerForContextMenu(moneyNodesListView);
        updateData();
    }

    private void updateData() {
        List<MoneyNode> moneyNodes;
        adapter.setNotifyOnChange(false);
        adapter.clear();

        try {
            moneyNodes = DatabaseHelper.getInstance().getMoneyNodes();
            moneyNodes.removeAll(excludedMoneyNodes);
        } catch (Exception e) {
            Log.e("TMM", "Failed to get money nodes: " + e.getMessage() + 
                    "\n" + e.getStackTrace());
            moneyNodes = new LinkedList<MoneyNode>();
        }

        for (MoneyNode node : moneyNodes) {
            adapter.add(node);
        }
        adapter.notifyDataSetChanged();
    }

    private void updateGui() {
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        DatabaseHelper.getInstance().close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_moneynode_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_add:
                intent = new Intent(this, 
                    MoneyNodeEditActivity.class);
                startActivityForResult(intent, REQCODE_ADD);
                return true;
            // TODO: All below could be merged with moneynodedetails activity
            case R.id.menu_manage_categories:
                intent = new Intent(this,
                    CategoryListActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_preferences:
                intent = new Intent(this,
                    PreferencesActivity.class);
                startActivityForResult(intent, REQCODE_PREF);
                return true;
            case R.id.menu_logout:
                intent = new Intent(this,
                    UserListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                UserList.setRememberedLogin(this, null);
                startActivity(intent);
                finish();
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
                    adapter.add(node);
                    break;
                case REQCODE_EDIT:
                    break;
            }
        }
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
                try {
                    DatabaseHelper.getInstance().deleteMoneyNode(node);
                    adapter.remove(node);
                } catch (DatabaseException e) {
                    Log.e("TMM", "Unable to delete money node", e);
                    Toast.makeText(
                        this, 
                        getResources().getString(
                            R.string.error_moneynode_delete), 
                        3).show();
                }
                return true;
            case R.id.menu_edit:
                Intent intent = new Intent(this, 
                    MoneyNodeEditActivity.class);
                intent.putExtra(MoneyNode.KEY_MONEYNODE, node);
                startActivityForResult(intent, REQCODE_EDIT);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Money nodes can be added from MoneyNodeList activities started
        // further down the stack so either we propagate some 'updateData'
        // flag down the stack or we force update everytime.
        updateData();
        updateGui();
    }
}
