package com.bigemap.osmdroiddemo.utils;

import org.osmdroid.util.BoundingBox;


public class BoundBox extends BoundingBox {

    public BoundBox(double north, double east, double south, double west) {
        super(north, east, south, west);
    }

    BoundBox tile2boundingBox(final int x, final int y, final int zoom) {
        double north = tile2lat(y, zoom);
        double south = tile2lat(y + 1, zoom);
        double west = tile2lon(x, zoom);
        double east = tile2lon(x + 1, zoom);
        return new BoundBox(north,east, south, west);
    }

    private double tile2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    private double tile2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }
}
