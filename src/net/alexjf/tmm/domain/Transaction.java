/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.domain;

import java.math.BigDecimal;

import net.alexjf.tmm.exceptions.DbObjectLoadException;
import net.alexjf.tmm.exceptions.DbObjectSaveException;
import net.sqlcipher.database.SQLiteDatabase;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * This class represents a base transaction of the application.
 */
public abstract class Transaction extends DatabaseObject {
    // Database tables
    public static final String TABLE_NAME = "Transactions";

    // Table Columns
    public static final String COL_ID = "id";
    public static final String COL_MONEYNODEID = "moneyNodeId";
    public static final String COL_VALUE = "value";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_CATEGORYID = "categoryId";

    // Schema
    private static final String SCHEMA_CREATETABLE = 
        "CREATE TABLE " + TABLE_NAME + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_MONEYNODEID + " INTEGER NOT NULL REFERENCES " + 
                MoneyNode.TABLE_NAME + " ON DELETE CASCADE," + 
            COL_VALUE + " NUMERIC NOT NULL," +
            COL_DESCRIPTION + " TEXT," +
            COL_CATEGORYID + " INTEGER REFERENCES Categories" +
        ");";

    // Database maintenance
    /**
     * Creates schemas associated with a Transaction domain.
     *
     * @param db Database where to create the schemas.
     */
    public static void onDatabaseCreation(SQLiteDatabase db) {
        db.execSQL(SCHEMA_CREATETABLE);
    }

    /**
     * Updates schemas associated with a Transaction domain.
     *
     * @param db Database where to update the schemas.
     * @param oldVersion The old version of the schemas.
     * @param newVersion The new version of the schemas.
     */
    public static void onDatabaseUpgrade(SQLiteDatabase db, int oldVersion, 
                                        int newVersion) {
    }

    // Private members
    private MoneyNode moneyNode;
    private BigDecimal value;
    private String description;
    private Category category;

    public Transaction(Long id) {
        setId(id);
    }

    /**
     * Constructs a new instance.
     *
     * @param moneyNode The moneyNode for this instance.
     * @param value The value for this instance.
     * @param description The description for this instance.
     * @param categoryId The categoryId for this instance.
     */
    public Transaction(MoneyNode moneyNode, BigDecimal value,
            String description, Category category) {
        this.moneyNode = moneyNode;
        this.value = value;
        this.description = description;
        this.category = category;
        setChanged(true);
    }

    @Override
    protected void internalLoad() throws DbObjectLoadException {
        Cursor cursor = getDb().query(TABLE_NAME, null, COL_ID + " = ?", 
                new String[] {getId().toString()}, null, null, null, null);
        
        try {
            if (cursor.moveToFirst()) {
                long moneyNodeId = cursor.getLong(1);

                if (moneyNode == null || !moneyNode.getId().equals(moneyNodeId)) {
                    moneyNode = MoneyNode.createFromId(moneyNodeId);
                }

                value = new BigDecimal(cursor.getString(2));

                description = cursor.getString(3);

                long categoryId = cursor.getLong(4);
                if (category == null || !category.getId().equals(categoryId)) {
                    category = Category.createFromId(categoryId);
                }
            } else {
                throw new DbObjectLoadException("Couldn't find transaction " + 
                        "associated with id " + getId());
            }
        } finally {
            cursor.close();
        }
    }

    @Override
    protected long internalSave() throws DbObjectSaveException {
        Long id = getId();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ID, id);
        contentValues.put(COL_MONEYNODEID, moneyNode.getId());
        contentValues.put(COL_VALUE, value.toString());
        contentValues.put(COL_DESCRIPTION, description);
        contentValues.put(COL_CATEGORYID, category.getId());

        long result;

        if (id != null) {
            result = getDb().update(TABLE_NAME, contentValues, 
                     COL_ID + " = ?", new String[] {id.toString()});
            result = (result == 0 ? -1 : id);
        } else {
            result = getDb().insert(TABLE_NAME, null, contentValues);
        }

        if (result >= 0) {
            return result;
        } else {
            throw new DbObjectSaveException("Couldn't save transaction " + 
                    "associated with id " + getId());
        }
    }

    /**
     * Gets the moneyNode for this instance.
     *
     * @return The moneyNode.
     */
    public MoneyNode getMoneyNode() {
        return this.moneyNode;
    }

    /**
     * Gets the value for this instance.
     *
     * @return The value.
     */
    public BigDecimal getValue() {
        return this.value;
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
     * Sets the moneyNode for this instance.
     *
     * @param moneyNode The moneyNode.
     */
    public void setMoneyNode(MoneyNode moneyNode) {
        this.moneyNode = moneyNode;
        setChanged(true);
    }

    /**
     * Sets the value for this instance.
     *
     * @param value The value.
     */
    public void setValue(BigDecimal value) {
        this.value = value;
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
     * Sets the category for this instance.
     *
     * @param category The category.
     */
    public void setCategory(Category category) {
        this.category = category;
        setChanged(true);
    }

    /**
     * Gets the category for this instance.
     *
     * @return The category.
     */
    public Category getCategory() {
        return this.category;
    }

    public Transaction clone() throws CloneNotSupportedException {
        return (Transaction) super.clone();
    }
}
