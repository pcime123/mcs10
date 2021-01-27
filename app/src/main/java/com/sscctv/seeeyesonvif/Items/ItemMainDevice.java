package com.sscctv.seeeyesonvif.Items;


public class ItemMainDevice {


    private String mMac, mIp, mModel, mName, rate;

    public ItemMainDevice(String mMac, String mIp, String mModel, String mName) {
        this.mMac = mMac;
        this.mIp = mIp;
        this.mModel = mModel;
        this.mName = mName;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getmMac() {
        return mMac;
    }

    public void setmMac(String mMac) {
        this.mMac = mMac;
    }

    public String getmIp() {
        return mIp;
    }

    public void setmIp(String mIp) {
        this.mIp = mIp;
    }

    public String getmModel() {
        return mModel;
    }

    public void setmModel(String mModel) {
        this.mModel = mModel;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }
}
