package io.connection.bluetooth.Database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

import java.io.Serializable;
import java.util.Date;

import io.connection.bluetooth.Database.annotations.Column;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by Kinjal on 11/12/2017.
 */
@Entity(nameInDb = "mb_user_availability")
public class MBUserAvailability implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Property(nameInDb = "player_id")
    @Unique
    private String playerId;

    @ToOne(joinProperty = "playerId")
    private MBNearbyPlayer player;

    @Property(nameInDb = "is_engaged")
    private int isEngaged;

    @Property(nameInDb = "updated_at")
    private Date updatedAt;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 346251757)
    private transient MBUserAvailabilityDao myDao;

    @Generated(hash = 1310028163)
    public MBUserAvailability(Long id, String playerId, int isEngaged,
            Date updatedAt) {
        this.id = id;
        this.playerId = playerId;
        this.isEngaged = isEngaged;
        this.updatedAt = updatedAt;
    }

    @Generated(hash = 1563106388)
    public MBUserAvailability() {
    }

    @Generated(hash = 1826707175)
    private transient String player__resolvedKey;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    @Keep
    public MBNearbyPlayer getPlayer() {
        return player;
    }

    @Keep
    public void setPlayer(MBNearbyPlayer player) {
        this.player = player;
    }

    public int getIsEngaged() {
        return isEngaged;
    }

    public void setIsEngaged(int isEngaged) {
        this.isEngaged = isEngaged;
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
    @Generated(hash = 1309805482)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMBUserAvailabilityDao() : null;
    }
}
