package io.connection.bluetooth.Api.response.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by KP49107 on 11-10-2017.
 */
public class PlayerGame {
    @SerializedName("game_id")
    @Expose
    private Long gameId;
    @SerializedName("game_name")
    @Expose
    private String gameName;
    @SerializedName("game_package_name")
    @Expose
    private String gamePackageName;
    @SerializedName("game_image_path")
    @Expose
    private String gameImagePath;

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
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
