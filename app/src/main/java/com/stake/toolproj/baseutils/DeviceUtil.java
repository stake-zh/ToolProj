package com.stake.toolproj.baseutils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import com.stake.toolproj.baseutils.encrypt.CryptoUtil;

import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class DeviceUtil {
    public static String getDeviceSerial() {
        String serial = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception ignored) {
        }
        return serial;
    }

    public static String getDeviceToken() {
        try {
            Context context = ApplicationStatus.getApplicationContext();

            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = "";
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                imei = tm.getDeviceId();
            }
            String androidId = android.provider.Settings.System.getString(context.getContentResolver(), "android_id");
            String serialNo = getDeviceSerial();

            return CryptoUtil.MD5Hash(imei + androidId + serialNo);
        } catch (Throwable e) {
            e.printStackTrace();
            return CryptoUtil.MD5Hash("Unknown Device");
        }
    }

}
