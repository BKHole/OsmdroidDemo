package com.bigemap.osmdroiddemo.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.TileSource.GoogleMapsTileSource;
import com.bigemap.osmdroiddemo.TileSource.GoogleSatelliteTileSource;
import com.bigemap.osmdroiddemo.adapter.MapSourceAdapter;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.entity.Location;
import com.bigemap.osmdroiddemo.entity.Map;
import com.bigemap.osmdroiddemo.entity.Track;
import com.bigemap.osmdroiddemo.overlay.MyLocationOverlay;
import com.bigemap.osmdroiddemo.service.MyLocationService;
import com.bigemap.osmdroiddemo.utils.AMapUtils;
import com.bigemap.osmdroiddemo.utils.DateUtils;
import com.bigemap.osmdroiddemo.utils.MapMeasureUtils;
import com.bigemap.osmdroiddemo.utils.PermissionUtils;
import com.bigemap.osmdroiddemo.utils.PositionUtils;
import com.bigemap.osmdroiddemo.utils.UIUtils;

import org.litepal.crud.DataSupport;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
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
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_ORIENTATION;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_SHOW_LOCATION;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_TILE_SOURCE;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_ZOOM_LEVEL;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_ZOOM_LEVEL_DOUBLE;

public class MainActivity extends BaseActivity implements View.OnClickListener,
        Inputtips.InputtipsListener, TextWatcher, AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";
    public static final String GOOGLE_MAP = "Google Map";
    public static final String GOOGLE_SATELLITE = "Google卫星图";
    public static final String OSM = "OpenStreetMap";
    private MapView mapView;
    //比例尺
    private ScaleBarOverlay mScaleBarOverlay;
    //指南针方向
//    private MyCompassOverlay mCompassOverlay = null;
    //设置导航图标的位置
    private MyLocationOverlay myLocationOverlay;
    //设置自定义定位，缩小，放大
    private ImageView locationBtn, track, mapModeBtn;
    private ImageView addPointBtn;
    private ImageView zoomInBtn, zoomOutBtn, emptyBtn;
    private ImageView undoBtn, prickBtn, saveBtn;//轨迹绘制操作按钮
    private RelativeLayout prickLayout;
    private ImageView trackRecord;//轨迹记录
    private Button shapeBtn, trackBtn, measureBtn, closeBtn;
    private LinearLayout mainBottomLayout, editToolLayout;

    //搜索框
    private CardView searchCardView;
    private AutoCompleteTextView searchText;
    private ImageButton editTextClearBtn;
    private List<Tip> tips;

    //地图选择
    private TextView normalMap, satelliteMap;
    private RecyclerView mapSourceList;//地图源列表
    private boolean isMapChanged=true;

//    private ScrollView contentScroll;
//    private ImageView importLayers;
//    private boolean isExpanded=true;

    private boolean isRecord = true;//用于轨迹记录按钮切换
    private int drawType = 0;//0:画线，1:图形，2:周长和面积，3:导入
    private GeoPoint convertedPoint;
    private List<GeoPoint> points;
    private List<GeoPoint> locationList;
    private MyReceiver myReceiver;
    private int zoomLevel;
    private Polyline polyline;
    private Polygon polygon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
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
            dataKeeper.putInt(Constant.PREFS_MAP_SOURCE, Constant.OSM);
        }

    }

    private void initData() {
        points = new ArrayList<>();
        locationList = new ArrayList<>();
    }

    private void initView() {
        zoomInBtn = $(R.id.btn_zoom_in);
        zoomOutBtn = $(R.id.btn_zoom_out);
        emptyBtn = $(R.id.btn_empty);
        locationBtn = $(R.id.btn_location);
        prickLayout = $(R.id.rl_center_prick);
        addPointBtn = $(R.id.btn_point_edit);
        trackRecord = $(R.id.btn_track_record);
        track = $(R.id.btn_track);
        searchText = $(R.id.search_editText);
        searchText.setCursorVisible(false);
        editTextClearBtn = $(R.id.edit_text_clear);

        mapModeBtn = $(R.id.btn_map_mode);
        mapModeBtn.setImageResource(R.drawable.ic_map_google);
        normalMap = $(R.id.tv_map_type_normal);
        satelliteMap = $(R.id.tv_map_type_satellite);
        searchCardView = $(R.id.search_box);
        searchCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.translucent_white_65));
        mapSourceList = $(R.id.list_map_source);

        editToolLayout = $(R.id.layout_main_edit_tool);
        shapeBtn = $(R.id.btn_edit_shape);
        trackBtn = $(R.id.btn_edit_track);
        measureBtn = $(R.id.btn_edit_measure);
        closeBtn = $(R.id.btn_edit_close);
        mainBottomLayout = $(R.id.layout_main_bottom);
        undoBtn = $(R.id.btn_edit_undo);
        prickBtn = $(R.id.btn_edit_prick);
        saveBtn = $(R.id.btn_edit_save);

