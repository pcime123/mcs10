package com.sscctv.seeeyesonvif.Service;

import android.os.Parcel;
import android.os.Parcelable;

public class CallDevice implements Parcelable {
    private String ipAddress;
    private String netMask;
    private String gateway;
    private String macAddress;
    private String status;
    private String response;


    public CallDevice() {
    }

    public CallDevice(Parcel in) {
        readFromParcel(in);
    }

    public CallDevice(String ipAddress, String netMask, String gateway, String macAddress, String status) {
        this.ipAddress = ipAddress;
        this.netMask = netMask;
        this.gateway = gateway;
        this.macAddress = macAddress;
        this.status = status;

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
        dest.writeString(ipAddress);
        dest.writeString(netMask);
        dest.writeString(gateway);
        dest.writeString(macAddress);
        dest.writeString(status);
        dest.writeString(response);

    }

    private void readFromParcel(Parcel in){
        ipAddress = in.readString();
        netMask = in.readString();
        gateway = in.readString();
        macAddress = in.readString();
        status = in.readString();
        response = in.readString();

    }

    public static final Creator CREATOR = new Creator() {
        public CallDevice createFromParcel(Parcel in) {
            return new CallDevice(in);
        }

        public CallDevice[] newArray(int size) {
            return new CallDevice[size];
        }
    };

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getNetMask() {
        return netMask;
    }

    public void setNetMask(String netMask) {
        this.netMask = netMask;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
