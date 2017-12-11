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
import android.os.Parcelable;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.activity.MainActivity;
import com.bigemap.osmdroiddemo.utils.PermissionUtils;

import org.osmdroid.util.GeoPoint;

public class MyLocationService extends Service implements LocationListener {
    private static final String TAG = "MyLocationService";
    private static final int UPDATE_TIME = 1000;
    private static final int UPDATE_DISTANCE = 10;

    private NotificationManager mNotificationManager;
    private LocationManager locationManager;
    private PowerManager.WakeLock wakeLock;
    private String networkProvider = LocationManager.NETWORK_PROVIDER;
    private String gpsProvider = LocationManager.GPS_PROVIDER;
    private Intent intent;
    public Location previousBestLocation = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        intent = new Intent();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
//        showNotification();
        acquireWakeLock();
        if (PermissionUtils.checkLocationPermission(this)) {
            locationManager.requestLocationUpdates(gpsProvider, UPDATE_TIME, UPDATE_DISTANCE, this);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        releaseWakeLock();
//        clearNotification();
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
//            Location location = locationManager.getLastKnownLocation(provider);
//            addCoordinates(location);
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

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
    /**
     * 判断新定位是否满足要求
     * @param location previousBestLocation
     * @param currentBestLocation locationChanged
     * @return boolean
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }
        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > UPDATE_TIME;
        boolean isSignificantlyOlder = timeDelta < -UPDATE_TIME;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }
    // 将GPS数据放入集合中
    public void addCoordinates(Location mLocation) {
        if (null != mLocation) {
            GeoPoint newPoint=new GeoPoint(mLocation);
            if (isBetterLocation(mLocation, previousBestLocation)){
                previousBestLocation=mLocation;
                sendLocation(newPoint);
            }
        }
    }

    /**
     * 将数据传递到activity中
     * @param geoPoint
     */
    private void sendLocation(GeoPoint geoPoint) {
        intent.putExtra("location", (Parcelable) geoPoint);
        intent.setAction("com.bigemap.osmdroiddemo.service.intent.locationList");
        sendBroadcast(intent);
    }

    /**
     * 睡眠状态唤醒定位
     * PARTIAL_WAKE_LOCK :保持CPU 运转，屏幕和键盘灯有可能是关闭的。
     * SCREEN_DIM_WAKE_LOCK ：保持CPU 运转，允许保持屏幕显示但有可能是灰的，允许关闭键盘灯
     * SCREEN_BRIGHT_WAKE_LOCK ：保持CPU 运转，允许保持屏幕高亮显示，允许关闭键盘灯
     * FULL_WAKE_LOCK ：保持CPU 运转，保持屏幕高亮显示，键盘灯也保持亮度
     */
    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (wakeLock == null) {
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK , "wakeLock");
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
