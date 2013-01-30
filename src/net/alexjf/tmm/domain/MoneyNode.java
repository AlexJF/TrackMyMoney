/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
 * This class represents a single user of the application.
 */
public class MoneyNode extends DatabaseObject {
    // Intent keys
    public static final String EXTRA_CURRENTMONEYNODE = "curMoneyNode";

    // Database tables
    public static final String TABLE_NAME = "MoneyNodes";

    // Table columns
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_ICON = "icon";
    public static final String COL_CURRENCY = "currency";
    public static final String COL_CREATIONDATE = "creationDate";
    public static final String COL_INITIALBALANCE = "initialBalance";

    // Queries
    private static final String QUERY_CREATETABLE = 
        "CREATE TABLE " + TABLE_NAME + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_NAME + " TEXT NOT NULL," +
            COL_DESCRIPTION + " TEXT," +
            COL_ICON + " TEXT," + 
            COL_CURRENCY + " TEXT," + 
            COL_CREATIONDATE + " DATETIME " + 
                "DEFAULT (DATETIME('now', 'localtime'))," +
            COL_INITIALBALANCE + " NUMERIC DEFAULT 0" +
        ");";
    private static final String QUERY_SINGLE = 
        "SELECT * FROM " + TABLE_NAME +
        " WHERE " + COL_ID + " = ?";
    private static final String QUERY_BALANCE = 
        "SELECT TOTAL(" + Transaction.COL_VALUE + ") " + 
        " FROM " + Transaction.TABLE_NAME +
        "  INNER JOIN " + ImmediateTransaction.TABLE_NAME + " USING (id) " +
        " WHERE " + Transaction.COL_MONEYNODEID + " = ?";
    private static final String QUERY_IMMEDIATETRANSACTIONS =
        "SELECT " + ImmediateTransaction.COL_ID + 
        " FROM " + Transaction.TABLE_NAME +
        "  INNER JOIN " + ImmediateTransaction.TABLE_NAME + " USING (id) " +
        " WHERE " + Transaction.COL_MONEYNODEID + " = ?";
    private static final String QUERY_SCHEDULEDTRANSACTIONS =
        "SELECT " + ScheduledTransaction.COL_ID + 
        " FROM " + Transaction.TABLE_NAME +
        "  INNER JOIN " + ScheduledTransaction.TABLE_NAME + " USING (id) " +
        " WHERE " + Transaction.COL_MONEYNODEID + " = ?";

