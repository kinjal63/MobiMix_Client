package io.connection.bluetooth.Database.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

/**
 * Created by Kinjal on 10/7/2017.
 */
@Entity(nameInDb = "mb_nearby_players")
public class MBNearbyPlayers {
    @Id
    private long id;

    @Property(nameInDb = "player_id")
    private String playerID;

    @Property(nameInDb = "player_name")
    private String playerName;

    @Property(nameInDb = "player_image_path")
    private String playerImagePath;

    @Property(nameInDb = "is_engaged")
    private int isEngaged;

    @Property(nameInDb = "active_game_name")
    private String activeGameName;

    @Property(nameInDb = "is_group_owner")
    private int isGroupOwner;

    @ToMany(joinProperties = {@JoinProperty(name = "player_id", referencedName = "player_id")})
    private List<MBPlayerGames> playerGameList;

    public String getPlayerID() {
        return playerID;
    }

    public void setPlayerID(String playerID) {
        this.playerID = playerID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
