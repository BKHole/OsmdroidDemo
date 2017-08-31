package com.bigemap.osmdroiddemo.overlay;

import android.graphics.Canvas;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

/**
 * 点图层
 */

public class PointOverlay extends Overlay {

    static PointOverlay pointOverlay = null;

    public static synchronized PointOverlay getInstance() {
        if (pointOverlay == null) {
            pointOverlay = new PointOverlay();
        }
        return pointOverlay;
    }

    @Override
    public void draw(Canvas c, MapView osmv, boolean shadow) {

    }
}
