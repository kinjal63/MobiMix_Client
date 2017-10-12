package io.connection.bluetooth.Database.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

/**
 * Created by Kinjal on 10/8/2017.
 */

@Entity(nameInDb = "mb_player_games")
public class MBPlayerGames {
    @Id
    private int id;

    @Property(nameInDb = "player_id")
    private String playerId;

    @ToOne(joinProperty = "game_id")
    private MBGameInfo gameInfo;

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
}
