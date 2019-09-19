package com.android.webex.spinsai.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.android.webex.spinsai.service.ApiService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class BaseActivity extends AppCompatActivity {

    protected static final String LAYOUT = "layout";
    protected int layout;
    private ProgressDialog dialog;

    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onBackPressed() {

    }

    /* for activity */
    public void replace(Fragment fragment, int resourceId) {
        FragmentTransaction transaction;
        transaction = getFragmentManager().beginTransaction();
        transaction.replace(resourceId, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // TODO: extract progressDialog out
    public void showBusyIndicator(String title, String message) {
        dialog = ProgressDialog.show(this, title, message);
    }

    public void updateDialogMessage(String message) {
        if (dialog != null) {
            dialog.setMessage(message);
        }
    }

    public void dismissBusyIndicator() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void postLogData(String logData) {
        ApiService.getInstance().post(logData, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) {
                call.cancel();
            }
        });
    }

    /* Only use for debug */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(Object event) {
    }
}
