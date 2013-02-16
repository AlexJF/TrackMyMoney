/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
