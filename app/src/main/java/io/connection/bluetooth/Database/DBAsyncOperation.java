package io.connection.bluetooth.Database;

import android.database.Cursor;
import android.net.Uri;

import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.connection.bluetooth.Database.action.IActionCRUD;
import io.connection.bluetooth.Database.action.IActionReadListener;
import io.connection.bluetooth.Database.entity.DaoSession;
import io.connection.bluetooth.Database.entity.MBGameInfo;
import io.connection.bluetooth.Database.entity.MBGameParticipants;
import io.connection.bluetooth.Database.entity.MBGameParticipantsDao;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.Database.entity.MBNearbyPlayerDao;
import io.connection.bluetooth.Database.entity.MBPlayerGames;
import io.connection.bluetooth.Database.entity.MBPlayerGamesDao;
import io.connection.bluetooth.Database.entity.MBUserAvailability;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.utils.GameConstants;

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

    @Override
    public void run() {
        DaoSession daoSession = MobiMixApplication.getInstance().getDaoSession();
        switch (this.params.event_) {
            case MobiMix.DBRequest.DB_FIND_NEARBY_PLAYERS:
                QueryBuilder<MBNearbyPlayer> q1 = daoSession.getMBNearbyPlayerDao().queryBuilder();
                q1.where(new WhereCondition.StringCondition("T.is_engaged=0"));
                List<MBNearbyPlayer> lstPlayers = q1.list();

                List<MBGameParticipants> l = daoSession.getMBGameParticipantsDao().loadAll();

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
                else {
                    ((IActionReadListener) this.iActionCRUDListener).onReadOperation(0, null);
                }
                break;
            case MobiMix.DBRequest.DB_UPDATE_GAME_TABLE:
                if(this.params.object_ != null) {
                    JSONObject object = params.object_;

                    String groupOwnerUserId = object.optString(GameConstants.GROUP_OWNER_USER_ID);
                    String connectedUserId = object.optString(GameConstants.CONNECTED_USER_ID);
                    long gameId = object.optLong(GameConstants.GAME_ID);
                    int connectionType = object.optInt(GameConstants.GAME_CONNECTION_TYPE);
                    int maxPlayers = object.optInt(GameConstants.GAME_MAX_PLAYERS);

                    int chkConnectionType = connectionType == 1 ? 2: 1;
                    Query<MBGameParticipants> query = daoSession.getMBGameParticipantsDao().queryRawCreate("where connection_type=? and game_id=?",
                            chkConnectionType, gameId);
                    List<MBGameParticipants> currentEntries = query.list();
                    if(currentEntries.size() > 0) {
                        daoSession.getMBGameParticipantsDao().deleteInTx(currentEntries);
                    }

                    MBNearbyPlayer groupOwnerPlayer = daoSession.getMBNearbyPlayerDao().load(groupOwnerUserId);
                    MBNearbyPlayer connectedPlayer = daoSession.getMBNearbyPlayerDao().load(connectedUserId);
                    MBGameInfo gameInfo = daoSession.getMBGameInfoDao().load(gameId);

                    MBGameParticipants gameParticipants = new MBGameParticipants();
                    gameParticipants.setConnectedPlayer(connectedPlayer);
                    gameParticipants.setConnnectedPlayerId(connectedPlayer.getPlayerId());

                    gameParticipants.setGroupOwnerPlayer(groupOwnerPlayer);
                    gameParticipants.setPlayerId(groupOwnerPlayer.getPlayerId());

                    gameParticipants.setGameInfo(gameInfo);
                    gameParticipants.setGameId(gameInfo.getGameId());

                    gameParticipants.setConnectionType(connectionType);
                    gameParticipants.setMaxPlayers(maxPlayers);
                    gameParticipants.setUpdatedAt(new Date());

                    daoSession.getMBGameParticipantsDao().insertOrReplace(gameParticipants);
                }
            break;
            case MobiMix.DBRequest.DB_FIND_GAME_FROM_ID:
                if(this.params.object_ != null) {
                    JSONObject object = params.object_;
                    long gameId = object.optLong(GameConstants.GAME_ID);
                    MBGameInfo gameInfo = MobiMixApplication.getInstance().getDaoSession().getMBGameInfoDao().load(gameId);

                    if(gameInfo != null) {
                        List<MBGameInfo> gameInfoList = new ArrayList<MBGameInfo>();
                        gameInfoList.add(gameInfo);
                        ((IActionReadListener) this.iActionCRUDListener).onReadOperation(0, gameInfoList);
                    }
                    else {
                        ((IActionReadListener) this.iActionCRUDListener).onReadOperation(DBError.GAME_NOT_FOUND.errorCode, null);
                    }
                }
                else {
                    ((IActionReadListener) this.iActionCRUDListener).onReadOperation(DBError.INCORRECT_INPUT.errorCode, null);
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
