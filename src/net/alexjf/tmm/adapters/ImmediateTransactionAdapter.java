/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.adapters;

import java.util.List;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.Category;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.domain.Transaction;
import net.alexjf.tmm.exceptions.DatabaseException;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImmediateTransactionAdapter extends ArrayAdapter<ImmediateTransaction> {
    private static final int ROW_VIEW_RESID = R.layout.transaction_list_row;

    private DatabaseHelper dbHelper;
    private Resources resources;
    private String packageName;

    public ImmediateTransactionAdapter(Context context, DatabaseHelper dbHelper, 
            List<ImmediateTransaction> objects) {
        super(context, ROW_VIEW_RESID, objects);
        this.dbHelper = dbHelper;
        this.resources = context.getResources();
        this.packageName = context.getPackageName();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // only inflate the view if it's null
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(ROW_VIEW_RESID, null);
        }

        Transaction transaction = getItem(position);
        try {
            transaction.load(dbHelper.getReadableDatabase());

            ImageView categoryIconImageView = (ImageView) view.findViewById(
                    R.id.transaction_caticon);

            int iconId = resources.getIdentifier("cat_icon_supermarket", 
                    "drawable", packageName);
            categoryIconImageView.setImageDrawable(
                    resources.getDrawable(iconId));

            TextView categoryTextView = (TextView) view.findViewById(
                    R.id.transaction_cat);
            Category cat = transaction.getCategory();
            if (cat != null) {
                cat.load(dbHelper.getReadableDatabase());
                categoryTextView.setText(cat.getName());
            }

            MoneyNode node = transaction.getMoneyNode();
            node.load(dbHelper.getReadableDatabase());

            TextView valueTextView = (TextView) view.findViewById(
                    R.id.transaction_value);
            valueTextView.setText(transaction.getValue().toString() + " " + 
                    node.getCurrency());
        } catch (DatabaseException e) {
            Log.e("TMM", e.getMessage(), e);
        }

        return view;
    }
}
