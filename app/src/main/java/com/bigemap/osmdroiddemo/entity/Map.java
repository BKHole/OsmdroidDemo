package com.bigemap.osmdroiddemo.entity;

/**
 * 地图源
 * Created by Think on 2017/10/18.
 */

public class Map {
    private int mapIcon;
    private String mapName;

    public int getMapIcon() {
        return mapIcon;
    }

    public void setMapIcon(int mapIcon) {
        this.mapIcon = mapIcon;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public Map(int mapIcon, String mapName) {
        this.mapIcon = mapIcon;
        this.mapName = mapName;
    }
}
