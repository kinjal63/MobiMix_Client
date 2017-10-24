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
    private Long gameId;

    @Property(nameInDb = "game_name")
    private String gameName;

    @Property(nameInDb = "game_package_name")
    @Unique
    private String gamePackageName;

    @Property(nameInDb = "game_image_path")
    private String gameImagePath;

    @Generated(hash = 1100796333)
    public MBGameInfo(Long gameId, String gameName, String gamePackageName,
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

    public Long getGameId() {
        return this.gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
}
