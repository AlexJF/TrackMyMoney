/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.fragments;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class TimePickerFragment extends DialogFragment {
    private static String KEY_CURRENTTIME = "currentTime";

    private OnTimeSetListener listener;
    private Calendar time;
    private DateFormat timeFormat;

    public TimePickerFragment() {
        time = Calendar.getInstance();
        timeFormat = DateFormat.getTimeInstance();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String savedTime = savedInstanceState.getString(KEY_CURRENTTIME);
            try {
                time.setTime(timeFormat.parse(savedTime));
            } catch (ParseException e) {
                Log.e("TMM", "Unable to parse saved time", e);
            }
        }
        // Use the current time as the default time in the picker
        int hours = getHours();
        int minutes = getMinutes();


        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), listener, hours, minutes, true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_CURRENTTIME, timeFormat.format(time.getTime()));
        super.onSaveInstanceState(outState);
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(OnTimeSetListener listener) {
        this.listener = listener;
    }

    /**
     * @return the time
     */
    public Calendar getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(Calendar time) {
        this.time = time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(Date time) {
        this.time.setTime(time);
    }

    /**
     * Gets the hour component of the time.
     *
     * @return hour component of time
     */
    public int getHours() {
        return time.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Gets the minute component of the time.
     *
     * @return minute component of time
     */
    public int getMinutes() {
        return time.get(Calendar.MINUTE);
    }

    /**
     * Sets the hour component of the time.
     *
     * @param hour hour to set
     */
    public void setHours(int hour) {
        time.set(Calendar.HOUR_OF_DAY, hour);
    }

    /**
     * Sets the minute component of the time.
     *
     * @param minute minute to set
     */
    public void setMinutes(int minute) {
        time.set(Calendar.MINUTE, minute);
    }

}
