package io.connection.bluetooth.Domain;

/**
 * Created by Kinjal on 11/24/2016.
 */
public class GameInfo {
    private long gameId;
    private String gamneName;
    private String gamePackageName;
    private String gameImagePath;

    public String getGamePackageName() {
        return gamePackageName;
    }

    public void setGamePackageName(String gamePackageName) {
        this.gamePackageName = gamePackageName;
    }

    private int networkType;

    public int getNetworkType() {
        return networkType;
    }

    public void setNetworkType(int networkType) {
        this.networkType = networkType;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getGamneName() {
        return gamneName;
    }

    public void setGamneName(String gamneName) {
        this.gamneName = gamneName;
    }

    public String getGameImagePath() {
        return gameImagePath;
    }

    public void setGameImagePath(String gameImagePath) {
        this.gameImagePath = gameImagePath;
    }
}
