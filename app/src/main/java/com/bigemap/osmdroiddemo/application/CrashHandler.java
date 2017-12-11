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

package com.bigemap.osmdroiddemo.application;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.bigemap.osmdroiddemo.utils.CommonUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static CrashHandler ourInstance = new CrashHandler();

    public static CrashHandler getInstance() {
        return ourInstance;
    }

    private Context mContext;
    private Thread.UncaughtExceptionHandler defaultHandler;

    private CrashHandler() {
    }

    void init(Context context) {
        mContext = context;
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(CrashHandler.class.getSimpleName(), "boom!boom!boom!");
        writeBoom(thread, ex);
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, ex);
        }
    }

    private boolean writeBoom(Thread thread, Throwable ex) {
        if (ex == null) {
            return false;
        }
        Calendar c = Calendar.getInstance(Locale.US);
        Date date = c.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS", Locale.US);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String logFileName = dateFormat.format(date) + ".log";

            File crashReport = new File(MainApplication.getCrashReportsPath() + "/" + logFileName);
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(crashReport);

                writer.append("Android version : " + Build.VERSION.RELEASE + "\n")
                        .append("Sdk version : " + Build.VERSION.SDK_INT + "\n")
//                        .append("App version name : " + BuildConfig.VERSION_NAME + "\n")
//                        .append("App version code : " + BuildConfig.VERSION_CODE + "\n")
//                        .append("CPU ABI : " + Arrays.toString(Build.SUPPORTED_ABIS) + "\n")
                        .append("Manufacturer : " + Build.MANUFACTURER + "\n")
                        .append("Model : " + Build.MODEL + "\n")
                        .flush();
                writer.append(Build.FINGERPRINT)
                        .flush();

                writer.append("\n************\n");
                ex.printStackTrace(writer);
                writer.flush();

                CommonUtils.writeToFile(logFileName, getLastLogPath(), false);
            } catch (IOException e) {
                Log.e(CrashHandler.class.getSimpleName(),
                        "write to " + crashReport.getAbsolutePath() + " exception!");
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
            return true;
        }
        return false;
    }

    public static String getLastLogPath() {
        return MainApplication.getCrashReportsPath() + "/last_log";
    }
}
