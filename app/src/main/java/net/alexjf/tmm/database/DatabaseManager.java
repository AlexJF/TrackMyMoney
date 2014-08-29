/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.database;

import net.alexjf.tmm.utils.CacheFactory;
import net.alexjf.tmm.utils.PreferenceManager;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;
import android.content.Context;
import android.util.Log;

public class DatabaseManager {

    private SQLiteDatabase database;
    private Context context = null;

    private static DatabaseManager instance = null;

    public static void initialize(Context context) {
    	instance = new DatabaseManager(context);
    }

    public static DatabaseManager getInstance() {
    	return instance;
    }

    private DatabaseManager(Context context) {
    	this.context = context;
    }

    public SQLiteDatabase getDatabase() {
    	return database;
    }

    public SQLiteDatabase getDatabase(String username, String password) {
    	closeDatabase();

    	database = new DatabaseHelper(
    			context,
    			getDatabaseName(username),
    			PreferenceManager.getInstance().getUserPreferences(username)
    		).getWritableDatabase(password);

    	return database;
    }

    public void closeDatabase() {
    	if (database != null) {
    		if (database.isOpen()) {
	    		database.close();
    		}
			CacheFactory.getInstance().clearCaches();
    	}

    	database = null;
    }

    public boolean login(String username, String password) {
        try {
            getDatabase(username, password);
            testDatabaseAccess(database);
            return true;
        } catch (SQLiteException e) {
            Log.e("TMM", e.getMessage(), e);
        } finally {
        	closeDatabase();
        }

        return false;
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        try {
        	getDatabase(username, oldPassword);
            database.rawQuery("PRAGMA rekey = '" + newPassword + "'", null);
            return true;
        } catch (SQLiteException e) {
            Log.e("TMM", e.getMessage(), e);
            return false;
        } finally {
        	closeDatabase();
        }
    }

    /**
     * Deletes a database associated with a particular user
     *
     * @param User user whose database we want to delete.
     */
    public void deleteDatabase(String username) {
        context.deleteDatabase(getDatabaseName(username));
    }

    private boolean testDatabaseAccess(SQLiteDatabase db) {
        try {
            db.query("sqlite_master", new String[] { "count(*)" }, null, null,
                    null, null, null, null);
            return true;
        } catch (SQLiteException e) {
            Log.e("TMM", e.getMessage(), e);
            return false;
        }
    }

    private String getDatabaseName(String username) {
    	return username + ".db";
    }
}