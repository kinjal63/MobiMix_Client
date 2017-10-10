package io.connection.bluetooth.Database.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

/**
 * Created by Kinjal on 10/8/2017.
 */
@Entity(nameInDb = "mb_game_info")
public class MBGameInfo {
    @Property(nameInDb = "game_name")
    private String gameName;

    @Property(nameInDb = "game_package_name")
    private String gamePackageName;

    @Property(nameInDb = "game_image_path")
    private String gameImagePath;

    @Property(nameInDb = "game_network_type")
    private int gameNetworkType;

    @ToOne(joinProperty = "game_id")
    private MBPlayerGames playerGames;

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

    public int getGameNetworkType() {
        return gameNetworkType;
    }

    public void setGameNetworkType(int gameNetworkType) {
        this.gameNetworkType = gameNetworkType;
    }
}