    // Database maintenance
    /**
     * Creates schemas associated with a MoneyNode domain.
     *
     * @param db Database where to create the schemas.
     */
    public static void onDatabaseCreation(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATETABLE);
    }

    /**
     * Updates schemas associated with a Category domain.
     *
     * @param db Database where to update the schemas.
     * @param oldVersion The old version of the schemas.
     * @param newVersion The new version of the schemas.
     */
    public static void onDatabaseUpgrade(SQLiteDatabase db, int oldVersion, 
                                        int newVersion) {
    }

    // Caching
    private static Cache<Long, MoneyNode> cache = new Cache<Long, MoneyNode>();

    /**
     * Gets an instance of MoneyNode with the specified id.
     *
     * The instance is reused from the cache or created if it doesn't exist on
     * the cache yet.
     *
     * @param id The id of the moneynode we want.
     * @return MoneyNode instance with specified id.
     */
    public static MoneyNode createFromId(Long id) {
        MoneyNode node = cache.get(id);

        if (node == null) {
            node = new MoneyNode(id);
            cache.put(id, node);
        }

        return node;
    }

    // Private members
    private String name;
    private String description;
    private String icon;
    private String currency;
    private Date creationDate;
    private BigDecimal initialBalance;
    private BigDecimal balance;

    private MoneyNode(Long id) {
        setId(id);
    }

    /**
     * Constructs a new instance.
     *
     * @param name The name for this instance.
     * @param description The description for this instance.
     * @param icon The icon drawable entry name for this instance.
     * @param creationDate The creationDate for this instance.
     * @param initialBalance The initialBalance for this instance.
     * @param currency The currency for this instance.
     */
    public MoneyNode(String name, String description,
            String icon, Date creationDate, BigDecimal initialBalance, 
            String currency) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.currency = currency;
        this.creationDate = creationDate;
        this.initialBalance = initialBalance;
        this.balance = initialBalance;
        setChanged(true);
    }

    @Override
    protected void internalLoad() throws DbObjectLoadException {
        Cursor cursor = getDb().rawQuery(QUERY_SINGLE, 
                new String[] {getId().toString()});
        
        if (cursor.moveToNext()) {
            name = cursor.getString(1);
            description = cursor.getString(2);
            icon = cursor.getString(3);
            currency = cursor.getString(4);
            creationDate = new Date(cursor.getLong(5));
            // TODO: Change this to take into account transactions
            // and cache the value
            initialBalance = new BigDecimal(cursor.getString(6));
        } else {
            throw new DbObjectLoadException("Couldn't find money node" + 
                    "associated with id " + getId());
        }

        cursor.close();
        cursor = getDb().rawQuery(QUERY_BALANCE, 
                new String[] {getId().toString()});

        if (cursor.moveToNext()) {
            balance = initialBalance.add(new BigDecimal(cursor.getString(0)));
        } else {
            balance = new BigDecimal(0);
        }
        cursor.close();
    }

    @Override
    protected long internalSave() throws DbObjectSaveException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ID, getId());
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_DESCRIPTION, description);
        contentValues.put(COL_ICON, icon);
        contentValues.put(COL_CURRENCY, currency);
        contentValues.put(COL_CREATIONDATE, creationDate.getTime());
        contentValues.put(COL_INITIALBALANCE, initialBalance.toString());

        long result = getDb().insertWithOnConflict(TABLE_NAME, null, 
                contentValues, SQLiteDatabase.CONFLICT_REPLACE);

        if (result >= 0) {
            cache.put(result, this);
            return result;
        } else {
            throw new DbObjectSaveException("Error saving moneynode to database");
        }
    }

    /**
     * Sets the name for this instance.
     *
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
        setChanged(true);
    }

    /**
     * Sets the description for this instance.
     *
     * @param description The description.
     */
    public void setDescription(String description) {
        this.description = description;
        setChanged(true);
    }

    /**
     * Sets the icon drawable entry name for this instance.
     *
     * @param icon The icon drawable entry name.
     */
    public void setLocation(String icon) {
        this.icon = icon;
        setChanged(true);
    }

    /**
     * Sets the currency for this instance.
     *
     * @param currency The currency.
     */
    public void setCurrency(String currency) {
        this.currency = currency;
        setChanged(true);
    }

    /**
     * Sets the creationDate for this instance.
     *
     * @param creationDate The creationDate.
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
        setChanged(true);
    }

    /**
     * Sets the initialBalance for this instance.
     *
     * @param initialBalance The initialBalance.
     */
    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
        setChanged(true);
    }

    /**
     * Sets the balance for this instance.
     *
     * @param balance The initialBalance.
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
        setChanged(true);
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
     * Gets the description for this instance.
     *
     * @return The description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the icon drawable entry name for this instance.
     *
     * @return The location.
     */
    public String getIcon() {
        return this.icon;
    }

    /**
     * Gets the currency for this instance.
     *
     * @return The currency.
     */
    public String getCurrency() {
        return this.currency;
    }

    /**
     * Gets the creationDate for this instance.
     *
     * @return The creationDate.
     */
    public Date getCreationDate() {
        return this.creationDate;
    }

    /**
     * Gets the initialBalance for this instance.
     *
     * @return The initialBalance.
     */
    public BigDecimal getInitialBalance() {
        return this.initialBalance;
    }

    /**
     * Gets the balance for this instance.
     *
     * @return The balance.
     */
    public BigDecimal getBalance() {
        return this.balance;
    }

    public List<ImmediateTransaction> getImmediateTransactions() 
        throws DatabaseException {
        dbReadyOrThrow();

        SQLiteDatabase db = getDb();

        List<ImmediateTransaction> immediateTransactions = 
            new LinkedList<ImmediateTransaction>();

        Cursor cursor = db.rawQuery(QUERY_IMMEDIATETRANSACTIONS, 
                new String[] {getId().toString()});

        while (cursor.moveToNext()) {
            ImmediateTransaction immediateTransaction = 
                new ImmediateTransaction(cursor.getLong(0));
            immediateTransaction.setDb(db);
            immediateTransaction.setMoneyNode(this);
            immediateTransactions.add(immediateTransaction);
        }

        cursor.close();

        return immediateTransactions;
    }

    public List<ScheduledTransaction> getScheduledTransactions() 
        throws DatabaseException {
        dbReadyOrThrow();

        SQLiteDatabase db = getDb();

        List<ScheduledTransaction> immediateTransactions = 
            new LinkedList<ScheduledTransaction>();

        Cursor cursor = db.rawQuery(QUERY_SCHEDULEDTRANSACTIONS, 
                new String[] {getId().toString()});

        while (cursor.moveToNext()) {
            ScheduledTransaction immediateTransaction = 
                new ScheduledTransaction(cursor.getLong(0));
            immediateTransaction.setDb(db);
            immediateTransaction.setMoneyNode(this);
            immediateTransactions.add(immediateTransaction);
        }
        
        cursor.close();

        return immediateTransactions;
    }

    public String toString() {
        return getName();
    }

    public static final Parcelable.Creator<MoneyNode> CREATOR =
        new Parcelable.Creator<MoneyNode>() {
            public MoneyNode createFromParcel(Parcel in) {
                Long id = in.readLong();
                return cache.get(id);
            }
 
            public MoneyNode[] newArray(int size) {
                return new MoneyNode[size];
            }
        };
}
