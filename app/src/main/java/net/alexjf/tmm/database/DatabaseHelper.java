/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.database;

import net.alexjf.tmm.domain.Category;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.domain.Transaction;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;

    DatabaseHelper(Context context, String databaseName, SharedPreferences prefs) {
        super(context, databaseName, null, DATABASE_VERSION, new DatabaseHelperHook(prefs));
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        Log.d("TMM", "opened database with version " + db.getVersion());

        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.rawExecSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        MoneyNode.onDatabaseCreation(db);
        Transaction.onDatabaseCreation(db);
        ImmediateTransaction.onDatabaseCreation(db);
        Category.onDatabaseCreation(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("TMM", "onUpgrade " + oldVersion + " " + newVersion);
        MoneyNode.onDatabaseUpgrade(db, oldVersion, newVersion);
        Transaction.onDatabaseUpgrade(db, oldVersion, newVersion);
        ImmediateTransaction.onDatabaseUpgrade(db, oldVersion, newVersion);
        Category.onDatabaseUpgrade(db, oldVersion, newVersion);
    }
}
