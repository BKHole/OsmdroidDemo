package com.bigemap.osmdroiddemo.entity;

import org.litepal.crud.DataSupport;

/**
 * 离线地图信息
 * Created by Think on 2017/11/15.
 */

public class OfflineMap extends DataSupport{
    private int id;
    private String name;
    private String description;
    private String elePath;//电子地图路径
    private String satelPath;//卫星地图路径

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getElePath() {
        return elePath;
    }

    public void setElePath(String elePath) {
        this.elePath = elePath;
    }

    public String getSatelPath() {
        return satelPath;
    }

    public void setSatelPath(String satelPath) {
        this.satelPath = satelPath;
    }
}
