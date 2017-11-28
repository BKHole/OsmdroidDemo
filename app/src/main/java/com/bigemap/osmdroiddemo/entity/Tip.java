package com.bigemap.osmdroiddemo.entity;

import com.bigemap.osmdroiddemo.utils.PositionUtils;
import com.google.gson.annotations.SerializedName;

import org.osmdroid.util.GeoPoint;

/**
 * location information
 * Created by Think on 2017/11/2.
 */

public class Tip {
    @SerializedName("category")
    private String category;
    @SerializedName("name")
    private String name;
    @SerializedName("district")
    private String district;
    @SerializedName("ignore_district")
    private String ignoreDistrict;
    @SerializedName("adcode")
    private String adcode;
    @SerializedName("rank")
    private String rank;
    @SerializedName("datatype_spec")
    private String datatypeSpec;
    @SerializedName("datatype")
    private String dataType;
    @SerializedName("address")
    private String address;
    @SerializedName("poiid")
    private String poiId;
    @SerializedName("x")
    private String x;
    @SerializedName("y")
    private String y;
    @SerializedName("x_entr")
    private String xEntr;
    @SerializedName("y_entr")
    private String yEntr;
    @SerializedName("id")
    private String id;
    @SerializedName("modxy")
    private String modxy;
    @SerializedName("lnglat")
    private String lnglat;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getIgnoreDistrict() {
        return ignoreDistrict;
    }

    public void setIgnoreDistrict(String ignoreDistrict) {
        this.ignoreDistrict = ignoreDistrict;
    }

    public String getAdcode() {
        return adcode;
    }

    public void setAdcode(String adcode) {
        this.adcode = adcode;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getDatatypeSpec() {
        return datatypeSpec;
    }

    public void setDatatypeSpec(String datatypeSpec) {
        this.datatypeSpec = datatypeSpec;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPoiId() {
        return poiId;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getxEntr() {
        return xEntr;
    }

    public void setxEntr(String xEntr) {
        this.xEntr = xEntr;
    }

    public String getyEntr() {
        return yEntr;
    }

    public void setyEntr(String yEntr) {
        this.yEntr = yEntr;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModxy() {
        return modxy;
    }

    public void setModxy(String modxy) {
        this.modxy = modxy;
    }

    public String getLnglat() {
        return lnglat;
    }

    public void setLnglat(String lnglat) {
        this.lnglat = lnglat;
    }

    public GeoPoint getPoint(){
        return PositionUtils.stringToPoint(getLnglat());
    }
}
