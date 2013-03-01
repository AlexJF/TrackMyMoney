/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.domain;

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
 * This class represents a transaction category.
 */
public class Category extends DatabaseObject {
    public static final String KEY_CATEGORY = "category";

    // Database tables
    public static final String TABLE_NAME = "Categories";

    // Table columns
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_ICON = "icon";

    // Queries
    private static final String QUERY_CREATETABLE = 
        "CREATE TABLE " + TABLE_NAME + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_NAME + " TEXT NOT NULL," +
            COL_ICON + " TEXT" +
        ");";

    // Database maintenance
    /**
     * Creates schemas associated with a Category domain.
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
    private static Cache<Long, Category> cache = new Cache<Long, Category>();

    /**
     * Gets an instance of Category with the specified id.
     *
     * The instance is reused from the cache or created if it doesn't exist on
     * the cache yet.
     *
     * @param id The id of the category we want.
     * @return Category instance with specified id.
     */
    public static Category createFromId(Long id) {
        if (id == null) {
            return null;
        }

        Category cat = cache.get(id);

        if (cat == null) {
            cat = new Category(id);
            cache.put(id, cat);
        }

        return cat;
    }

    // Private fields
    private String name;
    private String icon;

    /**
     * Create a new instance with known id.
     *
     * Full details about the instance can then be obtained after calling its
     * load() method.
     *
     * @param id Known id of an instance.
     */
    private Category(Long id) {
        setId(id);
    }

    /**
     * Constructs a new instance.
     *
     * @param name The name of this category.
     * @param icon The icon for this category.
     */
    public Category(String name, String icon) {
        this.name = name;
        this.icon = icon;
        setChanged(true);
    }

    @Override
    protected void internalLoad() throws DbObjectLoadException {
        Cursor cursor = getDb().query(TABLE_NAME, null, COL_ID + " = ?", 
                new String[] {getId().toString()}, null, null, null, null);
        
        try {
            if (cursor.moveToFirst()) {
                name = cursor.getString(1);
                icon = cursor.getString(2);
            } else {
                throw new DbObjectLoadException("Couldn't find category " + 
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
        contentValues.put(COL_NAME, getName());
        contentValues.put(COL_ICON, getIcon());

        long result;

        if (id != null) {
            result = getDb().update(TABLE_NAME, contentValues, 
                     COL_ID + " = ?", new String[] {id.toString()});
            result = (result == 0 ? -1 : id);
        } else {
            result = getDb().insert(TABLE_NAME, null, contentValues);
        }

        if (result >= 0) {
            cache.put(result, this);
            return result;
        } else {
            throw new DbObjectSaveException("Couldn't save category " + 
                    "associated with id " + getId());
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
        setChanged(true);
    }

    /**
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(String icon) {
        this.icon = icon;
        setChanged(true);
    }

    @Override
    public String toString() {
        return getName();
    }

    public static final Parcelable.Creator<Category> CREATOR =
        new Parcelable.Creator<Category>() {
            public Category createFromParcel(Parcel in) {
                Long id = in.readLong();
                return createFromId(id);
            }
 
            public Category[] newArray(int size) {
                return new Category[size];
            }
        };

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o instanceof Category) {
            Category cat = (Category) o;

            return this.getId().equals(cat.getId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public static class Comparator implements java.util.Comparator<Category> {
        public int compare(Category lhs, Category rhs) {
            try {
                lhs.load(); rhs.load();
            } catch (DatabaseException e) {
                return 1;
            }
            return lhs.getName().compareTo(rhs.getName());
        }
    }
}
