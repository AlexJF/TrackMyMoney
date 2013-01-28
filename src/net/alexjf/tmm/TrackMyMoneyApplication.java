/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm;

import android.app.Application;

import net.sqlcipher.database.SQLiteDatabase;

public class TrackMyMoneyApplication extends Application {
    @Override
    public void onCreate() {
        SQLiteDatabase.loadLibs(this);
    }
}
