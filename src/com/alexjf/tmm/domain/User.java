package com.alexjf.tmm.domain;

import com.alexjf.tmm.exceptions.LoginFailedException;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

/**
 * This class represents a single user of the application.
 */
public class User {
    private String name;
    private String password;
    private DatabaseHelper dbHelper;
    private Context context;

    User(String name, Context context) {
        this.name = name;
        this.password = null;
        this.context = context;
        this.dbHelper = null;
        this.dbHelper = new DatabaseHelper(this.context, getName() + ".db");
    }

    public void login(String password) throws LoginFailedException {
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getReadableDatabase(password);
            db.query("sqlite_master", new String[] { "count(*)" }, null, null, null, null, null, null);
        } catch (SQLiteException e) {
            this.password = null;
            throw new LoginFailedException(this.name, e);
        } finally {
            if (db != null) {
                db.close();
            }
        }

        this.password = password;
    }

    public SQLiteDatabase getReadableDatabase() {
        return dbHelper.getReadableDatabase(password);
    }

    public SQLiteDatabase getWritableDatabase() {
        return dbHelper.getReadableDatabase(password);
    }

    /**
     * Gets the name for this instance.
     *
     * @return The name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name for this instance.
     *
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return getName();
    };
}
