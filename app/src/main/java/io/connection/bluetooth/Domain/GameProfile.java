package io.connection.bluetooth.Domain;


import java.io.Serializable;

public class GameProfile implements Serializable{

    private String gameProfileId;
    private User userId;
    private GameLibrary gameLibrary;
   /* private Long startTime;
    private Long endTime;*/
    private Long creationDate;
    private Boolean isAvailable;

    public String getGameProfileId() {
        return gameProfileId;
    }

    public void setGameProfileId(String gameProfileId) {
        this.gameProfileId = gameProfileId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public GameLibrary getGameLibrary() {
        return gameLibrary;
    }

    public void setGameLibrary(GameLibrary gameLibrary) {
        this.gameLibrary = gameLibrary;
    }

/*    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }*/

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }
}
