package io.connection.bluetooth.adapter.model;

/**
 * Created by Kinjal on 7/23/2017.
 */

public class MyGameInfo {

    private String userId;
    private String groupOwnerId;
    private int isEngaged;
    private String currentActiveGame;
    private int allowedPlayersCount;

    public int getAllowedPlayersCount() {
        return allowedPlayersCount;
    }

    public void setAllowedPlayersCount(int allowedPlayersCount) {
        this.allowedPlayersCount = allowedPlayersCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupOwnerId() {
        return groupOwnerId;
    }

    public void setGroupOwnerId(String groupOwnerId) {
        this.groupOwnerId = groupOwnerId;
    }

    public int getIsEngaged() {
        return isEngaged;
    }

    public void setIsEngaged(int isEngaged) {
        this.isEngaged = isEngaged;
    }

    public String getCurrentActiveGame() {
        return currentActiveGame;
    }

    public void setCurrentActiveGame(String currentActiveGame) {
        this.currentActiveGame = currentActiveGame;
    }
}
