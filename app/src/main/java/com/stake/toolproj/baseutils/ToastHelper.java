package com.stake.toolproj.baseutils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;


@SuppressWarnings("unused")
public class ToastHelper {
	private Toast toast = null;
	private Handler handler;


	private static class ToastHelperHandler {
		public static final ToastHelper instance = new ToastHelper();
	}

	protected ToastHelper() {
        handler = new Handler(Looper.getMainLooper());
    }

	public static void toast(int resourceId) {
        toast(resourceId, Toast.LENGTH_SHORT);
    }

    public static void toast(int resourceId, int duration) {
		Context context = ApplicationStatus.getApplicationContext();
		if (context == null) {
            return;
        }

        String message = context.getString(resourceId);
        toast(message, duration);
    }

	public static void toast(String message) {
		toast(message, Toast.LENGTH_SHORT);
	}

	public static void toast(String message, int duration) {
        ToastHelperHandler.instance.showToast(message, duration);
	}

	private void showToast(String message, int duration) {
		if (message == null) {
			return;
		}

        handler.post(new Runnable() {
            @Override
            public void run() {
                Context context = ApplicationStatus.getApplicationContext();
                if (context == null) {
                    return;
                }
                if (toast == null) {
                    toast = Toast.makeText(context, message, duration);
                    toast.show();
                } else {
                    toast.setText(message);
                    toast.setDuration(duration);
                    toast.show();
                }
            }
        });

	}
}