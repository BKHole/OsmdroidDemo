package com.bigemap.osmdroiddemo.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.bigemap.osmdroiddemo.utils.LocationUtils;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    public static final String GOOGLE_MAP = "Google Map";
    public static final String GOOGLE_SATELLITE = "Google卫星图";
    public static final String OSM = "OpenStreetMap";
    private SharedPreferences mPrefs;
    private MapView mapView;
    //地图旋转
    private RotationGestureOverlay mRotationGestureOverlay;
    //比例尺
    private ScaleBarOverlay mScaleBarOverlay;
    //指南针方向
    private CompassOverlay mCompassOverlay = null;
    //设置导航图标的位置
    private LocationManager lm;
    private Location currentLocation = null;
    //设置自定义定位，缩小，放大
    private ImageView location, zoomIn, zoomOut, mapMode, addPoint, centerPoint;
    private int selectedValue = 0;//默认选中地图值
    private GeoPoint convertedPoint;
    private List<GeoPoint> points;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mapView = $(R.id.mapView);
        points = new ArrayList<>();
        initView();
        initTileSource();

        Log.d(TAG, mapView.getTileProvider().getTileSource().name());
        mapView.setDrawingCacheEnabled(true);
        mapView.setTilesScaledToDpi(true);//图源比例转换屏幕像素
        mapView.setUseDataConnection(true);//使用网络数据
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
        mCompassOverlay.enableCompass();
        mapView.getOverlays().add(this.mCompassOverlay);
        GeoPoint originPoint = getNetworkLocation();
        if (originPoint !=null){
            convertedPoint = PositionUtils.gps84_To_Gcj02(originPoint);
            setPoint(convertedPoint);
            Log.d(TAG, "onResume: convertedPoint lat="+convertedPoint.getLatitude()+", lng="+convertedPoint.getLongitude());
            mapView.getController().setCenter(convertedPoint);
        }else{
            GeoPoint endPoint = new GeoPoint(30.334141, 104.31532);
            setPoint(endPoint);
            mapView.getController().setCenter(endPoint);
        }
//        final int zoomLevel = mPrefs.getInt(Constant.PREFS_ZOOM_LEVEL, mPrefs.getInt(Constant.PREFS_ZOOM_LEVEL, 1));
//        mapView.getController().setZoom(zoomLevel);
//        final float orientation = mPrefs.getFloat(Constant.PREFS_ORIENTATION, 0);
//        mapView.setMapOrientation(orientation);
//        final String latitudeString = mPrefs.getString(Constant.PREFS_LATITUDE_STRING, null);
//        final String longitudeString = mPrefs.getString(Constant.PREFS_LONGITUDE_STRING, null);
//        if (latitudeString == null || longitudeString == null) { // case handled for historical reasons only
//            final int scrollX = mPrefs.getInt(Constant.PREFS_SCROLL_X, 0);
//            final int scrollY = mPrefs.getInt(Constant.PREFS_SCROLL_Y, 0);
//            mapView.scrollTo(scrollX, scrollY);
//        } else {
//            final double latitude = Double.valueOf(latitudeString);
//            final double longitude = Double.valueOf(longitudeString);
//            mapView.getController().setCenter(new GeoPoint(latitude, longitude));
//        }

