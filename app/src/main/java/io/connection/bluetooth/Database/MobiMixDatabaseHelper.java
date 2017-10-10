package io.connection.bluetooth.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.connection.bluetooth.Database.models.MBNearbyPlayers;

/**
 * Created by Kinjal on 10/7/2017.
 */

public class MobiMixDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mobimix.db";
    private static final int DATABASE_VERSION = 1;
    private DatabaseManager dbManager_ = null;

    MobiMixDatabaseHelper(Context context, DatabaseManager dbManager_) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.dbManager_ = dbManager_;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        dbManager_.createTable(MBNearbyPlayers.class);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
