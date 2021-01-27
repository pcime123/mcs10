package com.sscctv.seeeyesonvif.Items;


public class ItemDoorDevice {

    private boolean isSelected, isRegistered;
    private String group;
    private String dMac, dModel, dIp, dNetMask, dGateWay, loc;
    private String cMac, cIp, cNetMask, cGateWay;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public ItemDoorDevice(String dMac, String dIp, String dNetMask, String dGateWay, String dModel, String cMac, String cIp, String cNetMask, String cGateWay,
                          String loc, String group) {
        this.dMac = dMac;
        this.dIp = dIp;
        this.dNetMask = dNetMask;
        this.dGateWay = dGateWay;
        this.dModel = dModel;
        this.cMac = cMac;
        this.cIp = cIp;
        this.cNetMask = cNetMask;
        this.cGateWay = cGateWay;
        this.loc = loc;
        this.group = group;

    }

    public String getdModel() {
        return dModel;
    }

    public void setdModel(String dModel) {
        this.dModel = dModel;
    }

    public String getdIp() {
        return dIp;
    }

    public void setdIp(String dIp) {
        this.dIp = dIp;
    }

    public String getdNetMask() {
        return dNetMask;
    }

    public void setdNetMask(String dNetMask) {
        this.dNetMask = dNetMask;
    }

    public String getdGateWay() {
        return dGateWay;
    }

    public void setdGateWay(String dGateWay) {
        this.dGateWay = dGateWay;
    }

    public String getdMac() {
        return dMac;
    }

    public void setdMac(String dMac) {
        this.dMac = dMac;
    }

    public String getcIp() {
        return cIp;
    }

    public void setcIp(String cIp) {
        this.cIp = cIp;
    }

    public String getcMac() {
        return cMac;
    }

    public void setcMac(String cMac) {
        this.cMac = cMac;
    }

    public String getcNetMask() {
        return cNetMask;
    }

    public void setcNetMask(String cNetMask) {
        this.cNetMask = cNetMask;
    }

    public String getcGateWay() {
        return cGateWay;
    }

    public void setcGateWay(String cGateWay) {
        this.cGateWay = cGateWay;
    }
}
