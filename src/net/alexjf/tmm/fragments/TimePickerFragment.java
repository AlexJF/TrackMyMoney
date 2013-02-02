/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.fragments;

import java.util.Calendar;
import java.util.Date;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class TimePickerFragment extends DialogFragment {
    private OnTimeSetListener listener;
    private Calendar time;

    public TimePickerFragment(OnTimeSetListener listener) {
        this.listener = listener;
        this.time = Calendar.getInstance();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default time in the picker
        int hours = getHours();
        int minutes = getMinutes();


        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), listener, hours, minutes, true);
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
