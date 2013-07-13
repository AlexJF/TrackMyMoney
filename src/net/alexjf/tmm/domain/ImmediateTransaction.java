/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.domain;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;

import net.alexjf.tmm.exceptions.DatabaseException;
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
    public static final String KEY_TRANSACTION = "immediateTransaction";

    // Database tables
    public static final String TABLE_NAME = "ImmediateTransactions";

    // Table Columns
    public static final String COL_ID = "id";
    public static final String COL_EXECUTIONDATE = "executionDate";
    public static final String COL_TRANSFERTRANSID = "transferTransactionId";

    // Schema
    public static final String SCHEMA_CREATETABLE = 
        "CREATE TABLE " + TABLE_NAME + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + 
                "REFERENCES " + Transaction.TABLE_NAME + " ON DELETE CASCADE," +
            COL_EXECUTIONDATE + " DATETIME " + 
                "DEFAULT (DATETIME('now', 'localtime'))," +
            COL_TRANSFERTRANSID + " INTEGER " + 
                "REFERENCES " + Transaction.TABLE_NAME + " ON DELETE CASCADE" +
        ");";
    public static final String SCHEMA_ADDTRANSFERTRANS = 
        "ALTER TABLE " + TABLE_NAME + "ADD COLUMN " +
            COL_TRANSFERTRANSID + " INTEGER " + 
                "REFERENCES " + Transaction.TABLE_NAME + " ON DELETE CASCADE";

    // Database maintenance
    /**
     * Creates schemas associated with an ImmediateTransaction domain.
     *
     * @param db Database where to create the schemas.
     */
    public static void onDatabaseCreation(SQLiteDatabase db) {
        db.execSQL(SCHEMA_CREATETABLE);
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
        switch (oldVersion) {
            case 0:
                db.execSQL(SCHEMA_ADDTRANSFERTRANS);
                break;
        }
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
        if (id == null) {
            return null;
        }

        ImmediateTransaction trans = cache.get(id);

        if (trans == null) {
            trans = new ImmediateTransaction(id);
            cache.put(id, trans);
        }

        return trans;
    }

    // Private fields
    private Date executionDate;
    private ImmediateTransaction transferTransaction;
    private BigDecimal deltaValueFromPrevious;
    private BigDecimal valueOnDatabase;

    private ImmediateTransaction(Long id) {
        super(id);
        deltaValueFromPrevious = BigDecimal.valueOf(0);
        valueOnDatabase = BigDecimal.valueOf(0);
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
        this.transferTransaction = null;
        deltaValueFromPrevious = value;
        valueOnDatabase = BigDecimal.valueOf(0);
    }

    /**
     * Creates a copy of the transaction passed as argument.
     *
     * The created copy is a shallow copy. However, the only mutable objects
     * are the moneynode and the category of which there should only be one
     * instance anyway.
     *
     * @param original The transaction from which to copy from.
     */
    public ImmediateTransaction(ImmediateTransaction original) {
        this(original.getMoneyNode(), original.getValue(),
             original.getDescription(), original.getCategory(),
             original.getExecutionDate());
    }

    /**
     * Constructs a new instance symmetric to the provided one.
     *
     * This is used for creating an opposite transaction in a transfer.
     *
     * @param transferTransaction An existing transaction representing
     * one side of a transfer.
     * @param moneyNode The money node to which this new transaction
     * will be associated.
     */
    public ImmediateTransaction(ImmediateTransaction transferTransaction,
            MoneyNode moneyNode) {
        this(transferTransaction);
        this.setValue(this.getValue().multiply(new BigDecimal(-1)));
        this.setMoneyNode(moneyNode);
        this.transferTransaction = transferTransaction;
    }

    @Override
    protected void internalLoad() throws DbObjectLoadException {
        Cursor cursor = getDb().query(TABLE_NAME, null, COL_ID + " = ?", 
                new String[] {getId().toString()}, null, null, null, null);
        
        try {
            if (cursor.moveToFirst()) {
                executionDate = new Date(cursor.getLong(1));
                long transferTransId = cursor.getLong(2);

                if (transferTransaction == null || 
                    !transferTransaction.getId().equals(transferTransId)) {
                    transferTransaction = ImmediateTransaction.createFromId(
                            transferTransId);
                }
            } else {
                throw new DbObjectLoadException("Couldn't find immediate transaction " +
                        "associated with id "+ getId());
            }
        } finally {
            cursor.close();
        }

        super.internalLoad();
        valueOnDatabase = getValue();
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
            contentValues.put(COL_EXECUTIONDATE, executionDate.getTime());

            if (transferTransaction != null) {
                contentValues.put(COL_TRANSFERTRANSID, 
                        transferTransaction.getId());
            } else {
                contentValues.put(COL_TRANSFERTRANSID, (String) null);
            }

            long result;

            if (existingId != null) {
                result = getDb().update(TABLE_NAME, contentValues, 
                         COL_ID + " = ?", new String[] {id.toString()});
                result = (result == 0 ? -1 : id);
            } else {
                result = getDb().insert(TABLE_NAME, null, contentValues);
            }

            if (result >= 0) {
                db.setTransactionSuccessful();
                getMoneyNode().notifyBalanceChange(deltaValueFromPrevious);
                deltaValueFromPrevious = BigDecimal.valueOf(0);
                return id;
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
     * Gets the other transaction involved in a transfer with this one.
     *
     * @return An opposite ImmediateTransaction or null if not a transfer.
     */
    public ImmediateTransaction getTransferTransaction() {
        return this.transferTransaction;
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

    /**
     * Sets the other transaction involved in a transfer with this one.
     *
     * @param transferTransaction The opposite transaction in the transfer.
     */
    public void setTransferTransaction(
            ImmediateTransaction transferTransaction) {
        this.transferTransaction = transferTransaction;
        this.setLoaded(true);
    }

    public static final Parcelable.Creator<ImmediateTransaction> CREATOR =
        new Parcelable.Creator<ImmediateTransaction>() {
            public ImmediateTransaction createFromParcel(Parcel in) {
                Long id = in.readLong();
                return createFromId(id);
            }
 
            public ImmediateTransaction[] newArray(int size) {
                return new ImmediateTransaction[size];
            }
        };

    @Override
    public void setValue(BigDecimal value) {
        super.setValue(value);
        deltaValueFromPrevious = valueOnDatabase.subtract(value);
    }

    public ImmediateTransaction clone() throws CloneNotSupportedException {
        return (ImmediateTransaction) super.clone();
    }

    public static class DateComparator implements Comparator<ImmediateTransaction> {
        private boolean descending = false;

        public DateComparator(boolean descending) {
            this.descending = descending;
        }

        public int compare(ImmediateTransaction lhs, ImmediateTransaction rhs) {
            try {
                lhs.load(); rhs.load();
            } catch (DatabaseException e) {
                return 1;
            }
            int result = lhs.getExecutionDate().compareTo(rhs.getExecutionDate());
            if (descending) {
                result *= -1;
            }

            return result;
        }
    }
}