//        mapView.getController().setCenter(new GeoPoint(30.679879, 104.064855));

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
        mCompassOverlay.enableCompass();
        mScaleBarOverlay.enableScaleBar();
    }

    @Override
    protected void onPause() {
//        final SharedPreferences.Editor edit = mPrefs.edit();
//        edit.putString(Constant.PREFS_TILE_SOURCE, mapView.getTileProvider().getTileSource().name());
//        edit.putFloat(Constant.PREFS_ORIENTATION, mapView.getMapOrientation());
//        edit.putString(Constant.PREFS_LATITUDE_STRING, String.valueOf(mapView.getMapCenter().getLatitude()));
//        edit.putString(Constant.PREFS_LONGITUDE_STRING, String.valueOf(mapView.getMapCenter().getLongitude()));
//        edit.putInt(Constant.PREFS_ZOOM_LEVEL, mapView.getZoomLevel());
//        edit.putBoolean(Constant.PREFS_SHOW_LOCATION, mLocationOverlay.isMyLocationEnabled());
//        edit.putBoolean(Constant.PREFS_SHOW_COMPASS, mCompassOverlay.isCompassEnabled());
//        edit.commit();
        super.onPause();
        LocationUtils.unRegisterListener(this);//取消监听
        mCompassOverlay.disableCompass();
        mScaleBarOverlay.disableScaleBar();

    }

    // START PERMISSION CHECK
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private void checkPermissions() {
        List<String> permissions = new ArrayList<String>();
        String message = "OSMDroid permissions:";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            message += "\nStorage access to store map tiles.";
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            message += "\nLocation to show user location.";
        }
        if (!permissions.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } // else: We already have permissions, so handle as normal
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION and WRITE_EXTERNAL_STORAGE
                Boolean location = perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                Boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (location && storage) {
                    // All Permissions Granted
                    Toast.makeText(MainActivity.this, "All permissions granted", Toast.LENGTH_SHORT).show();
                } else if (location) {
                    Toast.makeText(this, "Storage permission is required to store map tiles to reduce data usage and for offline usage.", Toast.LENGTH_LONG).show();
                } else if (storage) {
                    Toast.makeText(this, "Location permission is required to show the user's location on map.", Toast.LENGTH_LONG).show();
                } else { // !location && !storage case
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Storage permission is required to store map tiles to reduce data usage and for offline usage." +
                            "\nLocation permission is required to show the user's location on map.", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    // END PERMISSION CHECK

    protected final <T extends View> T $(int id) {
        return (T) findViewById(id);
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
                switch (selectedValue){
                    case 0:
                    case 1:
                        mapView.getController().animateTo(convertedPoint);
                        break;
                    case 2:
                        mapView.getController().animateTo(getNetworkLocation());
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
                Toast.makeText(MainActivity.this, "总长" + distance + "米", Toast.LENGTH_LONG).show();
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
                .setSingleChoiceItems(items, selectedValue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
//                        Toast.makeText(MainActivity.this, items[which],
//                                Toast.LENGTH_SHORT).show();

                        switch (which) {
                            case 0://GOOGLE_MAP
                                if (TileSourceFactory.containsTileSource(GOOGLE_MAP)) {
                                    mapView.setTileSource(TileSourceFactory.getTileSource(GOOGLE_MAP));
                                    resetOverlays();
                                    setPoint(convertedPoint);
                                    mapView.getController().setCenter(convertedPoint);
                                    mapView.invalidate();
                                }
                                break;
                            case 1://GOOGLE_SATELLITE
                                if (TileSourceFactory.containsTileSource(GOOGLE_SATELLITE)) {
                                    mapView.setTileSource(TileSourceFactory.getTileSource(GOOGLE_SATELLITE));
                                    resetOverlays();
                                    setPoint(convertedPoint);
                                    mapView.getController().setCenter(convertedPoint);
                                    mapView.invalidate();
                                }
                                break;
                            case 2://OSM
                                mapView.setTileSource(TileSourceFactory.MAPNIK);
                                resetOverlays();
                                setPoint(getNetworkLocation());
                                mapView.getController().setCenter(getNetworkLocation());
                                mapView.invalidate();
                        }
                        selectedValue = which;
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    /**
     * 重置界面图层
     */
    private void resetOverlays() {
        mapView.getOverlays().clear();
        mapView.getOverlays().add(mCompassOverlay);
        mapView.getOverlays().add(mRotationGestureOverlay);
        mapView.getOverlays().add(mScaleBarOverlay);
    }
}
