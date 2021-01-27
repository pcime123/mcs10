package com.sscctv.seeeyesonvif.Items;


public class ItemGroup {
    private String model, ip, mac, loc;
    private String group;
    private int type;
    private boolean isSelected;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public ItemGroup(int type, String group, String model, String ip, String mac, String loc) {
        this.type = type;
        this.group = group;
        this.model = model;
        this.ip = ip;
        this.mac = mac;
        this.loc = loc;

    }
}
