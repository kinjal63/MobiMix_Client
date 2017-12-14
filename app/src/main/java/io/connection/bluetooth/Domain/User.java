package io.connection.bluetooth.Domain;

import java.io.Serializable;
import java.util.List;

/**
 * Created by songline on 31/07/16.
 */
public class User implements Serializable {
    private String id;
    private String name;
    private String password;
    private String macAddress;
    private String bluetoothName;
    private String email;
    private String deviceUUID;
    private Long dob;
    private String gender;
    private String photoUrl;
    private Boolean emailVerified;
    private Long creationDate;
    private List<GameProfile> gameProfiles;
    private List<DeviceDetails> userDeviceDetailses;
    private Long startTime;
    private Long endTime;
    private Double latitude;
    private Double longitude;
    private List<GameProfileTimeDetails> gameProfileTime;

    public List<GameProfileTimeDetails> getGameProfileTime() {
        return gameProfileTime;
    }

    public void setGameProfileTime(List<GameProfileTimeDetails> gameProfileTime) {
        this.gameProfileTime = gameProfileTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getBluetoothName(String bluetoothName) {
        return this.bluetoothName;
    }

    public void setBluetoothName(String bluetoothName) {
        this.bluetoothName = bluetoothName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    public String getBluetoothName() {
        return bluetoothName;
    }

    public Long getDob() {
        return dob;
    }

    public void setDob(Long dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public List<GameProfile> getGameProfiles() {
        return gameProfiles;
    }

    public void setGameProfiles(List<GameProfile> gameProfiles) {
        this.gameProfiles = gameProfiles;
    }

    public List<DeviceDetails> getUserDeviceDetailses() {
        return userDeviceDetailses;
    }

    public void setUserDeviceDetailses(List<DeviceDetails> userDeviceDetailses) {
        this.userDeviceDetailses = userDeviceDetailses;
    }

    public Long getStartTime() {
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
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

}
