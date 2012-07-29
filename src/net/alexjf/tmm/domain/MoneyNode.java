package net.alexjf.tmm.domain;

import java.math.BigDecimal;

import java.util.Date;


import android.content.ContentValues;

import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

/**
 * This class represents a single user of the application.
 */
public class MoneyNode extends DatabaseObject {
    // Database tables
    public static final String TABLE_NAME = "MoneyNodes";

    // Table Columns
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_LOCATION = "location";
    public static final String COL_ICON = "icon";
    public static final String COL_CURRENCY = "currency";
    public static final String COL_CREATIONDATE = "creationDate";
    public static final String COL_INITIALBALANCE = "initialBalance";

    private Long id;
    private String name;
    private String description;
    private String location;
    private String currency;
    private Date creationDate;
    private BigDecimal initialBalance;

    private BigDecimal balance;

    public static void onDatabaseCreation(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_NAME + " TEXT NOT NULL," +
                COL_DESCRIPTION + " TEXT," +
                COL_LOCATION + " TEXT," + 
                COL_CURRENCY + " TEXT," + 
                COL_CREATIONDATE + " DATETIME " + 
                    "DEFAULT (DATETIME('now', 'localtime'))," +
                COL_INITIALBALANCE + " TEXT DEFAULT 0" +
            ");");
    }

    public static void onDatabaseUpgrade(SQLiteDatabase db, int oldVersion, 
                                        int newVersion) {
    }

    public MoneyNode(Long id) {
        this.id = id;
    }

    /**
     * Constructs a new instance.
     *
     * @param id The id for this instance.
     * @param name The name for this instance.
     * @param description The description for this instance.
     * @param location The location for this instance.
     * @param currency The currency for this instance.
     * @param creationDate The creationDate for this instance.
     * @param initialBalance The initialBalance for this instance.
     */
    public MoneyNode(Long id, String name, String description,
            String location, String currency, Date creationDate,
            BigDecimal initialBalance) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.currency = currency;
        this.creationDate = creationDate;
        this.initialBalance = initialBalance;
        setChanged(true);
    }

    @Override
    protected void internalLoad(SQLiteDatabase db) throws Exception {
        if (id == null) {
            throw new Exception("Id not specified");
        }

        Cursor cursor = db.query(TABLE_NAME, null, COL_ID + " = ?", 
                new String[] {id.toString()}, null, null, null, null);
        
        if (cursor.moveToFirst()) {
            name = cursor.getString(1);
            description = cursor.getString(2);
            location = cursor.getString(3);
            currency = cursor.getString(4);
            creationDate = new Date(cursor.getLong(5));
            initialBalance = new BigDecimal(cursor.getString(6));
        } else {
            throw new Exception("Couldn't find money node associated with id " 
                    + id);
        }
    }

    @Override
    protected void internalSave(SQLiteDatabase db) throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ID, id);
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_DESCRIPTION, description);
        contentValues.put(COL_LOCATION, location);
        contentValues.put(COL_CURRENCY, currency);
        contentValues.put(COL_CREATIONDATE, creationDate.getTime());
        contentValues.put(COL_INITIALBALANCE, initialBalance.toString());

        db.insertWithOnConflict(TABLE_NAME, null, contentValues, 
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Sets the id for this instance.
     *
     * @param id The id.
     */
    public void setId(Long id) {
        this.id = id;
        setChanged(true);
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
     * Sets the location for this instance.
     *
     * @param location The location.
     */
    public void setLocation(String location) {
        this.location = location;
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
     * Gets the id for this instance.
     *
     * @return The id.
     */
    public Long getId() {
        return this.id;
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
     * Gets the location for this instance.
     *
     * @return The location.
     */
    public String getLocation() {
        return this.location;
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

    public String toString() {
        return getName();
    }
}
