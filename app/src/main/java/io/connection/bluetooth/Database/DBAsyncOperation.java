package io.connection.bluetooth.Database;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import io.connection.bluetooth.Database.action.IActionCRUD;
import io.connection.bluetooth.Database.action.IActionReadListener;
import io.connection.bluetooth.Database.entity.DaoSession;
import io.connection.bluetooth.Database.entity.MBGameInfo;
import io.connection.bluetooth.Database.entity.MBGameParticipants;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.Database.entity.MBPlayerGames;
import io.connection.bluetooth.Database.entity.MBPlayerGamesDao;
import io.connection.bluetooth.Database.entity.MBUserAvailability;
import io.connection.bluetooth.Database.entity.MBUserAvailabilityDao;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by Kinjal on 10/15/2017.
 */

public class DBAsyncOperation extends Thread {
    private DBParams params;
    private IActionCRUD iActionCRUDListener;

    public DBAsyncOperation(DBParams params, IActionCRUD iActionCRUD) {
        this.params = params;
        this.iActionCRUDListener = iActionCRUD;
    }

    public DBAsyncOperation() {
    }

    @Override
    public void run() {
        DaoSession daoSession = MobiMixApplication.getInstance().getDaoSession();
        switch (this.params.event_) {
            case MobiMix.DBRequest.DB_FIND_NEARBY_PLAYERS:
//                List<MBUserAvailability> us = daoSession.getMBUserAvailabilityDao().loadAll();
                QueryBuilder<MBNearbyPlayer> q1 = daoSession.getMBNearbyPlayerDao().queryBuilder();
                q1.join(MBUserAvailability.class, MBUserAvailabilityDao.Properties.PlayerId).
                        where(MBUserAvailabilityDao.Properties.IsEngaged.eq(0));
                List<MBNearbyPlayer> lstPlayers = q1.list();

                ((IActionReadListener)this.iActionCRUDListener).onReadOperation(0, lstPlayers);
                break;

            case MobiMix.DBRequest.DB_FIND_MUTUAL_GAMES:
                if(params.remoteUserIds_ != null) {
                    String whereIn = "";
                    for(String id : params.remoteUserIds_) {
                        whereIn += "'" + id + "',";
                    }
                    whereIn = whereIn.substring(0, whereIn.length() - 1);

                    QueryBuilder<MBGameInfo> q2 = daoSession.getMBGameInfoDao().queryBuilder();
//                            queryRawCreate(" join mb_player_games mpg on T.game_id = mpg.game_id where mpg.player_id IN (?)", whereIn);
//                            " group by mpg.player_id" +
//                                    " having count(mpg.game_id) > " + params.remoteUserIds_.size(), whereIn);

                    q2.join(MBPlayerGames.class, MBPlayerGamesDao.Properties.GameId);
                    q2.where(new WhereCondition.StringCondition("J1.player_id IN (" + whereIn + ")" +
                                    " group by J1.game_id" +
                                    " having count(J1.game_id) > " + (params.remoteUserIds_.size() - 1)));
                    List<MBGameInfo> playerGames = q2.list();
                    ((IActionReadListener) this.iActionCRUDListener).onReadOperation(0, playerGames);
                }
                break;
            case MobiMix.DBRequest.DB_UPDATE_GAME_TABLE:
                if(this.params.object_ != null) {
                    JSONObject object = params.object_;

                    String groupOwnerUserId = object.optString(Constants.GROUP_OWNER_USER_ID);
                    String connectedUserId = object.optString(Constants.CONNECTED_USER_ID);
                    long gameId = object.optLong(Constants.GAME_ID);
                    int connectionType = object.optInt(Constants.GAME_CONNECTION_TYPE);
                    int maxPlayers = object.optInt(Constants.GAME_MAX_PLAYERS);

                    MBNearbyPlayer groupOwnerPlayer = daoSession.getMBNearbyPlayerDao().load(groupOwnerUserId);
                    MBNearbyPlayer connectedPlayer = daoSession.getMBNearbyPlayerDao().load(connectedUserId);
                    MBGameInfo gameInfo = daoSession.getMBGameInfoDao().load(gameId);

                    MBGameParticipants gameParticipants = new MBGameParticipants();
                    gameParticipants.setConnectedPlayer(connectedPlayer);
                    gameParticipants.setGroupOwnerPlayer(groupOwnerPlayer);
                    gameParticipants.setGameInfo(gameInfo);
                    gameParticipants.setConnectionType(connectionType);
                    gameParticipants.setMaxPlayers(maxPlayers);
                    gameParticipants.setUpdatedAt(new Date());
                }
            break;
            default:
                break;
        }

        if(currentThread().isAlive() && !currentThread().interrupted()) {
            this.interrupt();
        }
    }
}
