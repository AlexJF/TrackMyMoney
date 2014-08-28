/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.adapters;

import java.util.List;

import org.joda.money.Money;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.utils.DrawableResolver;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MoneyNodeAdapter extends ArrayAdapter<MoneyNode> {
    private static final int ROW_VIEW_RESID = R.layout.list_row_moneynode;

    private int colorBalancePositive;
    private int colorBalanceNegative;
    private Integer colorBalanceDefault;

    public MoneyNodeAdapter(Context context) {
        super(context, ROW_VIEW_RESID);
        initialize();
    }

    public MoneyNodeAdapter(Context context,
            List<MoneyNode> objects) {
        super(context, ROW_VIEW_RESID, objects);
        initialize();
    }

    protected void initialize() {
        colorBalanceDefault = null;
        colorBalancePositive = getContext().getResources().getColor(R.color.positive);
        colorBalanceNegative = getContext().getResources().getColor(R.color.negative);
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
            node.load();

            ImageView iconImageView = (ImageView) view.findViewById(R.id.moneynode_icon);
            String iconName = node.getIcon();
            int iconId = DrawableResolver.getInstance().getDrawableId(iconName);
            if (iconId != 0) {
                iconImageView.setImageResource(iconId);
            }

            TextView nameLabel = (TextView) view.findViewById(R.id.moneynode_name);
            nameLabel.setText(node.getName());

            TextView balanceLabel = (TextView) view.findViewById(
                    R.id.moneynode_balance_value);
            Money balance = node.getBalance();

            balanceLabel.setText(balance.toString());

            if (colorBalanceDefault == null) {
                colorBalanceDefault = balanceLabel.getTextColors().getDefaultColor();
            }

            // If balance is positive
            if (balance.isPositive()) {
                balanceLabel.setTextColor(colorBalancePositive);
            }
            // If balance is neutral
            else if (balance.isZero()) {
                balanceLabel.setTextColor(colorBalanceDefault);
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
