package io.connection.bluetooth.Domain;

/**
 * Created by Kinjal on 1/28/2018.
 */

public class DataUsageModel {
    private String userId;
    private String rssi;
    private String networkOperatorId;
    private double latitude;
    private double longitude;
    private long timeStamp;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String getNetworkOperatorId() {
        return networkOperatorId;
    }

    public void setNetworkOperatorId(String networkOperatorId) {
        this.networkOperatorId = networkOperatorId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
