package com.android.webex.spinsai.launcher;

import android.app.Fragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.webex.spinsai.R;
import com.android.webex.spinsai.actions.WebexAgent;
import com.android.webex.spinsai.actions.commands.RequirePermissionAction;
import com.android.webex.spinsai.actions.events.OnCallMembershipEvent;
import com.android.webex.spinsai.actions.events.OnMediaChangeEvent;
import com.android.webex.spinsai.launcher.fragments.CallFragment;
import com.android.webex.spinsai.models.User;
import com.android.webex.spinsai.screentshot.BitmapHelper;
import com.android.webex.spinsai.screentshot.PathListDisplayActivity;
import com.android.webex.spinsai.ui.BaseActivity;
import com.android.webex.spinsai.ui.BaseFragment;
import com.android.webex.spinsai.utils.AppPrefs;
import com.ciscowebex.androidsdk.phone.CallObserver;
import com.github.benoitdion.ln.Ln;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LauncherActivity extends BaseActivity {

    @BindView(R.id.toolbar_title)
    TextView toolbar_title;

    @BindView(R.id.toolbar_desc)
    TextView toolbar_desc;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        ButterKnife.bind(this);

        CallFragment fragment = CallFragment.newInstance(AppPrefs.getInstance().getRoomId());
        replace(fragment, R.id.launcher);

        setSupportActionBar(toolbar);
        setUpToolBarTitle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_path, menu);
        //MenuItem item = menu.findItem(R.id.action_path_list);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.action_path_list && BitmapHelper.getInstance().getFilePathList().size() > 0) {
            startActivity(new Intent(this, PathListDisplayActivity.class));
            return true;
        } else {
            Toast.makeText(this, "No saved images to display", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
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
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
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

    private void cancelNotification() {
        NotificationManager notifyManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifyManager != null) {
            notifyManager.cancel(1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BitmapHelper.getInstance().clear();
    }
}
