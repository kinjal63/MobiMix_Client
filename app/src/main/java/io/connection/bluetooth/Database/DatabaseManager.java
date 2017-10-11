package io.connection.bluetooth.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Database.exception.NoTableMappedException;
import io.connection.bluetooth.Database.entity.MBNearbyPlayers;
import io.connection.bluetooth.Database.utils.DatabaseUtils;
import io.connection.bluetooth.MobiMixApplication;

/**
 * Created by Kinjal on 10/8/2017.
 */

public class DatabaseManager {
    private static DatabaseManager dbManager_ = new DatabaseManager();
    private static String object_ = "object";
    private Context context;
    private MobiMixDatabaseHelper mobimixDatabase;

    public DatabaseManager() {
        initDB();
    }

    private void initDB() {
        context = MobiMixApplication.getInstance().getContext();
        mobimixDatabase = new MobiMixDatabaseHelper(context, this);
    }

    private static DatabaseManager getInstance() {
        synchronized (object_) {
            if (dbManager_ == null) {
                dbManager_ = new DatabaseManager();
            }
        }
        return dbManager_;
    }

    private String getTable(Class<?> c) {
        String tableName = DatabaseUtils.getTableName(c);
        if (tableName == null) {
            try {
                throw new NoTableMappedException("No table mapped to class " + c.getSimpleName(), new Throwable());
            } catch (NoTableMappedException e) {
                e.printStackTrace();
            }
        }
        return tableName;
    }

    public void createTable(Class<?> cl) {
        String tableName = DatabaseUtils.getTableName(cl);
        if (tableName == null) {
            try {
                throw new NoTableMappedException("No table mapped to class " + cl.getSimpleName(), new Throwable());
            } catch (NoTableMappedException e) {
                return;
            }
        }
        String sql = "create table if not exists " + tableName + "(";
        Field fields[] = DatabaseUtils.getFields(cl);
        if (fields != null) {
            Field primaryField = null;
            for (Field field : fields) {
                if (DatabaseUtils.isPrimaryKeyColumn(field)) {
                    primaryField = field;
                } else {
                    sql += DatabaseUtils.getColumnName(field) + " " + DatabaseUtils.getColumnType(field) + ",";
                }
            }
            sql += primaryField != null ? (DatabaseUtils.getColumnName(primaryField) + " " + DatabaseUtils.getColumnType(primaryField)
                    + " primary key autoincrement") : "_id integer primary key autoincrement";
        }

        SQLiteDatabase sqliteDatabase = mobimixDatabase.getWritableDatabase();

        sqliteDatabase.execSQL(sql);
        sqliteDatabase.close();
    }

    public void insert(List<Object> objects) {
        SQLiteDatabase sqliteDatabase = this.mobimixDatabase.getWritableDatabase();
        for (Object object : objects) {
            String tableName = getTable(object.getClass());

            if (tableName != null) {
                sqliteDatabase.beginTransaction();
                sqliteDatabase.insert(tableName, null, DatabaseUtils.getFilledContentValues(object));
            }
        }
        if (sqliteDatabase.inTransaction()) {
            sqliteDatabase.endTransaction();
            sqliteDatabase.close();
        }
    }

    public void update(Object object, String whereKeyClause, String value) {
        SQLiteDatabase sqliteDatabase = this.mobimixDatabase.getWritableDatabase();

        String tableName = getTable(object.getClass());
        if(tableName != null) {
            sqliteDatabase.update(tableName, DatabaseUtils.getFilledContentValues(object), whereKeyClause + "= ?", new String[]{ value });
        }
        sqliteDatabase.close();
    }

    public void delete(Object object, String whereKeyClause, String value) {
        SQLiteDatabase sqliteDatabase = this.mobimixDatabase.getWritableDatabase();

        String tableName = getTable(object.getClass());
        if (tableName != null) {
            sqliteDatabase.delete(tableName, whereKeyClause + "= ?", null);
        }
        sqliteDatabase.close();
    }

//    public void queryTable(Class<?> c, String... args) {
//        SQLiteDatabase database = mobimixDatabase.getReadableDatabase();
//
//        String selectionArgs = args.
//        database.rawQuery(sql, new Str)
//    }

    public void queryPlayers() {
        SQLiteDatabase database = mobimixDatabase.getReadableDatabase();

        String tableName = getTable(MBNearbyPlayers.class);
        String sql = "select * from " + tableName;

        Cursor c = database.rawQuery(sql, null);
        List<MBNearbyPlayers> nearbyPlayerList = new ArrayList<>();
        if(c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                String playerId = c.getString(c.getColumnIndex("player_id"));
                String playerName = c.getString(c.getColumnIndex("player_name"));

                MBNearbyPlayers nearbyPlayer = new MBNearbyPlayers();
                nearbyPlayer.setPlayerID(playerId);
                nearbyPlayer.setPlayerName(playerName);

                nearbyPlayerList.add(nearbyPlayer);
            }
        }
        c.close();
        database.close();
    }

    public void queryGamesForSelectedPlayers() {
        SQLiteDatabase database = mobimixDatabase.getReadableDatabase();

    }
}
