package com.bigemap.osmdroiddemo.tileSource;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.IStyledTileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

/**
 * 谷歌卫星地图
 * Created by Think on 2017/9/1.
 */

public class GoogleSatelliteTileSource extends OnlineTileSourceBase implements IStyledTileSource<Integer> {
    public GoogleSatelliteTileSource(String aName, int aZoomMinLevel, int aZoomMaxLevel, int aTileSizePixels, String aImageFilenameEnding, String[] aBaseUrl) {
        super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, aBaseUrl);
    }

    @Override
    public String getTileURLString(MapTile aTile) {
        return getBaseUrl() + "&x=" + aTile.getX() + "&y=" + aTile.getY() + "&z=" + aTile.getZoomLevel();
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
}
