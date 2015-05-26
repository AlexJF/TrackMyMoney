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
import net.alexjf.tmm.fragments.MoneyNodeEditorFragment;
import net.alexjf.tmm.fragments.MoneyNodeListFragment.OnMoneyNodeActionListener;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.exceptions.ExitingException;
import net.alexjf.tmm.utils.Utils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.util.*;

public class MoneyNodeListActivity extends BaseActionBarActivity implements  OnMoneyNodeActionListener {
	private static final int REQCODE_ADD = 0;
	private static final int REQCODE_PREFS = 1;

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
	protected void onCreateInternal(Bundle savedInstanceState) throws ExitingException {
		Log.d("TMM", "MoneyNodeListActivity - onCreateInternal");

		Intent intent = getIntent();

		intention = intent.getStringExtra(KEY_INTENTION);

		if (intention == null) {
			intention = INTENTION_MANAGE;
		}

		// Don't need active database for logout
		if (intention.equals(INTENTION_LOGOUT)) {
			setRequiresDatabase(false);
		}

		super.onCreateInternal(savedInstanceState);
		setContentView(R.layout.activity_moneynode_list);

		if (intention.equals(INTENTION_LOGOUT)) {
			logout();
			return;
		}

		adapter = new MoneyNodeAdapter(this);

		ListView moneyNodesListView = (ListView) findViewById(
				R.id.moneynode_list);

		moneyNodesListView.setAdapter(adapter);

		balancePanel = (ViewGroup) findViewById(R.id.balance_panel);
		balanceTextView = (TextView) findViewById(R.id.balance_value);
		balances = new LinkedHashMap();

		registerForContextMenu(moneyNodesListView);
	}

	@Override
	protected void onDatabaseReady(Bundle savedInstanceState) {
		super.onDatabaseReady(savedInstanceState);

		Intent intent = getIntent();

		Log.d("TMM", "MoneyNodeListActivity - onDatabaseReady - " + intent);

		excludedMoneyNodes = intent.getParcelableArrayListExtra(KEY_EXCLUDE);

		Log.d("TMM", "MoneyNodeListActivity - excludedMoneyNodes=" + excludedMoneyNodes);

		if (excludedMoneyNodes == null) {
			excludedMoneyNodes = new LinkedList<MoneyNode>();
		}
	}

	private void updateData() {
		Log.d("TMM", "MoneyNodeListActivity - updateData");
		// Do nothing if database is not ready
		if (!isDatabaseReady()) {
			Log.d("TMM", "MoneyNodeListActivity - updateData - Database not ready");
			return;
		}

		List<MoneyNode> moneyNodes;
		adapter.setNotifyOnChange(false);
		adapter.clear();

		try {
			moneyNodes = MoneyNode.getMoneyNodes();
			Log.d("TMM", "MoneyNodeListActivity - moneyNodes=" + moneyNodes);
			Log.d("TMM", "MoneyNodeListActivity - excludedMoneyNodes=" + excludedMoneyNodes);

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

		// Only show balance panel if intention is manage (root) and there's more
		// than one money node.
		if (adapter.getCount() > 1 && intention.equals(INTENTION_MANAGE)) {
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

		// Only allow logout from the main MoneyNodeListActivity
		if (!intention.equals(INTENTION_MANAGE)) {
			menu.removeItem(R.id.menu_logout);
		}

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
				case REQCODE_PREFS:
					// Changes reflected by onResume
					break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResumeInternal() throws ExitingException {
		super.onResumeInternal();

		Log.d("TMM", "MoneyNodeListActivity - onResumeInternal");

		// Money nodes can be added from MoneyNodeList activities started
		// further down the stack so either we propagate some 'updateData'
		// flag down the stack or we force update everytime.
		updateData();
	}

	private void logout() {
		Log.d("TMM", "Logging out");
		Intent intent;

		intent = new Intent(this, UserListActivity.class);
		Utils.clearRememberedLogin();
		startActivity(intent);
		finish();
	}

	@Override
	public void onMoneyNodeAdded(MoneyNode moneyNode) {
		// Update handled by onResume
	}

	@Override
	public void onMoneyNodeSelected(MoneyNode moneyNode) {
		if (intention.equals(INTENTION_MANAGE)) {
			Intent intent = new Intent(MoneyNodeListActivity.this,
					MoneyNodeDetailsActivity.class);
			intent.putExtra(MoneyNode.KEY_MONEYNODE, moneyNode);
			startActivity(intent);
		} else {
			Intent data = new Intent();
			data.putExtra(MoneyNode.KEY_MONEYNODE, moneyNode);
			setResult(ActionBarActivity.RESULT_OK, data);
			finish();
		}
	}

	@Override
	public void onMoneyNodeRemoved(MoneyNode moneyNode) {
		// Update handled by onResume
	}

	@Override
	public void onMoneyNodeEdited(MoneyNode moneyNode, MoneyNodeEditorFragment.MoneyNodeEditOldInfo oldInfo) {
		// Update handled by onResume
	}
}
