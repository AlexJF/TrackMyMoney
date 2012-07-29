package net.alexjf.tmm.domain;

import java.math.BigDecimal;

import java.util.Date;

import android.content.ContentValues;

import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

/**
 * This class represents an immediate transaction of the application.
 */
public abstract class ImmediateTransaction extends Transaction {
    // Database tables
    public static final String TABLE_NAME = "ImmediateTransactions";

    // Table Columns
    public static final String COL_ID = "id";
    public static final String COL_EXECUTIONDATE = "executionDate";

    private Date executionDate;

    public static void onDatabaseCreation(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + 
                    "REFERENCES " + Transaction.TABLE_NAME + "," +
                COL_EXECUTIONDATE + " DATETIME " + 
                    "DEFAULT (DATETIME('now', 'localtime'))," +
            ");");
    }

    public static void onDatabaseUpgrade(SQLiteDatabase db, int oldVersion, 
                                        int newVersion) {
    }

    public ImmediateTransaction(Long id) {
        super(id);
    }

    @Override
    protected void internalLoad(SQLiteDatabase db) throws Exception {
        if (getId() == null) {
            throw new Exception("Id not specified");
        }

        Cursor cursor = db.query(TABLE_NAME, null, COL_ID + " = ?", 
                new String[] {getId().toString()}, null, null, null, null);
        
        if (cursor.moveToFirst()) {
            executionDate = new Date(cursor.getLong(1));
        } else {
            throw new Exception("Couldn't find immediate transaction " +
                    "associated with id "+ getId());
        }

        super.internalLoad(db);
    }

    @Override
    protected void internalSave(SQLiteDatabase db) throws Exception {
        try {
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_EXECUTIONDATE, executionDate.getTime());

            db.insertWithOnConflict(TABLE_NAME, null, contentValues, 
                    SQLiteDatabase.CONFLICT_REPLACE);

            super.internalSave(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Gets the executionDate for this instance.
     *
     * @return The executionDate.
     */
    public Date getExecutionDate() {
        return this.executionDate;
    }

    /**
     * Sets the executionDate for this instance.
     *
     * @param executionDate The executionDate.
     */
    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
        setChanged(true);
    }
}
