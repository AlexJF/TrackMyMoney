/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.domain.User;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.fragments.DatePickerFragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class MoneyNodeAddActivity extends SherlockFragmentActivity {
    public static final String EXTRA_NEWMONEYNODE = "newMoneyNode";

    private DatabaseHelper dbHelper;

    private EditText nameText;
    private EditText descriptionText;
    private EditText locationText;
    private Button creationDateButton;
    private EditText initialBalanceText;
    private Spinner currencySpinner;
    private Button addButton;
    private SimpleDateFormat dateFormat;

    public MoneyNodeAddActivity() {
        nameText = null;
        descriptionText = null;
        locationText = null;
        creationDateButton = null;
        initialBalanceText = null;
        currencySpinner = null;
        addButton = null;
        dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
    }
            
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moneynode_add);

        Intent intent = getIntent();
        User currentUser = (User) intent.getParcelableExtra(
                User.EXTRA_CURRENTUSER);
        dbHelper = new DatabaseHelper(getApplicationContext(), 
                currentUser);

        nameText = (EditText) findViewById(R.id.name_text);
        descriptionText = (EditText) findViewById(R.id.description_text);
        locationText = (EditText) findViewById(R.id.location_text);
        creationDateButton = (Button) findViewById(R.id.creationDate_button);
        initialBalanceText = (EditText) findViewById(R.id.initialBalance_text);
        currencySpinner = (Spinner) findViewById(R.id.currency_spinner);
        addButton = (Button) findViewById(R.id.add_button);

        Date currentDate = new Date();

        creationDateButton.setText(dateFormat.format(currentDate));

        creationDateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                DatePickerFragment datePicker = new DatePickerFragment(
                    new Handler(new CreationDatePickerCallback()));
                datePicker.show(getSupportFragmentManager(), "creationDate");
            }
        });

        addButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent data = new Intent();
                String name = nameText.getText().toString().trim();
                String description = descriptionText.getText().toString().trim();
                String location = locationText.getText().toString().trim();
                Date creationDate;
                try {
                    creationDate = dateFormat.parse(
                        creationDateButton.getText().toString());
                } catch (ParseException e) {
                    creationDate = new Date();
                }
                BigDecimal initialBalance = new BigDecimal(
                    initialBalanceText.getText().toString());
                String currency = currencySpinner.getSelectedItem().toString().trim();
                MoneyNode newNode = new MoneyNode(name, description, location,
                    creationDate, initialBalance, currency);
                try {
                    Log.d("TMM", "Adding moneynode " + name);
                    newNode.save(dbHelper.getWritableDatabase());
                    data.putExtra(EXTRA_NEWMONEYNODE, newNode);
                    setResult(SherlockFragmentActivity.RESULT_OK, data);
                    finish();
                } catch (DatabaseException e) {
                    Toast.makeText(MoneyNodeAddActivity.this,
                        "Error adding money node: " + e.getMessage(), 3).show();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.close();
    }

    private class CreationDatePickerCallback implements Handler.Callback {
        public boolean handleMessage(Message msg) {
            if (msg.what == DatePickerFragment.DATESET_MESSAGE) {
                Bundle data = msg.getData();
                int day = data.getInt(DatePickerFragment.DAY);
                int month = data.getInt(DatePickerFragment.MONTH);
                int year = data.getInt(DatePickerFragment.YEAR);
                GregorianCalendar calendar = new GregorianCalendar(year, month,
                        day);
                creationDateButton.setText(
                        dateFormat.format(calendar.getTime()));
                return true;
            }

            return false;
        }
    }
}
