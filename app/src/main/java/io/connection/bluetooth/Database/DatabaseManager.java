package io.connection.bluetooth.Database;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import io.connection.bluetooth.Api.response.entity.NearByPlayer;
import io.connection.bluetooth.Api.response.entity.PlayerGame;
import io.connection.bluetooth.Database.action.IActionCRUD;
import io.connection.bluetooth.Database.action.IActionReadListener;
import io.connection.bluetooth.Database.action.IActionUpdateListener;
import io.connection.bluetooth.Database.action.IDatabaseActionListener;
import io.connection.bluetooth.Database.entity.DaoSession;
import io.connection.bluetooth.Database.entity.MBGameInfo;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.Database.entity.MBNearbyPlayerDao;
import io.connection.bluetooth.Database.entity.MBPlayerGames;
import io.connection.bluetooth.Database.entity.MBPlayerGamesDao;
import io.connection.bluetooth.Database.entity.MBUserAvailability;
import io.connection.bluetooth.Database.entity.MBUserAvailabilityDao;
import io.connection.bluetooth.Database.utils.EntityUtils;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;

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
        MobiMixApplication.getInstance().getDaoSession().getMBUserAvailabilityDao().deleteAll();

        MBNearbyPlayer[] nearByPlayers = new MBNearbyPlayer[players.size()];

        int i = 0;
        for (NearByPlayer player : players) {
            nearByPlayers[i] = new MBNearbyPlayer();

            EntityUtils.copyProperties(player, nearByPlayers[i]);
            List<PlayerGame> playerGames = player.getPlayerGameList();
            if (playerGames.size() > 0) {
                MBGameInfo[] gameInfo = new MBGameInfo[playerGames.size()];
                List<MBPlayerGames> playerGamesMap = new ArrayList<>();

                int k = 0;
                for (PlayerGame playerGame : playerGames) {
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

        for(MBNearbyPlayer player : nearByPlayers) {
            MBUserAvailability userAvailability = new MBUserAvailability();
            userAvailability.setPlayer(player);
            userAvailability.setPlayerId(player.getPlayerId());
            userAvailability.setIsEngaged(1);
            userAvailability.setUpdatedAt(new Date());

            MobiMixApplication.getInstance().getDaoSession().getMBUserAvailabilityDao().insertOrReplace(userAvailability);
        }
    }

    public synchronized void update(Object object, String whereKeyClause, String value) {

    }

    // Update tables by scanning nearby users
    public synchronized void deleteUsersIfNotFoundInVicinity() {
        List<MBNearbyPlayer> players = MobiMixApplication.getInstance().getDaoSession().getMBNearbyPlayerDao().loadAll();
        HashSet<WifiP2PRemoteDevice> devices = WifiDirectService.getInstance(context).getWifiP2PDeviceList();

        List<String> emailIds = new ArrayList<>();
        emailIds.add(ApplicationSharedPreferences.getInstance(MobiMixApplication.getInstance().getContext()).getValue("email"));
        for (WifiP2PRemoteDevice device : devices) {
            emailIds.add(device.getName());
        }

        DaoSession daoSession = MobiMixApplication.getInstance().getDaoSession();
        QueryBuilder<MBNearbyPlayer> qb = daoSession.getMBNearbyPlayerDao().queryBuilder();
        List<MBNearbyPlayer> playersToUpdate = qb.where(MBNearbyPlayerDao.Properties.Email.in(emailIds)).list();

//        QueryBuilder<MBUserAvailability> qb = daoSession.getMBUserAvailabilityDao().queryBuilder();
//        qb.join(MBNearbyPlayer.class, MBNearbyPlayerDao.Properties.PlayerId)
//                .where(MBNearbyPlayerDao.Properties.Email.in(emailIds));
//        List<MBUserAvailability> userAvailabilities = qb.list();

        for (MBNearbyPlayer player : playersToUpdate) {
            MBUserAvailability userAvailabilityObj = daoSession.getMBUserAvailabilityDao().queryBuilder().
                    where(MBUserAvailabilityDao.Properties.PlayerId.eq(player.getPlayerId())).unique();
            userAvailabilityObj.setIsEngaged(0);
            daoSession.getMBUserAvailabilityDao().update(userAvailabilityObj);
        }


//        List<String> playerIdsToInActive = new ArrayList<>();
//        for (MBNearbyPlayer player : players) {
//            if (!emailsIds.contains(player.getEmail())) {
//                playerIdsToInActive.add(player.getPlayerId());
//            }
//        }

//        // Delete from mb_nearby_players table
//        DaoSession daoSession = MobiMixApplication.getInstance().getDaoSession();
//        DeleteQuery<MBNearbyPlayer> dQPlayers = daoSession.queryBuilder(MBNearbyPlayer.class)
//                .where(MBNearbyPlayerDao.Properties.Email.notIn(emailsIds))
//                .up();
//        dQPlayers.executeDeleteWithoutDetachingEntities();
//
//        // Delete from player_games table
//        DeleteQuery<MBPlayerGames> dbPlayerGames = daoSession.queryBuilder(MBPlayerGames.class)
//                .where(new WhereCondition().MBPlayerGamesDao.Properties.PlayerId.in(playerIdsToInActive))
//                .buildDelete();
//        dbPlayerGames.executeDeleteWithoutDetachingEntities();

    }

    public synchronized void findPlayers(DBParams params, final IDatabaseActionListener iDatabaseActionListener) {
        doAsync(params, new IActionReadListener() {
            @Override
            public void onReadOperation(int error, List<?> objects) {
                if (error == 0) {
                    iDatabaseActionListener.onDataReceived(objects);
                }
            }
        });
    }

    public synchronized void findMutualGames(DBParams params, final IDatabaseActionListener iDatabaseActionListener) {
        doAsync(params, new IActionReadListener() {
            @Override
            public void onReadOperation(int error, List<?> objects) {
                if (error == 0) {
                    iDatabaseActionListener.onDataReceived(objects);
                }
            }
        });
    }

    public synchronized void updateGameTable(DBParams params) {
        doAsync(params, new IActionUpdateListener() {
            @Override
            public void onUpdateAction(int error) {
                if (error == 0) {
                    Log.d("DatabaseManager", "Game table is updated successfully");
                }
            }
        });
    }

    public synchronized void delete() {
    }

    private void doAsync(DBParams params, IActionCRUD crudListener) {
        Thread t = new DBAsyncOperation(params, crudListener);
        t.start();
    }
}
