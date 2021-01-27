package com.sscctv.seeeyesonvif.Items;

import android.os.Parcel;
import android.os.Parcelable;

public class IPDevice implements Parcelable {
    private String name;
    private int port;
    private String id;
    private String password;
    private String ipAddress;
    private String mac;
    private String rtspUri;
    private String loc;
    private String mode;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public IPDevice() {
    }

    public IPDevice(Parcel in) {
        readFromParcel(in);
    }

    public IPDevice(String _name, String _ipAddress, int _port, String _id, String _password) {
        this.name = _name;
        this.ipAddress = _ipAddress;
        this.port= _port;
        this.id = _id;
        this.password = _password;

    }

// -------------------------------------------------------------------------
// Getters & Setters section - 각 필드에 대한 get/set 메소드들
// 여기서는 생략했음
// ....
// ....
// -------------------------------------------------------------------------


    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(ipAddress);
        dest.writeInt(port);
        dest.writeString(id);
        dest.writeString(password);
        dest.writeString(rtspUri);
        dest.writeString(loc);
        dest.writeString(mac);
        dest.writeString(mode);

    }

    private void readFromParcel(Parcel in){
        name = in.readString();
        ipAddress = in.readString();
        port = in.readInt();
        id = in.readString();
        password = in.readString();
        rtspUri = in.readString();
        loc = in.readString();
        mac = in.readString();
        mode = in.readString();
    }

    public static final Creator CREATOR = new Creator() {
        public IPDevice createFromParcel(Parcel in) {
            return new IPDevice(in);
        }

        public IPDevice[] newArray(int size) {
            return new IPDevice[size];
        }
    };

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRtspUri() {
        return rtspUri;
    }

    public void setRtspUri(String rtspUri) {
        this.rtspUri = rtspUri;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
