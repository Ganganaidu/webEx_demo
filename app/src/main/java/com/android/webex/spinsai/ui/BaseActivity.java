package com.android.webex.spinsai.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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

    public void dismissBusyIndicator() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /* Only use for debug */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(Object event) {
    }
}
