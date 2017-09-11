package com.bigemap.osmdroiddemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bigemap.osmdroiddemo.TileSource.GoogleMapsTileSource;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.utils.LocationUtils;
import com.bigemap.osmdroiddemo.utils.PositionUtils;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestActivity extends CheckPermissionsActivity {

    private static final String TAG = "TestActivity";
    private LocationManager locationManager;
    private Location location = null;
    private MapView mapView;
    private ScaleBarOverlay scaleBarOverlay;
    private CompassOverlay compassOverlay;
    private MyLocationNewOverlay myLocationOverlay;
    private ImageButton btnLocation, mapMode, zoomIn;
    private List<GeoPoint> points;
    private IMapController mController;
    private GeoPoint convertedPoint;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        btnLocation = (ImageButton) findViewById(R.id.btn_location);
        mapMode = (ImageButton) findViewById(R.id.btn_map_mode);
        zoomIn = (ImageButton) findViewById(R.id.btn_zoom_in);
        initTileSource();
        initListener();
        scaleBarOverlay = new ScaleBarOverlay(mapView);
        mController = mapView.getController();
        mController.setZoom(16);//地图显示级别
        //设置显示指南针
        compassOverlay = new CompassOverlay(mapView.getContext(), new InternalCompassOrientationProvider(mapView.getContext()), mapView);
        //显示图层
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);

//        GpsMyLocationProvider gpsProvider = new GpsMyLocationProvider(this);
//        gpsProvider.addLocationSource(LocationManager.NETWORK_PROVIDER);
//        myLocationOverlay = new MyLocationNewOverlay(gpsProvider, mapView);
//        myLocationOverlay.runOnFirstFix(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.d(TAG, String.format("First location fix: %s", myLocationOverlay.getLastFix()));
//                    }
//                }
//        );

//        GeoPoint startPoint = new GeoPoint(30.569692, 104.06185);
        GeoPoint startPoint = new GeoPoint(30.570127, 104.061845);
        GeoPoint endPoint = new GeoPoint(30.334141, 104.31532);
        GeoPoint thirdPoint = new GeoPoint(30.324221, 104.42091);
        points = new ArrayList<>();

//        setPolyline(points);
//        setPolygon(points);

        mapView.getOverlays().add(scaleBarOverlay);
        mapView.getOverlays().add(compassOverlay);
//        mapView.getOverlays().add(myLocationOverlay);
//        myLocationOverlay.enableMyLocation();
//        Log.d(TAG, "this.location is null....");

    }

    private void initListener() {
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (convertedPoint!=null){
                    mapView.getController().animateTo(convertedPoint);
                }
            }
        });

        mapMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoPoint centerPoint = (GeoPoint) mapView.getMapCenter();
                setPoint(centerPoint);
                points.add(centerPoint);
//                if (points.size()>1){
                Log.d(TAG, "onClick: " + points.size());
                setPolyline(points);
                mapView.invalidate();
                int distance = 0;
                for (int i = 0; i < points.size() - 1; i++) {
                    distance += getDistance(points.get(i), points.get(i + 1));
                }
                Toast.makeText(TestActivity.this, "总长" + distance + "米", Toast.LENGTH_LONG).show();
