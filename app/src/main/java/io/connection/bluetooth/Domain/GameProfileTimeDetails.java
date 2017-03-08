package io.connection.bluetooth.Domain;

/**
 * Created by songline on 21/12/16.
 */
public class GameProfileTimeDetails {

    private String gameProfileTimeId;
    private String gameProfileTimeSchedule;
    private Long gameProfileStartTime;
    private Long gameProfileEndTime;
    private User user;

    public String getGameProfileTimeId() {
        return gameProfileTimeId;
    }

    public void setGameProfileTimeId(String gameProfileTimeId) {
        this.gameProfileTimeId = gameProfileTimeId;
    }

    public String getGameProfileTimeSchedule() {
        return gameProfileTimeSchedule;
    }

    public void setGameProfileTimeSchedule(String gameProfileTimeSchedule) {
        this.gameProfileTimeSchedule = gameProfileTimeSchedule;
    }

    public Long getGameProfileStartTime() {
        return gameProfileStartTime;
    }

    public void setGameProfileStartTime(Long gameProfileStartTime) {
        this.gameProfileStartTime = gameProfileStartTime;
    }

    public Long getGameProfileEndTime() {
        return gameProfileEndTime;
    }

    public void setGameProfileEndTime(Long gameProfileEndTime) {
        this.gameProfileEndTime = gameProfileEndTime;
    }

    public void setUserId(User user) {
        this.user = user;
    }
}
