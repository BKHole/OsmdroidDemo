package com.bigemap.osmdroiddemo.application;

import android.content.Context;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.zhy.http.okhttp.OkHttpUtils;

import org.litepal.LitePalApplication;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class MainApplication extends LitePalApplication {
    private static final String TAG = "MainApplication";
    private RefWatcher mRefWatcher;
    private static String crashReportsPath;

    //字体图标
    @Override
    public void onCreate() {
        super.onCreate();
        mRefWatcher=LeakCanary.install(this);
        initCrashReportsPath();
        CrashHandler.getInstance().init(this);
        ForegroundObserver.init(this);
        initOkHttpUtils();
        ArchiveFileFactory.registerArchiveFileProvider(MBTilesFileArchive.class, "bmdb");
    }

    public static RefWatcher getRefWatcher(Context context) {
        MainApplication application = (MainApplication) context.getApplicationContext();
        return application.mRefWatcher;
    }

    private void initOkHttpUtils() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }

    public static String getCrashReportsPath() {
        return crashReportsPath;
    }

    private void initCrashReportsPath() {
        File filesDir = getExternalFilesDir(null);
        if (filesDir != null) {
            File crashReports = new File(filesDir.getAbsolutePath() + "/../crashReports");
            crashReports.mkdir();
            try {
                crashReportsPath = crashReports.getCanonicalPath();
                Log.d(TAG, "initCrashReportsPath: " +crashReportsPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