//                }

            }
        });
        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                points.clear();
                Log.d(TAG, "clear all points");
            }
        });
    }

    /**
     * 点
     * @param point
     */
    private void setPoint(GeoPoint point) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle("坐标：lat="+point.getLatitude()+",lng="+point.getLongitude());
        marker.setFlat(true);//设置marker平贴地图效果
        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }

    /**
     * 线
     * @param points
     */
    private void setPolyline(List<GeoPoint> points) {
        Polyline polyline = new Polyline();
        polyline.setWidth(10);
        polyline.setColor(R.color.colorAccent);
        polyline.setPoints(points);
        mapView.getOverlays().add(polyline);
    }

    /**
     * 面
     * @param points
     */
    private void setPolygon(List<GeoPoint> points) {
        Polygon polygon = new Polygon();
        polygon.setStrokeWidth(10);
        polygon.setStrokeColor(R.color.colorAccent);
        polygon.setFillColor(R.color.colorPrimary);
        polygon.setPoints(points);
        mapView.getOverlays().add(polygon);
    }

    /**
     * 计算两点间距离
     *
     * @param point1
     * @param point2
     * @return distance in meters
     */
    private int getDistance(GeoPoint point1, GeoPoint point2) {
        return point1.distanceTo(point2);
    }

    /**
     * 通过GPS获取定位信息
     */
    public GeoPoint getGPSLocation() {
        Location gps = LocationUtils.getGPSLocation(this);
        GeoPoint geoPoint=null;
        if (gps == null) {
            //设置定位监听，因为GPS定位，第一次进来可能获取不到，通过设置监听，可以在有效的时间范围内获取定位信息
            LocationUtils.addLocationListener(this, LocationManager.GPS_PROVIDER, new LocationUtils.ILocationListener() {
                @Override
                public void onSuccessLocation(Location location) {
                    if (location != null) {
                        Log.d(TAG, "gps location onSuccessLocation: lat=="+location.getLatitude()+", lng=="+location.getLongitude());
                        Log.d(TAG, "provider="+location.getProvider());
                    } else {
                        Log.d(TAG, "gps location is null");
                    }
                }
            });
        } else {
            geoPoint=new GeoPoint(gps.getLatitude(), gps.getLongitude());
            Log.d(TAG, "gps location: lat==" + gps.getLatitude() + "  lng==" + gps.getLongitude());
            Log.d(TAG, "provider="+gps.getProvider());
        }
        return geoPoint;
    }

    /**
     * 通过网络等获取定位信息
     */
    private GeoPoint getNetworkLocation() {
        Location net = LocationUtils.getNetWorkLocation(this);
        GeoPoint geoPoint=null;
        if (net == null) {
            Log.d(TAG, "net location is null");
        } else {
            geoPoint=new GeoPoint(net.getLatitude(), net.getLongitude());
            Log.d(TAG, "network location: lat== "+net.getLatitude()+", lng=="+net.getLongitude());
            Log.d(TAG, "provider=="+net.getProvider());
        }
        return geoPoint;
    }
    /**
     * 获取最佳定位方式
     *
     * @return
     */
    private GeoPoint getBestLocation() {
        Criteria c = new Criteria();//Criteria类是设置定位的标准信息（系统会根据你的要求，匹配最适合你的定位供应商），一个定位的辅助信息的类
        c.setPowerRequirement(Criteria.POWER_LOW);//设置低耗电
        c.setAltitudeRequired(true);//设置需要海拔
        c.setBearingAccuracy(Criteria.ACCURACY_COARSE);//设置COARSE精度标准
        c.setAccuracy(Criteria.ACCURACY_FINE);//设置高精度
        Location bestLocation = LocationUtils.getBestLocation(this, c);
        GeoPoint geoPoint=null;
        if (bestLocation != null) {
            geoPoint=new GeoPoint(bestLocation);
            Log.d(TAG, "longitude:" + bestLocation.getLongitude());
            Log.d(TAG, "latitude:" + bestLocation.getLatitude());
            Log.d(TAG, "provider:" + bestLocation.getProvider());
        }
        return geoPoint;
    }

    private void initTileSource() {
        GoogleMapsTileSource tileSource = new GoogleMapsTileSource(MainActivity.GOOGLE_MAP, 1, 20, 256, ".png",
                new String[]{Constant.URL_MAP_GOOGLE});
        mapView.setTileSource(tileSource);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GeoPoint origin = getNetworkLocation();
        if (origin!=null){
            convertedPoint = PositionUtils.gps84_To_Gcj02(origin.getLatitude(), origin.getLongitude());
            setPoint(convertedPoint);
            mController.setCenter(convertedPoint);
        }else{
            GeoPoint endPoint = new GeoPoint(30.334141, 104.31532);
            mController.setCenter(endPoint);
        }

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0
// , 0f, this);
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, this);
//        }
//        myLocationOverlay.enableFollowLocation();
//        myLocationOverlay.enableMyLocation();
        compassOverlay.enableCompass();
        scaleBarOverlay.enableScaleBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        myLocationOverlay.disableMyLocation();
        LocationUtils.unRegisterListener(this);//取消监听
//        locationManager.removeUpdates(this);
    }

    /*@Override
    public void onLocationChanged(Location location) {
//        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: " + provider);
    }*/
}
