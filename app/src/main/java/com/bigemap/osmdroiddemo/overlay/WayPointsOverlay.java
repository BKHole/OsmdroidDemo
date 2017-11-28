package com.bigemap.osmdroiddemo.overlay;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;


public class WayPointsOverlay extends ItemizedOverlay<OverlayItem> {
    private List<OverlayItem> wayPointItems;

    public WayPointsOverlay(Drawable pDefaultMarker) {
        super(pDefaultMarker);
        wayPointItems=new ArrayList<>();
    }

    public void addItem(GeoPoint geoPoint){
        String title="坐标：纬度=" + geoPoint.getLatitude() + ",经度=" + geoPoint.getLongitude();
        OverlayItem item=new OverlayItem(title, "", geoPoint);
        wayPointItems.add(item);
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return wayPointItems.get(i);
    }

    @Override
    public int size() {
        return wayPointItems.size();
    }

    @Override
    public boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView) {
        return false;
    }
}
