/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.utils;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.alexjf.tmm.domain.User;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.Adapter;

/**
 * This class implements miscelaneous function used throughout the entire app
 */
public class Utils {
    private static Map<Activity, Integer> prevOrientations =
        new HashMap<Activity, Integer>();

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
     * If dateStart or dateEnd are null, the start or end (respectively) are
     * considered infinite.
     *
     * @param date The date to check.
     * @param dateStart The starting date of the period.
     * @param dateEnd The ending date of the period.
     *
     * @return True if date is in [dateStart, dateEnd], false otherwise.
     */
    public static boolean dateBetween(Date date, Date dateStart, Date dateEnd) {
        Calendar calendar = null;
        if (date != null) {
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        }

        Calendar calendarStart = null;
        if (dateStart != null) {
            calendarStart = Calendar.getInstance();
            calendarStart.setTime(dateStart);
        }

        Calendar calendarEnd = null;
        if (dateEnd != null) {
            calendarEnd = Calendar.getInstance();
            calendarEnd.setTime(dateEnd);
        }
        return dateBetween(calendar, calendarStart, calendarEnd);
    }

    /**
     * {@link #dateBetween(Date, Date, Date)
     */
    public static boolean dateBetween(Calendar date, Calendar dateStart,
            Calendar dateEnd) {
        return (dateStart == null || dateStart.before(date)) &&
               (dateEnd == null || dateEnd.after(date));
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

    /**
     * Create a SHA-1 digest of the provided message.
     *
     * @param message The message to digest.
     *
     * @return The digest of the message using SHA-1.
     */
    public static String sha1(String message) {
        String hash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
            byte[] bytes = message.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for( byte b : bytes )
            {
                sb.append( String.format("%02X", b) );
            }
            hash = sb.toString();
        } catch(Exception e ) {
            Log.e("TMM", e.getMessage(), e);
        }
        return hash;
    }

    public static void preventOrientationChanges(Activity activity) {
        Integer prevOrientation = prevOrientations.get(activity);

        if (prevOrientation == null) {
            prevOrientation = activity.getRequestedOrientation();
            prevOrientations.put(activity, prevOrientation);
        } else {
            return;
        }

        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }
    }

    public static void allowOrientationChanges(Activity activity) {
        Integer prevOrientation = prevOrientations.get(activity);

        if (prevOrientation == null) {
            return;
        }

        activity.setRequestedOrientation(prevOrientation);
        prevOrientations.remove(activity);
    }

    private static final String USERDATA_REMEMBEREDLOGIN = "rememberedLogin";

    public static void setRememberedLogin(String username, String password) {
        PreferenceManager prefManager = PreferenceManager.getInstance();
        prefManager.writeGlobalStringPreference(USERDATA_REMEMBEREDLOGIN,
            username + ":" + password);
    }

    public static void clearRememberedLogin() {
        PreferenceManager prefManager = PreferenceManager.getInstance();
        prefManager.removeGlobalPreference(USERDATA_REMEMBEREDLOGIN);
    }

    public static RememberedLoginData getRememberedLogin() {
        PreferenceManager prefManager = PreferenceManager.getInstance();
        String[] rememberedLogin = prefManager.readGlobalStringPreference(
                USERDATA_REMEMBEREDLOGIN, "").split(":");

        if (rememberedLogin.length == 2) {
        	String username = rememberedLogin[0];
        	String password = rememberedLogin[1];

        	return new RememberedLoginData(username, password);
        }

        return null;
    }

    public static class RememberedLoginData {
		public String username;
    	public String passwordHash;

    	public RememberedLoginData(String username, String passwordHash) {
			super();
			this.username = username;
			this.passwordHash = passwordHash;
		}
    }

    private static final String USERDATA_CURRENTUSER = "currentUser";

    public static User getCurrentUser() {
        PreferenceManager prefManager = PreferenceManager.getInstance();

        String currentUsername = prefManager.readGlobalStringPreference(USERDATA_CURRENTUSER, null);

        if (currentUsername != null) {
        	return new User(currentUsername);
        } else {
        	return null;
        }
    }

    public static void setCurrentUser(String username) {
    	setCurrentUser(new User(username));
    }

    public static void setCurrentUser(User user) {
        if (user == null) {
        	return;
        }

        PreferenceManager prefManager = PreferenceManager.getInstance();

        prefManager.writeGlobalStringPreference(USERDATA_CURRENTUSER, user.getName());
    }
}
