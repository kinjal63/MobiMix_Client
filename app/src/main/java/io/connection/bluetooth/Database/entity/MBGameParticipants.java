package io.connection.bluetooth.Database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

/**
 * Created by Kinjal on 11/4/2017.
 */
@Entity(nameInDb = "mb_game_participants")
public class MBGameParticipants implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    @Property(nameInDb = "game_id")
    @Unique
    private long gameId;

    @Property(nameInDb = "player_id")
    @Unique
    private String playerId;

    @Property(nameInDb = "connected_player_id")
    @Unique
    private String connnectedPlayerId;

    @ToOne(joinProperty = "gameId")
    private MBGameInfo gameInfo;

    @ToOne(joinProperty = "playerId")
    private MBNearbyPlayer groupOwnerPlayer;

    @ToOne(joinProperty = "connnectedPlayerId")
    private MBNearbyPlayer connectedPlayer;

    @Property(nameInDb = "connection_type")
    private Integer connectionType;

    @Property(nameInDb = "max_players")
    private Integer maxPlayers;

    @Property(nameInDb = "updated_at")
    private Date updatedAt;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1346775092)
    private transient MBGameParticipantsDao myDao;

    @Generated(hash = 187027227)
    public MBGameParticipants(Long id, long gameId, String playerId,
            String connnectedPlayerId, Integer connectionType, Integer maxPlayers,
            Date updatedAt) {
        this.id = id;
        this.gameId = gameId;
        this.playerId = playerId;
        this.connnectedPlayerId = connnectedPlayerId;
        this.connectionType = connectionType;
        this.maxPlayers = maxPlayers;
        this.updatedAt = updatedAt;
    }

    @Generated(hash = 818036183)
    public MBGameParticipants() {
    }

    @Generated(hash = 404944660)
    private transient Long gameInfo__resolvedKey;
    @Generated(hash = 1556648346)
    private transient String groupOwnerPlayer__resolvedKey;
    @Generated(hash = 623169029)
    private transient String connectedPlayer__resolvedKey;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Keep
    public MBGameInfo getGameInfo() {
        return gameInfo;
    }

    @Keep
    public void setGameInfo(MBGameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    @Keep
    public MBNearbyPlayer getGroupOwnerPlayer() {
        return groupOwnerPlayer;
    }

    @Keep
    public void setGroupOwnerPlayer(MBNearbyPlayer groupOwnerPlayer) {
        this.groupOwnerPlayer = groupOwnerPlayer;
    }

    @Keep
    public MBNearbyPlayer getConnectedPlayer() {
        return connectedPlayer;
    }

    @Keep
    public void setConnectedPlayer(MBNearbyPlayer connectedPlayer) {
        this.connectedPlayer = connectedPlayer;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getConnnectedPlayerId() {
        return connnectedPlayerId;
    }

    public void setConnnectedPlayerId(String connnectedPlayerId) {
        this.connnectedPlayerId = connnectedPlayerId;
    }

    public Integer getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(Integer connectionType) {
        this.connectionType = connectionType;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
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
    @Generated(hash = 1746999955)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMBGameParticipantsDao() : null;
    }
}
