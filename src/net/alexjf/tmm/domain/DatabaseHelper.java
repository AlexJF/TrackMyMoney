/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.domain;

import java.util.ArrayList;
import java.util.List;

import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.exceptions.DatabaseUnknownUserException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;
import net.sqlcipher.database.SQLiteOpenHelper;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private User currentUser;
    private boolean preventClose = false;

    private static DatabaseHelper instance = null;

    public static DatabaseHelper initialize(Context context, User user) {
        instance = new DatabaseHelper(context, user);
        return getInstance();
    }

    /**
     * Prevent the closure of the database until otherwise stated.
     *
     * This is useful for AsyncTasks to be able to continue accessing the
     * database even when an activity restarts.
     *
     * @param preventClose the preventClose to set
     */
    public void setPreventClose(boolean preventClose) {
        this.preventClose = preventClose;
    }

    public static DatabaseHelper getInstance() {
        return instance;
    }

    private DatabaseHelper(Context context, User user) {
        super(context, user.getName() + ".db", null, DATABASE_VERSION);
        currentUser = user;
    }

    public SQLiteDatabase getReadableDatabase() 
        throws DatabaseUnknownUserException {
        userDefinedOrThrow();
        return getReadableDatabase(currentUser.getPassword());
    }

    public SQLiteDatabase getWritableDatabase()
        throws DatabaseUnknownUserException {
        userDefinedOrThrow();
        return getWritableDatabase(currentUser.getPassword());
    }

    public boolean login() {
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            db.query("sqlite_master", new String[] { "count(*)" }, null, null, 
                    null, null, null, null);
            return true;
        } catch (SQLiteException e) {
            return false;
        } catch (DatabaseUnknownUserException e) {
            Log.e("TMM", e.getMessage(), e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean changePassword(String newPassword) {
        SQLiteDatabase db = null;

        try {
            db = getWritableDatabase();
            db.rawQuery("PRAGMA rekey = '" + newPassword + "'", null);
            return true;
        } catch (SQLiteException e) {
            Log.e("TMM", e.getMessage(), e);
            return false;
        } catch (DatabaseUnknownUserException e) {
            Log.e("TMM", e.getMessage(), e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        MoneyNode.onDatabaseCreation(db);
        Transaction.onDatabaseCreation(db);
        ImmediateTransaction.onDatabaseCreation(db);
        ScheduledTransaction.onDatabaseCreation(db);
        Category.onDatabaseCreation(db);
        // TODO: Add these domain classes
        /*
        sqlStatements.add(
            "CREATE TABLE ScheduledToImmediate (" +
                "scheduledId INTEGER NOT NULL REFERENCES ScheduledTransactions ON DELETE CASCADE," + 
                "immediateId INTEGER NOT NULL REFERENCES ImmediateTransactions ON DELETE CASCADE" + 
            ");");
        sqlStatements.add(
            "CREATE TABLE Transfers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "inTransId INTEGER NOT NULL REFERENCES Transactions," +
                "outTransId INTEGER NOT NULL REFERENCES Transactions" +
            ");");

        for (String sql : sqlStatements) {
            db.execSQL(sql);
        }*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MoneyNode.onDatabaseUpgrade(db, oldVersion, newVersion);
        Transaction.onDatabaseUpgrade(db, oldVersion, newVersion);
        ImmediateTransaction.onDatabaseUpgrade(db, oldVersion, newVersion);
        ScheduledTransaction.onDatabaseUpgrade(db, oldVersion, newVersion);
        Category.onDatabaseUpgrade(db, oldVersion, newVersion);
    }

    public List<MoneyNode> getMoneyNodes() throws DatabaseException {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(MoneyNode.TABLE_NAME, 
                new String[] {MoneyNode.COL_ID}, 
                null, null, null, null, null, null);

        List<MoneyNode> moneyNodes = new ArrayList<MoneyNode>(cursor.getCount());

        while (cursor.moveToNext()) {
            MoneyNode moneyNode = MoneyNode.createFromId(cursor.getLong(0));
            moneyNode.setDb(db);
            moneyNodes.add(moneyNode);
        }

        cursor.close();

        return moneyNodes;
    }

    public MoneyNode getMoneyNodeWithName(String name) throws DatabaseException {
        SQLiteDatabase db = getReadableDatabase();
        Long id = null;

        Cursor cursor = db.query(MoneyNode.TABLE_NAME, 
                new String[] {MoneyNode.COL_ID}, 
                MoneyNode.COL_NAME + " = ?",
                new String[] {name},
                null, null, null, null);

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            id = cursor.getLong(0);
        }

        cursor.close();

        return MoneyNode.createFromId(id);
    }

    public boolean hasMoneyNodeWithName(String name) throws DatabaseException {
        return getMoneyNodeWithName(name) != null;
    }

    public void deleteMoneyNode(MoneyNode node) throws DatabaseException {
        if (node == null) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase();

        db.delete(MoneyNode.TABLE_NAME, MoneyNode.COL_ID + " = ?",
                new String[]{node.getId().toString()});
    }

    public List<Category> getCategories() throws DatabaseException {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(Category.TABLE_NAME, 
                new String[] {Category.COL_ID}, 
                null, null, null, null, null, null);

        List<Category> categories = new ArrayList<Category>(cursor.getCount());

        while (cursor.moveToNext()) {
            Category category = Category.createFromId(cursor.getLong(0));

            if (category == null) {
                Log.d("TMM", "Found null category");
                continue;
            }

            category.setDb(db);
            categories.add(category);
        }

        cursor.close();

        return categories;
    }

    public Category getCategoryWithName(String name) throws DatabaseException {
        Long id = null;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(Category.TABLE_NAME, 
                new String[] {Category.COL_ID}, 
                Category.COL_NAME + " = ?",
                new String[] {name},
                null, null, null, null);

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            id = cursor.getLong(0);
        }

        cursor.close();

        return Category.createFromId(id);
    }

    public boolean hasCategoryWithName(String name) throws DatabaseException {
        return getCategoryWithName(name) != null;
    }

    public void deleteCategory(Category category) throws DatabaseException {
        if (category == null) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase();

        db.delete(Category.TABLE_NAME, Category.COL_ID + " = ?",
                new String[]{category.getId().toString()});
    }

    /**
     * @return the currentUser
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Deletes a database associated with a particular user
     *
     * @param User user whose database we want to delete.
     */
    static public void deleteDatabase(Context context, User user) {
        context.deleteDatabase(user.getName() + ".db");
    }

    private void userDefinedOrThrow() 
        throws DatabaseUnknownUserException {
        if (currentUser == null) {
            throw new DatabaseUnknownUserException();
        }
    }

    @Override
    public synchronized void close() {
        if (preventClose) return;
        super.close();
    }
}
