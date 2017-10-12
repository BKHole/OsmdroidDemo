/*
 * Copyright 2016 Shen Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bigemap.osmdroiddemo;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;

public class MainApplication extends Application {
    private static Context mAppContext;
    private static String crashReportsPath= Environment.getExternalStorageDirectory()+ "/AOsmDemo/crashReports";
    private RefWatcher mRefWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = this;
        CrashHandler.getInstance().init(this);
        mRefWatcher=LeakCanary.install(this);
        File crash = new File(MainApplication.getCrashReportsPath() + "/crashed");
        if (crash.exists()) {
            crash.delete();
            Toast.makeText(this, "application had crashed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static RefWatcher getRefWatcher(Context context) {
        MainApplication application = (MainApplication) context.getApplicationContext();
        return application.mRefWatcher;
    }
    public static Context getAppContext() {
        if (mAppContext == null) throw new RuntimeException();

        return mAppContext;
    }

    public static String getCrashReportsPath() {
        return crashReportsPath;
    }

}
