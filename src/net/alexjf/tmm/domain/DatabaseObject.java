package net.alexjf.tmm.domain;

import net.sqlcipher.database.SQLiteDatabase;

public abstract class DatabaseObject {
    private boolean loaded;
    private boolean changed;

    public void load(SQLiteDatabase db) throws Exception {
        load(db, false);
    }

    public void load(SQLiteDatabase db, boolean force) throws Exception {
        if (!isLoaded() || force) {
            internalLoad(db);
            setLoaded(true);
        }
    }

    public void save(SQLiteDatabase db) throws Exception {
        save(db, false);
    }

    public void save(SQLiteDatabase db, boolean force) throws Exception {
        if (isChanged() || force) {
            internalSave(db);
            setChanged(false);
        }
    }

    protected abstract void internalLoad(SQLiteDatabase db) throws Exception;
    protected abstract void internalSave(SQLiteDatabase db) throws Exception;

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
}