//        contentScroll = $(R.id.scroll_layers_content);
//        importLayers = $(R.id.iv_layers_import);

        locationBtn.setOnClickListener(this);
        zoomInBtn.setOnClickListener(this);
        zoomOutBtn.setOnClickListener(this);
        emptyBtn.setOnClickListener(this);
        mapModeBtn.setOnClickListener(this);
        addPointBtn.setOnClickListener(this);
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
        searchText.setOnClickListener(this);
        searchText.addTextChangedListener(this);
        searchText.setOnItemClickListener(this);
        editTextClearBtn.setOnClickListener(this);
        normalMap.setOnClickListener(this);
        satelliteMap.setOnClickListener(this);
//        importLayers.setOnClickListener(this);
    }

    private void initTileSource() {
        GoogleMapsTileSource googleMapsTileSource = new GoogleMapsTileSource(GOOGLE_MAP, 1, 20, 512, ".png",
                new String[]{Constant.URL_MAP_GOOGLE});//加载图源
        TileSourceFactory.addTileSource(googleMapsTileSource);

        GoogleSatelliteTileSource satelliteTileSource = new GoogleSatelliteTileSource(GOOGLE_SATELLITE,
                1, 20, 512, ".png", new String[]{Constant.URL_MAP_GOOGLE_SATELLITE});
        TileSourceFactory.addTileSource(satelliteTileSource);

        final String tileSourceName = dataKeeper.get(PREFS_TILE_SOURCE, GOOGLE_MAP);
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
     * @return boolean
     */
    private boolean isFirstStart() {
        return dataKeeper.get("first_start", true);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        postUrl("login");
        Log.d(TAG, "onStart: ");
    }

    /**
     * 统计次数
     * @param type
     */
    private void postUrl(String type) {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        if (PermissionUtils.checkPhoneStatePermission(this)) {
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

        zoomLevel = dataKeeper.getInt(PREFS_ZOOM_LEVEL_DOUBLE, dataKeeper.getInt(PREFS_ZOOM_LEVEL, 10));
        mapView.getController().setZoom(zoomLevel);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.setOrientationProvider(new InternalCompassOrientationProvider(this));
        myLocationOverlay.enableCompass();
        mScaleBarOverlay.enableScaleBar();
        //判断程序是否第一次启动
        if (isFirstStart()) {
            mapView.getController().setCenter(new GeoPoint(30.5702183724, 104.0647735044));
        } else {
            final String latitudeString = dataKeeper.get(PREFS_LATITUDE_STRING, "30.5702183724");
            final String longitudeString = dataKeeper.get(PREFS_LONGITUDE_STRING, "104.0647735044");
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
            List<Location> locations = DataSupport.findAll(Location.class, trackId);
            ArrayList<GeoPoint> geoPoints = new ArrayList<>();
            for (Location location : locations) {
                geoPoints.add(new GeoPoint(Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude())));
            }
            setPolyline(geoPoints);
            final BoundingBox boundingBox = BoundingBox.fromGeoPoints(geoPoints);
            mapView.getController().animateTo(boundingBox.getCenter());
        }

    }

    @Override
    protected void onPause() {
        dataKeeper.put("first_start", false);
        dataKeeper.put(PREFS_TILE_SOURCE, mapView.getTileProvider().getTileSource().name());
        dataKeeper.put(PREFS_ORIENTATION, mapView.getMapOrientation());
        dataKeeper.put(PREFS_LATITUDE_STRING, String.valueOf(mapView.getMapCenter().getLatitude()));
        dataKeeper.put(PREFS_LONGITUDE_STRING, String.valueOf(mapView.getMapCenter().getLongitude()));
        dataKeeper.putInt(PREFS_ZOOM_LEVEL_DOUBLE, mapView.getZoomLevel());
        dataKeeper.put(PREFS_SHOW_LOCATION, myLocationOverlay.isMyLocationEnabled());
//        if (mCompassOverlay != null) {
//            edit.putBoolean(PREFS_SHOW_COMPASS, mCompassOverlay.isCompassEnabled());
//            this.mCompassOverlay.disableCompass();
//        }
        super.onPause();
        Log.d(TAG, "onPause: ");
        myLocationOverlay.disableMyLocation();
        mScaleBarOverlay.disableScaleBar();
    }

    @Override
    protected void onDestroy() {
//        postUrl("logout");
        super.onDestroy();
        if (myReceiver!=null){
            unregisterReceiver(myReceiver);
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                if (PermissionUtils.checkLocationPermission(this)) {
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
     *
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
        mapView.invalidate();
    }

    private void setPoint(GeoPoint point, String title) {
        Drawable icon = ContextCompat.getDrawable(this, R.drawable.img_point_search);
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
        mapView.invalidate();
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
        mapView.invalidate();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newText = s.toString().trim();
        if (!AMapUtils.IsEmptyOrNullString(newText)) {
            InputtipsQuery inputQuery = new InputtipsQuery(newText, null);
            Inputtips inputTips = new Inputtips(this, inputQuery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 0) {
            editTextClearBtn.setVisibility(View.VISIBLE);
        } else {
            editTextClearBtn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {// 正确返回
            tips = tipList;
            List<String> listString = new ArrayList<>();
            for (int i = 0; i < tipList.size(); i++) {
                listString.add(tipList.get(i).getName());
            }
            ArrayAdapter<String> aAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1, listString);
            searchText.setAdapter(aAdapter);
            aAdapter.notifyDataSetChanged();
        } else {
            toastUtils.showToast(rCode);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Tip tip = tips.get(position);
        if (tip.getPoint() != null) {
            LatLonPoint sharePoint = tip.getPoint();//gps坐标
            GeoPoint searchPoint = PositionUtils.gps84_To_Gcj02(new GeoPoint(sharePoint.getLatitude(), sharePoint.getLongitude()));
            mapView.getController().animateTo(searchPoint);
            setPoint(searchPoint, tip.getName());
        }
    }

    //隐藏软键盘和编辑框光标
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.getCurrentFocus() != null) {
            hideKeyboard();
            hideMapSource();
            hideMapType();
        }
        return super.onTouchEvent(event);
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
        switch (v.getId()) {
            case R.id.btn_location://定位
                if (PermissionUtils.checkLocationPermission(this)) {
                    if (myLocationOverlay.getMyLocation() != null) {
                        mapView.getController().animateTo(myLocationOverlay.getMyLocation());
                    } else {
                        showMissingPermissionDialog();
                    }
                } else {
                    checkPermissions();
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
                mapView.invalidate();
                break;
            case R.id.btn_map_mode://地图切换
                if (isMapChanged) {
                    isMapChanged=false;
                    showMapSource();
                    showMapType();
                } else {
                    isMapChanged=true;
                    hideMapSource();
                    hideMapType();
                }
                break;
            case R.id.btn_point_edit://开始绘制轨迹
                editToolLayout.setVisibility(View.VISIBLE);
                prickLayout.setVisibility(View.VISIBLE);
                trackRecord.setVisibility(View.GONE);
                mainBottomLayout.setVisibility(View.VISIBLE);
                searchCardView.setVisibility(View.GONE);
                myLocationOverlay.disableCompass();
                break;
            case R.id.btn_track_record://轨迹记录
                startTime = DateUtils.formatUTC(System.currentTimeMillis(), null);
                if (isRecord) {
                    isRecord=false;
                    locationList.clear();
                    trackRecord.setImageResource(R.drawable.ic_vector_pause);
                    toastUtils.showToast("开始记录轨迹");
                    startService(new Intent(this, MyLocationService.class));
                    // 注册广播
                    myReceiver = new MyReceiver();
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("com.bigemap.osmdroiddemo.service.intent.locationList");
                    registerReceiver(myReceiver, filter);
                    myLocationOverlay.enableFollowLocation();
                } else {
                    isRecord=true;
                    trackRecord.setImageResource(R.drawable.ic_vector_play);
                    stopService(new Intent(this, MyLocationService.class));
                    if (locationList.size() > 1) {
                        toastUtils.showToast("停止记录轨迹");
                    } else {
                        toastUtils.showToast("此次轨迹路线太短，不作记录");
                    }
                    unregisterReceiver(myReceiver);
                    myLocationOverlay.disableFollowLocation();
                    saveTrack(startTime, name, locationList);
                }
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
                    Track track = saveTrack(startTime, name, points);
                    int trackId=track.getId();
                    UIUtils.showTrackEditActivity(MainActivity.this, trackId);
                    Log.d(TAG, "trackId=" + trackId);
                } else {
                    toastUtils.showToast("轨迹点数不足，请绘制两点以上");
                }
                points.clear();
                break;
            case R.id.btn_edit_shape://图形
                points.clear();
                drawType = 1;
                saveBtn.setVisibility(View.VISIBLE);
                shapeBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
                trackBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
                measureBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
                break;
            case R.id.btn_edit_track://轨迹
                points.clear();
                drawType = 0;
                saveBtn.setVisibility(View.VISIBLE);
                shapeBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
                trackBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
                measureBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
                break;
            case R.id.btn_edit_measure://周长和面积
                points.clear();
                drawType = 2;
                saveBtn.setVisibility(View.GONE);
                shapeBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
                trackBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
                measureBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
                break;
            case R.id.btn_edit_close://关闭轨迹绘制
                myLocationOverlay.setOrientationProvider(new InternalCompassOrientationProvider(this));
                myLocationOverlay.enableCompass();
                points.clear();
                editToolLayout.setVisibility(View.GONE);
                prickLayout.setVisibility(View.GONE);
                trackRecord.setVisibility(View.VISIBLE);
                mainBottomLayout.setVisibility(View.GONE);
                searchCardView.setVisibility(View.VISIBLE);
                break;
            case R.id.edit_text_clear://清空搜索输入
                searchText.setText("");
                break;
            case R.id.search_editText://输入时
                searchText.setCursorVisible(true);
                searchCardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                break;
            case R.id.tv_map_type_normal://点击地图类型
                normalMap.setSelected(true);
                satelliteMap.setSelected(false);
                dataKeeper.put(Constant.PREFS_NORMAL_MAP_STATE, true);
                dataKeeper.put(Constant.PREFS_SATELLITE_STATE, false);
                showMapView();
                break;
            case R.id.tv_map_type_satellite://点击卫星地图
                normalMap.setSelected(false);
                satelliteMap.setSelected(true);
                dataKeeper.put(Constant.PREFS_NORMAL_MAP_STATE, false);
                dataKeeper.put(Constant.PREFS_SATELLITE_STATE, true);
                showMapView();
                break;
//            case R.id.iv_layers_import:
//                if (isExpanded) {
//                    isExpanded=false;
//                    contentScroll.setVisibility(View.VISIBLE);
//                } else {
//                    isExpanded=true;
//                    contentScroll.setVisibility(View.GONE);
//                }
//                break;
        }
    }

    /**
     * 保存轨迹
     * @param startTime 开始时间
     * @param name 轨迹名称
     * @param geoPoints 轨迹点
     * @return
     */
    private Track saveTrack(String startTime, String name, List<GeoPoint> geoPoints) {
        Track track = new Track();
        for (GeoPoint geoPoint : geoPoints) {
            Location point = new Location();
            point.setLatitude(String.valueOf(geoPoint.getLatitude()));
            point.setLongitude(String.valueOf(geoPoint.getLongitude()));
            point.save();
            track.getLocations().add(point);
        }
        track.setName(name);
        track.setStartTime(startTime);
        track.setTrackSource(1);
        track.setTrackType(drawType);
        track.save();
        return track;
    }

    /**
     * 绘制轨迹
     */
    private void drawTrack(List<GeoPoint> geoPoints) {
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

    /**
     * 显示地图
     */
    private void showMapView() {
        int mapSource = dataKeeper.getInt(Constant.PREFS_MAP_SOURCE, 0);
        boolean mapState = dataKeeper.get(Constant.PREFS_NORMAL_MAP_STATE, true);
        if (mapSource == Constant.GOOGLE_MAP) {
            if (mapState) {
                mapView.setTileSource(TileSourceFactory.getTileSource(GOOGLE_MAP));
                myLocationOverlay.setTileSource(Constant.GOOGLE_MAP);
            } else {
                mapView.setTileSource(TileSourceFactory.getTileSource(GOOGLE_SATELLITE));
                myLocationOverlay.setTileSource(Constant.GOOGLE_MAP);
            }
        } else if (mapSource == Constant.OSM) {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            myLocationOverlay.setTileSource(Constant.OSM);
        }
    }

    /**
     * 显示地图类型
     */
    private void showMapType() {
        normalMap.setVisibility(View.VISIBLE);
        satelliteMap.setVisibility(View.VISIBLE);
        boolean normal = dataKeeper.get(Constant.PREFS_NORMAL_MAP_STATE, true);
        boolean satellite = dataKeeper.get(Constant.PREFS_SATELLITE_STATE, false);
        normalMap.setSelected(normal);
        satelliteMap.setSelected(satellite);
    }

    /**
     * 显示地图源
     */
    private void showMapSource() {
        mapSourceList.setVisibility(View.VISIBLE);
        mapSourceList.setLayoutManager(new GridLayoutManager(this, 2));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mapSourceList.setHasFixedSize(true);

        List<Map> maps = new ArrayList<>();
        maps.add(new Map(R.drawable.ic_map_google, "谷歌"));
        maps.add(new Map(R.drawable.ic_map_osm, "OSM"));
        maps.add(new Map(R.drawable.ic_map_amap, "高德"));
        maps.add(new Map(R.drawable.ic_map_baidu, "百度"));

        final MapSourceAdapter adapter = new MapSourceAdapter(this);
        adapter.setDataList(maps);
        mapSourceList.setAdapter(adapter);
        int SelectedMapSource = dataKeeper.getInt(Constant.PREFS_MAP_SOURCE, 0);
        adapter.setItemSelected(SelectedMapSource);
        adapter.setOnItemListener(new MapSourceAdapter.OnItemListener() {
            @Override
            public void onClick(View v, int pos, Object data) {
                Map map = (Map) data;
                mapModeBtn.setImageResource(map.getMapIcon());
                adapter.setItemSelected(pos);
                dataKeeper.putInt(Constant.PREFS_MAP_SOURCE, pos);
                showMapView();
            }
        });
    }

    /**
     * 隐藏键盘
     */
    private void hideKeyboard() {
        searchText.setCursorVisible(false);
        searchCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.translucent_white_65));
        InputMethodManager inputMethodManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
    }

    /**
     * 隐藏地图选择
     */
    private void hideMapSource() {
        mapSourceList.setVisibility(View.GONE);
    }

    private void hideMapType() {
        normalMap.setVisibility(View.GONE);
        satelliteMap.setVisibility(View.GONE);
    }

    // 获取广播数据
    private class MyReceiver extends BroadcastReceiver {

        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            locationList = bundle.getParcelableArrayList("saveGps");
            if (locationList.size() > 1) {
                mapView.getOverlays().remove(polyline);
                setPolyline(locationList);
            }
        }
    }

    // 显示缺失权限提示
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = builder.create();
        builder.setMessage("当前应用缺少必要权限。\n\n请进入\"位置信息\"选择\"高精确度\"。\n\n最后点击两次后退按钮，即可返回。");
        // 拒绝, 退出应用
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
            }
        });
        builder.show();
    }

    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings");
        intent.setComponent(comp);
        intent.setAction("android.intent.action.VIEW");
        startActivityForResult(intent, 0);
    }

}
