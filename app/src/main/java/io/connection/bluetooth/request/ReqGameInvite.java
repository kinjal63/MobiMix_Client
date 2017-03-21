package io.connection.bluetooth.request;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Kinjal on 12/18/2016.
 */

public class ReqGameInvite {
    @SerializedName("userId")
    public String userId;

    @SerializedName("email")
    public String email;

    @SerializedName("remoteUserIds")
    public ArrayList<String> remoteUserIds;

    @SerializedName("wifiDeviceAddress")
    public String wifiAddress;

    @SerializedName("bluetoothDeviceAddress")
    public String bluetoothAddress;

    public ReqGameInvite(String userId, String email, ArrayList<String> remoteUserIds, String wifiAddress, String bluetoothAddress) {
        this.userId = userId;
        this.email = email;
        this.remoteUserIds = remoteUserIds;
        this.wifiAddress = wifiAddress;
        this.bluetoothAddress = bluetoothAddress;
    }
}
