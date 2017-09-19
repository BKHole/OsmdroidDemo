package com.bigemap.osmdroiddemo.activity;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.TileSource.GoogleMapsTileSource;
import com.bigemap.osmdroiddemo.TileSource.GoogleSatelliteTileSource;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.utils.PositionUtils;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends CheckPermissionsActivity implements View.OnClickListener {

    private static final String TAG = "TestActivity";
    public static final String GOOGLE_MAP = "Google Map";
    public static final String GOOGLE_SATELLITE = "Google卫星图";
    public static final String OSM = "OpenStreetMap";
    private MapView mapView;
    //地图旋转
    private RotationGestureOverlay mRotationGestureOverlay;
    //比例尺
    private ScaleBarOverlay mScaleBarOverlay;
    //指南针方向
    private CompassOverlay mCompassOverlay = null;
    //设置导航图标的位置
    private MyLocationNewOverlay myLocationOverlay;
    //设置自定义定位，缩小，放大
    private ImageView location, zoomIn, zoomOut, mapMode, addPoint, centerPoint;
    private int selectedTileSource = 0;//默认选中地图值
    private GeoPoint convertedPoint;
    private List<GeoPoint> points;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mapView = $(R.id.mapView);
        points = new ArrayList<>();
        initView();
        initTileSource();

        mapView.setDrawingCacheEnabled(true);
        mapView.setTilesScaledToDpi(true);//图源比例转换屏幕像素
        mapView.getController().setZoom(15);
        mapView.setMultiTouchControls(true);//触摸放大、缩小操作

        //地图自由旋转
        mRotationGestureOverlay = new RotationGestureOverlay(mapView);
        mapView.getOverlays().add(this.mRotationGestureOverlay);
        //比例尺
        final DisplayMetrics dm = getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(mapView);
        mScaleBarOverlay.setAlignRight(true);
        mScaleBarOverlay.setAlignBottom(true);
//        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 5, 35);
        mapView.getOverlays().add(mScaleBarOverlay);

        //显示指南针
        mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapView);
        mapView.getOverlays().add(this.mCompassOverlay);

        //显示定位
        GpsMyLocationProvider gps=new GpsMyLocationProvider(this);
        gps.addLocationSource(LocationManager.NETWORK_PROVIDER);
        myLocationOverlay=new MyLocationNewOverlay(gps, mapView);
        myLocationOverlay.setDrawAccuracyEnabled(false);
        mapView.getOverlays().add(myLocationOverlay);

    }

    private void initView() {
        zoomIn = $(R.id.btn_zoom_in);
        zoomOut = $(R.id.btn_zoom_out);
        location = $(R.id.btn_location);
        mapMode = $(R.id.btn_map_mode);
        centerPoint=$(R.id.btn_center);
        addPoint=$(R.id.btn_point_add);
        location.setOnClickListener(this);
        zoomIn.setOnClickListener(this);
        zoomOut.setOnClickListener(this);
        mapMode.setOnClickListener(this);
        addPoint.setOnClickListener(this);
    }

    private void initTileSource(){
        GoogleMapsTileSource tileSource = new GoogleMapsTileSource(GOOGLE_MAP, 1, 20, 256, ".png",
                new String[]{Constant.URL_MAP_GOOGLE});//加载图源
        TileSourceFactory.addTileSource(tileSource);
        GoogleSatelliteTileSource satelliteTileSource=new GoogleSatelliteTileSource(GOOGLE_SATELLITE,
                1, 20, 256, ".png", new String[]{Constant.URL_MAP_GOOGLE_SATELLITE});
        TileSourceFactory.addTileSource(satelliteTileSource);

        mapView.setTileSource(tileSource);
    }

    @Override
    protected void onResume() {
        super.onResume();

        myLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: first location="+myLocationOverlay.getLastFix());
                convertedPoint=PositionUtils.gps84_To_Gcj02(myLocationOverlay.getMyLocation());
//                setRoundPoint(convertedPoint);
                mapView.getController().setCenter(convertedPoint);
            }
        });
        mCompassOverlay.enableCompass();
        myLocationOverlay.enableMyLocation();
        mScaleBarOverlay.enableScaleBar();

    }

    @Override
    protected void onPause() {
        super.onPause();
        myLocationOverlay.disableMyLocation();
        mCompassOverlay.disableCompass();
        mScaleBarOverlay.disableScaleBar();

    }

    protected final <T extends View> T $(int id) {
        return (T) findViewById(id);
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
    }

    private void setRoundPoint(GeoPoint point){
        Drawable icon=ContextCompat.getDrawable(this,R.drawable.ic_member_pos);
        Marker marker=new Marker(mapView);
        marker.setPosition(point);
        marker.setIcon(icon);
        marker.setDraggable(true);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_location:
                switch (selectedTileSource){
                    case Constant.GOOGLE_MAP:
                    case Constant.GOOGLE_SATELLITE:
                        mapView.getController().animateTo(convertedPoint);
                        break;
                    case Constant.OSM:
                        mapView.getController().animateTo(myLocationOverlay.getMyLocation());
                        break;
                }
                break;
            case R.id.btn_zoom_in:
                mapView.getController().zoomIn();//默认按级缩放
                break;
            case R.id.btn_zoom_out:
                mapView.getController().zoomOut();
                break;
            case R.id.btn_map_mode:
                createMapModeView();
                break;
            case R.id.btn_point_add:
                GeoPoint centerPoint = (GeoPoint) mapView.getMapCenter();
                setRoundPoint(centerPoint);
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
                break;
        }
    }

    //地图种类选择项
    private void createMapModeView() {
        // 创建数据
        final String[] items = new String[]{GOOGLE_MAP, GOOGLE_SATELLITE, OSM};
        // 创建对话框构建器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 设置参数

        builder.setTitle("地图种类")
                .setSingleChoiceItems(items, selectedTileSource, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case Constant.GOOGLE_MAP://GOOGLE_MAP
                                if (TileSourceFactory.containsTileSource(GOOGLE_MAP)) {
                                    mapView.setTileSource(TileSourceFactory.getTileSource(GOOGLE_MAP));
                                    mapView.getController().setCenter(convertedPoint);

                                }
                                break;
                            case Constant.GOOGLE_SATELLITE://GOOGLE_SATELLITE
                                if (TileSourceFactory.containsTileSource(GOOGLE_SATELLITE)) {
                                    mapView.setTileSource(TileSourceFactory.getTileSource(GOOGLE_SATELLITE));
                                    mapView.getController().setCenter(convertedPoint);
                                }
                                break;
                            case Constant.OSM://OSM
                                mapView.setTileSource(TileSourceFactory.MAPNIK);
                                mapView.getController().setCenter(myLocationOverlay.getMyLocation());
                                mapView.postInvalidate();
                        }
                        selectedTileSource = which;
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
}
