package io.connection.bluetooth.Api.response.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Property;

import java.util.List;

/**
 * Created by KP49107 on 11-10-2017.
 */
public class NearByPlayer {
    @SerializedName("player_id")
    @Expose
    private String playerId;
    @SerializedName("player_name")
    @Expose
    private String playerName;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("player_image_path")
    @Expose
    private String playerImagePath;
    @SerializedName("is_engaged")
    @Expose
    private Integer isEngaged;
    @SerializedName("active_game_name")
    @Expose
    private String activeGameName;

    @SerializedName("is_group_owner")
    @Expose
    private Integer isGroupOwner;

    @SerializedName("group_owner_user_id")
    @Expose
    private String groupOwnerUserId;

    @SerializedName("max_players")
    @Expose
    private int maxPlayers;

    @SerializedName("player_games")
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getGroupOwnerUserId() {
        return groupOwnerUserId;
    }

    public void setGroupOwnerUserId(String groupOwnerUserId) {
        this.groupOwnerUserId = groupOwnerUserId;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public List<PlayerGame> getPlayerGameList() {
        return playerGames;
    }

    public void setPlayerGameList(List<PlayerGame> playerGameList) {
        this.playerGames = playerGameList;
    }
}
