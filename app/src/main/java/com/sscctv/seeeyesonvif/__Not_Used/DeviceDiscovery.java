package com.sscctv.seeeyesonvif.__Not_Used;

import android.os.Parcel;
import android.os.Parcelable;

public class DeviceDiscovery implements Parcelable {
    private String name;
    private String ip;
    private int port;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public DeviceDiscovery() {}

    public DeviceDiscovery(Parcel in) {
        readFromParcel(in);
    }

    public DeviceDiscovery(String _name, String _ip, int _port) {
        this.name = _name;
        this.ip = _ip;
        this.port = _port;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(port);
        dest.writeString(ip);
    }

    private void readFromParcel(Parcel in) {
        name = in.readString();
        port = in.readInt();
        ip = in.readString();

    }

    public static final Creator CREATOR = new Creator() {
        public DeviceDiscovery createFromParcel(Parcel in) {
            return new DeviceDiscovery(in);
        }

        public DeviceDiscovery[] newArray(int size) {
            return new DeviceDiscovery[size];
        }
    };




}
