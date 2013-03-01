/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.domain;

import java.math.BigDecimal;
import java.util.Date;

import net.alexjf.tmm.exceptions.DbObjectLoadException;
import net.alexjf.tmm.exceptions.DbObjectSaveException;
import net.alexjf.tmm.utils.Cache;
import net.sqlcipher.database.SQLiteDatabase;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class represents a scheduled transaction of the application.
 */
public class ScheduledTransaction extends Transaction {
    // Database tables
    public static final String TABLE_NAME = "ScheduledTransactions";

    // Table Columns
    public static final String COL_ID = "id";
    public static final String COL_SCHEDULEDDATE = "scheduledDate";
    public static final String COL_RECURRENCE = "recurrence";

    // Database maintenance
    /**
     * Creates schemas associated with a ScheduledTransaction domain.
     *
     * @param db Database where to create the schemas.
     */
    public static void onDatabaseCreation(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + 
                    "REFERENCES " + Transaction.TABLE_NAME + " ON DELETE CASCADE," +
                COL_SCHEDULEDDATE + " DATETIME " + 
                    "DEFAULT (DATETIME('now', 'localtime'))," +
                COL_RECURRENCE + " TEXT" + 
            ");");
    }

    /**
     * Updates schemas associated with a ScheduledTransaction domain.
     *
     * @param db Database where to update the schemas.
     * @param oldVersion The old version of the schemas.
     * @param newVersion The new version of the schemas.
     */
    public static void onDatabaseUpgrade(SQLiteDatabase db, int oldVersion, 
                                        int newVersion) {
    }

    // Caching
    private static Cache<Long, ScheduledTransaction> cache = 
        new Cache<Long, ScheduledTransaction>();

    /**
     * Gets an instance of ScheduledTransaction with the specified id.
     *
     * The instance is reused from the cache or created if it doesn't exist on
     * the cache yet.
     *
     * @param id The id of the transaction we want.
     * @return ScheduledTransaction instance with specified id.
     */
    public static ScheduledTransaction createFromId(Long id) {
        if (id == null) {
            return null;
        }

        ScheduledTransaction trans = cache.get(id);

        if (trans == null) {
            trans = new ScheduledTransaction(id);
            cache.put(id, trans);
        }

        return trans;
    }

    // Private fields
    private Date scheduledDate;
    // TODO: Change to Recurrence object
    private String recurrence;

    public ScheduledTransaction(Long id) {
        super(id);
    }

    /**
     * Constructs a new instance.
     *
     * @param moneyNode The money node associated with this transaction.
     * @param value The value of this transaction.
     * @param description The description of this transaction.
     * @param category The category of this transaction.
     * @param executionDate The executionDate for this instance.
     */
    public ScheduledTransaction(MoneyNode moneyNode, BigDecimal value, 
            String description, Category category, Date scheduledDate,
            String recurrence) {
        super(moneyNode, value, description, category);
        this.scheduledDate = scheduledDate;
        this.recurrence = recurrence;
    }

    @Override
    protected void internalLoad() throws DbObjectLoadException {
        Cursor cursor = getDb().query(TABLE_NAME, null, COL_ID + " = ?", 
                new String[] {getId().toString()}, null, null, null, null);
        
        try {
            if (cursor.moveToFirst()) {
                scheduledDate = new Date(cursor.getLong(1));
            } else {
                throw new DbObjectLoadException("Couldn't find immediate transaction " +
                        "associated with id "+ getId());
            }
        } finally {
            cursor.close();
        }

        super.internalLoad();
    }

    @Override
    protected long internalSave() throws DbObjectSaveException {
        SQLiteDatabase db = getDb();
        try {
            db.beginTransaction();

            Long existingId = getId();
            Long id = super.internalSave();

            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_ID, id);
            contentValues.put(COL_SCHEDULEDDATE, scheduledDate.getTime());

            long result;

            if (existingId != null) {
                result = getDb().update(TABLE_NAME, contentValues, 
                         COL_ID + " = ?", new String[] {id.toString()});
                result = (result == 0 ? -1 : id);
            } else {
                result = getDb().insert(TABLE_NAME, null, contentValues);
            }

            if (result > 0) {
                db.setTransactionSuccessful();
                return id;
            } else {
                throw new DbObjectSaveException("Couldn't save scheduled " +
                        "transaction data associated with id " + getId());
            }
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Gets the scheduledDate for this instance.
     *
     * @return The scheduledDate.
     */
    public Date getScheduledDate() {
        return this.scheduledDate;
    }

    /**
     * Sets the scheduledDate for this instance.
     *
     * @param scheduledDate The scheduledDate.
     */
    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
        setChanged(true);
    }

    /**
     * Sets the recurrence for this instance.
     *
     * @param recurrence The recurrence.
     */
    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
        setChanged(true);
    }

    /**
     * Gets the recurrence for this instance.
     *
     * @return The recurrence.
     */
    public String getRecurrence() {
        return this.recurrence;
    }

    public ScheduledTransaction clone() throws CloneNotSupportedException {
        return (ScheduledTransaction) super.clone();
    }

    public static final Parcelable.Creator<ScheduledTransaction> CREATOR =
        new Parcelable.Creator<ScheduledTransaction>() {
            public ScheduledTransaction createFromParcel(Parcel in) {
                Long id = in.readLong();
                return createFromId(id);
            }
 
            public ScheduledTransaction[] newArray(int size) {
                return new ScheduledTransaction[size];
            }
        };

}
