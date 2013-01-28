package net.alexjf.tmm.domain;

import java.math.BigDecimal;

import net.alexjf.tmm.exceptions.DbObjectLoadException;
import net.alexjf.tmm.exceptions.DbObjectSaveException;
import net.sqlcipher.database.SQLiteDatabase;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;

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

    private MoneyNode moneyNode;
    private BigDecimal value;
    private String description;
    private Long categoryId;

    public static void onDatabaseCreation(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_MONEYNODEID + " INTEGER NOT NULL REFERENCES " + 
                    MoneyNode.TABLE_NAME + "," + 
                COL_VALUE + " NUMERIC NOT NULL," +
                COL_DESCRIPTION + " TEXT," +
                COL_CATEGORYID + " INTEGER REFERENCES Categories" +
            ");");
    }

    public static void onDatabaseUpgrade(SQLiteDatabase db, int oldVersion, 
                                        int newVersion) {
    }

    public Transaction(Long id) {
        setId(id);
    }

    public Transaction(Parcel in) {
        super(in);
        readFromParcel(in);
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
            String description, Long categoryId) {
        this.moneyNode = moneyNode;
        this.value = value;
        this.description = description;
        this.categoryId = categoryId;
        setChanged(true);
    }

    @Override
    protected void internalLoad() throws DbObjectLoadException {
        Cursor cursor = getDb().query(TABLE_NAME, null, COL_ID + " = ?", 
                new String[] {getId().toString()}, null, null, null, null);
        
        try {
            if (cursor.moveToFirst()) {
                value = new BigDecimal(cursor.getString(1));
                long moneyNodeId = cursor.getLong(2);

                if (moneyNode == null || !moneyNode.getId().equals(moneyNodeId)) {
                    moneyNode = new MoneyNode(moneyNodeId);
                }

                description = cursor.getString(3);
                categoryId = cursor.getLong(4);
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
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ID, getId());
        contentValues.put(COL_MONEYNODEID, moneyNode.getId());
        contentValues.put(COL_VALUE, value.toString());
        contentValues.put(COL_DESCRIPTION, description);
        contentValues.put(COL_CATEGORYID, categoryId);

        long result = getDb().insertWithOnConflict(TABLE_NAME, null, 
                contentValues, SQLiteDatabase.CONFLICT_REPLACE);

        if (result > 0) {
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
     * Sets the categoryId for this instance.
     *
     * @param categoryId The categoryId.
     */
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
        setChanged(true);
    }

    /**
     * Gets the categoryId for this instance.
     *
     * @return The categoryId.
     */
    public Long getCategoryId() {
        return this.categoryId;
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        moneyNode = in.readParcelable(MoneyNode.class.getClassLoader());
        value = new BigDecimal(in.readString());
        description = in.readString();
        categoryId = in.readLong();
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeParcelable(moneyNode, flags);
        out.writeString(value.toString());
        out.writeString(description);
        out.writeLong(categoryId);
    }

    public int describeContents() {
        return 0;
    }
}
