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
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.TileSource.GoogleMapsTileSource;
import com.bigemap.osmdroiddemo.TileSource.GoogleSatelliteTileSource;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.db.TrackDao;
import com.bigemap.osmdroiddemo.entity.Coordinate;
import com.bigemap.osmdroiddemo.overlay.MyLocationOverlay;
import com.bigemap.osmdroiddemo.service.MyLocationService;
import com.bigemap.osmdroiddemo.utils.DateUtils;
import com.bigemap.osmdroiddemo.utils.MapMeasureUtils;
import com.bigemap.osmdroiddemo.utils.PermissionUtils;
import com.bigemap.osmdroiddemo.utils.PositionUtils;
import com.bigemap.osmdroiddemo.utils.UIUtils;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.bigemap.osmdroiddemo.constants.Constant.POST_URL;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_LATITUDE_STRING;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_LONGITUDE_STRING;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_NAME;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_ORIENTATION;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_SHOW_LOCATION;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_TILE_SOURCE;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_ZOOM_LEVEL;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_ZOOM_LEVEL_DOUBLE;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    public static final String GOOGLE_MAP = "Google Map";
    public static final String GOOGLE_SATELLITE = "Google卫星图";
    public static final String OSM = "OpenStreetMap";
    private SharedPreferences mPrefs;
    private MapView mapView;
    //地图旋转
    //比例尺
    private ScaleBarOverlay mScaleBarOverlay;
    //指南针方向
//    private MyCompassOverlay mCompassOverlay = null;
    //设置导航图标的位置
    private MyLocationOverlay myLocationOverlay;
    //设置自定义定位，缩小，放大
    private ImageView locationBtn, track, mapModeBtn;
    private ImageView addPointBtn, searchPointBtn;
    private ImageView zoomInBtn, zoomOutBtn, emptyBtn;
    private ImageView undoBtn, prickBtn, saveBtn;//轨迹绘制操作按钮
    private RelativeLayout prickLayout;
    private ImageView trackRecord;//轨迹记录
    private Button shapeBtn, trackBtn, measureBtn, closeBtn;
    private LinearLayout mainBottomLayout, editToolLayout;
    private int selectedTileSource = 0;//默认选中地图值
    private int clickCount = 1;//用于轨迹记录按钮切换
    private int drawType = 0;//0:画线，1:图形，2:周长和面积，3:导入
    private GeoPoint convertedPoint;
    private ArrayList<GeoPoint> points;
    private ArrayList<Location> locationList;
    private TrackDao trackDao;
    private MyReceiver myReceiver;
    private int zoomLevel;
    private Polyline polyline;
    private Polygon polygon;
    private PermissionUtils permissionUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        }
        setContentView(R.layout.activity_main);
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
        mapView.setUseDataConnection(true);
        mapView.setMultiTouchControls(true);//触摸放大、缩小操作

        //比例尺
        mScaleBarOverlay = new ScaleBarOverlay(mapView);
        mScaleBarOverlay.setAlignRight(true);
        mScaleBarOverlay.setAlignBottom(true);
        mScaleBarOverlay.setEnableAdjustLength(true);
        mapView.getOverlays().add(mScaleBarOverlay);

        //显示指南针
