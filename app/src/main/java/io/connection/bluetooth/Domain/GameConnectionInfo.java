package io.connection.bluetooth.Domain;

import com.google.gson.annotations.SerializedName;

/**
 * Created by KP49107 on 10-05-2017.
 */
public class GameConnectionInfo {
    @SerializedName("user_id")
    private String userId;

    @SerializedName("connected_user_id")
    private String connectedUserId;

    @SerializedName("game_id")
    private long gameId;

    @SerializedName("is_group_owner")
    private int isGroupOwner;

    @SerializedName("is_need_to_notify")
    private boolean isNeedToNotify;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getConnectedUserId() {
        return connectedUserId;
    }

    public void setConnectedUserId(String connectedUserId) {
        this.connectedUserId = connectedUserId;
    }

    public int getIsGroupOwner() {
        return isGroupOwner;
    }

    public void setIsGroupOwner(int isGroupOwner) {
        this.isGroupOwner = isGroupOwner;
    }

    public boolean getIsNeedToNotify() {
        return isNeedToNotify;
    }

    public void setIsNeedToNotify(boolean isNeedToNotify) {
        this.isNeedToNotify = isNeedToNotify;
    }
}
