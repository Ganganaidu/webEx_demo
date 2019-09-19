package com.android.webex.spinsai.service;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ApiService {

    private static final String URL = "https://telehealth-1.prod.agentacloud.com/beacon/";
    private static final MediaType JSON = MediaType.get("plain/text; charset=utf-8");

    private static ApiService instance;

    private ApiService() {
    }

    public static synchronized ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    private OkHttpClient client = new OkHttpClient();

    public void post(String postLogs, Callback callback) {
        RequestBody body = RequestBody.create(JSON, postLogs);

        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
//        try (Response response = client.newCall(request).execute()) {
//            return response.body() != null ? response.body().string() : null;
//
//        }
    }
}
