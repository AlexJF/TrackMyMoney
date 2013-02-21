/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.fragments;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class DatePickerFragment extends DialogFragment {
    private static String KEY_CURRENTDATE = "currentDate";

    private OnDateSetListener listener;
    private Calendar date;
    private DateFormat dateFormat;

    public DatePickerFragment() {
        date = Calendar.getInstance();
        dateFormat = DateFormat.getDateInstance();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            try {
                String savedDate = savedInstanceState.getString(KEY_CURRENTDATE);
                date.setTime(dateFormat.parse(savedDate));
            } catch (ParseException e) {
                Log.e("TMM", "Error parsing saved date", e);
            }
        }

        // Use the current date as the default date in the picker
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);


        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), listener, year, month, day);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_CURRENTDATE, dateFormat.format(date.getTime()));
        super.onSaveInstanceState(outState);
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(OnDateSetListener listener) {
        this.listener = listener;
    }

    /**
     * @return the date
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Calendar date) {
        this.date = date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date.setTime(date);
    }

    /**
     * Gets the day component of the date.
     *
     * @return day component of date
     */
    public int getDay() {
        return date.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Gets the month component of the date.
     *
     * @return month component of date
     */
    public int getMonth() {
        return date.get(Calendar.MONTH);
    }

    /**
     * Gets the year component of the date.
     *
     * @return year component of date
     */
    public int getYear() {
        return date.get(Calendar.YEAR);
    }

    /**
     * Sets the day component of the date.
     *
     * @param day day to set
     */
    public void setDay(int day) {
        date.set(Calendar.DAY_OF_MONTH, day);
    }

    /**
     * Sets the month component of the date.
     *
     * @param month month to set
     */
    public void setMonth(int month) {
        date.set(Calendar.MONTH, month);
    }

    /**
     * Sets the year component of the date.
     *
     * @param year year to set
     */
    public void setYear(int year) {
        date.set(Calendar.YEAR, year);
    }
}
