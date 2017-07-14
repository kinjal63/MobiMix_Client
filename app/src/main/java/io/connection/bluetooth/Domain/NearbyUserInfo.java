package io.connection.bluetooth.Domain;

import java.util.List;

/**
 * Created by Kinjal on 11/24/2016.
 */
public class NearbyUserInfo {
    private String userId;
    private String userImagePath;
    private String userFirstName;
    private String userLastName;
    private int isEngaged;
    private long activeGameId;

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    private List<GameInfo> gameInfoList;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserImagePath() {
        return userImagePath;
    }

    public void setUserImagePath(String userImagePath) {
        this.userImagePath = userImagePath;
    }

    public List<GameInfo> getGameInfoList() {
        return gameInfoList;
    }

    public void setGameInfoList(List<GameInfo> gameInfoList) {
        this.gameInfoList = gameInfoList;
    }

    public int isEngaged() {
        return isEngaged;
    }

    public void setEngaged(int engaged) {
        isEngaged = engaged;
    }

    public long getActiveGameId() {
        return activeGameId;
    }

    public void setActiveGameId(long activeGameId) {
        this.activeGameId = activeGameId;
    }
}
