package io.connection.bluetooth.Api.response.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by KP49107 on 11-10-2017.
 */
public class NearByPlayerResponse {
    @SerializedName("players")
    @Expose
    private List<NearByPlayer> players = null;

    public List<NearByPlayer> getPlayerlist() {
        return players;
    }

    public void setPlayerlist(List<NearByPlayer> playerlist) {
        this.players = playerlist;
    }
}
