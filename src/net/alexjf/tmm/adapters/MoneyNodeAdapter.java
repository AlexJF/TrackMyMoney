/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.adapters;

import java.util.List;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.MoneyNode;

import net.alexjf.tmm.exceptions.DatabaseException;

import net.alexjf.tmm.R;

public class MoneyNodeAdapter extends ArrayAdapter<MoneyNode> {
    private static final int ROW_VIEW_RESID = R.layout.moneynode_list_row;

    private DatabaseHelper dbHelper;

    public MoneyNodeAdapter(Context context, DatabaseHelper dbHelper, 
            List<MoneyNode> objects) {
        super(context, ROW_VIEW_RESID, objects);
        this.dbHelper = dbHelper;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // only inflate the view if it's null
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(ROW_VIEW_RESID, null);
        }

        MoneyNode node = getItem(position);
        try {
            node.load(dbHelper.getReadableDatabase());

            TextView nameLabel = (TextView) view.findViewById(R.id.moneynode_name);
            nameLabel.setText(node.getName());

            TextView balanceLabel = (TextView) view.findViewById(
                    R.id.moneynode_balance_value);
            balanceLabel.setText(node.getBalance().toString() + " " + 
                    node.getCurrency());
        } catch (DatabaseException e) {
            Log.e("TMM", e.getMessage());
        }

        return view;
    }
}
