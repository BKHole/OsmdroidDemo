package com.bigemap.osmdroiddemo.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.adapter.MapSourceAdapter;
import com.bigemap.osmdroiddemo.adapter.OfflineMapSourceAdapter;
import com.bigemap.osmdroiddemo.adapter.SimpleTreeRecyclerAdapter;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.entity.BaseGraph;
import com.bigemap.osmdroiddemo.entity.Map;
import com.bigemap.osmdroiddemo.entity.OfflineMap;
import com.bigemap.osmdroiddemo.entity.Result;
import com.bigemap.osmdroiddemo.entity.Tip;
import com.bigemap.osmdroiddemo.entity.Title;
import com.bigemap.osmdroiddemo.http.HttpCallback;
import com.bigemap.osmdroiddemo.http.HttpClient;
import com.bigemap.osmdroiddemo.kml.ReadKml;
import com.bigemap.osmdroiddemo.kml.WriteKml;
import com.bigemap.osmdroiddemo.overlay.MyLocationOverlay;
import com.bigemap.osmdroiddemo.overlay.MyPolyline;
import com.bigemap.osmdroiddemo.service.LocationService;
import com.bigemap.osmdroiddemo.tileSource.GoogleMapsTileSource;
import com.bigemap.osmdroiddemo.tileSource.GoogleSatelliteTileSource;
import com.bigemap.osmdroiddemo.tileSource.MBTileProvider;
import com.bigemap.osmdroiddemo.treelist.Node;
import com.bigemap.osmdroiddemo.treelist.OnTreeNodeClickListener;
import com.bigemap.osmdroiddemo.utils.DateUtils;
import com.bigemap.osmdroiddemo.utils.MapMeasureUtils;
import com.bigemap.osmdroiddemo.utils.PermissionUtils;
import com.bigemap.osmdroiddemo.utils.PositionUtils;
import com.bigemap.osmdroiddemo.utils.binding.Bind;
import com.bigemap.osmdroiddemo.view.IconView;
import com.bigemap.osmdroiddemo.viewholder.OnItemListener;
import com.zhy.http.okhttp.callback.StringCallback;

import org.litepal.crud.DataSupport;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayWithIW;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

import static com.bigemap.osmdroiddemo.constants.Constant.POST_URL;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_LATITUDE_STRING;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_LONGITUDE_STRING;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_ORIENTATION;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_TILE_SOURCE;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_ZOOM_LEVEL;
import static com.bigemap.osmdroiddemo.constants.Constant.PREFS_ZOOM_LEVEL_DOUBLE;

public class MainActivity extends BaseActivity implements View.OnClickListener,
        TextWatcher, AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";
    public static final String GOOGLE_MAP = "Google Map";
    public static final String GOOGLE_SATELLITE = "Google卫星图";
    private ScaleBarOverlay mScaleBarOverlay;//比例尺
    private MyLocationOverlay myLocationOverlay; //定位显示
    private GpsMyLocationProvider gps;

    @Bind(R.id.drawer_layout)
    private DrawerLayout mDrawerLayout;//侧边栏
    @Bind(R.id.mapView)
    private MapView mapView;//地图
    @Bind(R.id.btn_location)
    private IconView locationBtn;//定位
    @Bind(R.id.view_main_bottom)
    private LinearLayout navMainBottom;//底部导航栏父控件
    //底部导航栏子控件
    @Bind(R.id.main_tab_map)
    private LinearLayout tabMap;//地图
    @Bind(R.id.main_tab_edit)
    private LinearLayout tabEdit;//编辑
    @Bind(R.id.main_tab_track)
    private LinearLayout tabTrack;//轨迹
    @Bind(R.id.main_tab_offline)
    private LinearLayout tabOffline;//离线
    //    @Bind(R.id.main_tab_mine)
//    private LinearLayout tabMine;//我
    //轨迹绘制操作/
    @Bind(R.id.edit_bottom_tool_ly)
    private LinearLayout editBottomTool;//底部编辑父控件
    @Bind(R.id.btn_edit_undo)
    private TextView undoBtn;//撤销，上一步
    @Bind(R.id.btn_edit_empty)
    private TextView emptyBtn;//清空，删除界面上所有轨迹
    @Bind(R.id.btn_edit_rename)
    private TextView editBtn;//编辑
    @Bind(R.id.btn_edit_finish)
    private TextView endBtn;//结束
    @Bind(R.id.btn_edit_delete)
    private TextView deleteBtn;//删除
    @Bind(R.id.btn_edit_save)
    private TextView saveBtn;//保存

    @Bind(R.id.rl_center_prick)
    private RelativeLayout prickLayout;//十字编辑区域
    @Bind(R.id.edit_top_tool_ly)
    private LinearLayout editTopTool;//顶部编辑区域
    @Bind(R.id.btn_track_record)
    private IconView trackRecord;//轨迹记录
    @Bind(R.id.btn_edit_shape)
    private TextView polygonMode;//图形
    @Bind(R.id.btn_edit_line)
    private TextView lineMode;//路线
    @Bind(R.id.btn_edit_poi)
    private TextView poiMode;//点
    @Bind(R.id.btn_edit_close)
    private TextView closeBtn;//关闭

    //搜索框
    @Bind(R.id.search_box)
    private LinearLayout searchBox;
    @Bind(R.id.search_editText)
    private AutoCompleteTextView searchText;
    @Bind(R.id.edit_text_clear)
    private ImageButton editTextClearBtn;

    //侧边栏
    @Bind(R.id.list_layers_content)
    private RecyclerView myLayersList;
    @Bind(R.id.iv_layers_import)
    private ImageView importLayers;

    private String drawType = "";//0:画线，1:图形，2:周长和面积，3:导入
    private List<GeoPoint> points;//编辑模式描点记录

    private ArrayList<GeoPoint> locationList;//轨迹记录定位
    private MyReceiver myReceiver;
    private boolean isRecording = false;//是否正在记录轨迹
    private boolean isMapChanged = true;//地图源状态
    private boolean isFullScreen = true;//是否全屏显示
    private boolean isEditMode = false;//是否处于编辑模式
    private boolean isMyTrack = false;//是否处于轨迹界面
    private boolean isOffline = false;//是否处于离线模式
    private boolean isOnline = true;//是否处于在线模式
    private boolean isFinished = false;//是否结束绘制

    //地图选择
    @Bind(R.id.btn_map_mode)
    private ImageView mapModeBtn;
    @Bind(R.id.divider_map_mode)
    private View dividerView;
    @Bind(R.id.tv_map_type_normal)
    private TextView normalMap;
    @Bind(R.id.tv_map_type_satellite)
    private TextView satelliteMap;
    @Bind(R.id.list_map_source)
    private RecyclerView mapSourceList;//在线地图源列表
    @Bind(R.id.btn_add_offline)
    private Button offlineBtn;

    public static boolean isBackground = true;
    private List<Tip> tips;
    private List<Node> mData;
    private List<BaseGraph> baseGraphs;
