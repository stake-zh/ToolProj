package com.stake.toolproj.baseutils;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings({"SimplifiableIfStatement", "unused", "SpellCheckingInspection"})
public class NetworkUtil {
    public interface NetworkInfoChangedListener {
        void onNetworkInfoChanged(NetworkInfo networkInfo);
    }

    public interface NetworkType {
        int Network_WIFI = 0;
        int Network_2G = 1;
        int Network_3G = 2;
        int Network_4G = 3;
        int Network_Mobile = 4;
        int Network_None = 5;
    }

    static {
        staticInitialize();
    }

    private static TelephonyManager telephonyManager;
    private static WifiManager wifiManager;
    private static NetworkInfo networkInfo;
    private static CopyOnWriteArrayList<NetworkInfoChangedListener> networkInfoListenerList = new CopyOnWriteArrayList<>();

    private final static String PREFERENCE_SIM_MOBILE = "simluteMobileNet";


    private static void staticInitialize() {
        Context applicationContext = ApplicationStatus.getApplicationContext();
        if (applicationContext == null) {
            return;
        }

        IntentFilter connectivityChangeFilter = new IntentFilter();
        connectivityChangeFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        applicationContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (context == null) {
                    notifyListener(networkInfo);
                    return;
                }

                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    networkInfo = connectivityManager.getActiveNetworkInfo();
                }
                notifyListener(networkInfo);
            }
        }, connectivityChangeFilter);

        ConnectivityManager connectivityManager = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
    }

    private static void notifyListener(NetworkInfo networkInfo) {
        for (NetworkInfoChangedListener listener : networkInfoListenerList) {
            listener.onNetworkInfoChanged(networkInfo);
        }
    }

    private static TelephonyManager getTelephonyManager() {
        if (telephonyManager == null) {
            telephonyManager = (TelephonyManager) ApplicationStatus.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        }

        return telephonyManager;
    }

    private static WifiManager getWifiManager() {
        if (wifiManager == null) {
            wifiManager = (WifiManager) ApplicationStatus.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }

        return wifiManager;
    }

    public static void registerNetworkChangeListener(NetworkInfoChangedListener listener) {
        if (networkInfoListenerList.contains(listener)) {
            return;
        }

        networkInfoListenerList.add(listener);
    }

    public static void unregisterNetworkChangeListener(NetworkInfoChangedListener listener) {
        if (!networkInfoListenerList.contains(listener)) {
            return;
        }

        networkInfoListenerList.remove(listener);
    }

    public static boolean isNetAvailable() {
        if (networkInfo == null) {
            return false;
        }

        return networkInfo.isAvailable();
    }

    public static boolean isNetTypeMobile() {
        if (networkInfo == null) {
            return false;
        }
        return ConnectivityManager.TYPE_MOBILE == networkInfo.getType();
    }

    public static boolean isNetTypeWifi() {
        if (networkInfo == null) {
            return false;
        }
        return ConnectivityManager.TYPE_WIFI == networkInfo.getType();
    }

    public static int getNetworkType() {
        if (networkInfo == null || !networkInfo.isConnected()) {
            return NetworkType.Network_None;
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI ||
                networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET ||
                networkInfo.getType() == ConnectivityManager.TYPE_WIMAX) {
            return NetworkType.Network_WIFI;
        }

        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            int subType = networkInfo.getSubtype();
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return NetworkType.Network_2G;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return NetworkType.Network_3G;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return NetworkType.Network_4G;
            }

            String subTypeName = networkInfo.getSubtypeName();
            if (subTypeName.equalsIgnoreCase("TD-SCDMA") || subTypeName.equalsIgnoreCase("WCDMA") || subTypeName.equalsIgnoreCase("CDMA2000")) {
                return NetworkType.Network_3G;
            }

            return NetworkType.Network_Mobile;
        }

        return NetworkType.Network_None;
    }

    public static String getNetworkTypeName() {
        switch (getNetworkType()) {
            case NetworkType.Network_WIFI:
                return "WIFI";
            case NetworkType.Network_2G:
                return "2G";
            case NetworkType.Network_3G:
                return "3G";
            case NetworkType.Network_4G:
                return "4G";
            case NetworkType.Network_Mobile:
                return "MOBILE";
            case NetworkType.Network_None:
            default:
                return "NO_NETWORK";
        }
    }

    public static boolean isMobileAvailable() {
        try {
            ConnectivityManager manager = (ConnectivityManager) ApplicationStatus.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileInfo != null && mMobileInfo.isAvailable()) {
                return mMobileInfo.isConnected();
            }
            return false;
        } catch (Throwable e) {
            return false;
        }
    }

    public static NetworkInfo getNetworkInfo() {
        return networkInfo;
    }

    public static int getCellid(Context context) {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return 0;
            }
            return ((GsmCellLocation) getTelephonyManager().getCellLocation()).getCid();
        } catch (Throwable e) {
            return 0;
        }
    }

    public static String getSubscriberId(Context context) {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return "";
            }
            return getTelephonyManager().getSubscriberId();
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getCarrierName() {
        try {
            return getTelephonyManager().getNetworkOperatorName();
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getProvidersName(Context context) {
        String imsi = getSubscriberId(context);
        if (TextUtils.isEmpty(imsi)) {
            return "";
        }

        if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
            return "中国移动";
        } else if (imsi.startsWith("46001")) {
            return "中国联通";
        } else if (imsi.startsWith("46003")) {
            return "中国电信";
        }

        return "";
    }

    public static String getWifiSSID() {
        try {
            String ssid = getWifiManager().getConnectionInfo().getSSID();

            if (Build.VERSION.SDK_INT >= 17) {
                if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.substring(1, ssid.length() - 1);
                }
            }

            return ssid;
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getWifiIpAddress() {
        try {
            int ipAddress = getWifiManager().getConnectionInfo().getIpAddress();

            return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getCarrierIpAddress() {
        try {
            for (Enumeration<NetworkInterface> interfaceEnumeration = NetworkInterface.getNetworkInterfaces(); interfaceEnumeration.hasMoreElements(); ) {
                NetworkInterface networkInterface = interfaceEnumeration.nextElement();
                for (Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses(); addressEnumeration.hasMoreElements(); ) {
                    InetAddress inetAddress = addressEnumeration.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
