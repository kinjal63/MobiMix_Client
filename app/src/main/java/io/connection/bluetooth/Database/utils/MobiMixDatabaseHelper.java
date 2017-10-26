package io.connection.bluetooth.Database.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.connection.bluetooth.Database.MobiMixProvider;
import io.connection.bluetooth.core.MobiMix;

/**
 * Created by kp49107 on 26-10-2017.
 */
public class MobiMixDatabaseHelper extends SQLiteOpenHelper {

    public MobiMixDatabaseHelper(Context context) {
        super(context, MobiMix.MBDatabase.DATABASE_NAME, null, MobiMix.MBDatabase.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