//    private FolderOverlay poiMarkers;//导入kml中所有点
    private MyPolyline gpsLine = null;//gps记录的轨迹
    private MyPolyline editLine = null;//当前正在编辑的轨迹
    private Polygon editPolygon = null;//当前正在编辑的多边形
//    private List<Marker> markers;
    private FolderOverlay polylines;//当前正在编辑的所有轨迹
    private FolderOverlay polygons;//当前正在编辑的所有多边形
    private FolderOverlay tempMarkers;//当前正在编辑的多边形的点
    private FolderOverlay markers;//当前正在编辑的点
    private SimpleTreeRecyclerAdapter treeRecyclerAdapter;
    private Node root;//根节点
    private int selectedPosition = -1;//当前状态未选中
    private int saveType;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        }
        if (isFirstStart()) {
//            Configuration.getInstance().setOsmdroidBasePath(new File(Constant.APP_BASE_PATH));
//            Configuration.getInstance().setOsmdroidTileCache(new File(Constant.TILE_CACHE));
            Configuration.getInstance().setTileDownloadThreads((short) 4);//设置瓦片下载线程数
        }
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        initView();
        initData();
        initTileSource();

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

        //显示定位
        gps = new GpsMyLocationProvider(this);
        gps.setLocationUpdateMinTime(1000);//默认两秒钟更新一次
        gps.addLocationSource(LocationManager.NETWORK_PROVIDER);
        myLocationOverlay = new MyLocationOverlay(gps, mapView);
        myLocationOverlay.setDrawAccuracyEnabled(false);
        mapView.getOverlays().add(myLocationOverlay);

        mapClickEvent();

        if (mapView.getTileProvider().getTileSource().equals(TileSourceFactory.MAPNIK)) {
            myLocationOverlay.setTileSource(Constant.OSM);
            dataKeeper.putInt(Constant.PREFS_MAP_SOURCE, Constant.OSM);
        }

        root = new Node(1, 0, "我的图层");
        root.setChecked(true);
        mData.add(root);

        treeRecyclerAdapter = new SimpleTreeRecyclerAdapter(
                myLayersList, this, mData, 1, R.drawable.tree_ex, R.drawable.tree_ec);
        myLayersList.setLayoutManager(new LinearLayoutManager(this));
        myLayersList.setHasFixedSize(true);
        myLayersList.setAdapter(treeRecyclerAdapter);

        treeRecyclerAdapter.setOnTreeNodeClickListener(treeNodeListener);
    }

    private void initData() {
        points = new ArrayList<>();
        locationList = new ArrayList<>();
        mData = new ArrayList<>();
        baseGraphs = new ArrayList<>();
        initEditData();
    }

    private void initView() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        mDrawerLayout.addDrawerListener(drawerListener);
        tabMap.setSelected(true);

        locationBtn.setOnClickListener(this);
        mapModeBtn.setOnClickListener(this);
        tabMap.setOnClickListener(this);
        tabEdit.setOnClickListener(this);
        tabTrack.setOnClickListener(this);
        tabOffline.setOnClickListener(this);
