package com.bigemap.osmdroiddemo.overlay;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.entity.Coordinate;

import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 图形绘制坐标点标记
 * Created by Think on 2017/9/29.
 */

public class PointsOverlay extends ItemizedOverlay<OverlayItem> {
    private List<OverlayItem> pointItems = new ArrayList<OverlayItem>();
    private ArrayList<Coordinate> points;

    public PointsOverlay(Drawable pDefaultMarker) {
        super(pDefaultMarker);
    }
    public PointsOverlay(Context context){
        this(ContextCompat.getDrawable(context,R.drawable.ic_member_pos));
    }

    public void setPoints(ArrayList<Coordinate> coordinates){
        this.points=coordinates;
        refreshMarkers();

    }
    @Override
    protected OverlayItem createItem(int i) {
        return pointItems.get(i);
    }

    @Override
    public int size() {
        return pointItems.size();
    }

    @Override
    public boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView) {
        return false;
    }
    
    private void refreshMarkers(){
        pointItems.clear();

        for (Coordinate coordinate: points){
            GeoPoint geoPoint=new GeoPoint(coordinate.getLatitude(),coordinate.getLongitude());
//            String description=coordinate.getX()+","+coordinate.getY();
            OverlayItem item=new OverlayItem(coordinate.getName(), coordinate.getDescription(), geoPoint);
            pointItems.add(item);
        }
        populate();
    }
}
