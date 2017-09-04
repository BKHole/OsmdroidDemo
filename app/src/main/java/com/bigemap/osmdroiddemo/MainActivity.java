package com.bigemap.osmdroiddemo;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
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
import android.widget.ImageButton;
import android.widget.Toast;

import com.bigemap.osmdroiddemo.TileSource.GoogleMapsTileSource;
import com.bigemap.osmdroiddemo.TileSource.GoogleSatelliteTileSource;
import com.bigemap.osmdroiddemo.constants.Constant;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {

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
    private MyLocationNewOverlay mLocationOverlay;
    private LocationManager lm;
    private Location currentLocation = null;
    //设置自定义定位，缩小，放大
    private ImageButton location, zoomIn, zoomOut, mapMode;
    private int selectedValue = 0;//默认选中地图值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }
//        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        mapView = $(R.id.mapView);
        initView();
        initTileSource();

        Log.d("MyTag", mapView.getTileProvider().getTileSource().name());
        mapView.setDrawingCacheEnabled(true);
//        mapView.setBuiltInZoomControls(true);//显示默认缩放控制按钮
        mapView.setTilesScaledToDpi(true);//图源比例转换屏幕像素
        mapView.setUseDataConnection(true);//使用网络数据
        mapView.getController().setZoom(15);
        mapView.setMultiTouchControls(true);//触摸放大、缩小操作

        //地图自由旋转
        mRotationGestureOverlay = new RotationGestureOverlay(mapView);
        mRotationGestureOverlay.setEnabled(true);
        mapView.getOverlays().add(this.mRotationGestureOverlay);
        //比例尺
        final DisplayMetrics dm = getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(mapView);
//        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setAlignRight(true);
        mScaleBarOverlay.setAlignBottom(true);
//        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 5, 35);
        mapView.getOverlays().add(mScaleBarOverlay);
        //定位当前位置
        Bitmap bMap = BitmapFactory.decodeResource(mapView.getContext().getResources(), R.drawable.marker_default);
        GpsMyLocationProvider provider = new GpsMyLocationProvider(this);
        provider.addLocationSource(LocationManager.NETWORK_PROVIDER);
        mLocationOverlay = new MyLocationNewOverlay(provider, mapView);
        mLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                Log.d("MyTag", String.format("First location fix: %s", mLocationOverlay.getLastFix()));
            }
        });
        mLocationOverlay.setPersonIcon(bMap);
        mapView.getOverlays().add(mLocationOverlay);
        mLocationOverlay.enableMyLocation();//设置可视

        //显示指南针
        mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapView);
        mCompassOverlay.enableCompass();
        mapView.getOverlays().add(this.mCompassOverlay);

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
        location.setOnClickListener(this);
        zoomIn.setOnClickListener(this);
        zoomOut.setOnClickListener(this);
        mapMode.setOnClickListener(this);
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
        try {
            lm.removeUpdates(this);
        } catch (Exception ex) {
        }

        mCompassOverlay.disableCompass();
        mLocationOverlay.disableFollowLocation();
        mLocationOverlay.disableMyLocation();
        mScaleBarOverlay.disableScaleBar();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        final String tileSourceName = mPrefs.getString(Constant.PREFS_TILE_SOURCE,
//                TileSourceFactory.DEFAULT_TILE_SOURCE.name());
//        try {
//            final ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
//            mapView.setTileSource(tileSource);
//        } catch (final IllegalArgumentException e) {
//            mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
//        }
//        if (mPrefs.getBoolean(Constant.PREFS_SHOW_LOCATION, false)) {
//            this.mLocationOverlay.enableMyLocation();
//        }
//        if (mPrefs.getBoolean(Constant.PREFS_SHOW_COMPASS, false)) {
//            if (mCompassOverlay!=null)
//                //this call is needed because onPause, the orientation provider is destroyed to prevent context leaks
//                this.mCompassOverlay.setOrientationProvider(new InternalCompassOrientationProvider(mapView.getContext()));
//            this.mCompassOverlay.enableCompass();
//        }
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            //this fails on AVD 19s, even with the appcompat check, says no provided named gps is available
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0l, 0f, this);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0l, 0f, this);
            }
        } catch (Exception ex) {
        }
        mLocationOverlay.enableFollowLocation();
        mLocationOverlay.enableMyLocation();
        mCompassOverlay.enableCompass();
        mScaleBarOverlay.enableScaleBar();
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

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_location:
                if (currentLocation != null) {
                    GeoPoint myPosition = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                    mapView.getController().animateTo(myPosition);
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
                                    mapView.invalidate();
                                }
                                break;
                            case 1://GOOGLE_SATELLITE
                                if (TileSourceFactory.containsTileSource(GOOGLE_SATELLITE)) {
                                    mapView.setTileSource(TileSourceFactory.getTileSource(GOOGLE_SATELLITE));
                                    mapView.invalidate();
                                }
                                break;
                            case 2://OSM
                                mapView.setTileSource(TileSourceFactory.MAPNIK);
                                mapView.invalidate();

                        }
                        selectedValue = which;
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
}
