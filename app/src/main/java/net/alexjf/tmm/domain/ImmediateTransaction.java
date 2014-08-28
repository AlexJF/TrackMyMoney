/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.domain;

import java.util.Comparator;
import java.util.Date;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.exceptions.DbObjectLoadException;
import net.alexjf.tmm.exceptions.DbObjectSaveException;
import net.alexjf.tmm.utils.Cache;
import net.alexjf.tmm.utils.CacheFactory;
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
    private static final String SCHEMA_CREATETABLE =
        "CREATE TABLE " + TABLE_NAME + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " +
                "REFERENCES " + Transaction.TABLE_NAME + " ON DELETE CASCADE," +
            COL_EXECUTIONDATE + " DATETIME " +
                "DEFAULT (DATETIME('now', 'localtime'))," +
            COL_TRANSFERTRANSID + " INTEGER " +
                "REFERENCES " + Transaction.TABLE_NAME + " ON DELETE SET NULL" +
        ");";
    private static final String SCHEMA_TRANSFER_TO_TMP =
    	"INSERT INTO " + TABLE_NAME + "_tmp SELECT * FROM " + TABLE_NAME + ";";
    private static final String SCHEMA_FK_DISABLE =
    	"PRAGMA foreign_keys=OFF;";
    private static final String SCHEMA_DROP_TABLE =
    	"DROP TABLE " + TABLE_NAME + ";";
    private static final String SCHEMA_TMP_TO_NEW =
    	"ALTER TABLE " + TABLE_NAME + "_tmp RENAME TO " + TABLE_NAME + ";";
    private static final String SCHEMA_FK_CHECK =
    	"PRAGMA foreign_key_check;";
    private static final String SCHEMA_FK_ENABLE =
    	"PRAGMA foreign_keys=ON;";

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
            case 1:
            case 2:
            	db.execSQL(SCHEMA_CREATETABLE.replaceFirst(TABLE_NAME, TABLE_NAME + "_tmp"));
            	db.execSQL(SCHEMA_TRANSFER_TO_TMP);
            	db.execSQL(SCHEMA_FK_DISABLE);
            	db.execSQL(SCHEMA_DROP_TABLE);
            	db.execSQL(SCHEMA_TMP_TO_NEW);
            	db.execSQL(SCHEMA_FK_CHECK);
            	db.execSQL(SCHEMA_FK_ENABLE);
                break;
        }
    }

    // Caching
    private static Cache<Long, ImmediateTransaction> cache =
    		CacheFactory.getInstance().getCache("ImmediateTransaction");

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
    private Money deltaValueFromPrevious;
    private Money valueOnDatabase;
    private MoneyNode moneyNodeOnDatabase;

    private ImmediateTransaction(Long id) {
        super(id);
        deltaValueFromPrevious = Money.zero(CurrencyUnit.EUR);
        valueOnDatabase = Money.zero(CurrencyUnit.EUR);
        moneyNodeOnDatabase = null;
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
    public ImmediateTransaction(MoneyNode moneyNode, Money value,
            String description, Category category, Date executionDate) {
        super(moneyNode, value, description, category);
        this.executionDate = executionDate;
        this.transferTransaction = null;
        deltaValueFromPrevious = value;
        valueOnDatabase = Money.zero(value.getCurrencyUnit());
        moneyNodeOnDatabase = null;
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
        this.setValue(this.getValue().negated());
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

                if (!cursor.isNull(2)) {
                    long transferTransId = cursor.getLong(2);

                    if (transferTransaction == null ||
                        !transferTransaction.getId().equals(transferTransId)) {
                        transferTransaction = ImmediateTransaction.
                            createFromId(transferTransId);
                    }
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
        moneyNodeOnDatabase = getMoneyNode();
        deltaValueFromPrevious = Money.zero(moneyNodeOnDatabase.getCurrency());
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

                // If this transaction was just now added, signal that
                // the opposing transfer transaction has changed cause it
                // now has access to the id of this transaction.
                if (transferTransaction != null) {
                    transferTransaction.setChanged(true);
                }

                cache.put(result, this);
            }

            if (result >= 0) {
                db.setTransactionSuccessful();

                // If value changed since last save, update balance
                // caches on the money nodes.
                if (!deltaValueFromPrevious.isZero()) {
                    MoneyNode currentMoneyNode = getMoneyNode();

                    // If transaction was assigned to a different
                    // money node
                    if (moneyNodeOnDatabase != null &&
                        moneyNodeOnDatabase != currentMoneyNode) {

                        // Remove previous value from that money node's
                        // balance cache.
                        moneyNodeOnDatabase.notifyBalanceChange(
                            valueOnDatabase.negated());

                        // Add entire new value to new money node's
                        // balance cache.
                        currentMoneyNode.notifyBalanceChange(
                            getValue());
                    }
                    // If new transaction or same money node, only
                    // update delta.
                    else {
                        currentMoneyNode.notifyBalanceChange(
                            deltaValueFromPrevious);
                    }

                    deltaValueFromPrevious = Money.zero(currentMoneyNode.getCurrency());
                    valueOnDatabase = getValue();
                    moneyNodeOnDatabase = getMoneyNode();
                }

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
        setChanged(true);
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
    public void setValue(Money value) {
        super.setValue(value);
        deltaValueFromPrevious = value.minus(valueOnDatabase);
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

            if (result == 0) {
                result = lhs.getId().compareTo(rhs.getId());
            }

            if (descending) {
                result *= -1;
            }

            return result;
        }
    }
}
