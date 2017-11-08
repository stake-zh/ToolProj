package com.stake.toolproj.baseutils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SimpleFileDownloader {
    public static boolean downloadFile(String url, String path) {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        OutputStream output = null;
        BufferedInputStream input = null;
        InputStream inputStream = null;
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                return false;
            }
            inputStream = response.body().byteStream();
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            output = new FileOutputStream(file);
            input = new BufferedInputStream(inputStream);
            byte[] data = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
            response.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
