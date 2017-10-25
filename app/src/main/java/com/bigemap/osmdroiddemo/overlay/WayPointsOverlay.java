package com.bigemap.osmdroiddemo.overlay;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.entity.Location;
import com.bigemap.osmdroiddemo.entity.Track;

import org.litepal.crud.DataSupport;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 轨迹图层
 * Created by Think on 2017/9/22.
 */

public class WayPointsOverlay extends ItemizedOverlay<OverlayItem> {
    /**
     * List of waypoints to display on the map.
     */
    private List<OverlayItem> wayPointItems = new ArrayList<OverlayItem>();

    private long trackId;

    public WayPointsOverlay(Context ctx, Drawable pDefaultMarker, long trackId) {
        super(pDefaultMarker);
        this.trackId = trackId;
        refresh();
    }

    public WayPointsOverlay(final Context pContext,final long trackId) {
        this(pContext,pContext.getResources().getDrawable(R.drawable.ic_member_pos), trackId);
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

    private void refresh() {
        wayPointItems.clear();

        List<Location> locations= DataSupport.findAll(Location.class,trackId);
        Track track=DataSupport.find(Track.class, trackId);
        for (Location location: locations) {
            GeoPoint point=new GeoPoint(Double.valueOf(location.getLatitude()),Double.valueOf(location.getLongitude()));
            OverlayItem i = new OverlayItem(track.getName(),track.getName(), point);
            wayPointItems.add(i);
        }
        populate();
    }
}
