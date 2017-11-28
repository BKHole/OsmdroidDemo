package com.bigemap.osmdroiddemo.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 *  response data collection
 * Created by Think on 2017/11/1.
 */

public class Data {
    @SerializedName("code")
    private String code;
    @SerializedName("timestamp")
    private String timestamp;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<Title> getTitles() {
        return titles;
    }

    public void setTitles(List<Title> titles) {
        this.titles = titles;
    }

    @SerializedName("tip_list")
    private List<Title> titles;

}
