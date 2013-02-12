/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.fragments;

import net.alexjf.tmm.R;
import net.alexjf.tmm.activities.ImmedTransactionEditActivity;
import net.alexjf.tmm.adapters.ImmediateTransactionAdapter;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.fragments.ImmedTransactionEditorFragment.ImmedTransactionEditOldInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

public class ImmedTransactionListFragment extends ListFragment {
    private static final int REQCODE_EDIT = 0;

    private OnImmedTransactionActionListener listener;

    public interface OnImmedTransactionActionListener {
        public void onImmedTransactionSelected(ImmediateTransaction transaction);
        public void onImmedTransactionRemoved(ImmediateTransaction transaction);
        public void onImmedTransactionEdited(ImmediateTransaction transaction,
                ImmedTransactionEditOldInfo oldInfo);
    }

    public ImmedTransactionListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_immedtransaction_list, container, false);

        ListView transactionsListView = (ListView) v.findViewById(android.R.id.list);
        registerForContextMenu(transactionsListView);

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnImmedTransactionActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + 
                    " must implement OnImmedTransactionActionListener");
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        android.view.MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_immedtransaction_list, menu);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        ImmediateTransactionAdapter adapter = (ImmediateTransactionAdapter) getListAdapter();
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        ImmediateTransaction transaction = adapter.getItem(info.position);
        switch (item.getItemId()) {
            case R.id.menu_remove:
                try {
                    transaction.getMoneyNode().removeImmediateTransaction(transaction);
                    adapter.remove(transaction);
                    listener.onImmedTransactionRemoved(transaction);
                } catch (DatabaseException e) {
                    Log.e("TMM", "Unable to remove immediate transaction", e);
                }
                return true;
            case R.id.menu_edit:
                Intent intent = new Intent(getListView().getContext(), 
                    ImmedTransactionEditActivity.class);
                intent.putExtra(ImmediateTransaction.KEY_TRANSACTION, transaction);
                startActivityForResult(intent, REQCODE_EDIT);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, 
            Intent data) {
        ImmediateTransactionAdapter adapter = (ImmediateTransactionAdapter) getListAdapter();
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQCODE_EDIT:
                    ImmediateTransaction transaction = (ImmediateTransaction)
                        data.getParcelableExtra(ImmediateTransaction.KEY_TRANSACTION);
                    ImmedTransactionEditOldInfo oldInfo = (ImmedTransactionEditOldInfo)
                        data.getParcelableExtra(ImmedTransactionEditOldInfo.KEY_OLDINFO);
                    adapter.notifyDataSetChanged();
                    listener.onImmedTransactionEdited(transaction, oldInfo);
                    break;
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ImmediateTransaction selectedTransaction = (ImmediateTransaction) 
            getListAdapter().getItem(position);
        listener.onImmedTransactionSelected(selectedTransaction);
    }
}
