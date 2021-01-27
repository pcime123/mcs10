package com.sscctv.seeeyesonvif.Map.utils;

import android.widget.ImageView;

public class ItemMapConfig {

    private ImageView icon;
    private String model;
    private String ip;
    private String mac;
    private String loc;
    private String group;
    private ImageView tag;
    private ImageView remove;
    private boolean isTagged;
    private boolean isSelected;
    private float xPos;
    private float yPos;

    public ItemMapConfig(String model, String mac, String ip, String group, String loc, boolean isTagged) {
        this.model = model;
        this.mac = mac;
        this.ip = ip;
        this.group = group;
        this.loc = loc;
        this.isTagged = isTagged;
    }

    public ItemMapConfig() {
    }

    public float getxPos() {
        return xPos;
    }

    public void setxPos(float xPos) {
        this.xPos = xPos;
    }

    public float getyPos() {
        return yPos;
    }

    public void setyPos(float yPos) {
        this.yPos = yPos;
    }

    public String getMac() {
        return mac;
    }

    public boolean isTagged() {
        return isTagged;
    }

    public void setTagged(boolean tagged) {
        isTagged = tagged;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public ImageView getIcon() {
        return icon;
    }

    public void setIcon(ImageView icon) {
        this.icon = icon;
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

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public ImageView getTag() {
        return tag;
    }

    public void setTag(ImageView tag) {
        this.tag = tag;
    }

    public ImageView getRemove() {
        return remove;
    }

    public void setRemove(ImageView remove) {
        this.remove = remove;
    }
}
