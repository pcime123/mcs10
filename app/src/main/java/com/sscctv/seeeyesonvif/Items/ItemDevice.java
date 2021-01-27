package com.sscctv.seeeyesonvif.Items;


public  class ItemDevice {

    private String model, type, ip, mac, version, mask, gate, loc, http, https, rate;
    private boolean isSelected, isRegistered;
    private String call, group;
    private String rtspUri;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getHttp() {
        return http;
    }

    public void setHttp(String http) {
        this.http = http;
    }

    public String getHttps() {
        return https;
    }

    public void setHttps(String https) {
        this.https = https;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getGate() {
        return gate;
    }

    public void setGate(String gate) {
        this.gate = gate;
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

    public String getCall() {
        return call;
    }

    public void setCall(String call) {
        this.call = call;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public String getRtspUri() {
        return rtspUri;
    }

    public void setRtspUri(String rtspUri) {
        this.rtspUri = rtspUri;
    }

    public ItemDevice(String model, String mac, String type, String ip, String mask, String gate, String version, String http, String https, String loc, String group, String rtspUri, boolean isRegistered) {
        this.model = model;
        this.type = type;
        this.ip = ip;
        this.mac = mac;
        this.version = version;
        this.mask = mask;
        this.gate = gate;
        this.http = http;
        this.https = https;
        this.loc = loc;
        this.group = group;
        this.rtspUri = rtspUri;
        this.isRegistered = isRegistered;

    }


}
