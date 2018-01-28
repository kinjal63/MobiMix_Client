package io.connection.bluetooth.Database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.CancellationSignal;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.StandardDatabase;

import java.util.HashMap;

import io.connection.bluetooth.Database.entity.DaoSession;
import io.connection.bluetooth.Database.utils.MobiMixDatabaseHelper;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.core.MobiMix;

/**
 * Created by Kinjal on 10/25/2017.
 */

public class MobiMixProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.mobimix.provider";
    static final String URL = "content://" + PROVIDER_NAME; // + "/mobimix";

    static final UriMatcher uriMatcher;
    private static HashMap<String, String> values;

    private SQLiteDatabase db;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "mb_nearby_players", DBUri.URI_NEARBY_PLAYERS.ordinal());
        uriMatcher.addURI(PROVIDER_NAME, "mb_nearby_players/*", DBUri.URI_NEARBY_PLAYERS.ordinal());
        uriMatcher.addURI(PROVIDER_NAME, "mb_nearby_players/#", DBUri.URI_NEARBY_PLAYERS_BY_ID.ordinal());
        uriMatcher.addURI(PROVIDER_NAME, "mb_nearby_players/#", DBUri.URI_NEARBY_PLAYERS_BY_ID.ordinal());

        uriMatcher.addURI(PROVIDER_NAME, "mb_game_participants", DBUri.URI_GAME_PARTICIPANTS.ordinal());
        uriMatcher.addURI(PROVIDER_NAME, "mb_game_participants/*", DBUri.URI_GAME_PARTICIPANTS.ordinal());
        uriMatcher.addURI(PROVIDER_NAME, "mb_game_participants/#", DBUri.URI_GAME_PARTICIPANTS_BY_ID.ordinal());
    }

    @Override
    public boolean onCreate() {
//        Database database = MobiMixApplication.getInstance().getDaoSession().getDatabase();
//        db = ((StandardDatabase)database).getSQLiteDatabase();
//        if (db != null) {
            return true;
//        }
//        return false;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        if(db == null) {
            Database database = MobiMixApplication.getInstance().getDaoSession().getDatabase();
            db = ((StandardDatabase)database).getSQLiteDatabase();
        }
        long rowId = 0;
        switch (uriMatcher.match(uri)) {
            case 1:
                rowId = db.insertOrThrow(MobiMix.MBDatabase.TABLE_NAME_NEARBY_PLAYERS, "", contentValues);
                break;
            default:
                break;
        }
        if(rowId > 0) {
            Uri _uri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(_uri, null);
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        String sql = null;
        if(db == null) {
            Database database = MobiMixApplication.getInstance().getDaoSession().getDatabase();
            db = ((StandardDatabase)database).getSQLiteDatabase();
        }

        DBUri dbUri = DBUri.getDBUri(uriMatcher.match(uri));
        switch (dbUri) {
            case URI_GAME_PARTICIPANTS:
                sql = "select mgp.connection_type as connection_type,\n" +
                        "(select player_name from mb_nearby_players where email=?) as user_name,\n" +
                        "(select email from mb_nearby_players where player_id=mgp.player_id) as group_owner_device_name, \n" +
                        "group_concat(mnp.player_name) as game_participants \n" +
                        "from mb_nearby_players mnp \n" +
                        "join mb_game_participants mgp on (mnp.player_id=mgp.player_id or mnp.player_id=mgp.connected_player_id) \n" +
                        "join mb_game_info mgi on mgp.game_id=mgi.game_id \n" +
                        "where mgi.game_package_name = ? group by mgi.game_id";
            default:
                break;
        }
        Cursor c = db.rawQuery(sql, selectionArgs);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }
}
