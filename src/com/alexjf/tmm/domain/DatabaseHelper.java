package com.alexjf.tmm.domain;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
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
        List<String> sqlStatements = new LinkedList<String>();
        sqlStatements.add(
            "CREATE TABLE MoneyNodes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "description TEXT," +
                "location TEXT," + 
                "icon BLOB," + 
                "currency TEXT," + 
                "creationDate DATETIME DEFAULT (DATETIME('now', 'localtime'))," +
                "initialBalance DECIMAL DEFAULT 0" +
            ");");
        sqlStatements.add(
            "CREATE TABLE Transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "value DECIMAL NOT NULL," +
                "description TEXT" +
            ");");
        sqlStatements.add(
            "CREATE TABLE ImmediateTransactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT REFERENCES Transactions ON DELETE CASCADE," +
                "executionDate DATETIME NOT NULL DEFAULT (DATETIME('now', 'localtime'))" +
            ");");
        sqlStatements.add(
            "CREATE TABLE ScheduledTransactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT REFERENCES Transactions ON DELETE CASCADE," +
                "scheduledDate DATETIME NOT NULL," +
                "recurrence TEXT" + 
            ");");
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
        }
    }

	@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
