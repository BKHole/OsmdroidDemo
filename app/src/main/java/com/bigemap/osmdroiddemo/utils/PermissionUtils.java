package com.bigemap.osmdroiddemo.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionUtils {
    private final static Map<Integer, OnRequestPermissionsResult> mRequestCache = new HashMap<>();

    public final static String[] LOCATION_PERMISSION = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static boolean checkLocationPermission(Context context) {
        return checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                && checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static boolean checkPhoneStatePermission(Context context) {
        return checkPermission(context, Manifest.permission.READ_PHONE_STATE);
    }

    private static boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkPermissions(Context context, String[] perms) {
        for (String perm : perms) {
            if (!checkPermission(context, perm)) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermissionsAndThen(Fragment fragment, int requestCode,
                                                 String[] perms,
                                                 PermsCallback permsCallback) {
        Activity activity = fragment.getActivity();
        if (Build.VERSION.SDK_INT < 23 || checkPermissions(activity, perms)) {
            permsCallback.onAllGranted();
            return;
        }

        OnRequestPermissionsResult r = prepareRequest(activity, requestCode, perms, permsCallback);
        fragment.requestPermissions(r.getRequestPerms(), requestCode);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void requestPermissionsAndThen(Activity activity, int requestCode,
                                                 String[] perms,
                                                 PermsCallback permsCallback) {
        if (Build.VERSION.SDK_INT < 23 || checkPermissions(activity, perms)) {
            permsCallback.onAllGranted();
            return;
        }

        OnRequestPermissionsResult r = prepareRequest(activity, requestCode, perms, permsCallback);
        activity.requestPermissions(r.getRequestPerms(), requestCode);
    }

    private static OnRequestPermissionsResult prepareRequest(Context context,
                                                             int requestCode,
                                                             String[] perms,
                                                             PermsCallback permsCallback) {
        // only request denied permissions
        List<String> permsDenied = new ArrayList<>();
        for (String perm : perms) {
            if (!checkPermission(context, perm)) {
                permsDenied.add(perm);
            }
        }
        String[] permsToReq = permsDenied.toArray(new String[permsDenied.size()]);
        OnRequestPermissionsResult r = new OnRequestPermissionsResult(requestCode, permsToReq, permsCallback);
        synchronized (mRequestCache) {
            mRequestCache.put(requestCode, r);
        }
        return r;
    }

    public static void dispatchPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        OnRequestPermissionsResult r = mRequestCache.get(requestCode);
        if (r != null) {
            mRequestCache.remove(requestCode);
            r.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static abstract class PermsCallback {
        public abstract void onAllGranted();

        public abstract void onAllDenied();
    }

    public static class OnRequestPermissionsResult {
        private int mRequestCode;
        private String[] mRequestPerms;
        private PermsCallback mPermsCallback;

        private OnRequestPermissionsResult(int requestCode, String[] requestPerms,
                                           PermsCallback callback) {
            mRequestCode = requestCode;
            mRequestPerms = requestPerms;
            mPermsCallback = callback;
        }

        public String[] getRequestPerms() {
            return mRequestPerms;
        }

        public int getRequestCode() {
            return mRequestCode;
        }

        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            if (requestCode != mRequestCode) {
                return;
            }

            boolean granted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (granted) {
                mPermsCallback.onAllGranted();
            } else {
                mPermsCallback.onAllDenied();
            }
        }
    }
}