//        mCompassOverlay = new MyCompassOverlay(this, mapView);
//        mCompassOverlay.enableCompass();
//        mapView.getOverlays().add(mCompassOverlay);

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
        permissionUtils = new PermissionUtils(this);
    }

    private void initView() {
        zoomInBtn = $(R.id.btn_zoom_in);
        zoomOutBtn = $(R.id.btn_zoom_out);
        emptyBtn = $(R.id.btn_empty);
        locationBtn = $(R.id.btn_location);
        mapModeBtn = $(R.id.btn_map_mode);
        prickLayout=$(R.id.rl_center_prick);
        addPointBtn = $(R.id.btn_point_edit);
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
        emptyBtn.setOnClickListener(this);
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
        prickLayout.setOnClickListener(this);
    }

    private void initTileSource() {
        GoogleMapsTileSource googleMapsTileSource = new GoogleMapsTileSource(GOOGLE_MAP, 1, 20, 512, ".png",
                new String[]{Constant.URL_MAP_GOOGLE});//加载图源

        TileSourceFactory.addTileSource(googleMapsTileSource);
        GoogleSatelliteTileSource satelliteTileSource = new GoogleSatelliteTileSource(GOOGLE_SATELLITE,
                1, 20, 512, ".png", new String[]{Constant.URL_MAP_GOOGLE_SATELLITE});
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

    /**
     * 判断程序是否第一次启动
     *
     * @return boolean
     */
    private boolean isFirstStart() {
        return mPrefs.getBoolean("first_start", true);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        postUrl("login");
        Log.d(TAG, "onStart: ");
    }

    /**
     * 统计次数
     *
     * @param type
     */
    private void postUrl(String type) {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        if (permissionUtils.checkPermission(Manifest.permission.READ_PHONE_STATE)) {
            TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telManager.getDeviceId();
            RequestBody requestBodyPost = new FormBody.Builder()
                    .add("type", type)
                    .add("imei", imei)
                    .build();

            Request requestPost = new Request.Builder()
                    .url(POST_URL)
                    .post(requestBodyPost)
                    .build();
            mOkHttpClient.newCall(requestPost).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String string = response.body().string();
                    Log.d(TAG, "onResponse: " + string);
                }
            });
        }
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

        zoomLevel = mPrefs.getInt(PREFS_ZOOM_LEVEL_DOUBLE, mPrefs.getInt(PREFS_ZOOM_LEVEL, 10));
        mapView.getController().setZoom(zoomLevel);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableCompass();
        mScaleBarOverlay.enableScaleBar();
        //判断程序是否第一次启动
        if (isFirstStart()) {
            mapView.getController().setCenter(new GeoPoint(30.5702183724, 104.0647735044));
        } else {
            final String latitudeString = mPrefs.getString(PREFS_LATITUDE_STRING, "30.5702183724");
            final String longitudeString = mPrefs.getString(PREFS_LONGITUDE_STRING, "104.0647735044");
            final double latitude = Double.valueOf(latitudeString);
            final double longitude = Double.valueOf(longitudeString);
            mapView.getController().animateTo(new GeoPoint(latitude, longitude));
        }

