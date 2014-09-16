/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import net.alexjf.tmm.R;
import net.alexjf.tmm.adapters.MoneyNodeAdapter;
import net.alexjf.tmm.database.DatabaseManager;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.utils.Utils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.util.*;

public class MoneyNodeListActivity extends ActionBarActivity {
	private static final int REQCODE_ADD = 0;
	private static final int REQCODE_EDIT = 1;
	private static final int REQCODE_PREFS = 2;

	public static final String KEY_INTENTION = "intention";
	public static final String KEY_EXCLUDE = "exclude";
	public static final String INTENTION_MANAGE = "manage";
	public static final String INTENTION_SELECT = "select";
	public static final String INTENTION_LOGOUT = "logout";

	private MoneyNodeAdapter adapter;

	private String intention;
	private List<MoneyNode> excludedMoneyNodes;

	private ViewGroup balancePanel;
	private TextView balanceTextView;
	private Map<CurrencyUnit, Money> balances;

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

		if (intention.equals(INTENTION_LOGOUT)) {
			logout();
			return;
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
					setResult(ActionBarActivity.RESULT_OK, data);
					finish();
				}
			}
		});

		balancePanel = (ViewGroup) findViewById(R.id.balance_panel);
		balanceTextView = (TextView) findViewById(R.id.balance_value);
		balances = new LinkedHashMap();

		registerForContextMenu(moneyNodesListView);
		updateData();
	}

	private void updateData() {
		List<MoneyNode> moneyNodes;
		adapter.setNotifyOnChange(false);
		adapter.clear();

		try {
			moneyNodes = MoneyNode.getMoneyNodes();
			moneyNodes.removeAll(excludedMoneyNodes);
		} catch (Exception e) {
			Log.e("TMM", "Failed to get money nodes: " + e.getMessage(), e);
			moneyNodes = new LinkedList<MoneyNode>();
		}

		balances.clear();

		for (MoneyNode node : moneyNodes) {
			adapter.add(node);

			try {
				node.load();
				CurrencyUnit moneyNodeCurrency = node.getCurrency();

				Money currencyBalance = balances.get(moneyNodeCurrency);

				if (currencyBalance == null) {
					currencyBalance = Money.zero(moneyNodeCurrency);
				}

				balances.put(moneyNodeCurrency, currencyBalance.plus(node.getBalance()));
			} catch (DatabaseException e) {
				Log.e("TMM", "Unable to load money node " + node + ": " + e.getMessage(), e);
			}
		}

		updateGui();
	}

	private void updateGui() {
		adapter.notifyDataSetChanged();

		if (adapter.getCount() > 1) {
			SpannableStringBuilder sb = new SpannableStringBuilder();
			Resources resources = getResources();

			int colorValuePositive = resources.getColor(R.color.positive);
			int colorValueNegative = resources.getColor(R.color.negative);

			boolean first = true;

			for (Map.Entry<CurrencyUnit, Money> balance : balances.entrySet()) {
				if (!first) {
					sb.append(", ");
				}

				first = false;

				Money balanceValue = balance.getValue();

				int startIndex = sb.length();
				sb.append(balanceValue.toString());
				int endIndex = sb.length();

				if (balanceValue.isPositive()) {
					sb.setSpan(new ForegroundColorSpan(colorValuePositive), startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
				}
				else if (balanceValue.isNegative()) {
					sb.setSpan(new ForegroundColorSpan(colorValueNegative), startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
				}
			}

			balanceTextView.setText(sb);
			balancePanel.setVisibility(View.VISIBLE);
		} else {
			balancePanel.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isTaskRoot() && isFinishing()) {
			DatabaseManager.getInstance().closeDatabase();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
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
				startActivityForResult(intent, REQCODE_PREFS);
				return true;
			case R.id.menu_logout:
				logout();
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
					// Changes reflected by onResume
					break;
				case REQCODE_EDIT:
					// Changes reflected by onResume
					break;
				case REQCODE_PREFS:
					// Changes reflected by onResume
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
					MoneyNode.deleteMoneyNode(node);
					updateData();
				} catch (DatabaseException e) {
					Log.e("TMM", "Unable to delete money node", e);
					Toast.makeText(
							this,
							getResources().getString(
									R.string.error_moneynode_delete),
							Toast.LENGTH_LONG).show();
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
	}

	private void logout() {
		Log.d("TMM", "Logging out");
		Intent intent;

		intent = new Intent(this,
				UserListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Utils.clearRememberedLogin();
		startActivity(intent);
		finish();
	}
}
