package io.connection.bluetooth.request;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Kinjal on 12/18/2016.
 */

public class ReqGameInvite {
    @SerializedName("userId")
    public String userId;

    @SerializedName("remoteUserIds")
    public ArrayList<String> remoteUserIds;

    @SerializedName("wifiDeviceAddress")
    public String wifiAddress;

    @SerializedName("bluetoothDeviceAddress")
    public String bluetoothAddress;

    public ReqGameInvite(String userId, ArrayList<String> remoteUserIds, String wifiAddress, String bluetoothAddress) {
        this.userId = userId;
        this.remoteUserIds = remoteUserIds;
        this.wifiAddress = wifiAddress;
        this.bluetoothAddress = bluetoothAddress;
    }
}
