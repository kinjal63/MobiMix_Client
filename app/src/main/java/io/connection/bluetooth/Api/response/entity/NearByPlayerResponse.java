package io.connection.bluetooth.Api.response.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by KP49107 on 11-10-2017.
 */
public class NearByPlayerResponse implements Serializable {
    @SerializedName("playerlist")
    private List<NearByPlayer> players;

    public List<NearByPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<NearByPlayer> players) {
        this.players = players;
    }
}
