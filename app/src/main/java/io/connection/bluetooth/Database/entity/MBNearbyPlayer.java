package io.connection.bluetooth.Database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;

import java.io.Serializable;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Unique;

import io.connection.bluetooth.Database.annotations.Exclude;

/**
 * Created by Kinjal on 10/7/2017.
 */
@Entity(nameInDb = "mb_nearby_players")
public class MBNearbyPlayer implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Property(nameInDb = "player_id")
    private String playerId;

    @Property(nameInDb = "player_name")
    private String playerName;

    @Property(nameInDb = "email")
    private String email;

    @Property(nameInDb = "player_image_path")
    private String playerImagePath;

    @Property(nameInDb = "is_engaged")
    private int isEngaged;

    @Property(nameInDb = "active_game_name")
    private String activeGameName;

    @Property(nameInDb = "is_group_owner")
    private int isGroupOwner;

    @Property(nameInDb = "group_owner_user_id")
    private String groupOwnerUserId;

    @Property(nameInDb = "max_players")
    private int maxPlayers;

    @ToMany
    @JoinEntity(entity = MBPlayerGames.class, sourceProperty = "playerId", targetProperty = "gameId")
    @Exclude
    private List<MBGameInfo> playerGames;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1708284874)
    private transient MBNearbyPlayerDao myDao;

    @Generated(hash = 2023716044)
    public MBNearbyPlayer(String playerId, String playerName, String email, String playerImagePath,
            int isEngaged, String activeGameName, int isGroupOwner, String groupOwnerUserId,
            int maxPlayers) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.email = email;
        this.playerImagePath = playerImagePath;
        this.isEngaged = isEngaged;
        this.activeGameName = activeGameName;
        this.isGroupOwner = isGroupOwner;
        this.groupOwnerUserId = groupOwnerUserId;
        this.maxPlayers = maxPlayers;
    }

    @Generated(hash = 763795256)
    public MBNearbyPlayer() {
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerImagePath() {
        return this.playerImagePath;
    }

    public void setPlayerImagePath(String playerImagePath) {
        this.playerImagePath = playerImagePath;
    }

    public int getIsEngaged() {
        return this.isEngaged;
    }

    public void setIsEngaged(int isEngaged) {
        this.isEngaged = isEngaged;
    }

    public String getActiveGameName() {
        return this.activeGameName;
    }

    public void setActiveGameName(String activeGameName) {
        this.activeGameName = activeGameName;
    }

    public int getIsGroupOwner() {
        return this.isGroupOwner;
    }

    public void setIsGroupOwner(int isGroupOwner) {
        this.isGroupOwner = isGroupOwner;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 193937895)
    public List<MBGameInfo> getPlayerGames() {
        if (playerGames == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MBGameInfoDao targetDao = daoSession.getMBGameInfoDao();
            List<MBGameInfo> playerGamesNew = targetDao._queryMBNearbyPlayer_PlayerGames(playerId);
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

    public String getGroupOwnerUserId() {
        return groupOwnerUserId;
    }

    public void setGroupOwnerUserId(String groupOwnerUserId) {
        this.groupOwnerUserId = groupOwnerUserId;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setPlayerGames(List<MBGameInfo> playerGames) {
        this.playerGames = playerGames;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 364374368)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMBNearbyPlayerDao() : null;
    }
}
