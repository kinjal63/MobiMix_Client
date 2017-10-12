package io.connection.bluetooth.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Api.response.entity.NearByPlayer;
import io.connection.bluetooth.Api.response.entity.PlayerGame;
import io.connection.bluetooth.Database.entity.MBGameInfo;
import io.connection.bluetooth.Database.exception.NoTableMappedException;
import io.connection.bluetooth.Database.entity.MBNearbyPlayers;
import io.connection.bluetooth.Database.utils.DatabaseUtils;
import io.connection.bluetooth.Database.utils.EntityUtils;
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

    public static DatabaseManager getInstance() {
        return dbManager_;
    }

    public void insertPlayers(List<NearByPlayer> players) {
        MBNearbyPlayers[] nearByPlayers = new MBNearbyPlayers[players.size()];
        int i = 0;
        for(NearByPlayer player : players) {
            EntityUtils.copyProperties(player, nearByPlayers[i]);
            List<PlayerGame> playerGames = player.getPlayerGameList();
            if(playerGames.size() > 0) {
                MBGameInfo[] gameInfo = new MBGameInfo[playerGames.size()];
                int k = 0;
                for(PlayerGame playerGame : playerGames) {
                    EntityUtils.copyProperties(playerGame, gameInfo[k]);
                    nearByPlayers[i].getPlayerGameList().add(gameInfo[k]);
                    k++;
                }
            }
            i++;
        }
        MobiMixApplication.getInstance().getDaoSession().getMBNearbyPlayersDao().insertInTx(nearByPlayers);
    }

    public void update(Object object, String whereKeyClause, String value) {

    }

    public List<MBNearbyPlayers> findPlayers() {
        List<MBNearbyPlayers> nearByPlayers = MobiMixApplication.getInstance().getDaoSession().getMBNearbyPlayersDao().loadAll();
        return nearByPlayers;
    }

    public void delete() {
        SQLiteDatabase database = mobimixDatabase.getReadableDatabase();

    }
}
