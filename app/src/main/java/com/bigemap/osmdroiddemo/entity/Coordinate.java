package com.bigemap.osmdroiddemo.entity;

/**
 * kml解析实体类，存储kml解析后的关键数据
 * Created by Think on 2017/9/25.
 */

public class Coordinate {
    private double x;
    private double y;
    private String name;
    private String costValue;//描述

    public Coordinate(double x, double y, String name)
    {
        this.x = x;
        this.y = y;
        this.name = name;
    }
    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }
    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCostValue() {
        return costValue;
    }

    public void setCostValue(String costValue) {
        this.costValue = costValue;
    }
}
