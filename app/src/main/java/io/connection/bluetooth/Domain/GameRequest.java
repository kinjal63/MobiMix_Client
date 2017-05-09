package io.connection.bluetooth.Domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by KP49107 on 09-05-2017.
 */
public class GameRequest implements Parcelable {
    @SerializedName("connection_invite")
    private int connectionInvite;

    @SerializedName("remote_user_id")
    private int remoteUserId;

    @SerializedName("remote_user_name")
    private int remoteUserName;

    @SerializedName("game_id")
    private int gameId;

    @SerializedName("game_name")
    private int gameName;

    @SerializedName("bluetooth_address")
    private int bluetoothAddress;

    @SerializedName("wifi_address")
    private int wifiAddress;


    public int getBluetoothAddress() {
        return bluetoothAddress;
    }

    public void setBluetoothAddress(int bluetoothAddress) {
        this.bluetoothAddress = bluetoothAddress;
    }

    public int getConnectionInvite() {
        return connectionInvite;
    }

    public void setConnectionInvite(int connectionInvite) {
        this.connectionInvite = connectionInvite;
    }

    public int getRemoteUserId() {
        return remoteUserId;
    }

    public void setRemoteUserId(int remoteUserId) {
        this.remoteUserId = remoteUserId;
    }

    public int getRemoteUserName() {
        return remoteUserName;
    }

    public void setRemoteUserName(int remoteUserName) {
        this.remoteUserName = remoteUserName;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getGameName() {
        return gameName;
    }

    public void setGameName(int gameName) {
        this.gameName = gameName;
    }

    public int getWifiAddress() {
        return wifiAddress;
    }

    public void setWifiAddress(int wifiAddress) {
        this.wifiAddress = wifiAddress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString("");
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public GameRequest createFromParcel(Parcel in) {
            return new GameRequest(in);
        }

        public GameRequest[] newArray(int size) {
            return new GameRequest[size];
        }
    };
}
