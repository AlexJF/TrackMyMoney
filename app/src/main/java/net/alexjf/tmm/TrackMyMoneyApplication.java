/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm;

import net.alexjf.tmm.database.DatabaseManager;
import net.alexjf.tmm.utils.DrawableResolver;
import net.alexjf.tmm.utils.PreferenceManager;
import net.sqlcipher.database.SQLiteDatabase;
import android.app.Application;

public class TrackMyMoneyApplication extends Application {
    @Override
    public void onCreate() {
        SQLiteDatabase.loadLibs(this);
        DrawableResolver.initialize(this);
        PreferenceManager.initialize(this);
        DatabaseManager.initialize(this);
    }
}
