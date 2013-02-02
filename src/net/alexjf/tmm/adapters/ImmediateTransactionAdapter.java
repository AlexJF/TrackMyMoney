/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.adapters;

import java.util.List;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.Category;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.domain.Transaction;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.utils.DrawableResolver;

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
    private static final int ROW_VIEW_RESID = R.layout.list_row_immedtransaction;

    private Resources resources;
    private int colorValuePositive;
    private int colorValueNegative;
    private Integer colorValueDefault;

    public ImmediateTransactionAdapter(Context context, 
            List<ImmediateTransaction> objects) {
        super(context, ROW_VIEW_RESID, objects);
        resources = context.getResources();
        colorValuePositive = context.getResources().getColor(R.color.balance_positive);
        colorValueNegative = context.getResources().getColor(R.color.balance_negative);
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
            transaction.load();

            Category cat = transaction.getCategory();
            if (cat != null) {
                cat.load();

                ImageView categoryIconImageView = (ImageView) view.findViewById(
                        R.id.transaction_caticon);
                int iconId = DrawableResolver.getInstance().getDrawableId(
                        transaction.getCategory().getIcon());
                if (iconId != 0) {
                    categoryIconImageView.setImageDrawable(
                            resources.getDrawable(iconId));
                }

                TextView categoryTextView = (TextView) view.findViewById(
                        R.id.transaction_cat);
                categoryTextView.setText(cat.getName());
            }

            MoneyNode node = transaction.getMoneyNode();
            node.load();

            TextView valueTextView = (TextView) view.findViewById(
                    R.id.transaction_value);
            if (colorValueDefault == null) {
                colorValueDefault = valueTextView.getTextColors().getDefaultColor();
            }
            valueTextView.setText(transaction.getValue().toString() + " " + 
                    node.getCurrency());
            int signum = transaction.getValue().signum();
            // If value is positive
            if (signum == 1) {
                valueTextView.setTextColor(colorValuePositive);
            } 
            // If value is neutral
            else if (signum == 0) {
                valueTextView.setTextColor(colorValueDefault);
            }
            // If value is negative
            else {
                valueTextView.setTextColor(colorValueNegative);
            }
        } catch (DatabaseException e) {
            Log.e("TMM", e.getMessage(), e);
        }

        return view;
    }
}
