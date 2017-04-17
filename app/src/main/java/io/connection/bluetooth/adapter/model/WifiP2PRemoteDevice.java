package io.connection.bluetooth.adapter.model;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kinjal on 14-04-2017.
 */
public class WifiP2PRemoteDevice implements Parcelable {
    private WifiP2pDevice device;
    private String name;

    public WifiP2PRemoteDevice(WifiP2pDevice device, String name) {
        this.device = device;
        this.name = name;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(device);
        dest.writeString(name);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<WifiP2PRemoteDevice> CREATOR =
            new Parcelable.Creator<WifiP2PRemoteDevice>() {
                @Override
                public WifiP2PRemoteDevice createFromParcel(Parcel source) {
                    WifiP2pDevice device = (WifiP2pDevice)source.readValue(null);
                    String name = source.readString();
                    return new WifiP2PRemoteDevice(device, name);
                }

                public WifiP2PRemoteDevice[] newArray(int size) {
                    return new WifiP2PRemoteDevice[size];
                }
            };

    public String getName() {
        return this.name;
    }

    public WifiP2pDevice getDevice() {
        return this.device;
    }
}
