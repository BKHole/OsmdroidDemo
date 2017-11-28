package com.bigemap.osmdroiddemo.http;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bigemap.osmdroiddemo.entity.Result;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.io.File;

import okhttp3.Call;

public class HttpClient {

    private static final String TIP_URL="http://ditu.amap.com/service/poiTipslite";

    public static void downloadFile(String url, String destFileDir, String destFileName, @Nullable final HttpCallback<File> callback) {
        OkHttpUtils.get().url(url).build()
                .execute(new FileCallBack(destFileDir, destFileName) {
                    @Override
                    public void inProgress(float progress, long total, int id) {
                    }

                    @Override
                    public void onResponse(File file, int id) {
                        if (callback != null) {
                            callback.onSuccess(file);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (callback != null) {
                            callback.onFail(e);
                        }
                    }

                    @Override
                    public void onAfter(int id) {
                        if (callback != null) {
                            callback.onFinish();
                        }
                    }
                });
    }



    public static void getBitmap(String url, @NonNull final HttpCallback<Bitmap> callback) {
        OkHttpUtils.get().url(url).build()
                .execute(new BitmapCallback() {
                    @Override
                    public void onResponse(Bitmap bitmap, int id) {
                        callback.onSuccess(bitmap);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void postImei(String url, String type, String imei, StringCallback callback){
        OkHttpUtils.post().url(url)
                .addParams("type", type)
                .addParams("imei", imei).build().execute(callback);
    }

    public static void getTip(BoundingBox boundingBox,String keyword, @NonNull final HttpCallback<Result> callback){
        String geoobj=boundingBox.getLonWest()+"|"+boundingBox.getLatNorth()+"|"+boundingBox.getLonEast()+"|"+boundingBox.getLatSouth();
        OkHttpUtils.get().url(TIP_URL)
                .addParams("geoobj", geoobj)
                .addParams("words", keyword)
                .build().execute(new JsonCallback<Result>(Result.class) {
            @Override
            public void onError(Call call, Exception e, int id) {
                callback.onFail(e);
            }

            @Override
            public void onResponse(Result response, int id) {
                callback.onSuccess(response);
            }

            @Override
            public void onAfter(int id) {
                callback.onFinish();
            }
        });
    }
}
