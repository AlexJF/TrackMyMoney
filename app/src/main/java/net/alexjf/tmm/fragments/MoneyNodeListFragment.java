/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import net.alexjf.tmm.R;
import net.alexjf.tmm.activities.MoneyNodeEditActivity;
import net.alexjf.tmm.adapters.MoneyNodeAdapter;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.fragments.MoneyNodeEditorFragment.MoneyNodeEditOldInfo;
import net.alexjf.tmm.interfaces.IWithAdapter;

public class MoneyNodeListFragment extends ListFragment implements IWithAdapter {
	private static final int REQCODE_EDIT = 0;

	private OnMoneyNodeActionListener listener;

	private boolean databaseReady;

	public interface OnMoneyNodeActionListener {
		public void onMoneyNodeAdded(MoneyNode moneyNode);

		public void onMoneyNodeSelected(MoneyNode moneyNode);

		public void onMoneyNodeRemoved(MoneyNode moneyNode);

		public void onMoneyNodeEdited(MoneyNode moneyNode,
				MoneyNodeEditOldInfo oldInfo);
	}

	public MoneyNodeListFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_moneynode_list, container, false);

		ListView transactionsListView = (ListView) v.findViewById(android.R.id.list);
		registerForContextMenu(transactionsListView);

		FragmentManager fm = getFragmentManager();

		if (databaseReady) {
			Log.d("TMM", "MoneyNodeListFragment - onCreateView - databaseReady");
		}

		return v;
	}

	public void onDatabaseReady() {
		if (databaseReady) {
			Log.d("TMM", "ImmedTransactionListFragment - onDatabaseReady");
			return;
		}

		databaseReady = true;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnMoneyNodeActionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() +
					" must implement OnMoneyNodeActionListener");
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		android.view.MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_moneynode_list, menu);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		MoneyNodeAdapter adapter = (MoneyNodeAdapter) getListAdapter();
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		MoneyNode moneyNode = adapter.getItem(info.position);

		switch (item.getItemId()) {
			case R.id.menu_remove:
				try {
					MoneyNode.deleteMoneyNode(moneyNode);
					adapter.remove(moneyNode);
					listener.onMoneyNodeRemoved(moneyNode);
				} catch (DatabaseException e) {
					Log.e("TMM", "Unable to remove money node", e);
				}
				return true;
			case R.id.menu_edit:
				Intent intent = new Intent(getListView().getContext(),
						MoneyNodeEditActivity.class);
				intent.putExtra(MoneyNode.KEY_MONEYNODE, moneyNode);
				startActivityForResult(intent, REQCODE_EDIT);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			MoneyNode moneyNode;

			switch (requestCode) {
				case REQCODE_EDIT:
					moneyNode = (MoneyNode)
							data.getParcelableExtra(MoneyNode.KEY_MONEYNODE);
					MoneyNodeEditOldInfo oldInfo = (MoneyNodeEditOldInfo)
							data.getParcelableExtra(MoneyNodeEditOldInfo.KEY_OLDINFO);
					listener.onMoneyNodeEdited(moneyNode, oldInfo);
					break;
			}
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		MoneyNode selectedMoneyNode = (MoneyNode) getListAdapter().getItem(position);
		listener.onMoneyNodeSelected(selectedMoneyNode);
	}

	@Override
	public BaseAdapter getAdapter() {
		return (BaseAdapter) getListAdapter();
	}

	@Override
	public void setAdapter(BaseAdapter adapter) {
		setListAdapter(adapter);
	}
}
