package io.connection.bluetooth.Database;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Database.action.IActionCRUD;
import io.connection.bluetooth.Database.action.IActionReadListener;
import io.connection.bluetooth.Database.entity.MBGameInfo;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.Database.entity.MBPlayerGames;
import io.connection.bluetooth.Database.entity.MBPlayerGamesDao;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;

/**
 * Created by Kinjal on 10/15/2017.
 */

public class AsyncOperation extends Thread {
    private DBParams params;
    private IActionCRUD iActionCRUDListener;

    public AsyncOperation(DBParams params, IActionCRUD iActionCRUD) {
        this.params = params;
        this.iActionCRUDListener = iActionCRUD;
    }

    @Override
    public void run() {
        switch (this.params.event_) {
            case MobiMix.DBRequest.DB_FIND_NEARBY_PLAYERS:
                List<MBNearbyPlayer> lstPlayers = MobiMixApplication.getInstance().getDaoSession().loadAll(MBNearbyPlayer.class);
                List<MBPlayerGames> playerGames2 = MobiMixApplication.getInstance().getDaoSession().getMBPlayerGamesDao().loadAll();
                ((IActionReadListener)this.iActionCRUDListener).onReadOperation(0, lstPlayers);
                break;

            case MobiMix.DBRequest.DB_FIND_MUTUAL_GAMES:
                if(params.remoteUserIds_ != null) {
                    QueryBuilder qb = MobiMixApplication.getInstance().getDaoSession().getMBGameInfoDao().queryBuilder();
                    qb.join(MBPlayerGames.class, MBPlayerGamesDao.Properties.GameId)
                            .where(new WhereCondition.StringCondition(MBPlayerGamesDao.Properties.PlayerId.in(params.remoteUserIds_) +
                                    " group by " + MBPlayerGamesDao.Properties.GameId +
                                    " having count(" + MBPlayerGamesDao.Properties.GameId + ") > " + params.remoteUserIds_.size()));
                    List<MBGameInfo> playerGames = qb.list();

                    ((IActionReadListener) this.iActionCRUDListener).onReadOperation(0, playerGames);
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
