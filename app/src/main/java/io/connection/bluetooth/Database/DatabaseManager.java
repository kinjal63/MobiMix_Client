package io.connection.bluetooth.Database;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.connection.bluetooth.Api.response.entity.NearByPlayer;
import io.connection.bluetooth.Api.response.entity.PlayerGame;
import io.connection.bluetooth.Database.action.IActionCRUD;
import io.connection.bluetooth.Database.action.IActionReadListener;
import io.connection.bluetooth.Database.action.IDatabaseActionListener;
import io.connection.bluetooth.Database.entity.MBGameInfo;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.Database.entity.MBPlayerGames;
import io.connection.bluetooth.Database.utils.EntityUtils;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.core.MobiMix;

/**
 * Created by Kinjal on 10/8/2017.
 */

public class DatabaseManager {
    private static DatabaseManager dbManager_ = new DatabaseManager();
    private static String object_ = "object";
    private Context context;

    public DatabaseManager() {
        initDB();
    }

    private void initDB() {
        context = MobiMixApplication.getInstance().getContext();
    }

    public static DatabaseManager getInstance() {
        return dbManager_;
    }

    public synchronized void insertPlayers(List<NearByPlayer> players) {
        MobiMixApplication.getInstance().getDaoSession().getMBNearbyPlayerDao().deleteAll();
        MobiMixApplication.getInstance().getDaoSession().getMBPlayerGamesDao().deleteAll();
        MobiMixApplication.getInstance().getDaoSession().getMBGameInfoDao().deleteAll();

        MBNearbyPlayer[] nearByPlayers = new MBNearbyPlayer[players.size()];
        int i = 0;
        for(NearByPlayer player : players) {
            nearByPlayers[i] = new MBNearbyPlayer();

            EntityUtils.copyProperties(player, nearByPlayers[i]);
            List<PlayerGame> playerGames = player.getPlayerGameList();
            if(playerGames.size() > 0) {
                MBGameInfo[] gameInfo = new MBGameInfo[playerGames.size()];
                List<MBPlayerGames> playerGamesMap = new ArrayList<>();

                int k = 0;
                for(PlayerGame playerGame : playerGames) {
                    gameInfo[k] = new MBGameInfo();
                    EntityUtils.copyProperties(playerGame, gameInfo[k]);

                    // Construct an MBPlayerGame object to map player and games
                    MBPlayerGames mbPlayerGameMap = new MBPlayerGames();
                    mbPlayerGameMap.setPlayerId(player.getPlayerId());
                    mbPlayerGameMap.setGameId(playerGame.getGameId());
                    playerGamesMap.add(mbPlayerGameMap);

                    k++;
                }

                MobiMixApplication.getInstance().getDaoSession().getMBPlayerGamesDao().insertOrReplaceInTx(playerGamesMap);
                MobiMixApplication.getInstance().getDaoSession().getMBGameInfoDao().insertOrReplaceInTx(gameInfo);

                nearByPlayers[i].setPlayerGames(Arrays.asList(gameInfo));
            }
            i++;
        }
        MobiMixApplication.getInstance().getDaoSession().getMBNearbyPlayerDao().insertOrReplaceInTx(nearByPlayers);
    }

    public synchronized void update(Object object, String whereKeyClause, String value) {

    }

    public synchronized void findPlayers(DBParams params, final IDatabaseActionListener iDatabaseActionListener) {
        doAsyncFetch(params, new IActionReadListener() {
            @Override
            public void onReadOperation(int error, List<?> objects) {
                if(error == 0) {
                    iDatabaseActionListener.onDataReceived(objects);
                }
            }
        });
    }

    public synchronized void findMutualGames(DBParams params, final IDatabaseActionListener iDatabaseActionListener) {
        doAsyncFetch(params, new IActionReadListener() {
            @Override
            public void onReadOperation(int error, List<?> objects) {
                if(error == 0) {
                    iDatabaseActionListener.onDataReceived(objects);
                }
            }
        });
    }

    public synchronized void delete() {
    }

    private void doAsyncFetch(DBParams params, IActionCRUD crudListener) {
        Thread t = new AsyncOperation(params, crudListener);
        t.start();
    }
}
