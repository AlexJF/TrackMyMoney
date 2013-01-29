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
 * This class represents an immediate transaction of the application.
 */
public class ImmediateTransaction extends Transaction {
    // Database tables
    public static final String TABLE_NAME = "ImmediateTransactions";

    // Table Columns
    public static final String COL_ID = "id";
    public static final String COL_EXECUTIONDATE = "executionDate";

    // Queries
    public static final String QUERY_CREATETABLE = 
        "CREATE TABLE " + TABLE_NAME + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + 
                "REFERENCES " + Transaction.TABLE_NAME + "," +
            COL_EXECUTIONDATE + " DATETIME " + 
                "DEFAULT (DATETIME('now', 'localtime'))" +
        ");";

    // Database maintenance
    /**
     * Creates schemas associated with an ImmediateTransaction domain.
     *
     * @param db Database where to create the schemas.
     */
    public static void onDatabaseCreation(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATETABLE);
    }

    /**
     * Updates schemas associated with an ImmediateTransaction domain.
     *
     * @param db Database where to update the schemas.
     * @param oldVersion The old version of the schemas.
     * @param newVersion The new version of the schemas.
     */
    public static void onDatabaseUpgrade(SQLiteDatabase db, int oldVersion, 
                                        int newVersion) {
    }

    // Caching
    private static Cache<Long, ImmediateTransaction> cache = 
        new Cache<Long, ImmediateTransaction>();

    /**
     * Gets an instance of ImmediateTransaction with the specified id.
     *
     * The instance is reused from the cache or created if it doesn't exist on
     * the cache yet.
     *
     * @param id The id of the transaction we want.
     * @return ImmediateTransaction instance with specified id.
     */
    public static ImmediateTransaction createFromId(Long id) {
        ImmediateTransaction trans = cache.get(id);

        if (trans == null) {
            trans = new ImmediateTransaction(id);
            cache.put(id, trans);
        }

        return trans;
    }

    // Private fields
    private Date executionDate;

    public ImmediateTransaction(Long id) {
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
    public ImmediateTransaction(MoneyNode moneyNode, BigDecimal value, 
            String description, Category category, Date executionDate) {
        super(moneyNode, value, description, category);
        this.executionDate = executionDate;
    }

    @Override
    protected void internalLoad() throws DbObjectLoadException {
        Cursor cursor = getDb().query(TABLE_NAME, null, COL_ID + " = ?", 
                new String[] {getId().toString()}, null, null, null, null);
        
        try {
            if (cursor.moveToFirst()) {
                executionDate = new Date(cursor.getLong(1));
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
            setId(super.internalSave());

            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_ID, getId());
            contentValues.put(COL_EXECUTIONDATE, executionDate.getTime());

            long result = db.insertWithOnConflict(TABLE_NAME, null, 
                    contentValues, SQLiteDatabase.CONFLICT_REPLACE);

            if (result > 0) {
                db.setTransactionSuccessful();
                return getId();
            } else {
                throw new DbObjectSaveException("Couldn't save immediate " +
                        "transaction data associated with id " + getId());
            }
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

    public static final Parcelable.Creator<ImmediateTransaction> CREATOR =
        new Parcelable.Creator<ImmediateTransaction>() {
            public ImmediateTransaction createFromParcel(Parcel in) {
                Long id = in.readLong();
                return cache.get(id);
            }
 
            public ImmediateTransaction[] newArray(int size) {
                return new ImmediateTransaction[size];
            }
        };

}
