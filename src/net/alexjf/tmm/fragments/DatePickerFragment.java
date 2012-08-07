package net.alexjf.tmm.fragments;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.support.v4.app.DialogFragment;

import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment
                                implements DatePickerDialog.OnDateSetListener {
    public static final int DATESET_MESSAGE = 1;
    public static final String DAY = "day";
    public static final String MONTH = "month";
    public static final String YEAR = "year";

    private static final String DATESET_MESSAGE_BUNDLEID = "dateSetMessage";

    private Message dateSetMessage;

    public DatePickerFragment(Handler handler) {
        dateSetMessage = handler.obtainMessage(DATESET_MESSAGE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        if (savedInstanceState != null) {
            dateSetMessage = savedInstanceState.
                getParcelable(DATESET_MESSAGE_BUNDLEID);
        }

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(DATESET_MESSAGE_BUNDLEID, dateSetMessage);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        final Message message = Message.obtain(dateSetMessage);
        Bundle bundle = new Bundle();
        bundle.putInt(DAY, day);
        bundle.putInt(MONTH, month);
        bundle.putInt(YEAR, year);
        message.setData(bundle);
        message.sendToTarget();
    }
}
