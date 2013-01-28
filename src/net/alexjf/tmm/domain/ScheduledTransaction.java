package net.alexjf.tmm.domain;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.alexjf.tmm.exceptions.DbObjectLoadException;
import net.alexjf.tmm.exceptions.DbObjectSaveException;
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

    private Date scheduledDate;
    // TODO: Change to Recurrence object
    private String recurrence;

    public static void onDatabaseCreation(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + 
                    "REFERENCES " + Transaction.TABLE_NAME + "," +
                COL_SCHEDULEDDATE + " DATETIME " + 
                    "DEFAULT (DATETIME('now', 'localtime'))," +
                COL_RECURRENCE + " TEXT" + 
            ");");
    }

    public static void onDatabaseUpgrade(SQLiteDatabase db, int oldVersion, 
                                        int newVersion) {
    }

    public ScheduledTransaction(Long id) {
        super(id);
    }

    public ScheduledTransaction(Parcel in) {
        super(in);
        readFromParcel(in);
    }

    /**
     * Constructs a new instance.
     *
     * @param moneyNode The money node associated with this transaction.
     * @param value The value of this transaction.
     * @param description The description of this transaction.
     * @param categoryId The categoryId of this transaction.
     * @param executionDate The executionDate for this instance.
     */
    public ScheduledTransaction(MoneyNode moneyNode, BigDecimal value, 
            String description, Long categoryId, Date scheduledDate,
            String recurrence) {
        super(moneyNode, value, description, categoryId);
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
            setId(super.internalSave());

            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_ID, getId());
            contentValues.put(COL_SCHEDULEDDATE, scheduledDate.getTime());

            long result = db.insertWithOnConflict(TABLE_NAME, null, 
                    contentValues, SQLiteDatabase.CONFLICT_REPLACE);

            if (result > 0) {
                db.setTransactionSuccessful();
                return getId();
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

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        try {
            scheduledDate = (new SimpleDateFormat()).parse(in.readString());
        } catch (ParseException e) {
            scheduledDate = new Date();
        }
        recurrence = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(scheduledDate.toString());
        out.writeString(recurrence);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ScheduledTransaction> CREATOR =
        new Parcelable.Creator<ScheduledTransaction>() {
            public ScheduledTransaction createFromParcel(Parcel in) {
                return new ScheduledTransaction(in);
            }
 
            public ScheduledTransaction[] newArray(int size) {
                return new ScheduledTransaction[size];
            }
        };
}
