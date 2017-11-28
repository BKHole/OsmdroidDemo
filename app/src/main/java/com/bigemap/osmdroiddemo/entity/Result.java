package com.bigemap.osmdroiddemo.entity;

import com.google.gson.annotations.SerializedName;


/**
 * location search result
 * Created by Think on 2017/11/1.
 */

public class Result {

    @SerializedName("status")
    private String status;
    @SerializedName("is_general_search")
    private String isSearch;
    @SerializedName("version")
    private String version;
    @SerializedName("result")
    private String result;
    @SerializedName("message")
    private String message;
    @SerializedName("total")
    private int total;

    @SerializedName("data")
    private Data data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIsSearch() {
        return isSearch;
    }

    public void setIsSearch(String isSearch) {
        this.isSearch = isSearch;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
