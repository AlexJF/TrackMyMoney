package net.alexjf.tmm.database;

// Based on: http://blog.osom.info/2014/03/migrating-to-sqlcipher-for-android-3x.html

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;
import android.content.SharedPreferences;

public class DatabaseHelperHook implements SQLiteDatabaseHook {
    private static final String PREFS_KEY =
            "net.sqlcipher.database.v3.migrated";

    private final SharedPreferences prefs;

    public static void resetMigrationFlag(SharedPreferences prefs, String dbPath) {
        prefs.edit().putBoolean(dbPath, false).commit();
    }

    public DatabaseHelperHook(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public void preKey(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void postKey(SQLiteDatabase database) {
        boolean isMigrated=prefs.getBoolean(PREFS_KEY, false);

        if (!isMigrated) {
            database.rawExecSQL("PRAGMA cipher_migrate;");
            prefs.edit().putBoolean(PREFS_KEY, true).commit();
        }
    }
}
