/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.fragments;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import net.alexjf.tmm.R;

import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;

public class DateIntervalBarFragment extends Fragment 
    implements OnDateSetListener {

    private static enum SPINNER_POS {
        TODAY, YESTERDAY, THISMONTH, LASTMONTH,
        THISYEAR, ALLTIME, CUSTOM;
    }

    final static SPINNER_POS[] SPINNER_POS_VALUES = SPINNER_POS.values();

    private OnDateIntervalChangedListener listener;

    private Calendar startDate;
    private Calendar endDate;
    private DateFormat dateFormat;
    private boolean startDateBeingEdited;
    private boolean allTime;

    private DatePickerFragment datePicker;

    private Spinner dateIntervalSpinner;
    private Button startDateButton;
    private Button endDateButton;
    private View customSelector;

    public interface OnDateIntervalChangedListener {
        /**
         * Receives notification of date interval change on DateIntervalBar.
         * 
         * New date interval between startDate and endDate. If these are null,
         * no limit established on start/end.
         *
         * @param startDate Starting date of interval.
         * @param endDate Ending date of interval.
         */
        public void onDateIntervalChanged(Date startDate, Date endDate);
    }

    public DateIntervalBarFragment() {
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        dateFormat = DateFormat.getDateInstance();
        allTime = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dateinterval_bar, container, false);

        dateIntervalSpinner = (Spinner) v.findViewById(R.id.interval_spinner);
        startDateButton = (Button) v.findViewById(R.id.start_button);
        endDateButton = (Button) v.findViewById(R.id.end_button);
        customSelector = v.findViewById(R.id.custom_selector);

        datePicker = new DatePickerFragment(this);

        dateIntervalSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, 
                        int position, long id) {
                        try {
                            if (SPINNER_POS_VALUES[position] == SPINNER_POS.CUSTOM) {
                                allTime = false;
                                customSelector.setVisibility(View.VISIBLE);
                            } 
                            else if (SPINNER_POS_VALUES[position] == SPINNER_POS.ALLTIME) {
                                allTime = true;
                                updateDates(true);
                            }
                            else {
                                allTime = false;
                                customSelector.setVisibility(View.GONE);
                                resetDates();
                                switch (SPINNER_POS_VALUES[position]) {
                                    case YESTERDAY:
                                        startDate.roll(Calendar.DAY_OF_MONTH, -1);
                                        endDate.roll(Calendar.DAY_OF_MONTH, -1);
                                        break;
                                    case THISMONTH:
                                        startDate.set(Calendar.DAY_OF_MONTH, 1);
                                        endDate.set(Calendar.DAY_OF_MONTH, 
                                            endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                                        break;
                                    case LASTMONTH:
                                        startDate.roll(Calendar.MONTH, -1);
                                        endDate.roll(Calendar.MONTH, -1);
                                        startDate.set(Calendar.DAY_OF_MONTH, 1);
                                        endDate.set(Calendar.DAY_OF_MONTH, 
                                            endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                                        break;
                                    case THISYEAR:
                                        startDate.set(Calendar.DAY_OF_YEAR, 1);
                                        endDate.set(Calendar.DAY_OF_YEAR, 
                                            endDate.getActualMaximum(Calendar.DAY_OF_YEAR));
                                        break;
                                    case TODAY:
                                    default:
                                        break;
                                }

                                updateDates(true);
                            }
                        } catch (IndexOutOfBoundsException e) {
                            Log.e("TMM", "Date interval selection out of bounds",
                                    e);
                        }
                    };

                    public void onNothingSelected(AdapterView<?> parent) {
                    };
                });

        startDateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                try {
                    datePicker.setDate(dateFormat.parse(
                            startDateButton.getText().toString()));
                } catch (ParseException e) {
                }
                startDateBeingEdited = true;
                datePicker.show(getFragmentManager(), "startDate");
            }
        });

        endDateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                try {
                    datePicker.setDate(dateFormat.parse(
                            endDateButton.getText().toString()));
                } catch (ParseException e) {
                }
                startDateBeingEdited = false;
                datePicker.show(getFragmentManager(), "endDate");
            }
        });

        return v;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar;

        if (startDateBeingEdited) {
            calendar = startDate;
        } else {
            calendar = endDate;
        }

        calendar.set(year, month, day);
        updateDates(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnDateIntervalChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + 
                    " must implement OnDateIntervalChangedListener");
        }
    }

    public Date getStartDate() {
        return startDate.getTime();
    }

    public Date getEndDate() {
        return endDate.getTime();
    }

    public void setStartDate(Date date) {
        startDate.setTime(date);
        updateDates(true);
    }

    public void setEndDate(Date date) {
        endDate.setTime(date);
        updateDates(true);
    }

    private void updateDates(boolean changed) {
        String startDateString = dateFormat.format(startDate.getTime());
        String endDateString = dateFormat.format(endDate.getTime());

        startDateButton.setText(startDateString);
        endDateButton.setText(endDateString);

        if (changed) {
            if (allTime) {
                listener.onDateIntervalChanged(null, null);
            } else {
                listener.onDateIntervalChanged(startDate.getTime(), endDate.getTime());
            }
        }
    }

    private void resetDates() {
        Date today = new Date();
        startDate.setTime(today);
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        endDate.setTime(today);
        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND, 59);
    }
}

