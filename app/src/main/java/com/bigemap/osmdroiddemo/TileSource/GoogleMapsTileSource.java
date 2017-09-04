package com.bigemap.osmdroiddemo.TileSource;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.IStyledTileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

/**
 * 谷歌标准地图
 * Created by libotao on 2017/8/24.
 */

public class GoogleMapsTileSource extends OnlineTileSourceBase implements IStyledTileSource<Integer> {

    public static final String GoogleVectorMap="http://mt3.google.cn/vt/lyrs=m@142&hl=zh-CN&gl=cn";

    public GoogleMapsTileSource(String aName, int aZoomMinLevel, int aZoomMaxLevel, int aTileSizePixels, String aImageFilenameEnding, String[] aBaseUrl) {
        super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, aBaseUrl);
    }

    @Override
    public void setStyle(Integer style) {

    }

    @Override
    public void setStyle(String style) {

    }

    @Override
    public Integer getStyle() {
        return null;
    }

    @Override
    public String getTileURLString(MapTile aTile) {
        return getBaseUrl() + "&x=" + aTile.getX() + "&y=" + aTile.getY() + "&z=" + aTile.getZoomLevel();
    }
}
