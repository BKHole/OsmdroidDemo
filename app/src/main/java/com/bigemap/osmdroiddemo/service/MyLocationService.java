package com.bigemap.osmdroiddemo.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.activity.MainActivity;
import com.bigemap.osmdroiddemo.entity.Track;
import com.bigemap.osmdroiddemo.utils.PermissionUtils;
import com.bigemap.osmdroiddemo.utils.PositionUtils;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class MyLocationService extends Service implements LocationListener {
    private static final String TAG = "MyLocationService";
    private static final int UPDATE_TIME = 2000;
    private static final int UPDATE_DISTANCE = 5;

    private NotificationManager mNotificationManager;
    public ArrayList<Location> locationArrayList;
    private LocationManager locationManager;
    private PowerManager.WakeLock wakeLock;
    private String networkProvider = LocationManager.NETWORK_PROVIDER;
    private String gpsProvider = LocationManager.GPS_PROVIDER;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        locationArrayList = new ArrayList<>();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        showNotification();
        acquireWakeLock();
        if (PermissionUtils.checkLocationPermission(this)) {
            locationManager.requestLocationUpdates(gpsProvider, UPDATE_TIME, UPDATE_DISTANCE, this);
//            locationManager.requestLocationUpdates(networkProvider, 2000, 0, this);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        releaseWakeLock();
        clearNotification();
        locationArrayList.clear();
        locationArrayList = null;
        if (locationManager != null) {
            locationManager.removeUpdates(this);
            locationManager = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: ");
        addCoordinates(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged: provider=" + provider);
        switch (status) {
            // GPS状态为可见时2
            case LocationProvider.AVAILABLE:
                Log.i(TAG, "当前GPS状态为可见状态");
                if (provider.equals("gps")) {
                    if (PermissionUtils.checkLocationPermission(this)) {
                        locationManager.requestLocationUpdates(gpsProvider, UPDATE_TIME, UPDATE_DISTANCE, this);
                    }
                }
                break;
            // GPS状态为服务区外时0
            case LocationProvider.OUT_OF_SERVICE:
                Log.i(TAG, "当前GPS状态为服务区外状态");
                break;
            // GPS状态为暂停服务时1
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.i(TAG, "当前GPS状态为暂停服务状态");
                if (provider.equals("gps")) {
                    if (PermissionUtils.checkLocationPermission(this)) {
                        locationManager.requestLocationUpdates(networkProvider, UPDATE_TIME, UPDATE_DISTANCE, this);
                    }
                }
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: provider=" + provider);
        if (PermissionUtils.checkLocationPermission(this)) {
            if ("gps".equals(provider)) {
                locationManager.requestLocationUpdates(gpsProvider, UPDATE_TIME, UPDATE_DISTANCE, this);
            } else {
                locationManager.requestLocationUpdates(networkProvider, UPDATE_TIME, UPDATE_DISTANCE, this);
            }
            Location location = locationManager.getLastKnownLocation(provider);
            addCoordinates(location);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: ");
        if ("gps".equals(provider)) {
            if (PermissionUtils.checkLocationPermission(this)) {
                locationManager.requestLocationUpdates(networkProvider, UPDATE_TIME, UPDATE_DISTANCE, this);
            }
        }
    }

    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Bigemap");
        builder.setContentText("正在记录轨迹...");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setTicker("新消息");
        if (Build.VERSION.SDK_INT >= 17) {
            builder.setShowWhen(true);
        }
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT < 16) {
            mNotificationManager.notify(1, builder.getNotification());
        } else {
            mNotificationManager.notify(1, builder.build());
        }
    }

    private void clearNotification() {
        mNotificationManager.cancel(1);
    }

    // 将GPS数据放入集合中
    public void addCoordinates(Location mLocation) {
        if (null != mLocation) {
            locationArrayList.add(mLocation);
            if (!MainActivity.isBackground){
                sendLocation(locationArrayList);
            }
        }
    }

    /**
     * 将数据传递到activity中
     *
     * @param list 定位数据列表
     */
    private void sendLocation(ArrayList<Location> list) {
        ArrayList<GeoPoint> convertedList = PositionUtils.wgsToGcj(list);
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("saveGps", convertedList);
        intent.setAction("com.bigemap.osmdroiddemo.service.intent.locationList");
        sendBroadcast(intent);
    }

    /**
     * 睡眠状态唤醒定位
     */
    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (wakeLock == null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakeLock");
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
