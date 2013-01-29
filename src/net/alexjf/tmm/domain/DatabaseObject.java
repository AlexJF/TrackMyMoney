/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.domain;

import net.alexjf.tmm.exceptions.DatabaseNotReadyException;
import net.alexjf.tmm.exceptions.DbObjectLoadException;
import net.alexjf.tmm.exceptions.DbObjectSaveException;
import net.alexjf.tmm.exceptions.UnknownIdException;
import net.sqlcipher.database.SQLiteDatabase;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class DatabaseObject implements Parcelable {
    private Long id;
    private transient SQLiteDatabase db;
    private boolean loaded;
    private boolean changed;

    public DatabaseObject() {
        id = null;
        db = null;
        loaded = false;
        changed = false;
    }

    public void load()
        throws DatabaseNotReadyException, UnknownIdException, 
               DbObjectLoadException {
        load(false);
    }

    public void load(SQLiteDatabase db) 
        throws DatabaseNotReadyException, UnknownIdException, 
               DbObjectLoadException {
        setDb(db);
        load(false);
    }

    public void load(boolean force) 
        throws DatabaseNotReadyException, UnknownIdException, 
               DbObjectLoadException {
        dbReadyOrThrow();

        if (!isLoaded() || force) {
            if (getId() == null) {
                throw new UnknownIdException();
            }

            internalLoad();
            setLoaded(true);
        }
    }

    public void load(SQLiteDatabase db, boolean force) 
        throws DatabaseNotReadyException, UnknownIdException, 
               DbObjectLoadException {
        setDb(db);
        load(force);
    }

    public void save() 
        throws DatabaseNotReadyException, DbObjectSaveException {
        save(false);
    }

    public void save(SQLiteDatabase db)
        throws DatabaseNotReadyException, DbObjectSaveException {
        setDb(db);
        save(false);
    }

    public void save(boolean force)
        throws DatabaseNotReadyException, DbObjectSaveException {
        dbReadyOrThrow();

        if (isChanged() || force) {
            id = internalSave();
            setChanged(false);
        }
    }

    public void save(SQLiteDatabase db, boolean force) 
        throws DatabaseNotReadyException, DbObjectSaveException {
        setDb(db);
        save(force);
    }

    protected abstract void internalLoad() throws DbObjectLoadException;
    protected abstract long internalSave() throws DbObjectSaveException;

    protected void dbReadyOrThrow() throws DatabaseNotReadyException {
        if (db == null || !db.isOpen()) {
            throw new DatabaseNotReadyException();
        }
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
     * Sets the id for this instance.
     *
     * @param id The id.
     */
    void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the db for this instance.
     *
     * @return The db.
     */
    public SQLiteDatabase getDb() {
        return this.db;
    }

    /**
     * Sets the db for this instance.
     *
     * @param db The db.
     */
    public void setDb(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Determines if this instance is loaded.
     *
     * @return The loaded.
     */
    public boolean isLoaded() {
        return this.loaded;
    }

    /**
     * Determines if this instance is changed.
     *
     * @return The changed.
     */
    public boolean isChanged() {
        return this.changed;
    }

    /**
     * Sets whether or not this instance is loaded.
     *
     * @param loaded The loaded.
     */
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    /**
     * Sets whether or not this instance is changed.
     *
     * @param changed The changed.
     */
    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
    }

    public int describeContents() {
        return 0;
    }
}
