/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.fragments;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import net.alexjf.tmm.R;
import net.alexjf.tmm.adapters.CategoryAdapter;
import net.alexjf.tmm.domain.Category;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;

import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ImmedTransactionEditorFragment extends Fragment 
    implements OnDateSetListener {
    private static final String KEY_CURRENTTRANSACTION = "currentTransaction";

    private OnImmediateTransactionEditListener listener;
    private DatabaseHelper dbHelper;

    private ImmediateTransaction transaction;
    private MoneyNode currentMoneyNode;

    private DatePickerFragment datePicker;

    private EditText descriptionText;
    // TODO: I think a button + category list fragment would be better than a
    // spinner here otherwise we have problems with the adapters
    private Spinner categorySpinner;
    private Button executionDateButton;
    private EditText valueText;
    private TextView currencyTextView;
    private Button addButton;
    private SimpleDateFormat dateFormat;

    public interface OnImmediateTransactionEditListener {
        public void onImmediateTransactionCreated(ImmediateTransaction node);
        public void onImmediateTransactionEdited(ImmediateTransaction node);
    }

    public ImmedTransactionEditorFragment() {
        dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_immedtransaction_editor, 
                container, false);

        descriptionText = (EditText) v.findViewById(R.id.description_text);
        categorySpinner = (Spinner) v.findViewById(R.id.category_spinner);
        executionDateButton = (Button) v.findViewById(R.id.executionDate_button);
        valueText = (EditText) v.findViewById(R.id.value_text);
        currencyTextView = (TextView) v.findViewById(R.id.currency_label);
        addButton = (Button) v.findViewById(R.id.add_button);

        datePicker = new DatePickerFragment(this);

        executionDateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                try {
                    datePicker.setDate(dateFormat.parse(
                            executionDateButton.getText().toString()));
                } catch (ParseException e) {
                }
                datePicker.show(getFragmentManager(), "creationDate");
            }
        });

        addButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                String description = descriptionText.getText().toString().trim();

                Category category = (Category) categorySpinner.getSelectedItem();

                Date executionDate;
                try {
                    executionDate = dateFormat.parse(
                        executionDateButton.getText().toString());
                } catch (ParseException e) {
                    executionDate = new Date();
                }

                BigDecimal value;
                try {
                    value = new BigDecimal(valueText.getText().toString());
                } catch (NumberFormatException e) {
                    value = new BigDecimal(0);
                }

                if (transaction == null) {
                    ImmediateTransaction newTransaction = 
                        new ImmediateTransaction(currentMoneyNode, value, 
                                description, category, executionDate);
                    listener.onImmediateTransactionCreated(newTransaction);
                } else {
                    transaction.setDescription(description);
                    transaction.setCategory(category);
                    transaction.setExecutionDate(executionDate);
                    transaction.setValue(value);
                    listener.onImmediateTransactionEdited(transaction);
                }
            }
        });

        if (savedInstanceState != null) {
            transaction = savedInstanceState.getParcelable(KEY_CURRENTTRANSACTION);
        }
        
        updateTransactionFields();

        return v;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        GregorianCalendar calendar = new GregorianCalendar(year, month, day);
        executionDateButton.setText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_CURRENTTRANSACTION, transaction);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnImmediateTransactionEditListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + 
                    " must implement OnImmediateTransactionEditListener");
        }
    }

    /**
     * @param dbHelper the dbHelper to set
     */
    public void setDbHelper(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        updateCategorySpinner();
    }

    /**
     * @return the transaction
     */
    public ImmediateTransaction getTransaction() {
        return transaction;
    }

    /**
     * @param node the transaction to set
     */
    public void setTransaction(ImmediateTransaction trans) {
        ImmediateTransaction prevNode = this.transaction;
        this.transaction = trans;

        if (trans != null) {
            setCurrentMoneyNode(trans.getMoneyNode());
        }

        if (prevNode != transaction) {
            updateTransactionFields();
        }
    }

    /**
     * @return the currentMoneyNode
     */
    public MoneyNode getCurrentMoneyNode() {
        return currentMoneyNode;
    }

    /**
     * @param currentMoneyNode the currentMoneyNode to set
     */
    public void setCurrentMoneyNode(MoneyNode currentMoneyNode) {
        this.currentMoneyNode = currentMoneyNode;
    }

    private void updateTransactionFields() {
        // If we are adding a new node, reset all fields
        if (transaction == null) {
            descriptionText.setText("");
            executionDateButton.setText(dateFormat.format(new Date()));
            valueText.setText("");
            addButton.setText("Add");
        // If we are editing a node, fill fields with current information
        } else {
            descriptionText.setText(transaction.getDescription());
            executionDateButton.setText(dateFormat.format(
                        transaction.getExecutionDate()));
            valueText.setText(transaction.getValue().toString());
            addButton.setText("Edit");
        }

        if (currentMoneyNode != null) {
            currencyTextView.setText(currentMoneyNode.getCurrency());
        }
    }

    private void updateCategorySpinner() {
        if (dbHelper == null) {
            return;
        }

        List<Category> availableCategories;

        try {
            availableCategories = dbHelper.getCategories();
        } catch (Exception e) {
            Log.e("TMM", "Error loading available categories", e);
            availableCategories = new ArrayList<Category>();
        }

        CategoryAdapter categoryAdapter = new CategoryAdapter(getActivity(), dbHelper,
                availableCategories);
        categorySpinner.setAdapter(categoryAdapter);
    }
}

