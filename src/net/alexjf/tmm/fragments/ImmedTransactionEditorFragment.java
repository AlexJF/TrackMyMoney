/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.fragments;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;

import net.alexjf.tmm.R;
import net.alexjf.tmm.activities.CategoryListActivity;
import net.alexjf.tmm.domain.Category;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.utils.DrawableResolver;
import net.alexjf.tmm.utils.Utils;

import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class ImmedTransactionEditorFragment extends Fragment 
    implements OnDateSetListener, OnTimeSetListener {
    private static final String KEY_CURRENTTRANSACTION = "currentTransaction";
    private static final String KEY_SELECTEDCATEGORY = "selectedCategory";

    private OnImmediateTransactionEditListener listener;
    private Category selectedCategory;

    private ImmediateTransaction transaction;
    private MoneyNode currentMoneyNode;

    private TimePickerFragment timePicker;
    private DatePickerFragment datePicker;

    private EditText descriptionText;
    private Button categoryButton;
    private Button executionDateButton;
    private Button executionTimeButton;
    private EditText valueText;
    private TextView currencyTextView;
    private Button addButton;
    private DateFormat dateFormat;
    private DateFormat timeFormat;

    public ImmedTransactionEditorFragment() {
        dateFormat = DateFormat.getDateInstance();
        timeFormat = DateFormat.getTimeInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_immedtransaction_editor, 
                container, false);

        descriptionText = (EditText) v.findViewById(R.id.description_text);
        categoryButton = (Button) v.findViewById(R.id.category_button);
        executionDateButton = (Button) v.findViewById(R.id.executionDate_button);
        executionTimeButton = (Button) v.findViewById(R.id.executionTime_button);
        valueText = (EditText) v.findViewById(R.id.value_text);
        currencyTextView = (TextView) v.findViewById(R.id.currency_label);
        addButton = (Button) v.findViewById(R.id.add_button);

        timePicker = new TimePickerFragment(this);
        datePicker = new DatePickerFragment(this);

        categoryButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), 
                    CategoryListActivity.class);
                intent.putExtra(CategoryListActivity.KEY_INTENTION, 
                    CategoryListActivity.INTENTION_SELECT);
                startActivityForResult(intent, 0);
            }
        });

        executionDateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                try {
                    datePicker.setDate(dateFormat.parse(
                            executionDateButton.getText().toString()));
                } catch (ParseException e) {
                }
                datePicker.show(getFragmentManager(), "executionDate");
            }
        });

        executionTimeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                try {
                    timePicker.setTime(timeFormat.parse(
                            executionTimeButton.getText().toString()));
                } catch (ParseException e) {
                }
                timePicker.show(getFragmentManager(), "executionTime");
            }
        });

        addButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                String description = descriptionText.getText().toString().trim();

                Date executionDate;
                Date executionTime;
                Date executionDateTime;
                try {
                    executionDate = dateFormat.parse(
                        executionDateButton.getText().toString());
                    executionTime = timeFormat.parse(
                        executionTimeButton.getText().toString());
                } catch (ParseException e) {
                    executionTime = executionDate = new Date();
                }

                executionDateTime = Utils.combineDateTime(executionDate, 
                    executionTime);

                BigDecimal value;
                try {
                    value = new BigDecimal(valueText.getText().toString());
                } catch (NumberFormatException e) {
                    value = new BigDecimal(0);
                }

                if (transaction == null) {
                    ImmediateTransaction newTransaction = 
                        new ImmediateTransaction(currentMoneyNode, value, 
                                description, selectedCategory, executionDateTime);
                    listener.onImmediateTransactionCreated(newTransaction);
                } else {
                    ImmedTransactionEditOldInfo oldInfo = 
                        new ImmedTransactionEditOldInfo(transaction);
                    transaction.setDescription(description);
                    transaction.setCategory(selectedCategory);
                    transaction.setExecutionDate(executionDateTime);
                    transaction.setValue(value);
                    listener.onImmediateTransactionEdited(transaction, 
                            oldInfo);
                }
            }
        });

        if (savedInstanceState != null) {
            transaction = savedInstanceState.getParcelable(KEY_CURRENTTRANSACTION);
            selectedCategory = savedInstanceState.getParcelable(KEY_SELECTEDCATEGORY);
        }
        
        updateTransactionFields();

        return v;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        GregorianCalendar calendar = new GregorianCalendar(year, month, day);
        executionDateButton.setText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hours, int minutes) {
        GregorianCalendar calendar = new GregorianCalendar(0, 0, 0, hours, minutes);
        executionTimeButton.setText(timeFormat.format(calendar.getTime()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_CURRENTTRANSACTION, transaction);
        outState.putParcelable(KEY_SELECTEDCATEGORY, selectedCategory);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            selectedCategory = (Category) data.getParcelableExtra(
                    Category.KEY_CATEGORY);
            updateCategoryFields();
        }
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
            executionTimeButton.setText(timeFormat.format(new Date()));
            valueText.setText("");
            addButton.setText("Add");
        // If we are editing a node, fill fields with current information
        } else {
            descriptionText.setText(transaction.getDescription());
            executionDateButton.setText(dateFormat.format(
                        transaction.getExecutionDate()));
            executionTimeButton.setText(timeFormat.format(
                        transaction.getExecutionDate()));
            valueText.setText(transaction.getValue().toString());
            addButton.setText("Edit");
        }

        if (currentMoneyNode != null) {
            currencyTextView.setText(currentMoneyNode.getCurrency());
        }

        updateCategoryFields();
    }

    private void updateCategoryFields() {
        if (selectedCategory == null && transaction != null) {
            selectedCategory = transaction.getCategory();
        }

        if (selectedCategory == null) {
            categoryButton.setText(R.string.category_nonselected);
            categoryButton.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, 0, 0);
        } else {
            categoryButton.setText(selectedCategory.getName());
            int drawableId = DrawableResolver.getInstance().getDrawableId(
                    selectedCategory.getIcon());
            categoryButton.setCompoundDrawablesWithIntrinsicBounds(
                    drawableId, 0, 0, 0);
        }
    }

    public static class ImmedTransactionEditOldInfo implements Parcelable {
        public static final String KEY_OLDINFO = "oldImmedTransactionInfo";
        private static final DateFormat dateFormat = DateFormat.getDateTimeInstance();

        private String description;
        private Category category;
        private Date executionDate;
        private BigDecimal value;

        public ImmedTransactionEditOldInfo(ImmediateTransaction trans) {
            this(trans.getDescription(), trans.getCategory(),
                    trans.getExecutionDate(), trans.getValue());
        }

        public ImmedTransactionEditOldInfo(String description,
                Category category, Date executionDate, BigDecimal value) {
            this.description = description;
            this.category = category;
            this.executionDate = executionDate;
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public Category getCategory() {
            return category;
        }

        public Date getExecutionDate() {
            return executionDate;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(description);
            out.writeParcelable(category, flags);
            out.writeString(dateFormat.format(executionDate));
            out.writeString(value.toString());
        }

        public int describeContents() {
            return 0;
        }

        public static final Parcelable.Creator<ImmedTransactionEditOldInfo> CREATOR =
            new Parcelable.Creator<ImmedTransactionEditOldInfo>() {
                public ImmedTransactionEditOldInfo createFromParcel(Parcel in) {
                    String description = in.readString();
                    Category category = (Category) in.readParcelable(
                            Category.class.getClassLoader());
                    Date date = null;
                    try {
                        date = dateFormat.parse(in.readString());
                    } catch (ParseException e) {
                        Log.d("TMM", e.getMessage(), e);
                    }
                    BigDecimal value = new BigDecimal(in.readString());
                    return new ImmedTransactionEditOldInfo(description, 
                            category, date, value);
                }
     
                public ImmedTransactionEditOldInfo[] newArray(int size) {
                    return new ImmedTransactionEditOldInfo[size];
                }
            };
    }

    public interface OnImmediateTransactionEditListener {
        public void onImmediateTransactionCreated(ImmediateTransaction transaction);
        public void onImmediateTransactionEdited(ImmediateTransaction transaction, 
                ImmedTransactionEditOldInfo oldInfo);
    }
}

