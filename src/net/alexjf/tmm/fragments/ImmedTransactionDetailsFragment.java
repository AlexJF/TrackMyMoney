/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.fragments;

import java.math.BigDecimal;
import java.text.DateFormat;

import net.alexjf.tmm.R;
import net.alexjf.tmm.adapters.TwoLineTextAdapter;
import net.alexjf.tmm.domain.Category;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.utils.DrawableResolver;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class ImmedTransactionDetailsFragment extends Fragment {
    private static DateFormat dateTimeFormat = DateFormat.getDateTimeInstance();

    private ListView detailsListView;
    private TwoLineTextAdapter<TransactionDetails> adapter;

    private ImmediateTransaction transaction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_immedtransaction_details, 
                container, false);

        detailsListView = (ListView) v.findViewById(android.R.id.list);

        adapter = new TwoLineTextAdapter<TransactionDetails>(
            getActivity()) {
                public void setTitleText(TextView titleTextView, 
                    TransactionDetails item) {
                    titleTextView.setText(item.label);
                };

                public void setValueText(TextView valueTextView, 
                    TransactionDetails item) {
                    valueTextView.setText(item.value);
                    valueTextView.setCompoundDrawablesWithIntrinsicBounds(
                        item.leftDrawableId, 0, 0, 0);

                    if (item.valueColor == null) {
                        valueTextView.setTextColor(valueTextView.getTextColors().
                            getDefaultColor());
                    } else {
                        valueTextView.setTextColor(item.valueColor);
                    }
                };
        };

        detailsListView.setAdapter(adapter);

        return v;
    }

    public void setTransaction(ImmediateTransaction transaction) {
        if (this.transaction == transaction) {
            return;
        }

        this.transaction = transaction;
        updateDetails();
    }

    public void updateDetails() {
        adapter.setNotifyOnChange(false);
        adapter.clear();

        try {
            transaction.load();
        } catch (DatabaseException e) {
            Log.e("TMM", "Error loading transaction", e);
            return;
        }

        Resources res = getResources();

        String moneyNodeLabel = res.getString(R.string.moneyNode);
        MoneyNode node = transaction.getMoneyNode();
        try {
            node.load();
            int drawableId = DrawableResolver.getInstance().getDrawableId(
                node.getIcon());
            adapter.add(new TransactionDetails(moneyNodeLabel,
                node.getName(), drawableId));
        } catch (DatabaseException e) {
            Log.e("TMM", "Error loading money node", e);
        }

        String descriptionLabel = res.getString(R.string.description);
        String descriptionValue = transaction.getDescription();

        if (descriptionValue.trim().equals("")) {
            descriptionValue = "None";
        }

        adapter.add(new TransactionDetails(descriptionLabel, 
            descriptionValue));

        String categoryLabel = res.getString(R.string.category);
        Category category = transaction.getCategory();
        try {
            category.load();
            int drawableId = DrawableResolver.getInstance().getDrawableId(
                category.getIcon());
            adapter.add(new TransactionDetails(categoryLabel, 
                category.getName(), drawableId));
        } catch (DatabaseException e) {
            Log.e("TMM", "Error loading category", e);
            return;
        }

        String executionDateLabel = res.getString(R.string.executionDate);
        adapter.add(new TransactionDetails(executionDateLabel,
            dateTimeFormat.format(transaction.getExecutionDate())));

        String valueLabel = res.getString(R.string.value);
        BigDecimal value = transaction.getValue();
        TransactionDetails valueDetails = new TransactionDetails(valueLabel,
            value.toString() + " " + node.getCurrency());

        if (value.signum() > 0) {
            valueDetails.valueColor = res.getColor(R.color.positive);
        } 
        else if (value.signum() < 0) {
            valueDetails.valueColor = res.getColor(R.color.negative);
        }

        adapter.add(valueDetails);

        String fromLabel = res.getString(R.string.transfer_moneynode);
        ImmediateTransaction transferTransaction = 
            transaction.getTransferTransaction();
        try {
            transferTransaction.load();
            MoneyNode transferNode = transferTransaction.getMoneyNode();
            transferNode.load();
            int drawableId = DrawableResolver.getInstance().getDrawableId(
                transferNode.getIcon());
            adapter.add(new TransactionDetails(fromLabel, 
                transferNode.getName(), drawableId));
        } catch (DatabaseException e) {
            Log.e("TMM", "Error loading transfer money node", e);
            return;
        }

        adapter.notifyDataSetChanged();
    }

    private class TransactionDetails {
        public String label;
        public String value;
        public int leftDrawableId;
        public Integer valueColor;

        public TransactionDetails(String label, String value) {
            this(label, value, 0);
        }

        public TransactionDetails(String label, String value,
            int leftDrawableId) {
            this(label, value, leftDrawableId, null);
        }

        public TransactionDetails(String label, String value, 
            int leftDrawableId, Integer valueColor) {
            this.label = label;
            this.value = value;
            this.leftDrawableId = leftDrawableId;
            this.valueColor = valueColor;
        }
    }
}

