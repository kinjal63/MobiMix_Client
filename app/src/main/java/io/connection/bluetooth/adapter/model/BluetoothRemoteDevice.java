package io.connection.bluetooth.adapter.model;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by KP49107 on 14-04-2017.
 */
public class BluetoothRemoteDevice implements Parcelable {
    private BluetoothDevice device;
    private String name;

    public BluetoothRemoteDevice(BluetoothDevice device, String name) {
        this.device = device;
        this.name = name;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.device);
        dest.writeString(this.name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static Parcelable.Creator<BluetoothRemoteDevice> CREATOR =
            new Parcelable.Creator<BluetoothRemoteDevice>() {
                @Override
                public BluetoothRemoteDevice createFromParcel(Parcel source) {
                    BluetoothDevice device = (BluetoothDevice)source.readValue(null);
                    String name = source.readString();
                    return new BluetoothRemoteDevice(device, name);
                }

                @Override
                public BluetoothRemoteDevice[] newArray(int size) {
                    return new BluetoothRemoteDevice[0];
                }
            };

    public String getName() {
        return this.name;
    }

    public BluetoothDevice getDevice() {
        return this.device;
    }
}
