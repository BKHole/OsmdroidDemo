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

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.bigemap.osmdroiddemo.utils.DateUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;


public class CrashHandler implements Thread.UncaughtExceptionHandler {

    public static CrashHandler getInstance() {
        return new CrashHandler();
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
        if (writeBoom(thread, ex)) {
            File crash = new File(MainApplication.getCrashReportsPath() + "/crashed");
            try {
                new FileOutputStream(crash).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, ex);
        }
    }

    private boolean writeBoom(Thread thread, Throwable ex) {
        if (ex == null) {
            return false;
        }
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String logFileName = DateUtils.formatUTC(System.currentTimeMillis(), null) + ".log";

            File crashReport = new File(MainApplication.getCrashReportsPath() + "/" + logFileName);
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(crashReport);

                writer.append("Android version : " + Build.VERSION.RELEASE + "\n")
                        .append("Sdk version : " + Build.VERSION.SDK_INT + "\n")
                        .append("App version name : " + BuildConfig.VERSION_NAME + "\n")
                        .append("App version code : " + BuildConfig.VERSION_CODE + "\n")
//                        .append("CPU ABI : " + Arrays.toString(Build.SUPPORTED_ABIS) + "\n")
                        .append("Manufacturer : " + Build.MANUFACTURER + "\n")
                        .append("Model : " + Build.MODEL + "\n")
                        .flush();
                writer.append(Build.FINGERPRINT)
                        .flush();

                writer.append("\n************\n");
                ex.printStackTrace(writer);
                writer.flush();

                writeToFile(logFileName, getLastLogPath(), false);
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

    private static void writeToFile(String s, String filePath, boolean append) {
        File f = new File(filePath);
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(f, append));
            writer.write(s);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
