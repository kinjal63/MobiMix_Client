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

    @SerializedName("remote_user_name")
    private String remoteUserName;

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

    public String getRemoteUserName() {
        return remoteUserName;
    }

    public void setRemoteUserName(String remoteUserName) {
        this.remoteUserName = remoteUserName;
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
        bundle.putString("remote_user_name", this.remoteUserName);
        bundle.putString("bluetooth_address", this.bluetoothAddress);
        bundle.putString("wifi_address", this.wifiAddress);
        dest.writeBundle(bundle);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public GameRequest createFromParcel(Parcel in) {
            Bundle bundle = new Bundle();
            GameRequest gameRequest = new GameRequest();
            gameRequest.setConnectionType(bundle.getInt("connection_type"));
            gameRequest.setGameId(bundle.getLong("game_id"));
            gameRequest.setGameName(bundle.getString("game_name"));
            gameRequest.setGamePackageName(bundle.getString("game_package_name"));
            gameRequest.setRemoteUserId(bundle.getString("remote_user_id"));
            gameRequest.setRemoteUserName(bundle.getString("remote_user_name"));
            gameRequest.setBluetoothAddress(bundle.getString("bluetooth_address"));
            gameRequest.setWifiAddress(bundle.getString("wifi_address"));

            return gameRequest;
        }

        public GameRequest[] newArray(int size) {
            return new GameRequest[size];
        }
    };
}
