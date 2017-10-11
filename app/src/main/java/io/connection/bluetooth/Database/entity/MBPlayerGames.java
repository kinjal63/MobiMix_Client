package io.connection.bluetooth.Database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

/**
 * Created by Kinjal on 10/8/2017.
 */

@Entity(nameInDb = "mb_player_games")
public class MBPlayerGames {
    @Id
    private int id;

    @Property(nameInDb = "game_id")
    private long gameId;

    @Property(nameInDb = "player_id")
    private String playerId;

    @ToOne()
    private MBGameInfo gameInfo;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 454677320)
    private transient MBPlayerGamesDao myDao;

    @Generated(hash = 509337577)
    public MBPlayerGames(int id, long gameId, String playerId) {
        this.id = id;
        this.gameId = gameId;
        this.playerId = playerId;
    }

    @Generated(hash = 982500114)
    public MBPlayerGames() {
    }
    @Generated(hash = 766443881)
    private transient boolean gameInfo__refreshed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getGameId() {
        return gameId;
    }
    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 2136837220)
    public MBGameInfo getGameInfo() {
        if (gameInfo != null || !gameInfo__refreshed) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MBGameInfoDao targetDao = daoSession.getMBGameInfoDao();
            targetDao.refresh(gameInfo);
            gameInfo__refreshed = true;
        }
        return gameInfo;
    }

    /** To-one relationship, returned entity is not refreshed and may carry only the PK property. */
    @Generated(hash = 787206429)
    public MBGameInfo peakGameInfo() {
        return gameInfo;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 632524047)
    public void setGameInfo(MBGameInfo gameInfo) {
        synchronized (this) {
            this.gameInfo = gameInfo;
            gameInfo__refreshed = true;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 465254229)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMBPlayerGamesDao() : null;
    }
}
