/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.adapters;

import java.math.BigDecimal;
import java.util.List;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.exceptions.DatabaseException;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MoneyNodeAdapter extends ArrayAdapter<MoneyNode> {
    private static final int ROW_VIEW_RESID = R.layout.moneynode_list_row;

    private DatabaseHelper dbHelper;
    private Context context;
    private int colorBalancePositive;
    private int colorBalanceNegative;

    public MoneyNodeAdapter(Context context, DatabaseHelper dbHelper, 
            List<MoneyNode> objects) {
        super(context, ROW_VIEW_RESID, objects);
        this.dbHelper = dbHelper;
        this.context = context;
        colorBalancePositive = context.getResources().getColor(R.color.balance_positive);
        colorBalanceNegative = context.getResources().getColor(R.color.balance_negative);
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

            ImageView iconImageView = (ImageView) view.findViewById(R.id.moneynode_icon);
            String iconName = node.getIcon();
            if (iconName != null) {
                // TODO: Use an application-level drawable cache here
                int iconId = parent.getResources().getIdentifier(
                        iconName, "drawable", context.getPackageName());
                if (iconId != 0) {
                    iconImageView.setImageResource(iconId);
                }
            }

            TextView nameLabel = (TextView) view.findViewById(R.id.moneynode_name);
            nameLabel.setText(node.getName());

            TextView balanceLabel = (TextView) view.findViewById(
                    R.id.moneynode_balance_value);
            balanceLabel.setText(node.getBalance().toString() + " " + 
                    node.getCurrency());
            int signum = node.getBalance().signum();
            // If balance is positive
            if (signum == 1) {
                balanceLabel.setTextColor(colorBalancePositive);
            } 
            // If balance is neutral
            else if (signum == 0) {
                balanceLabel.setTextColor(balanceLabel.getTextColors().getDefaultColor());
            }
            // If balance is negative
            else {
                balanceLabel.setTextColor(colorBalanceNegative);
            }
        } catch (DatabaseException e) {
            Log.e("TMM", e.getMessage(), e);
        }

        return view;
    }
}
