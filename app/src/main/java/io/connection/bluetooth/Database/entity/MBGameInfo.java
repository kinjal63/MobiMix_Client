package io.connection.bluetooth.Database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Unique;

import io.connection.bluetooth.Database.annotations.Exclude;

/**
 * Created by Kinjal on 10/8/2017.
 */
@Entity(nameInDb = "mb_game_info")
public class MBGameInfo {
    @Id
    @Property(nameInDb = "game_id")
    @Unique
    private long gameId;

    @Property(nameInDb = "game_name")
    private String gameName;

    @Property(nameInDb = "game_package_name")
    @Unique
    private String gamePackageName;

    @Property(nameInDb = "game_image_path")
    private String gameImagePath;

    @ToMany(referencedJoinProperty = "gameId")
    @Exclude
    private List<MBPlayerGames> playerGames;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1633434567)
    private transient MBGameInfoDao myDao;

    @Generated(hash = 583894352)
    public MBGameInfo(long gameId, String gameName, String gamePackageName,
            String gameImagePath) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.gamePackageName = gamePackageName;
        this.gameImagePath = gameImagePath;
    }

    @Generated(hash = 549549529)
    public MBGameInfo() {
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGamePackageName() {
        return gamePackageName;
    }

    public void setGamePackageName(String gamePackageName) {
        this.gamePackageName = gamePackageName;
    }

    public String getGameImagePath() {
        return gameImagePath;
    }

    public void setGameImagePath(String gameImagePath) {
        this.gameImagePath = gameImagePath;
    }

    public long getGameId() {
        return this.gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 727111660)
    public List<MBPlayerGames> getPlayerGames() {
        if (playerGames == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MBPlayerGamesDao targetDao = daoSession.getMBPlayerGamesDao();
            List<MBPlayerGames> playerGamesNew = targetDao
                    ._queryMBGameInfo_PlayerGames(gameId);
            synchronized (this) {
                if (playerGames == null) {
                    playerGames = playerGamesNew;
                }
            }
        }
        return playerGames;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 406221209)
    public synchronized void resetPlayerGames() {
        playerGames = null;
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
    @Generated(hash = 1433930595)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMBGameInfoDao() : null;
    }
}
