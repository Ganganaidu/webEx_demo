/*
 * Copyright 2016-2017 Cisco Systems Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package com.android.webex.spinsai.launcher;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.webex.spinsai.R;
import com.android.webex.spinsai.actions.WebexAgent;
import com.android.webex.spinsai.actions.commands.RequirePermissionAction;
import com.android.webex.spinsai.actions.events.OnCallMembershipEvent;
import com.android.webex.spinsai.actions.events.OnMediaChangeEvent;
import com.android.webex.spinsai.launcher.fragments.CallFragment;
import com.android.webex.spinsai.models.User;
import com.android.webex.spinsai.ui.BaseActivity;
import com.android.webex.spinsai.ui.BaseFragment;
import com.android.webex.spinsai.utils.AppPrefs;
import com.ciscowebex.androidsdk.phone.CallObserver;
import com.github.benoitdion.ln.Ln;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


public class LauncherActivity extends BaseActivity {

    TextView toolbar_title;
    TextView toolbar_desc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        CallFragment fragment = CallFragment.newInstance(AppPrefs.getInstance().getRoomId());
        replace(fragment, R.id.launcher);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_desc = findViewById(R.id.toolbar_desc);
        setSupportActionBar(toolbar);

        setUpToolBarTitle();
    }

    @Override
    public void onBackPressed() {
        goBackStack();
    }

    public void goBackStack() {
        finish();
//        FragmentManager manager = getFragmentManager();
//        BaseFragment fm = (BaseFragment) getFragment();
//        fm.onBackPressed();
//        if (manager.getBackStackEntryCount() == 1) {
//            manager.popBackStackImmediate();
//            super.onBackPressed();
//        } else {
//            //manager.popBackStackImmediate();
//            finish();
//        }
    }

    private void setUpToolBarTitle() {
        if (getSupportActionBar() != null) {
            User user = WebexAgent.getInstance().getUser();
            String patientName = null;
            String providerName = null;
            String title;

            if (user.getPatName() != null && !user.getPatName().isEmpty()) {
                patientName = "Patient: " + user.getPatName();
                toolbar_desc.setText(patientName);
            }

            if (user.getProvName() != null && !user.getProvName().isEmpty()) {
                providerName = "Provider: " + user.getProvName();
                toolbar_title.setText(providerName);
            }

            if (patientName == null && providerName == null) {
                title = WebexAgent.getInstance().getUser().getCsnId();
                toolbar_desc.setVisibility(View.GONE);
                toolbar_title.setText(title);
            }
        }
    }

    public void replace(BaseFragment fragment) {
        fragment.replace(this, R.id.launcher);
    }

    public Fragment getFragment() {
        return getFragmentManager().findFragmentById(R.id.launcher);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        RequirePermissionAction.PermissionsRequired(requestCode, grantResults);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnMediaChangeEvent event) {
        Ln.d("OnMediaChangeEvent: " + event.callEvent);
        if (event.callEvent instanceof CallObserver.SendingSharingEvent) {
            Ln.d("Activity SendingSharingEvent: " + ((CallObserver.SendingSharingEvent) event.callEvent).isSending());
            if (!((CallObserver.SendingSharingEvent) event.callEvent).isSending()) {
                cancelNotification();
                moveToFront();
                updateSharingSwitch(false);
                Toast.makeText(this, "Stop to share content", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnCallMembershipEvent event) {
        if (event.callEvent instanceof CallObserver.MembershipSendingSharingEvent) {
            Ln.d("Activity CallMembership email: " + event.callEvent.getCallMembership().getEmail() +
                    "  isSendingSharing: " + event.callEvent.getCallMembership().isSendingSharing());
        }
    }

    protected void moveToFront() {
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (int i = 0; i < recentTasks.size(); i++) {
            // bring to front
            if (recentTasks.get(i).baseActivity.toShortString().indexOf("com.android.webex.spinsai") > -1) {
                activityManager.moveTaskToFront(recentTasks.get(i).id, ActivityManager.MOVE_TASK_WITH_HOME);
            }
        }
    }

    private void cancelNotification() {
        NotificationManager notifyManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.cancel(1);
    }

    private void updateSharingSwitch(boolean flag) {
        Switch shareSwitch = (Switch) findViewById(R.id.switchShareContent);
        if (shareSwitch != null && shareSwitch.isChecked())
            shareSwitch.setChecked(flag);
    }
}
