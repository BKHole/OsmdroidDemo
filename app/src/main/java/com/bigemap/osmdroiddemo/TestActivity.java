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
import android.widget.ImageView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bigemap.osmdroiddemo.TileSource.GoogleMapsTileSource;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.utils.DateUtils;
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
    private Location myLocation = null;
    private MapView mapView;
    private ScaleBarOverlay scaleBarOverlay;
    private CompassOverlay compassOverlay;
    private MyLocationNewOverlay myLocationOverlay;
    private ImageView btnLocation, mapMode, zoomIn;
    private List<GeoPoint> points;
    private IMapController mController;
    private GeoPoint convertedPoint;

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        btnLocation = (ImageView) findViewById(R.id.btn_location);
        mapMode = (ImageView) findViewById(R.id.btn_map_mode);
        zoomIn = (ImageView) findViewById(R.id.btn_zoom_in);
        initTileSource();
        initListener();
        initLocation();
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
        points = new ArrayList<>();

        mapView.getOverlays().add(scaleBarOverlay);
        mapView.getOverlays().add(compassOverlay);

//        mapView.getOverlays().add(myLocationOverlay);
//        myLocationOverlay.enableMyLocation();
    }

    private void initListener() {
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (convertedPoint!=null){
//                    mapView.getController().animateTo(convertedPoint);
//                }
                GeoPoint point=new GeoPoint(myLocation);
                mController.animateTo(point);
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
     * 初始化定位
     *
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private void initLocation(){
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
        locationClient.startLocation();
    }

    /**
     * 默认的定位参数
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private AMapLocationClientOption getDefaultOption(){
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(false);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(false); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
                StringBuffer sb = new StringBuffer();
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if(location.getErrorCode() == 0){
                    myLocation=location;
                    sb.append("定位成功" + "\n");
                    sb.append("定位类型: " + location.getLocationType() + "\n");
                    sb.append("经    度    : " + location.getLongitude() + "\n");
                    sb.append("纬    度    : " + location.getLatitude() + "\n");
                    sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
                    sb.append("提供者    : " + location.getProvider() + "\n");

                    sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                    sb.append("角    度    : " + location.getBearing() + "\n");
                    // 获取当前提供定位服务的卫星个数
                    sb.append("星    数    : " + location.getSatellites() + "\n");
                    sb.append("国    家    : " + location.getCountry() + "\n");
                    sb.append("省            : " + location.getProvince() + "\n");
                    sb.append("市            : " + location.getCity() + "\n");
                    sb.append("城市编码 : " + location.getCityCode() + "\n");
                    sb.append("区            : " + location.getDistrict() + "\n");
                    sb.append("区域 码   : " + location.getAdCode() + "\n");
                    sb.append("地    址    : " + location.getAddress() + "\n");
                    sb.append("兴趣点    : " + location.getPoiName() + "\n");
                    //定位完成的时间
                    sb.append("定位时间: " + DateUtils.formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
                } else {
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + location.getErrorCode() + "\n");
                    sb.append("错误信息:" + location.getErrorInfo() + "\n");
                    sb.append("错误描述:" + location.getLocationDetail() + "\n");
                }
                //定位之后的回调时间
                sb.append("回调时间: " + DateUtils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");

                //解析定位结果，
                String result = sb.toString();
//                tvResult.setText(result);
                Log.d(TAG, "onLocationChanged: "+sb.toString());
            } else {
                Log.d(TAG, "定位失败，loc is null");
            }
        }
    };
    /**
     * 销毁定位
     *
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private void destroyLocation(){
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
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

    private void initTileSource() {
        GoogleMapsTileSource tileSource = new GoogleMapsTileSource(MainActivity.GOOGLE_MAP, 1, 20, 256, ".png",
                new String[]{Constant.URL_MAP_GOOGLE});
        mapView.setTileSource(tileSource);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AMapLocation location=locationClient.getLastKnownLocation();
            GeoPoint point=new GeoPoint(location);
            setPoint(point);
            mController.setCenter(point);
//        }else{
//            GeoPoint endPoint = new GeoPoint(30.334141, 104.31532);
//            mController.setCenter(endPoint);
//        }

//        GeoPoint origin = getNetworkLocation();
//        if (origin!=null){
//            convertedPoint = PositionUtils.gps84_To_Gcj02(origin.getLatitude(), origin.getLongitude());
//            setPoint(convertedPoint);
//            mController.setCenter(convertedPoint);
//        }else{
//            GeoPoint endPoint = new GeoPoint(30.334141, 104.31532);
//            mController.setCenter(endPoint);
//        }


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
        locationClient.stopLocation();
//        myLocationOverlay.disableMyLocation();
//        LocationUtils.unRegisterListener(this);//取消监听
//        locationManager.removeUpdates(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        destroyLocation();
    }
}
