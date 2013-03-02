/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.widget.Adapter;

/**
 * This class implements miscelaneous function used throughout the entire app
 */
public class Utils {
    /**
     * Combines 2 Date objects, one representing date (d/m/y) and another
     * representing time (h:m:s) into a single Date object
     *
     * @param date The Date object representing a date (e.g. d/m/y)
     * @param time The Date object representing a time (e.g. h:m:s)
     *
     * @return A Date object combining the values of the 2 parameters.
     */
    public static Date combineDateTime(Date date, Date time) {
        Calendar calendarA = Calendar.getInstance();
        calendarA.setTime(date);

        Calendar calendarB = Calendar.getInstance();
        calendarB.setTime(time);

        calendarA.set(Calendar.HOUR_OF_DAY, calendarB.get(Calendar.HOUR_OF_DAY));
        calendarA.set(Calendar.MINUTE, calendarB.get(Calendar.MINUTE));
        calendarA.set(Calendar.SECOND, calendarB.get(Calendar.SECOND));
        calendarA.set(Calendar.MILLISECOND, calendarB.get(Calendar.MILLISECOND));

        return calendarA.getTime();
    }

    /**
     * Checks if provided date is between the date period specified by
     * [dateStart, dateEnd].
     *
     * @param date The date to check.
     * @param dateStart The starting date of the period.
     * @param dateEnd The ending date of the period.
     *
     * @return True if date is in [dateStart, dateEnd], false otherwise.
     */
    public static boolean dateBetween(Date date, Date dateStart, Date dateEnd) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(dateStart);
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(dateEnd);
        return dateBetween(calendar, calendarStart, calendarEnd);
    }

    /**
     * {@link #dateBetween(Date, Date, Date)
     */
    public static boolean dateBetween(Calendar date, Calendar dateStart, 
            Calendar dateEnd) {
        return dateStart.before(date) && dateEnd.after(date);
    }

    /**
     * Converts dp units into pixel units.
     *
     * Allows setting sizes programatically in java using dp.
     *
     * @param context The context where we are calculating the size.
     * @param displayPixels The value of the size in dp.
     *
     * @return The value of the size in px.
     */
    public static int displayPixelsToPixels(Context context, int displayPixels) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (displayPixels * scale + 0.5f);
    }


    /**
     * Copies the content of the provided adapter to the provided list.
     *
     * @param adapter The adapter to copy the content from.
     * @param list The list to which to copy the content.
     */
    @SuppressWarnings("unchecked")
    public static <T> void fromAdapterToList(Adapter adapter, List<T> list) {
        int numElements = adapter.getCount();

        list.clear();
        for (int i = 0; i < numElements; i++) {
            list.add((T) adapter.getItem(i));
        }
    }
}
