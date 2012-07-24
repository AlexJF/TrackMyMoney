package com.alexjf.tmm;

import android.app.Application;

import net.sqlcipher.database.SQLiteDatabase;

public class TrackMyMoneyApplication extends Application {
    @Override
    public void onCreate() {
        SQLiteDatabase.loadLibs(this);
    }
}
