/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.domain;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;

import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;
import net.sqlcipher.database.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    
    private String password;

    public DatabaseHelper(Context context, User user) {
        super(context, user.getName() + ".db", null, DATABASE_VERSION);
        this.password = user.getPassword();
    }

    @Override
    public SQLiteDatabase getReadableDatabase(String password) {
        this.password = password;
        return super.getReadableDatabase(password);
    }

    @Override
    public SQLiteDatabase getWritableDatabase(String password) {
        this.password = password;
        return super.getWritableDatabase(password);
    }

    public SQLiteDatabase getReadableDatabase() {
        return getReadableDatabase(password);
    }

    public SQLiteDatabase getWritableDatabase() {
        return getWritableDatabase(password);
    }

    public boolean login(String password) {
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase(password);
            db.query("sqlite_master", new String[] { "count(*)" }, null, null, 
                    null, null, null, null);
            return true;
        } catch (SQLiteException e) {
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
    }

    public List<MoneyNode> getMoneyNodes() throws Exception {
        SQLiteDatabase db = getReadableDatabase();

        List<MoneyNode> moneyNodes = new LinkedList<MoneyNode>();

        Cursor cursor = db.query(MoneyNode.TABLE_NAME, 
                new String[] {MoneyNode.COL_ID}, 
                null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            MoneyNode moneyNode = new MoneyNode(cursor.getLong(0));
            moneyNode.setDb(db);
            moneyNodes.add(moneyNode);
        }

        cursor.close();

        return moneyNodes;
    }

    /**
     * Sets the password for this instance.
     *
     * @param password The password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
