package io.connection.bluetooth.Domain;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by KP49107 on 09-05-2017.
 */
public class GameRequest implements Parcelable {
    @SerializedName("connection_type")
    private int connectionType;

    @SerializedName("notification_type")
    private int notificationType;

    @SerializedName("remote_user_id")
    private String remoteUserId;

    @SerializedName("requester_user_id")
    private String requesterUserId;

    @SerializedName("remote_user_name")
    private String requesterUserName;

    @SerializedName("game_id")
    private long gameId;

    @SerializedName("game_name")
    private String gameName;

    @SerializedName("game_package_name")
    private String gamePackageName;

    @SerializedName("bluetooth_address")
    private String bluetoothAddress;

    @SerializedName("wifi_address")
    private String wifiAddress;

    @SerializedName("socket_address")
    private String socketAddress;

    public String getBluetoothAddress() {
        return bluetoothAddress;
    }

    public void setBluetoothAddress(String bluetoothAddress) {
        this.bluetoothAddress = bluetoothAddress;
    }

    public int getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(int connectionType) {
        this.connectionType = connectionType;
    }

    public String getRemoteUserId() {
        return remoteUserId;
    }

    public void setRemoteUserId(String remoteUserId) {
        this.remoteUserId = remoteUserId;
    }

    public String getRequesterUserName() {
        return requesterUserName;
    }

    public void setRequesterUserName(String requesterUserName) {
        this.requesterUserName = requesterUserName;
    }

    public String getRequesterUserId() {
        return requesterUserId;
    }

    public void setRequesterUserId(String requesterUserId) {
        this.requesterUserId = requesterUserId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getWifiAddress() {
        return wifiAddress;
    }

    public void setWifiAddress(String wifiAddress) {
        this.wifiAddress = wifiAddress;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }

    public String getGamePackageName() {
        return gamePackageName;
    }

    public void setGamePackageName(String gamePackageName) {
        this.gamePackageName = gamePackageName;
    }

    public String getSocketAddress() {
        return socketAddress;
    }

    public void setSocketAddress(String socketAddress) {
        this.socketAddress = socketAddress;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return this.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putLong("game_id", this.gameId);
        bundle.putString("game_name", this.gameName);
        bundle.putString("game_package_name", this.gamePackageName);
        bundle.putString("remote_user_id", this.remoteUserId);
        bundle.putString("requester_user_name", this.requesterUserName);
        bundle.putString("bluetooth_address", this.bluetoothAddress);
        bundle.putString("wifi_address", this.wifiAddress);
        bundle.putInt("notification_type", this.notificationType);
        bundle.putInt("connection_type", this.connectionType);
        bundle.putString("socket_address", this.socketAddress);
        dest.writeBundle(bundle);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public GameRequest createFromParcel(Parcel in) {
            Bundle bundle = in.readBundle();
            GameRequest gameRequest = new GameRequest();
            gameRequest.setConnectionType(bundle.getInt("connection_type"));
            gameRequest.setGameId(bundle.getLong("game_id"));
            gameRequest.setGameName(bundle.getString("game_name"));
            gameRequest.setGamePackageName(bundle.getString("game_package_name"));
            gameRequest.setRemoteUserId(bundle.getString("remote_user_id"));
            gameRequest.setRequesterUserName(bundle.getString("requester_user_name"));
            gameRequest.setBluetoothAddress(bundle.getString("bluetooth_address"));
            gameRequest.setWifiAddress(bundle.getString("wifi_address"));
            gameRequest.setNotificationType(bundle.getInt("notification_type"));
            gameRequest.setSocketAddress(bundle.getString("socket_address"));
            return gameRequest;
        }

        public GameRequest[] newArray(int size) {
            return new GameRequest[size];
        }
    };
}
