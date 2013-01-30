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
import net.alexjf.tmm.fragments.DrawablePickerFragment;
import net.alexjf.tmm.fragments.DrawablePickerFragment.OnDrawablePickedListener;
import net.alexjf.tmm.utils.DrawableResolver;

import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class MoneyNodeAddActivity extends SherlockFragmentActivity 
    implements OnDateSetListener, OnDrawablePickedListener {
    public static final String EXTRA_NEWMONEYNODE = "newMoneyNode";
    public static final String EXTRA_SELECTEDICON = "selectedIcon";

    private DatabaseHelper dbHelper;

    private DatePickerFragment datePicker;
    private DrawablePickerFragment drawablePicker;

    private String selectedDrawableName;

    private EditText nameText;
    private EditText descriptionText;
    private ImageView iconImageView;
    private Button iconImageButton;
    private Button creationDateButton;
    private EditText initialBalanceText;
    private Spinner currencySpinner;
    private Button addButton;
    private SimpleDateFormat dateFormat;

    public MoneyNodeAddActivity() {
        datePicker = null;
        drawablePicker = null;

        nameText = null;
        descriptionText = null;
        iconImageView = null;
        iconImageButton = null;
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
        iconImageView = (ImageView) findViewById(R.id.icon_image);
        iconImageButton = (Button) findViewById(R.id.icon_button);
        creationDateButton = (Button) findViewById(R.id.creationDate_button);
        initialBalanceText = (EditText) findViewById(R.id.initialBalance_text);
        currencySpinner = (Spinner) findViewById(R.id.currency_spinner);
        addButton = (Button) findViewById(R.id.add_button);

        datePicker = new DatePickerFragment(this);
        Date currentDate = new Date();

        creationDateButton.setText(dateFormat.format(currentDate));

        creationDateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                try {
                    datePicker.setDate(dateFormat.parse(creationDateButton.getText().toString()));
                } catch (ParseException e) {
                }
                datePicker.show(getSupportFragmentManager(), "creationDate");
            }
        });

        drawablePicker = new DrawablePickerFragment(this, "glyphish_");

        iconImageButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                drawablePicker.show(getSupportFragmentManager(), "icon");
            }
        });

        addButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent data = new Intent();
                String name = nameText.getText().toString().trim();
                String description = descriptionText.getText().toString().trim();
                Date creationDate;
                try {
                    creationDate = dateFormat.parse(
                        creationDateButton.getText().toString());
                } catch (ParseException e) {
                    creationDate = new Date();
                }

                BigDecimal initialBalance;
                try {
                    initialBalance = new BigDecimal(
                        initialBalanceText.getText().toString());
                } catch (NumberFormatException e) {
                    initialBalance = new BigDecimal(0);
                }

                String currency = currencySpinner.getSelectedItem().toString().trim();
                MoneyNode newNode = new MoneyNode(name, description, selectedDrawableName,
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
        
        if (savedInstanceState != null) {
            selectedDrawableName = savedInstanceState.getString(EXTRA_SELECTEDICON);
            int iconId = DrawableResolver.getInstance().getDrawableId(selectedDrawableName);
            if (iconId != 0) {
                iconImageView.setImageResource(iconId);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.close();
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        GregorianCalendar calendar = new GregorianCalendar(year, month, day);
        creationDateButton.setText(dateFormat.format(calendar.getTime()));
    }

    public void onDrawablePicked(int drawableId, String drawableName) {
        iconImageView.setImageResource(drawableId);
        selectedDrawableName = drawableName;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_SELECTEDICON, selectedDrawableName);
        super.onSaveInstanceState(outState);
    }
}
