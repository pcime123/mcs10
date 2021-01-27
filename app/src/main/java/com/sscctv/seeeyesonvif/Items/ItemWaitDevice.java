package com.sscctv.seeeyesonvif.Items;

public class ItemWaitDevice {
    private String callNum;
    private String callModel;
    private String callIp;
    private String callMac;
    private String callLoc;
    private String callDate;
    private String callTime;
    private String callType;
    private String callStatus;

    public ItemWaitDevice(String callNum, String callModel, String callIp, String callMac, String callLoc, String callDate, String callTime, String callType, String callStatus) {
        this.callNum = callNum;
        this.callModel = callModel;
        this.callIp = callIp;
        this.callMac = callMac;
        this.callLoc = callLoc;
        this.callDate = callDate;
        this.callTime = callTime;
        this.callType = callType;
        this.callStatus = callStatus;
    }

    public String getCallNum() {
        return callNum;
    }

    public void setCallNum(String callNum) {
        this.callNum = callNum;
    }

    public String getCallModel() {
        return callModel;
    }

    public void setCallModel(String callModel) {
        this.callModel = callModel;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallIp() {
        return callIp;
    }

    public void setCallIp(String callIp) {
        this.callIp = callIp;
    }

    public String getCallMac() {
        return callMac;
    }

    public void setCallMac(String callMac) {
        this.callMac = callMac;
    }

    public String getCallDate() {
        return callDate;
    }

    public void setCallDate(String callDate) {
        this.callDate = callDate;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public String getCallTime() {
        return callTime;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }

    public String getCallLoc() {
        return callLoc;
    }

    public void setCallLoc(String callLoc) {
        this.callLoc = callLoc;
    }
}
