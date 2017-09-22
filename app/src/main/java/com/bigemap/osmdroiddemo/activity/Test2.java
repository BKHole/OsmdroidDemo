package com.bigemap.osmdroiddemo.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.TileSource.GoogleMapsTileSource;
import com.bigemap.osmdroiddemo.TileSource.GoogleSatelliteTileSource;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.db.TrackDao;
import com.bigemap.osmdroiddemo.overlay.MyLocationOverlay;
import com.bigemap.osmdroiddemo.service.MyLocationService;
import com.bigemap.osmdroiddemo.utils.DateUtils;
import com.bigemap.osmdroiddemo.utils.PositionUtils;
import com.bigemap.osmdroiddemo.utils.UIUtils;

import org.osmdroid.tileprovider.tilesource.ITileSource;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_LATITUDE_STRING;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_LONGITUDE_STRING;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_NAME;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_ORIENTATION;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_SHOW_COMPASS;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_SHOW_LOCATION;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_TILE_SOURCE;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_ZOOM_LEVEL;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_ZOOM_LEVEL_DOUBLE;

public class Test2 extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Test2";
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
    private MyLocationOverlay myLocationOverlay;
    //设置自定义定位，缩小，放大
    private ImageView locationBtn, track, mapModeBtn;
    private ImageView addPointBtn, searchPointBtn;
    private ImageView zoomInBtn, zoomOutBtn;
    private ImageView undoBtn, prickBtn, saveBtn;//轨迹绘制操作按钮
    private ImageView trackRecord, centerPointImg;//轨迹记录
    private Button shapeBtn, trackBtn, measureBtn, closeBtn;
    private LinearLayout mainBottomLayout, editToolLayout;
    private int selectedTileSource = 0;//默认选中地图值
    private int clickCount = 1;//用于轨迹记录按钮切换
    private int drawType = 0;//0:画线，1:图形，2:周长和面积
    private GeoPoint convertedPoint;
    private ArrayList<GeoPoint> points;
    private ArrayList<Location> locationList;
    private TrackDao trackDao;
    private boolean isFirstIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }
        setContentView(R.layout.activity_main);
        isFirstIn = true;
        init();
    }

    private void init() {
        mapView = $(R.id.mapView);
        initData();
        initView();
        initTileSource();

        Log.d(TAG, "init: tileSource" + mapView.getTileProvider().getTileSource().name());
        mapView.setDrawingCacheEnabled(true);
        mapView.setTilesScaledToDpi(true);//图源比例转换屏幕像素
        final int zoomLevel = mPrefs.getInt(PREFS_ZOOM_LEVEL_DOUBLE, mPrefs.getInt(PREFS_ZOOM_LEVEL, 15));
        mapView.getController().setZoom(zoomLevel);
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
        mapView.getOverlays().add(mCompassOverlay);

        //显示定位
        GpsMyLocationProvider gps = new GpsMyLocationProvider(this);
        gps.setLocationUpdateMinTime(2000);//默认两秒钟更新一次
        gps.addLocationSource(LocationManager.NETWORK_PROVIDER);
        myLocationOverlay = new MyLocationOverlay(gps, mapView);
        myLocationOverlay.setDrawAccuracyEnabled(false);
        mapView.getOverlays().add(myLocationOverlay);

        if (mapView.getTileProvider().getTileSource().equals(TileSourceFactory.MAPNIK)) {
            myLocationOverlay.setTileSource(Constant.OSM);
            selectedTileSource = Constant.OSM;
        }

    }

    private void initData() {
        points = new ArrayList<>();
        locationList = new ArrayList<>();
        trackDao = new TrackDao(this);
        mPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void initView() {
        zoomInBtn = $(R.id.btn_zoom_in);
        zoomOutBtn = $(R.id.btn_zoom_out);
        locationBtn = $(R.id.btn_location);
        mapModeBtn = $(R.id.btn_map_mode);
        centerPointImg = $(R.id.btn_center);
        addPointBtn = $(R.id.btn_point_add);
        searchPointBtn = $(R.id.btn_point_search);
        trackRecord = $(R.id.btn_track_record);
        track = $(R.id.btn_track);

        editToolLayout = $(R.id.layout_main_edit_tool);
        shapeBtn = $(R.id.btn_edit_shape);
        trackBtn = $(R.id.btn_edit_track);
        measureBtn = $(R.id.btn_edit_measure);
        closeBtn = $(R.id.btn_edit_close);
        mainBottomLayout = $(R.id.layout_main_bottom);
        undoBtn = $(R.id.btn_edit_undo);
        prickBtn = $(R.id.btn_edit_prick);
        saveBtn = $(R.id.btn_edit_save);

        locationBtn.setOnClickListener(this);
        zoomInBtn.setOnClickListener(this);
        zoomOutBtn.setOnClickListener(this);
        mapModeBtn.setOnClickListener(this);
        addPointBtn.setOnClickListener(this);
        searchPointBtn.setOnClickListener(this);
        trackRecord.setOnClickListener(this);
        track.setOnClickListener(this);
        undoBtn.setOnClickListener(this);
        prickBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        shapeBtn.setOnClickListener(this);
        trackBtn.setOnClickListener(this);
        measureBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
    }

    private void initTileSource() {
        GoogleMapsTileSource googleMapsTileSource = new GoogleMapsTileSource(GOOGLE_MAP, 1, 20, 256, ".png",
                new String[]{Constant.URL_MAP_GOOGLE});//加载图源

        TileSourceFactory.addTileSource(googleMapsTileSource);
        GoogleSatelliteTileSource satelliteTileSource = new GoogleSatelliteTileSource(GOOGLE_SATELLITE,
                1, 20, 256, ".png", new String[]{Constant.URL_MAP_GOOGLE_SATELLITE});
        TileSourceFactory.addTileSource(satelliteTileSource);

        final String tileSourceName = mPrefs.getString(PREFS_TILE_SOURCE, GOOGLE_MAP);
        try {
            final ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
            mapView.setTileSource(tileSource);
        } catch (final IllegalArgumentException e) {
            mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
            myLocationOverlay.setTileSource(Constant.OSM);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

        myLocationOverlay.enableMyLocation();
        if (isFirstIn) {
            myLocationOverlay.runOnFirstFix(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: first location=" + myLocationOverlay.getLastFix());
                    convertedPoint = myLocationOverlay.getMyLocation();
                    mapView.getController().setCenter(convertedPoint);
                }
            });
        } else {
            final String latitudeString = mPrefs.getString(PREFS_LATITUDE_STRING, null);
            final String longitudeString = mPrefs.getString(PREFS_LONGITUDE_STRING, null);
            if (latitudeString == null || longitudeString == null) { // case handled for historical reasons only
                mapView.getController().setCenter(new GeoPoint(30.5702183724, 104.0647735044));
            } else {
                final double latitude = Double.valueOf(latitudeString);
                final double longitude = Double.valueOf(longitudeString);
                mapView.getController().setCenter(new GeoPoint(latitude, longitude));
            }
        }

        mScaleBarOverlay.enableScaleBar();
        if (mPrefs.getBoolean(PREFS_SHOW_COMPASS, false)) {
            if (mCompassOverlay != null)
                //this call is needed because onPause, the orientation provider is destroyed to prevent context leaks
                this.mCompassOverlay.setOrientationProvider(new InternalCompassOrientationProvider(this));
            this.mCompassOverlay.enableCompass();
        }

    }

    @Override
    protected void onPause() {
        isFirstIn = false;
        final SharedPreferences.Editor edit = mPrefs.edit();
        edit.putString(PREFS_TILE_SOURCE, mapView.getTileProvider().getTileSource().name());
        edit.putFloat(PREFS_ORIENTATION, mapView.getMapOrientation());
        edit.putString(PREFS_LATITUDE_STRING, String.valueOf(mapView.getMapCenter().getLatitude()));
        edit.putString(PREFS_LONGITUDE_STRING, String.valueOf(mapView.getMapCenter().getLongitude()));
        edit.putInt(PREFS_ZOOM_LEVEL_DOUBLE, mapView.getZoomLevel());
        edit.putBoolean(PREFS_SHOW_LOCATION, myLocationOverlay.isMyLocationEnabled());
        if (mCompassOverlay != null) {
            edit.putBoolean(PREFS_SHOW_COMPASS, mCompassOverlay.isCompassEnabled());
            this.mCompassOverlay.disableCompass();
        }
        edit.apply();
        super.onPause();
        Log.d(TAG, "onPause: ");
        myLocationOverlay.disableMyLocation();
        mScaleBarOverlay.disableScaleBar();

    }

    // START PERMISSION CHECK
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
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
                Map<String, Integer> perms = new HashMap<>();
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
                    Toast.makeText(Test2.this, "All permissions granted", Toast.LENGTH_SHORT).show();
                } else if (location) {
                    Toast.makeText(this, "Storage permission is required to store map tiles to reduce data usage and for offline usage.", Toast.LENGTH_LONG).show();
                } else if (storage) {
                    Toast.makeText(this, "Location permission is required to show the user's location on map.", Toast.LENGTH_LONG).show();
                } else { // !location && !storage case
                    // Permission Denied
                    Toast.makeText(Test2.this, "Storage permission is required to store map tiles to reduce data usage and for offline usage." +
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
     * 点
     *
     * @param point
     */
    private void setRoundPoint(GeoPoint point) {
        Drawable icon = ContextCompat.getDrawable(this, R.drawable.ic_member_pos);
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setIcon(icon);
        marker.setDraggable(true);
        marker.setTitle("坐标：lat=" + point.getLatitude() + ",lng=" + point.getLongitude());
        marker.setFlat(true);//设置marker平贴地图效果
        mapView.getOverlays().add(marker);
    }

    /**
     * 线
     *
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
     *
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
        String time = DateUtils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd");
        String startTime="";
        String name = "";
        switch (drawType) {
            case 0:
                name = trackBtn.getText() + "&" + time;
                break;
            case 1:
                name = shapeBtn.getText() + "&" + time;
                break;
            case 2:
                name = measureBtn.getText() + "&" + time;
                break;
        }
        String sourceType = myLocationOverlay.getLastFix().getProvider();
        switch (v.getId()) {
            case R.id.btn_location://定位
                switch (selectedTileSource) {
                    case Constant.GOOGLE_MAP:
                    case Constant.GOOGLE_SATELLITE:
                        mapView.getController().animateTo(convertedPoint);
                        break;
                    case Constant.OSM:
                        mapView.getController().animateTo(PositionUtils.gcj_To_Gps84(convertedPoint));
                        break;
                }
                break;
            case R.id.btn_zoom_in://放大
                mapView.getController().zoomIn();//默认按级缩放
                break;
            case R.id.btn_zoom_out://缩小
                mapView.getController().zoomOut();
                break;
            case R.id.btn_map_mode://地图切换
                createMapModeView();
                break;
            case R.id.btn_point_add://绘制轨迹
                editToolLayout.setVisibility(View.VISIBLE);
                centerPointImg.setVisibility(View.VISIBLE);
                trackRecord.setVisibility(View.GONE);
                mainBottomLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_point_search://搜索位置
                break;
            case R.id.btn_track_record://轨迹记录
                if (clickCount % 2 != 0) {
                    startTime=DateUtils.formatUTC(System.currentTimeMillis(), null);
                    locationList.clear();
                    trackRecord.setImageResource(R.drawable.btn_track_record_end);
                    Toast.makeText(this, "开始记录轨迹", Toast.LENGTH_SHORT).show();
                    startService(new Intent(this, MyLocationService.class));
                    // 注册广播
                    MyReceiver receiver = new MyReceiver();
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("com.bigemap.osmdroiddemo.service.intent.locationList");
                    registerReceiver(receiver, filter);
                } else {
                    trackRecord.setImageResource(R.drawable.btn_track_record_start);
                    stopService(new Intent(this, MyLocationService.class));
                    if (locationList.size() > 1) {
                        Toast.makeText(Test2.this, "停止记录轨迹", Toast.LENGTH_SHORT).show();
                        Location lastLocation = locationList.get(locationList.size() - 1);
                        setRoundPoint(new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude()));
                        trackDao.addTrack(name, null, startTime, locationList, sourceType, 0);
                    } else {
                        Toast.makeText(this, "此次轨迹路线太短，不作记录", Toast.LENGTH_SHORT).show();
                    }
                    Log.d(TAG, "trackRecord save success");
                }
                clickCount++;
                break;
            case R.id.btn_track://查看轨迹记录
                UIUtils.showTrackActivity(Test2.this);
                break;
            case R.id.btn_edit_undo://撤销上一步
                break;
            case R.id.btn_edit_prick://轨迹描点
                GeoPoint centerPoint = (GeoPoint) mapView.getMapCenter();
                setRoundPoint(centerPoint);
                points.add(centerPoint);
                Log.d(TAG, "onClick: " + points.size());
                switch (drawType) {
                    case 0://画线
                        setPolyline(points);
                        int distance = 0;
                        for (int i = 0; i < points.size() - 1; i++) {
                            distance += getDistance(points.get(i), points.get(i + 1));
                        }
                        Toast.makeText(Test2.this, "总长" + distance + "米", Toast.LENGTH_SHORT).show();
                        break;
                    case 1://图形
                        setPolygon(points);
                        break;
                    case 2://周长和面积
                        break;
                }
                break;
            case R.id.btn_edit_save://保存轨迹并跳转编辑
                if (points.size() > 1) {
                    long trackId = trackDao.insertTrack(name, null, time, points, sourceType, drawType);
                    UIUtils.showTrackEditActivity(Test2.this, trackId);
                    Log.d(TAG, "trackId=" + trackId);
                } else {
                    Toast.makeText(Test2.this, "轨迹点数不足，请绘制两点以上", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_edit_shape://图形
                points.clear();
                drawType = 1;
                shapeBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                trackBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                measureBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                break;
            case R.id.btn_edit_track://轨迹
                points.clear();
                drawType = 0;
                shapeBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                trackBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                measureBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                break;
            case R.id.btn_edit_measure://周长和面积
                points.clear();
                drawType = 2;
                shapeBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                trackBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                measureBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case R.id.btn_edit_close://关闭轨迹绘制
                editToolLayout.setVisibility(View.GONE);
                centerPointImg.setVisibility(View.GONE);
                trackRecord.setVisibility(View.VISIBLE);
                mainBottomLayout.setVisibility(View.GONE);
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
                                    myLocationOverlay.setTileSource(Constant.GOOGLE_MAP);
                                }
                                break;
                            case Constant.GOOGLE_SATELLITE://GOOGLE_SATELLITE
                                if (TileSourceFactory.containsTileSource(GOOGLE_SATELLITE)) {
                                    mapView.setTileSource(TileSourceFactory.getTileSource(GOOGLE_SATELLITE));
                                    myLocationOverlay.setTileSource(Constant.GOOGLE_SATELLITE);
                                }
                                break;
                            case Constant.OSM://OSM
                                mapView.setTileSource(TileSourceFactory.MAPNIK);
                                myLocationOverlay.setTileSource(Constant.OSM);
                                break;
                        }
                        selectedTileSource = which;
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    // 获取广播数据
    private class MyReceiver extends BroadcastReceiver {

        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            locationList = (ArrayList<Location>) bundle.getSerializable("saveGps");
            ArrayList<GeoPoint> points = new ArrayList<>();
            for (Location geoPoint : locationList) {
                points.add(new GeoPoint(geoPoint));
            }
            setPolyline(points);
        }
    }
}
