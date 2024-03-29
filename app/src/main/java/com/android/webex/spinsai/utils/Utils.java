package com.android.webex.spinsai.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

public class Utils {

    public static boolean isStorageReady() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static boolean isPermissionGranted(Context context) {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