//        tabMine.setOnClickListener(this);
        trackRecord.setOnClickListener(this);
        undoBtn.setOnClickListener(this);
        emptyBtn.setOnClickListener(this);
        endBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        editBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        polygonMode.setOnClickListener(this);
        lineMode.setOnClickListener(this);
        poiMode.setOnClickListener(this);
        prickLayout.setOnClickListener(this);
        searchText.setOnClickListener(this);
        searchText.addTextChangedListener(this);
        searchText.setOnItemClickListener(this);
        editTextClearBtn.setOnClickListener(this);
        normalMap.setOnClickListener(this);
        satelliteMap.setOnClickListener(this);
        importLayers.setOnClickListener(this);
        offlineBtn.setOnClickListener(this);
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
        }
    }

    /**
     * 监听侧边栏
     */
    private DrawerLayout.SimpleDrawerListener drawerListener = new DrawerLayout.SimpleDrawerListener() {
        @Override
        public void onDrawerOpened(View drawerView) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            super.onDrawerOpened(drawerView);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            super.onDrawerClosed(drawerView);
        }
    };

    private OnTreeNodeClickListener treeNodeListener = new OnTreeNodeClickListener() {
        @Override
        public void onClick(Node node, int position) {
            treeRecyclerAdapter.setItemSelected(position);
            BaseGraph baseGraph = (BaseGraph) node.bean;
            selectedPosition = position;
            if (baseGraph != null) {
                Log.d(TAG, "onClick: id=" + baseGraph.getId());
            }
            Log.d(TAG, "onClick: position=" + position);
        }

        @Override
        public void onCheckChange(Node node, int position, List<Node> checkedNodes) {
            Log.d(TAG, "onCheckChange: nodeId=" + node.getId());
            Log.d(TAG, "onCheckChange: position=" + position);
            BaseGraph baseGraph = (BaseGraph) node.bean;
            if (baseGraph != null) {
                switch (baseGraph.getType()) {
                    case Constant.POLYGON:
                        if (!node.isChecked()){
                            polygons.getItems().get(baseGraph.getId()).setEnabled(false);
                        }else{
                            polygons.getItems().get(baseGraph.getId()).setEnabled(true);
                        }
                        break;
                    case Constant.POLYLINE:
                        if (!node.isChecked()){
                            polylines.getItems().get(baseGraph.getId()).setEnabled(false);
                        }else{
                            polylines.getItems().get(baseGraph.getId()).setEnabled(true);
                        }
                        break;
                    case Constant.POI:
                        if (!node.isChecked()){
                            markers.getItems().get(baseGraph.getId()).setEnabled(false);
                        }else{
                            markers.getItems().get(baseGraph.getId()).setEnabled(true);
                        }
                        break;
                }
            }
            mapView.invalidate();
        }
    };

    /**
     * 点击地图操作
     */
    private void mapClickEvent() {
        Overlay mapEventOverlay = new Overlay() {
            @Override
            public void draw(Canvas c, MapView osmv, boolean shadow) {

            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
                if (isOffline || isEditMode) {
                    return super.onSingleTapConfirmed(e, mapView);
                }
                if (isFullScreen) {
                    isFullScreen = false;
                    isMapChanged = true;
                    navMainBottom.setVisibility(View.GONE);
                    importLayers.setVisibility(View.GONE);
                } else {
                    isFullScreen = true;
                    isMapChanged = true;
                    navMainBottom.setVisibility(View.VISIBLE);
                    importLayers.setVisibility(View.VISIBLE);
                }
                return super.onSingleTapConfirmed(e, mapView);
            }
        };
        mapView.getOverlays().add(mapEventOverlay);
    }

    /**
     * 判断程序是否第一次启动
     *
     * @return boolean
     */
    private boolean isFirstStart() {
        return dataKeeper.get("first_start", true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: test");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
//        postUrl("login");
    }

    /**
     * 统计次数
     *
     * @param type
     */
    private void postUrl(String type) {
        if (PermissionUtils.checkPhoneStatePermission(this)) {
            TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telManager.getDeviceId();
            HttpClient.postImei(POST_URL, type, imei, new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {

                }

                @Override
                public void onResponse(String response, int id) {
                    Log.d(TAG, "onResponse: " + response);
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
        isBackground = false;
        if (isOnline) {
            int zoomLevel = dataKeeper.getInt(PREFS_ZOOM_LEVEL_DOUBLE, dataKeeper.getInt(PREFS_ZOOM_LEVEL, 10));
            mapView.getController().setZoom(zoomLevel);
        }
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.setOrientationProvider(new InternalCompassOrientationProvider(this));
        if (!isEditMode) {
            myLocationOverlay.enableCompass();
        }
        mScaleBarOverlay.enableScaleBar();
        //判断程序是否第一次启动
        if (isFirstStart()) {
            mapView.getController().setCenter(new GeoPoint(30.5702183724, 104.0647735044));
        } else {
            if (isMyTrack) {
                return;
            }
            final String latitudeString = dataKeeper.get(PREFS_LATITUDE_STRING, "30.5702183724");
            final String longitudeString = dataKeeper.get(PREFS_LONGITUDE_STRING, "104.0647735044");
            final double latitude = Double.valueOf(latitudeString);
            final double longitude = Double.valueOf(longitudeString);
            mapView.getController().animateTo(new GeoPoint(latitude, longitude));
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        isBackground = true;
        dataKeeper.put("first_start", false);
        if (isOnline) {
            saveMapInfo();
        }
        if (!isMapChanged) {
            isMapChanged = true;
            hideMapSource();
        }
        myLocationOverlay.disableMyLocation();
        myLocationOverlay.disableCompass();
        mScaleBarOverlay.disableScaleBar();
        super.onPause();
//        if (poiMarkers != null) {
//            poiMarkers.closeAllInfoWindows();
//        }
    }


    @Override
    protected void onDestroy() {
//        postUrl("logout");
        Log.d(TAG, "onDestroy: ");
        if (isRecording) {
            unregisterReceiver(myReceiver);
            myReceiver = null;
        }
        locationList.clear();
        baseGraphs.clear();
        baseGraphs = null;
        if (markers!=null){
            markers.closeAllInfoWindows();
            mapView.getOverlays().remove(markers);
        }
        if (polylines != null) {
            polylines.closeAllInfoWindows();
            mapView.getOverlays().remove(polylines);
        }
        if (polygons != null) {
            polygons.closeAllInfoWindows();
            mapView.getOverlays().remove(polygons);
        }
        for (Overlay overlay : mapView.getOverlays()) {
            if (overlay instanceof OverlayWithIW) {
                ((OverlayWithIW) overlay).onDestroy();
                mapView.getOverlays().remove(overlay);
            }
        }
        gps.clearLocationSources();
        mapView.onDetach();
        super.onDestroy();
    }

    // START PERMISSION CHECK
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        if (!PermissionUtils.checkLocationPermission(this)) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!PermissionUtils.checkPhoneStatePermission(this)) {
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
                Log.d(TAG, "onRequestPermissionsResult: do nothing");
                if (PermissionUtils.checkLocationPermission(this)) {
                    myLocationOverlay.runOnFirstFix(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: first location=" + myLocationOverlay.getLastFix());
                            mapView.getController().animateTo(myLocationOverlay.getMyLocation());
                        }
                    });
                } else {
                    toastUtils.showSingletonToast("需要定位权限");
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
        marker.setTitle("纬度=" + point.getLatitude() + ",\n\n经度=" + point.getLongitude());
        marker.setFlat(true);//设置marker平贴地图效果
        if (drawType.equals(Constant.POI)){
            markers.add(marker);
        }else{
            tempMarkers.add(marker);
        }
//        mapView.getOverlays().removeAll(markers);
//        markers.add(marker);
//        mapView.getOverlays().addAll(markers);
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 0) {
            editTextClearBtn.setVisibility(View.VISIBLE);
        } else {
            editTextClearBtn.setVisibility(View.INVISIBLE);
        }
        String newText = s.toString().trim();
        if (!TextUtils.isEmpty(newText)) {
            BoundingBox box = mapView.getBoundingBox();
            HttpClient.getTip(box, newText, new HttpCallback<Result>() {
                @Override
                public void onSuccess(Result result) {
                    tips = getLocationTitle(result.getData().getTitles());
                    List<String> listString = new ArrayList<>();
                    for (int i = 0; i < tips.size(); i++) {
                        listString.add(tips.get(i).getName());
                    }
                    ArrayAdapter<String> aAdapter = new ArrayAdapter<>(
                            MainActivity.this,
                            android.R.layout.simple_list_item_1, listString);
                    searchText.setAdapter(aAdapter);
                    aAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFail(Exception e) {
                    Log.d(TAG, "onFail: ");
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        searchText.setText("");
        Tip tip = tips.get(position);
        GeoPoint searchPoint = tip.getPoint();
        mapView.getController().animateTo(searchPoint);
        setPoint(searchPoint, tip.getName());
    }

    /**
     * 排除空坐标地址
     *
     * @param titles
     * @return
     */
    private List<Tip> getLocationTitle(List<Title> titles) {
        List<Tip> tips = new ArrayList<>();
        for (Title title : titles) {
            if (title.getTip().getPoint() != null) {
                tips.add(title.getTip());
            }
        }
        return tips;
    }

    //隐藏软键盘和编辑框光标
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.getCurrentFocus() != null) {
            hideKeyboard();
            hideMapSource();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        String time = DateUtils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd");
        String name;
        if (drawType.equals(Constant.POLYLINE)) {
            name = lineMode.getText() + "&" + time;
        } else if (drawType.equals(Constant.POLYGON)) {
            name = polygonMode.getText() + "&" + time;
        } else {
            name = poiMode.getText() + "&" + time;
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
            case R.id.btn_map_mode://地图切换
                if (isMapChanged) {
                    isMapChanged = false;
                    showMapSource();
                } else {
                    isMapChanged = true;
                    hideMapSource();
                }
                break;
            case R.id.main_tab_edit://绘制轨迹
                isEditMode = true;
                editTopTool.setVisibility(View.VISIBLE);
                prickLayout.setVisibility(View.VISIBLE);
                editBottomTool.setVisibility(View.VISIBLE);
                trackRecord.setVisibility(View.GONE);
                searchBox.setVisibility(View.GONE);
                navMainBottom.setVisibility(View.GONE);
                myLocationOverlay.disableCompass();

//                markers = new ArrayList<>();
                break;
            case R.id.main_tab_map://在线地图
                isOnline = true;
                isOffline = false;
                tabMap.setSelected(true);
//                tabMine.setSelected(false);
                tabOffline.setSelected(false);
                if (!isMapChanged) {
                    isMapChanged = true;
                    hideMapSource();
                }
                loadOnline();
                showAll();
                break;
            case R.id.main_tab_track://轨迹记录
                Intent intent = new Intent(MainActivity.this, MyTrackActivity.class);
                startActivityForResult(intent, 33);
                break;
            case R.id.main_tab_offline://离线地图
                isOffline = true;
                isOnline = false;
                tabOffline.setSelected(true);
                tabMap.setSelected(false);
//                tabMine.setSelected(false);
                saveMapInfo();
                hideAll();
                showOfflineMap();
//                loadOffline(Constant.APP_BASE_PATH);
                Log.d(TAG, "onClick: " + mapView.getTileProvider().toString());
                break;
//            case R.id.main_tab_mine://我
//                tabOffline.setSelected(false);
//                tabMine.setSelected(true);
//                tabMap.setSelected(false);
//                break;
            case R.id.btn_track_record://记录轨迹
                recordingTrack();
                break;
            case R.id.rl_center_prick:
                if (TextUtils.isEmpty(drawType)) {
                    toastUtils.showSingletonToast("请先选择上方绘制类型");
                } else {
                    GeoPoint centerPoint = (GeoPoint) mapView.getMapCenter();
                    setRoundPoint(centerPoint);
                    points.add(centerPoint);
                    drawTrack(points);
                }
                break;
            case R.id.btn_edit_undo://撤销上一步
                if (points.size() > 0) {
                    points.remove(points.size() - 1);
                    drawTrack(points);
                    if (drawType.equals(Constant.POI)){
                        markers.getItems().remove(markers.getItems().size()-1);
                    }else {
                        tempMarkers.getItems().remove(tempMarkers.getItems().size()-1);
                    }
//                    mapView.getOverlays().removeAll(markers);
//                    markers.remove(markers.size() - 1);
//                    mapView.getOverlays().addAll(markers);
                    mapView.invalidate();
                }
                break;
            case R.id.btn_edit_empty://清空界面
                points.clear();
//                markers.clear();
                for (Overlay overlay : mapView.getOverlays()) {
                    if (overlay instanceof OverlayWithIW) {
                        ((OverlayWithIW) overlay).onDestroy();
                        mapView.getOverlays().remove(overlay);
                    }
                    if (overlay instanceof FolderOverlay) {
                        ((FolderOverlay) overlay).closeAllInfoWindows();
                        mapView.getOverlays().remove(overlay);
                    }
                }
                tempMarkers=null;
                markers=null;
                polylines=null;
                polygons=null;
                mapView.invalidate();
                resetEditStatus();
                initEditData();

                mData.clear();
                mData.add(root);
                treeRecyclerAdapter.addDataAll(mData, 0);
                break;
            case R.id.btn_edit_finish://完成本次绘制
                saveLine(points);
                points.clear();
                resetEditStatus();
                break;
            case R.id.btn_edit_close://关闭轨迹绘制
                if (points.size() > 0) {
                    toastUtils.showSingletonToast("请先结束本次操作");
                } else {
                    isEditMode = false;
                    editLine = null;
                    editPolygon = null;
                    resetEditStatus();
                    if (isOffline) {
                        editTopTool.setVisibility(View.GONE);
                        prickLayout.setVisibility(View.GONE);
                        editBottomTool.setVisibility(View.GONE);
                        navMainBottom.setVisibility(View.VISIBLE);
                        return;
                    }
                    myLocationOverlay.setOrientationProvider(new InternalCompassOrientationProvider(this));
                    myLocationOverlay.enableCompass();

                    editTopTool.setVisibility(View.GONE);
                    prickLayout.setVisibility(View.GONE);
                    editBottomTool.setVisibility(View.GONE);
                    trackRecord.setVisibility(View.VISIBLE);
                    searchBox.setVisibility(View.VISIBLE);
                    navMainBottom.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.iv_layers_import://侧边栏
                mDrawerLayout.openDrawer(Gravity.LEFT);
                break;
            case R.id.btn_edit_delete://删除
                if (selectedPosition > 0) {
                    builder = new AlertDialog.Builder(this);
                    builder.setTitle("需要删除该条数据吗？");
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mData.remove(selectedPosition);
                            treeRecyclerAdapter.setItemSelected(-1);
                            treeRecyclerAdapter.addDataAll(mData, 1);
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                } else {
                    toastUtils.showSingletonToast("请先选中需要编辑的选项");
                }
                break;
            case R.id.btn_edit_rename://编辑
//                mDrawerLayout.closeDrawers();
                if (selectedPosition != -1) {
                    askSaveDialog(mData.get(selectedPosition).getName(), 1);
                } else {
                    toastUtils.showSingletonToast("请先选中需要编辑的选项");
                }
                break;
            case R.id.btn_edit_save://保存轨迹kml
                if (isFinished && baseGraphs.size() > 0) {
                    saveType = 12;
                    askSaveDialog(name, 0);
                } else {
                    toastUtils.showSingletonToast("当前没有绘制数据或结束绘制，无法保存");
                }
                break;
            case R.id.btn_edit_shape://图形
                if (points.size() > 0) {
                    toastUtils.showSingletonToast("请先结束本次操作");
                } else {
                    polygonMode.setSelected(true);
                    lineMode.setSelected(false);
                    poiMode.setSelected(false);
                    drawType = Constant.POLYGON;

                    Polygon polygon = new Polygon();
                    polygon.setStrokeWidth(5);
                    polygon.setFillColor(Color.GRAY);
                    editPolygon = polygon;
                    polygons.add(editPolygon);
                }
                break;
            case R.id.btn_edit_line://线
                if (points.size() > 0) {
                    toastUtils.showSingletonToast("请先结束本次操作");
                } else {
                    polygonMode.setSelected(false);
                    lineMode.setSelected(true);
                    poiMode.setSelected(false);
                    drawType = Constant.POLYLINE;

                    MyPolyline polyline = new MyPolyline();
                    polyline.setWidth(5);
                    editLine = polyline;
                    polylines.add(editLine);
                }
                break;
            case R.id.btn_edit_poi://点
                if (points.size() > 0) {
                    toastUtils.showSingletonToast("请先结束本次操作");
                } else {
                    polygonMode.setSelected(false);
                    lineMode.setSelected(false);
                    poiMode.setSelected(true);
                    drawType = Constant.POI;
                }
                break;
            case R.id.edit_text_clear://清空搜索输入
                searchText.setText("");
                break;
            case R.id.search_editText://输入时
                searchBox.setSelected(true);
                searchText.setFocusable(true);
                searchText.setFocusableInTouchMode(true);
                searchText.setCursorVisible(true);
                break;
            case R.id.tv_map_type_normal://点击地图类型
                normalMap.setSelected(true);
                satelliteMap.setSelected(false);
                if (isOnline) {
                    dataKeeper.put(Constant.PREFS_NORMAL_MAP_STATE, true);
                    dataKeeper.put(Constant.PREFS_SATELLITE_STATE, false);
                    showMapView();
                } else if (isOffline) {
                    dataKeeper.put(Constant.PREFS_OFFLINE_ELECTRIC_STATE, true);
                    dataKeeper.put(Constant.PREFS_OFFLINE_SATELLITE_STATE, false);
                    showOfflineMap();
                }
                break;
            case R.id.tv_map_type_satellite://点击卫星地图
                normalMap.setSelected(false);
                satelliteMap.setSelected(true);
                if (isOnline) {
                    dataKeeper.put(Constant.PREFS_NORMAL_MAP_STATE, false);
                    dataKeeper.put(Constant.PREFS_SATELLITE_STATE, true);
                    showMapView();
                } else if (isOffline) {
                    dataKeeper.put(Constant.PREFS_OFFLINE_ELECTRIC_STATE, false);
                    dataKeeper.put(Constant.PREFS_OFFLINE_SATELLITE_STATE, true);
                    showOfflineMap();
                }
                break;
            case R.id.btn_add_offline://添加离线地图源
                startActivityForResult(new Intent(this, OfflineMapChooseActivity.class), 33);
                break;
        }
    }


    /**
     * 重置绘制初始状态
     */
    private void resetEditStatus() {
        drawType = "";
        polygonMode.setSelected(false);
        lineMode.setSelected(false);
        poiMode.setSelected(false);
    }

    /**
     * 初始化绘制数据
     */
    private void initEditData() {
        if (tempMarkers==null){
            tempMarkers=new FolderOverlay();
            mapView.getOverlays().add(tempMarkers);
        }
        if (markers==null){
            markers=new FolderOverlay();
            mapView.getOverlays().add(markers);
        }
        if (polygons==null){
            polygons = new FolderOverlay();
            mapView.getOverlays().add(polygons);
        }
        if (polylines==null){
            polylines = new FolderOverlay();
            mapView.getOverlays().add(polylines);
        }
    }

    /**
     * 记录轨迹
     */
    private void recordingTrack() {
        if (!isRecording) {
            isRecording = true;
            trackRecord.setText(R.string.track_stop);
            trackRecord.setTextColor(Color.RED);
            toastUtils.showToast("开始记录轨迹");
            if (gpsLine != null) {
                builder = new AlertDialog.Builder(this);
                builder.setTitle("需要继续上一次的记录吗？");
                builder.setCancelable(false);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startBackground();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        locationList.clear();
                        gpsLine = new MyPolyline();
                        gpsLine.setWidth(5);
                        gpsLine.setColor(Color.RED);
                        mapView.getOverlays().add(gpsLine);
                        startBackground();
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            } else {
                startBackground();
                gpsLine = new MyPolyline();
                gpsLine.setWidth(5);
                gpsLine.setColor(Color.RED);
                mapView.getOverlays().add(gpsLine);
            }
        } else {
            isRecording = false;
            trackRecord.setText(R.string.track_start);
            trackRecord.setTextColor(Color.WHITE);
            stopBackground();
            if (locationList.size() > 2) {
                toastUtils.showToast("停止记录轨迹");
                saveType = 11;
                askSaveDialog("轨迹#" + baseGraphs.size(), 0);
                BaseGraph baseGraph = new BaseGraph();
                baseGraph.setId(baseGraphs.size());
                baseGraph.setType(Constant.POLYLINE);
                baseGraph.setName("轨迹#" + baseGraphs.size());
                baseGraph.getGeoPoints().addAll(locationList);
                baseGraphs.add(baseGraph);

                Node node = new Node<>(baseGraphs.size() + 1, root.getId(), baseGraph.getName(), baseGraph);
                node.setChecked(true);
                mData.add(node);
                treeRecyclerAdapter.addDataAll(mData, 1);
            } else {
                toastUtils.showToast("此次轨迹路线太短，不作记录");
            }
        }
    }

    /*
    停止后台
     */
    private void stopBackground() {
        stopService(new Intent(this, LocationService.class));
        unregisterReceiver(myReceiver);
    }

    /*
    开启后台
     */
    private void startBackground() {
        startService(new Intent(this, LocationService.class));
        // 注册广播
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.bigemap.osmdroiddemo.service.intent.locationList");
        registerReceiver(myReceiver, filter);
    }

    /*
    加载在线地图
     */
    private void loadOnline() {
        String tileSourceName = dataKeeper.get(PREFS_TILE_SOURCE, GOOGLE_MAP);
        ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
        mapView.setTileSource(tileSource);
        mapView.setTileProvider(new MapTileProviderBasic(getApplicationContext(), tileSource));
        int zoomLevel = dataKeeper.getInt(PREFS_ZOOM_LEVEL_DOUBLE, dataKeeper.getInt(PREFS_ZOOM_LEVEL, 10));
        mapView.getController().setZoom(zoomLevel);
        String latitudeString = dataKeeper.get(PREFS_LATITUDE_STRING, "30.5702183724");
        String longitudeString = dataKeeper.get(PREFS_LONGITUDE_STRING, "104.0647735044");
        double latitude = Double.valueOf(latitudeString);
        double longitude = Double.valueOf(longitudeString);
        mapView.getController().animateTo(new GeoPoint(latitude, longitude));
        mapView.setUseDataConnection(true);
        mapView.setDrawingCacheEnabled(true);
        mapView.setMultiTouchControls(true);
    }

    /*
   保存地图状态信息
    */
    private void saveMapInfo() {
        dataKeeper.put(PREFS_TILE_SOURCE, mapView.getTileProvider().getTileSource().name());
        dataKeeper.put(PREFS_ORIENTATION, mapView.getMapOrientation());
        dataKeeper.put(PREFS_LATITUDE_STRING, String.valueOf(mapView.getMapCenter().getLatitude()));
        dataKeeper.put(PREFS_LONGITUDE_STRING, String.valueOf(mapView.getMapCenter().getLongitude()));
        dataKeeper.putInt(PREFS_ZOOM_LEVEL_DOUBLE, mapView.getZoomLevel());
    }

    /*
    加载离线
     */
    private void loadOffline(String filePath) {
        Log.d(TAG, "loadOffline: " + filePath);
        mapViewOtherData(filePath);
        if (myLocationOverlay.getMyLocation() != null) {
            mapView.getController().setCenter(myLocationOverlay.getMyLocation());
        }
        mapView.setUseDataConnection(false);
        mapView.setTilesScaledToDpi(true);
        mapView.setMultiTouchControls(false);
    }

    public void mapViewOtherData(String strFilepath) {
        File f = new File(strFilepath);
        if (f.exists()) {
            File[] list = f.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    if (list[i].isDirectory()) {
                        continue;
                    }
                    String name = list[i].getName().toLowerCase();
                    if (!name.contains(".")) {
                        continue; //skip files without an extension
                    }
                    name = name.substring(name.lastIndexOf(".") + 1);
                    if (name.length() == 0) {
                        continue;
                    }
                    if (name.equalsIgnoreCase("bmdb")) {
                        try {

                            //ok found a file we support and have a driver for the format, for this demo, we'll just use the first one
                            //create the offline tile provider, it will only do offline file archives
                            //again using the first file
                            MBTileProvider provider = new MBTileProvider(new SimpleRegisterReceiver(this), list[i]);
                            //tell osmdroid to use that provider instead of the default rig which is (asserts, cache, files/archives, online
                            mapView.setTileProvider(provider);
                            Log.d(TAG, "mapViewOtherData: " + provider.getMaximumZoomLevel());
                            this.mapView.setTileSource(provider.getTileSource());
                            toastUtils.showToast("Using " + list[i].getAbsolutePath());
                            this.mapView.invalidate();
                            return;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }else{
                        toastUtils.showSingletonToast("请使用BIGEMAP瓦片库格式！");
                    }
                }
            }
        } else {
            toastUtils.showToast(f.getAbsolutePath() + " 该路径已失效");
        }
    }

    /*
    切换离线模式
     */
    private void hideAll() {
        searchBox.setVisibility(View.GONE);
        trackRecord.setVisibility(View.GONE);
    }

    /*
    切换地图模式
     */
    private void showAll() {
        searchBox.setVisibility(View.VISIBLE);
        trackRecord.setVisibility(View.VISIBLE);
    }

    /**
     * 保存轨迹到本地
     *
     * @param name 轨迹名称
     */
    private void saveTrackSD(String name) {
        WriteKml writeKml = new WriteKml(this);
        try {
            if (saveType == 12) {
                writeKml.createKml(name, baseGraphs);//绘制轨迹保存
            } else if (saveType == 11) {
                Log.d(TAG, "saveTrackSD: listSize=" + locationList.size());
                writeKml.createKml(name, locationList, Constant.POLYLINE);//轨迹记录保存
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param name
     * @param type 0,保存轨迹名;1,重命名;2,删除
     */
    private void askSaveDialog(String name, int type) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_view_track_edit, null);
        final EditText nameEt = (EditText) view.findViewById(R.id.et_track_edit_name);
        nameEt.setText(name);
        builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle(R.string.dialog_track_edit);
        if (type == 0) {
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveTrackSD(nameEt.getText().toString());
                    dialog.dismiss();
                }
            });
        } else if (type == 1) {
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Node node = mData.get(selectedPosition);
                    node.setName(nameEt.getText().toString());
                    mData.set(selectedPosition, node);
                    treeRecyclerAdapter.addDataAll(mData, 1);
                    dialog.dismiss();
                }
            });
        }
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 保存绘制线到内存
     *
     * @param geoPoints
     */
    private void saveLine(List<GeoPoint> geoPoints) {
        int pid = (int) root.getId();
        if (drawType.equals(Constant.POLYLINE)) {
            if (geoPoints.size() > 1) {
                isFinished = true;
                tempMarkers.getItems().clear();
                mapView.invalidate();
                BaseGraph baseGraph = new BaseGraph();
                baseGraph.setId(polylines.getItems().size()-1);
                baseGraph.setType(drawType);
                baseGraph.setName("Line#" + polylines.getItems().size());
                baseGraph.getGeoPoints().addAll(PositionUtils.gcjToGps(geoPoints));
                baseGraphs.add(baseGraph);

//                MyPolyline polyline = new MyPolyline();
//                polyline.setWidth(5);
//                editLine = polyline;
//                polylines.add(editLine);

                Node node = new Node<>(baseGraphs.size() + 1, pid, baseGraph.getName(), baseGraph);
                node.setChecked(true);
                mData.add(node);
            } else {
                isFinished = false;
                toastUtils.showSingletonToast("当前绘制点数过少不能记录该条数据");
            }
        } else if (drawType.equals(Constant.POLYGON)) {
            if (geoPoints.size() > 2) {
                isFinished = true;
                tempMarkers.getItems().clear();
                mapView.invalidate();
                BaseGraph baseGraph = new BaseGraph();
                baseGraph.setId(polygons.getItems().size()-1);
                baseGraph.setType(drawType);
                baseGraph.setName("Polygon#" + polygons.getItems().size());
                baseGraph.getGeoPoints().addAll(PositionUtils.gcjToGps(geoPoints));
                baseGraphs.add(baseGraph);

//                Polygon polygon = new Polygon();
//                polygon.setStrokeWidth(5);
//                polygon.setFillColor(Color.GRAY);
//                editPolygon = polygon;
//                polygons.add(editPolygon);

                Node node = new Node<>(baseGraphs.size() + 1, pid, baseGraph.getName(), baseGraph);
                node.setChecked(true);
                mData.add(node);
            } else {
                isFinished = false;
                toastUtils.showSingletonToast("当前绘制点数过少不能记录该条数据");
            }
        } else if (drawType.equals(Constant.POI)) {
            if (geoPoints.size() > 0) {
                isFinished = true;
                for (GeoPoint geoPoint : geoPoints) {
                    BaseGraph baseGraph = new BaseGraph();
                    baseGraph.setId(markers.getItems().size()-1);
                    baseGraph.setType(drawType);
                    baseGraph.setName("Point#" + markers.getItems().size());
                    baseGraph.getGeoPoints().add(PositionUtils.gcj_To_Gps84(geoPoint));
                    baseGraphs.add(baseGraph);

                    Node node = new Node<>(baseGraphs.size() + 1, pid, baseGraph.getName(), baseGraph);
                    node.setChecked(true);
                    mData.add(node);
                }
            }else {
                isFinished = false;
                toastUtils.showSingletonToast("当前绘制点数过少不能记录该条数据");
            }

        }
        treeRecyclerAdapter.addDataAll(mData, 1);
    }

    /**
     * 绘制轨迹
     */
    private void drawTrack(List<GeoPoint> geoPoints) {
        switch (drawType) {
            case Constant.POLYGON:
                editPolygon.setPoints(points);
                if (geoPoints.size() > 2) {
                    double area = MapMeasureUtils.calculateArea(geoPoints);
                    String perimeter = MapMeasureUtils.calculatePerimeter(geoPoints);
                    String s;
                    if (area >= 1.0) {
                        s = "周长：" + perimeter + "米" + "\n" + "面积：" + area + "平方公里";
                    } else {
                        s = "周长：" + perimeter + "米" + "\n" + "面积：" + area * 1000000.0 + "平方米";
                    }
                    BasicInfoWindow in = new BasicInfoWindow(R.layout.bonuspack_bubble, mapView);
                    editPolygon.setInfoWindow(in);
                    editPolygon.setTitle(s);
                }
                break;
            case Constant.POLYLINE:
                editLine.setPoints(geoPoints);
                break;
            default:
                break;
        }
    }

    /**
     * 显示在线地图
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
        if (isOnline) {
            boolean normal = dataKeeper.get(Constant.PREFS_NORMAL_MAP_STATE, true);
            boolean satellite = dataKeeper.get(Constant.PREFS_SATELLITE_STATE, false);
            normalMap.setSelected(normal);
            satelliteMap.setSelected(satellite);
        } else if (isOffline) {
            boolean normal = dataKeeper.get(Constant.PREFS_OFFLINE_ELECTRIC_STATE, true);
            boolean satellite = dataKeeper.get(Constant.PREFS_OFFLINE_SATELLITE_STATE, false);
            normalMap.setSelected(normal);
            satelliteMap.setSelected(satellite);
        }
    }

    /*
    显示离线地图
     */
    private void showOfflineMap() {
        boolean mapState = dataKeeper.get(Constant.PREFS_OFFLINE_ELECTRIC_STATE, true);
        String elePath = dataKeeper.get(Constant.PREFS_OFFLINE_ELE_PATH, "");
        String satelPath = dataKeeper.get(Constant.PREFS_OFFLINE_SATEL_PATH, "");
        if (mapState) {
            if (!TextUtils.isEmpty(elePath)) {
                loadOffline(elePath);
            } else {
                toastUtils.showSingletonToast("没有配置有效离线电子地图路径");
            }
        } else {
            if (!TextUtils.isEmpty(satelPath)) {
                loadOffline(satelPath);
            } else {
                toastUtils.showSingletonToast("没有配置有效离线卫星地图路径");
            }
        }

    }

    /**
     * 显示地图源
     */
    private void showMapSource() {
        dividerView.setVisibility(View.VISIBLE);
        mapSourceList.setVisibility(View.VISIBLE);
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mapSourceList.setHasFixedSize(true);
        if (isOnline) {
            mapSourceList.setLayoutManager(new GridLayoutManager(this, 2));
            List<Map> maps = new ArrayList<>();
            maps.add(new Map(R.drawable.ic_map_google, "谷歌"));
            maps.add(new Map(R.drawable.ic_map_osm, "OSM"));
            maps.add(new Map(R.drawable.ic_map_amap, "高德"));
            maps.add(new Map(R.drawable.ic_map_baidu, "百度"));

            final MapSourceAdapter adapter = new MapSourceAdapter(this);
            adapter.setDataList(maps);
            mapSourceList.setAdapter(adapter);
            int selectedMapSource = dataKeeper.getInt(Constant.PREFS_MAP_SOURCE, 0);
            adapter.setItemSelected(selectedMapSource);
            adapter.setOnItemListener(new OnItemListener() {
                @Override
                public void onClick(View v, int pos, Object data) {
                    Map map = (Map) data;
                    mapModeBtn.setImageResource(map.getMapIcon());
                    adapter.setItemSelected(pos);
                    dataKeeper.putInt(Constant.PREFS_MAP_SOURCE, pos);
                    showMapView();
                }
            });
            showMapType();
        } else if (isOffline) {
            offlineBtn.setVisibility(View.VISIBLE);
            mapSourceList.setLayoutManager(new LinearLayoutManager(this));
            List<OfflineMap> offlineMaps = DataSupport.findAll(OfflineMap.class);
            if (offlineMaps.size() > 0) {
                final int selectedSource = dataKeeper.getInt(Constant.PREFS_OFFLINE_MAP_SOURCE, -1);
                final OfflineMapSourceAdapter adapter = new OfflineMapSourceAdapter(this);
                adapter.setData(offlineMaps);
                mapSourceList.setAdapter(adapter);
                adapter.setItemSelected(selectedSource);
                adapter.setOnItemListener(new OnItemListener() {
                    @Override
                    public void onClick(View v, int pos, Object data) {
                        OfflineMap offlineMap = (OfflineMap) data;
                        if (selectedSource==pos){
                            return;
                        }
                        adapter.setItemSelected(pos);
                        dataKeeper.putInt(Constant.PREFS_OFFLINE_MAP_SOURCE, pos);
                        dataKeeper.put(Constant.PREFS_OFFLINE_ELE_PATH, offlineMap.getElePath());
                        dataKeeper.put(Constant.PREFS_OFFLINE_SATEL_PATH, offlineMap.getSatelPath());
                        showOfflineMap();
                    }
                });
                showMapType();
            }
        }
    }

    /**
     * 隐藏键盘
     */
    private void hideKeyboard() {
        searchBox.setSelected(false);
        searchText.setCursorVisible(false);
        InputMethodManager inputMethodManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
    }

    /**
     * 隐藏地图源
     */
    private void hideMapSource() {
        dividerView.setVisibility(View.GONE);
        mapSourceList.setVisibility(View.GONE);
        offlineBtn.setVisibility(View.GONE);
        hideMapType();
    }

    /**
     * 隐藏地图类型
     */
    private void hideMapType() {
        normalMap.setVisibility(View.GONE);
        satelliteMap.setVisibility(View.GONE);
    }

    // 获取广播数据
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            GeoPoint geoPoint = intent.getParcelableExtra("location");
            locationList.add(geoPoint);
//            ArrayList<GeoPoint> compressList= new DouglasUtil(locationList, 10).compress();
            ArrayList<GeoPoint> convertedList = PositionUtils.wgsToGcj(locationList);
            if (locationList.size() > 1) {
                gpsLine.setPoints(convertedList);
            }
            mapView.getController().animateTo(PositionUtils.gps84_To_Gcj02(geoPoint));
        }

    }

    // 显示缺失权限提示
    private void showMissingPermissionDialog() {
        builder = new AlertDialog.Builder(this);
        builder.setMessage("当前应用缺少必要权限。\n\n请进入\"位置信息\"选择\"高精确度\"。\n\n最后点击两次后退按钮，即可返回。");
        // 拒绝, 退出应用
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
            }
        });
        builder.create().show();
    }

    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings");
        intent.setComponent(comp);
        intent.setAction("android.intent.action.VIEW");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 33) {
            isMyTrack = true;
            String filePath = data.getStringExtra("filePath");
            ReadKml readKml = new ReadKml();
            try {
                readKml.parseKml(filePath);
                List<BaseGraph> graphs = readKml.getBaseGraphs();
                for (BaseGraph baseGraph : graphs) {
                    baseGraphs.add(baseGraph);
                    Node node = new Node<>(baseGraphs.size() + 1, root.getId(), baseGraph.getName(), baseGraph);
                    node.setChecked(true);
                    mData.add(node);
                    switch (baseGraph.getType()) {
                        case Constant.POI:
                            addPoi(baseGraph);
                            break;
                        case Constant.POLYGON:
                            addPolygon(baseGraph);
                            break;
                        case Constant.POLYLINE:
                            addLine(baseGraph);
                            break;
                        default:
                            break;
                    }
                }
                treeRecyclerAdapter.addDataAll(mData, 1);
                List<GeoPoint> gcjPoints = PositionUtils.wgsToGcj02(readKml.getGeoPoints());
                final BoundingBox boundingBox = BoundingBox.fromGeoPoints(gcjPoints);
                mapView.zoomToBoundingBox(boundingBox, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    添加点
     */
    private void addPoi(BaseGraph baseGraph) {
        for (GeoPoint geoPoint : baseGraph.getGeoPoints()) {
            Marker marker = new Marker(mapView);
            marker.setPosition(PositionUtils.gps84_To_Gcj02(geoPoint));
            marker.setTitle(baseGraph.getName());
            marker.setSnippet("经度=" + geoPoint.getLongitude() + ",\n\n纬度=" + geoPoint.getLatitude());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            markers.add(marker);
        }
    }

    /*
    添加线
     */
    private void addLine(BaseGraph baseGraph) {
        MyPolyline polyline = new MyPolyline();
        polyline.setWidth(5);
        polyline.setColor(Color.RED);
        polyline.setPoints(PositionUtils.wgsToGcj02(baseGraph.getGeoPoints()));
        polylines.add(polyline);
//        mapView.getOverlays().add(polyline);
    }

    /*
    添加多边形
     */
    private void addPolygon(BaseGraph baseGraph) {
        Polygon polygon = new Polygon();
        polygon.setStrokeWidth(5);
        polygon.setFillColor(Color.GRAY);
        polygon.setPoints(PositionUtils.wgsToGcj02(baseGraph.getGeoPoints()));
        polygons.add(polygon);
//        mapView.getOverlays().add(polygon);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }
}
