package io.connection.bluetooth.Api.response.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by KP49107 on 11-10-2017.
 */
public class NearByPlayer {
    @SerializedName("playerId")
    @Expose
    private String playerId;
    @SerializedName("playerName")
    @Expose
    private String playerName;
    @SerializedName("playerImagePath")
    @Expose
    private String playerImagePath;
    @SerializedName("isEngaged")
    @Expose
    private Integer isEngaged;
    @SerializedName("activeGameName")
    @Expose
    private String activeGameName;
    @SerializedName("isGroupOwner")
    @Expose
    private Integer isGroupOwner;
    @SerializedName("playerGames")
    @Expose
    private List<PlayerGame> playerGames = null;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerImagePath() {
        return playerImagePath;
    }

    public void setPlayerImagePath(String playerImagePath) {
        this.playerImagePath = playerImagePath;
    }

    public Integer getIsEngaged() {
        return isEngaged;
    }

    public void setIsEngaged(Integer isEngaged) {
        this.isEngaged = isEngaged;
    }

    public String getActiveGameName() {
        return activeGameName;
    }

    public void setActiveGameName(String activeGameName) {
        this.activeGameName = activeGameName;
    }

    public Integer getIsGroupOwner() {
        return isGroupOwner;
    }

    public void setIsGroupOwner(Integer isGroupOwner) {
        this.isGroupOwner = isGroupOwner;
    }

    public List<PlayerGame> getPlayerGameList() {
        return playerGames;
    }

    public void setPlayerGameList(List<PlayerGame> playerGameList) {
        this.playerGames = playerGameList;
    }
}
