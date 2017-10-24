package io.connection.bluetooth.Database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by Kinjal on 10/8/2017.
 */

@Entity(nameInDb = "mb_player_games", indexes = {@Index(value = "gameId, playerId", unique = true)})
public class MBPlayerGames {
    @Id
    private Integer id;

    @Property(nameInDb = "game_id")
    private Long gameId;

    @Property(nameInDb = "player_id")
    private String playerId;

    @Generated(hash = 1725855511)
    public MBPlayerGames(Integer id, Long gameId, String playerId) {
        this.id = id;
        this.gameId = gameId;
        this.playerId = playerId;
    }

    @Generated(hash = 982500114)
    public MBPlayerGames() {
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
}
