package io.connection.bluetooth.Api.response.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by KP49107 on 11-10-2017.
 */
public class PlayerGame {
    @SerializedName("gameId")
    @Expose
    private Integer gameId;
    @SerializedName("gameName")
    @Expose
    private String gameName;
    @SerializedName("gamePackageName")
    @Expose
    private String gamePackageName;
    @SerializedName("gameImagePath")
    @Expose
    private String gameImagePath;

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
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

}