//        if (mPrefs.getBoolean(PREFS_SHOW_COMPASS, false)) {
//            if (mCompassOverlay != null) {
//                //this call is needed because onPause, the orientation provider is destroyed to prevent context leaks
//                this.mCompassOverlay.setOrientationProvider(new InternalCompassOrientationProvider(this));
//                this.mCompassOverlay.enableCompass();
//            }
//        }

        long trackId = getIntent().getLongExtra("trackId", -1);
        if (trackId > -1) {
            ArrayList<Location> locations = trackDao.getTrackPoints(trackId);
            ArrayList<GeoPoint> geoPoints = new ArrayList<>();
            for (Location location : locations) {
                geoPoints.add(new GeoPoint(location));
            }
            setPolyline(geoPoints);
            final BoundingBox boundingBox = BoundingBox.fromGeoPoints(geoPoints);
            mapView.getController().animateTo(boundingBox.getCenter());
        }

        Coordinate coordinate = getIntent().getParcelableExtra("coordinate");
        if (coordinate != null) {
            GeoPoint geoPoint = new GeoPoint(coordinate.getLatitude(), coordinate.getLongitude());
            mapView.getController().animateTo(geoPoint);
            setPoint(geoPoint, coordinate.getName());
        }
    }

    @Override
    protected void onPause() {
        final SharedPreferences.Editor edit = mPrefs.edit();
        edit.putBoolean("first_start", false);
        edit.putString(PREFS_TILE_SOURCE, mapView.getTileProvider().getTileSource().name());
        edit.putFloat(PREFS_ORIENTATION, mapView.getMapOrientation());
        edit.putString(PREFS_LATITUDE_STRING, String.valueOf(mapView.getMapCenter().getLatitude()));
        edit.putString(PREFS_LONGITUDE_STRING, String.valueOf(mapView.getMapCenter().getLongitude()));
        edit.putInt(PREFS_ZOOM_LEVEL_DOUBLE, mapView.getZoomLevel());
        edit.putBoolean(PREFS_SHOW_LOCATION, myLocationOverlay.isMyLocationEnabled());
//        if (mCompassOverlay != null) {
//            edit.putBoolean(PREFS_SHOW_COMPASS, mCompassOverlay.isCompassEnabled());
//            this.mCompassOverlay.disableCompass();
//        }
        edit.apply();
        super.onPause();
        Log.d(TAG, "onPause: ");
        myLocationOverlay.disableMyLocation();
        mScaleBarOverlay.disableScaleBar();
    }

    @Override
    protected void onDestroy() {
//        postUrl("logout");
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    // START PERMISSION CHECK

    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private final int REQUEST_CODE_ASK_LOCATION_PERMISSIONS = 123;

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } // else: We already have permissions, so handle as normal
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                if (permissionUtils.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    myLocationOverlay.runOnFirstFix(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: first location=" + myLocationOverlay.getLastFix());
                            convertedPoint = myLocationOverlay.getMyLocation();
                            mapView.getController().animateTo(convertedPoint);
                        }
                    });
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }
    // END PERMISSION CHECK

    /**
     * 点
     * @param point
     */
    private void setRoundPoint(GeoPoint point) {
        Drawable icon = ContextCompat.getDrawable(this, R.drawable.ic_member_pos);
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setIcon(icon);
        marker.setTitle("坐标：lat=" + point.getLatitude() + ",lng=" + point.getLongitude());
        marker.setFlat(true);//设置marker平贴地图效果
        mapView.getOverlays().add(marker);
    }

    private void setPoint(GeoPoint point, String title) {
        Drawable icon = ContextCompat.getDrawable(this, R.drawable.location_search);
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setIcon(icon);
        marker.setTitle(title);
        marker.setFlat(true);//设置marker平贴地图效果
        mapView.getOverlays().add(marker);
    }

    /**
     * 线
     * @param points
     */
    private void setPolyline(List<GeoPoint> points) {
        polyline = new Polyline();
        polyline.setWidth(8);
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
        polygon = new Polygon();
        polygon.setStrokeWidth(5);

        polygon.setStrokeColor(R.color.colorAccent);
        polygon.setFillColor(R.color.colorPrimary);
        polygon.setPoints(points);
        mapView.getOverlays().add(polygon);
    }

    @Override
    public void onClick(View v) {
        String time = DateUtils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd");
        String startTime;
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
//        String sourceType = myLocationOverlay.getLastFix().getProvider();
        switch (v.getId()) {
            case R.id.btn_location://定位
                switch (selectedTileSource) {
                    case Constant.GOOGLE_MAP:
                    case Constant.GOOGLE_SATELLITE:
//                        permissionUtils.permissionsCheck(Manifest.permission.ACCESS_FINE_LOCATION,REQUEST_CODE_ASK_LOCATION_PERMISSIONS)
                        if (permissionUtils.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            mapView.getController().animateTo(myLocationOverlay.getMyLocation());
                        }else{
                            checkPermissions();
                        }
                        break;
                    case Constant.OSM:
                        if (permissionUtils.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            mapView.getController().animateTo(PositionUtils.gcj_To_Gps84(myLocationOverlay.getMyLocation()));
                        }else{
                            checkPermissions();
                        }
                        break;
                }
                break;
            case R.id.btn_zoom_in://放大
                mapView.getController().zoomIn();//默认按级缩放
                break;
            case R.id.btn_zoom_out://缩小
                mapView.getController().zoomOut();
                break;
            case R.id.btn_empty:
                points.clear();
                mapView.getOverlays().clear();
                mapView.getOverlays().add(mScaleBarOverlay);
//                mapView.getOverlays().add(mCompassOverlay);
                mapView.getOverlays().add(myLocationOverlay);
                break;
            case R.id.btn_map_mode://地图切换
                createMapModeView();
                break;
            case R.id.btn_point_edit://开始绘制轨迹
                editToolLayout.setVisibility(View.VISIBLE);
                prickLayout.setVisibility(View.VISIBLE);
                trackRecord.setVisibility(View.GONE);
                mainBottomLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_point_search://搜索位置
                Intent intent = new Intent(this, PointKeywordSearchActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_track_record://轨迹记录
                startTime = DateUtils.formatUTC(System.currentTimeMillis(), null);
                if (clickCount % 2 != 0) {
                    locationList.clear();
                    trackRecord.setImageResource(R.drawable.btn_track_record_end);
                    Toast.makeText(this, "开始记录轨迹", Toast.LENGTH_SHORT).show();
                    startService(new Intent(this, MyLocationService.class));
                    // 注册广播
                    myReceiver = new MyReceiver();
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("com.bigemap.osmdroiddemo.service.intent.locationList");
                    registerReceiver(myReceiver, filter);
                } else {
                    trackRecord.setImageResource(R.drawable.btn_track_record_start);
                    stopService(new Intent(this, MyLocationService.class));
                    if (locationList.size() > 1) {
                        Toast.makeText(MainActivity.this, "停止记录轨迹", Toast.LENGTH_SHORT).show();
                        trackDao.addTrack(name, null, startTime, locationList, "gps", 0);
                    } else {
                        Toast.makeText(this, "此次轨迹路线太短，不作记录", Toast.LENGTH_SHORT).show();
                    }
                    unregisterReceiver(myReceiver);
                }
                clickCount++;
                break;
            case R.id.btn_track://查看轨迹记录
                UIUtils.showTrackActivity(MainActivity.this);
                break;
            case R.id.btn_edit_undo://撤销上一步
                if (points.size() > 0) {
                    points.remove(points.size() - 1);
                    drawTrack(points);
                }
                break;
            case R.id.rl_center_prick:
            case R.id.btn_edit_prick://轨迹描点
                GeoPoint centerPoint = (GeoPoint) mapView.getMapCenter();
                setRoundPoint(centerPoint);
                points.add(centerPoint);
                drawTrack(points);
                break;
            case R.id.btn_edit_save://保存轨迹并跳转编辑
                startTime = DateUtils.formatUTC(System.currentTimeMillis(), null);
                if (points.size() > 1) {
                    long trackId = trackDao.insertTrack(name, null, startTime, points, "custom", drawType);
                    UIUtils.showTrackEditActivity(MainActivity.this, trackId);
                    Log.d(TAG, "trackId=" + trackId);
                } else {
                    Toast.makeText(MainActivity.this, "轨迹点数不足，请绘制两点以上", Toast.LENGTH_SHORT).show();
                }
                points.clear();
                break;
            case R.id.btn_edit_shape://图形
                points.clear();
                drawType = 1;
                saveBtn.setVisibility(View.VISIBLE);
                shapeBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                trackBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                measureBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                break;
            case R.id.btn_edit_track://轨迹
                points.clear();
                drawType = 0;
                saveBtn.setVisibility(View.VISIBLE);
                shapeBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                trackBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                measureBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                break;
            case R.id.btn_edit_measure://周长和面积
                points.clear();
                drawType = 2;
                saveBtn.setVisibility(View.GONE);
                shapeBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                trackBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                measureBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case R.id.btn_edit_close://关闭轨迹绘制
                points.clear();
                editToolLayout.setVisibility(View.GONE);
                prickLayout.setVisibility(View.GONE);
                trackRecord.setVisibility(View.VISIBLE);
                mainBottomLayout.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * 绘制轨迹
     */
    private void drawTrack(ArrayList<GeoPoint> geoPoints) {
        switch (drawType) {
            case 0://画线
                mapView.getOverlays().remove(polyline);
                setPolyline(geoPoints);
                break;
            case 1://图形
                mapView.getOverlays().remove(polygon);
                setPolygon(geoPoints);
                break;
            case 2://周长和面积
                mapView.getOverlays().remove(polygon);
                setPolygon(geoPoints);
                if (geoPoints.size() > 2) {
                    String area = MapMeasureUtils.calculateArea(geoPoints);
                    String perimeter = MapMeasureUtils.calculatePerimeter(geoPoints);
                    BasicInfoWindow in = new BasicInfoWindow(R.layout.bonuspack_bubble, mapView);
                    polygon.setInfoWindow(in);
                    String s = "周长：" + perimeter + "米" + "\n" + "面积：" + area + "平方公里";
                    polygon.setTitle(s);
                }
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
            if (points.size() > 1) {
                mapView.getOverlays().remove(polyline);
                setPolyline(points);
            }
        }
    }
}
