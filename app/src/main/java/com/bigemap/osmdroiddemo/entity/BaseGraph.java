package com.bigemap.osmdroiddemo.entity;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * 点，线，面基础对象
 * Created by Think on 2017/11/9.
 */

public class BaseGraph {
    private int id;
    private String name;
    private String type;
    private List<GeoPoint> geoPoints;

    public BaseGraph(){
        geoPoints=new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<GeoPoint> getGeoPoints() {
        return geoPoints;
    }

    public void setGeoPoints(List<GeoPoint> geoPoints) {
        this.geoPoints = geoPoints;
    }
}
