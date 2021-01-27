package com.sscctv.seeeyesonvif.Settings;

public class ItemRange {

    private int num;
    private String result;

    public void setNum(int num) {
        this.num = num;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getNum() {
        return num;
    }

    public String getResult() {
        return result;
    }

    public ItemRange(int num, String result) {
        this.num = num;
        this.result = result;
    }
}
