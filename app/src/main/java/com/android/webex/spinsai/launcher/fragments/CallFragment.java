package com.android.webex.spinsai.launcher.fragments;


import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.webex.spinsai.R;
import com.android.webex.spinsai.actions.WebexAgent;
import com.android.webex.spinsai.actions.commands.RequirePermissionAction;
import com.android.webex.spinsai.actions.events.AnswerEvent;
import com.android.webex.spinsai.actions.events.DialEvent;
import com.android.webex.spinsai.actions.events.HangupEvent;
import com.android.webex.spinsai.actions.events.OnAuxStreamEvent;
import com.android.webex.spinsai.actions.events.OnCallMembershipEvent;
import com.android.webex.spinsai.actions.events.OnConnectEvent;
import com.android.webex.spinsai.actions.events.OnDisconnectEvent;
import com.android.webex.spinsai.actions.events.OnMediaChangeEvent;
import com.android.webex.spinsai.actions.events.OnRingingEvent;
import com.android.webex.spinsai.actions.events.PermissionAcquiredEvent;
import com.android.webex.spinsai.actions.events.WebexAgentEvent;
import com.android.webex.spinsai.launcher.LauncherActivity;
import com.android.webex.spinsai.screentshot.BitmapHelper;
import com.android.webex.spinsai.screentshot.SSDisplayActivity;
import com.android.webex.spinsai.screentshot.ScreenShot;
import com.android.webex.spinsai.service.AwakeService;
import com.android.webex.spinsai.ui.BaseFragment;
import com.android.webex.spinsai.ui.FullScreenSwitcher;
import com.android.webex.spinsai.utils.AppPrefs;
import com.ciscowebex.androidsdk.phone.CallMembership;
import com.ciscowebex.androidsdk.phone.MediaRenderView;
import com.ciscowebex.androidsdk.phone.MultiStreamObserver;
import com.github.benoitdion.ln.Ln;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;

import static com.ciscowebex.androidsdk.phone.CallObserver.RemoteSendingSharingEvent;
import static com.ciscowebex.androidsdk.phone.CallObserver.SendingSharingEvent;

/**
 * A simple {@link BaseFragment} subclass.
 */
public class CallFragment extends BaseFragment implements ScreenShot.PixelShotListener {

    private static final String TAG = "CallFragment";

    protected static final int MEDIA_PROJECTION_REQUEST = 2;
    private static final String CALLEE = "callee";
    private static final String INCOMING_CALL = "incoming";
    private WebexAgent agent;
    private FullScreenSwitcher screenSwitcher;
    private boolean isConnected = false;

    @BindView(R.id.localView)
    View localView;

    @BindView(R.id.remoteView)
    View remoteView;

    @BindView(R.id.screenShare)
    View screenShare;

    @BindView(R.id.buttonHangup)
    Button buttonHangup;

    @BindView(R.id.radioFrontCam)
    RadioButton radioFrontCam;

    @BindView(R.id.radioBackCam)
    RadioButton radioBackCam;

    @BindView(R.id.call_layout)
    ConstraintLayout layout;

    @BindView(R.id.takeScreenShot)
    Button takeScreenShot;

    @BindView(R.id.imageView2)
    ImageView imageView2;

    @BindView(R.id.imageView3)
    ImageView imageView3;

    @BindView(R.id.rootMediaLayout)
    ConstraintLayout rootMediaLayout;

    private int screenShotCount = 1;
    private String mainViewScreenShotPath = "";

    // Required empty public constructor
    class AuxStreamViewHolder {
        View item;
        MediaRenderView mediaRenderView;
        ImageView viewAvatar;
        TextView textView;

        AuxStreamViewHolder(View item) {
            this.item = item;
            this.mediaRenderView = item.findViewById(R.id.view_video);
            this.viewAvatar = item.findViewById(R.id.view_avatar);
            this.textView = item.findViewById(R.id.name);
        }
    }

    public CallFragment() {

    }

    public static CallFragment newAnswerCallInstance() {
        return CallFragment.newInstance(INCOMING_CALL);
    }

    public static CallFragment newInstance(String id) {
        CallFragment fragment = new CallFragment();
        Bundle args = new Bundle();
        args.putInt(LAYOUT, R.layout.fragment_call);
        args.putString(CALLEE, AppPrefs.getInstance().getRoomId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        agent = WebexAgent.getInstance();
        screenSwitcher = new FullScreenSwitcher(getActivity(), layout, remoteView);
        updateScreenShareView();

        if (!isConnected) {
            setViewAndChildrenEnabled(layout, false);
            ((SurfaceView) localView).setZOrderMediaOverlay(true);
            ((SurfaceView) screenShare).setZOrderMediaOverlay(true);
            //requirePermission();
            makeCall();
        }
    }

    private static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    private void setupWidgetStates() {
        switch (agent.getDefaultCamera()) {
            case FRONT:
                radioFrontCam.setChecked(true);
                break;
            case BACK:
                radioBackCam.setChecked(true);
                break;
            case CLOSE:
                localView.setVisibility(View.GONE);
                break;
        }
        updateScreenShareView();
    }

    private void updateScreenShareView() {
        screenShare.setVisibility(agent.isScreenSharing() ? View.VISIBLE : View.INVISIBLE);
    }

    private void requirePermission() {
        new RequirePermissionAction(getActivity()).execute();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void setButtonsEnable(boolean enable) {
        buttonHangup.setEnabled(enable);
    }

    @OnClick(R.id.buttonHangup)
    public void onHangup() {
        displayAlert();
    }

    @OnClick(R.id.takeScreenShot)
    public void onScreenShotClicked() {
        screenShotCount = 1;
        ScreenShot.of(remoteView).setResultListener(this).save(false);
        ScreenShot.of(localView).setResultListener(this).save(true);
    }

    @Override
    public void onPixelShotSuccess(String path) {
        Log.d(TAG, "file path" + path);
    }

    @Override
    public void onReturnBitmap(Bitmap bitmap) {
        BitmapHelper.getInstance().setRemoteBView(bitmap);
    }

    @Override
    public void onReturnBitmapLocal(Bitmap bitmap) {
        BitmapHelper.getInstance().setLocalBView(bitmap);
        //navigate to new view
        startActivity(new Intent(getActivity(), SSDisplayActivity.class));
    }

    @Override
    public void onPixelShotFailed() {

    }


    private void displayAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setMessage("Are you sure ?");
        alertDialog.setTitle(getString(R.string.app_name));
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton(
                "Yes",
                (dialog, id) -> {
                    agent.hangup();
                    getActivity().finish();
                    dialog.cancel();
                });

        alertDialog.setNegativeButton(
                "No",
                (dialog, id) -> dialog.cancel());

        AlertDialog alert11 = alertDialog.create();
        alert11.show();
    }

    @OnClick(R.id.remoteView)
    public void onRemoteViewClicked() {
        screenSwitcher.toggleFullScreen();
        updateFullScreenLayout();
    }

    private void updateFullScreenLayout() {
        updateScreenShareView();
        ((SurfaceView) remoteView).setZOrderMediaOverlay(screenSwitcher.isFullScreen());
        localView.setVisibility(screenSwitcher.isFullScreen() ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.radioBackCam)
    public void onBackCamRadioClicked() {
        agent.setBackCamera();
    }

    @OnClick(R.id.radioFrontCam)
    public void onFrontCamRadioClicked() {
        agent.setFrontCamera();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        screenSwitcher.updateOnRotation();
        updateScreenShareView();
    }

    @Override
    public void onBackPressed() {
        if (isConnected)
            agent.hangup();
    }

    private void makeCall() {
        String callee = getCallee();
        if (callee.isEmpty())
            return;

        if (callee.equals(INCOMING_CALL)) {
            setButtonsEnable(false);
            agent.answer(localView, remoteView, screenShare);
            return;
        }

        agent.dial(callee, localView, remoteView, screenShare);
        setButtonsEnable(true);
    }

    private String getCallee() {
        Bundle bundle = getArguments();
        return bundle != null ? bundle.getString(CALLEE) : "";
    }

    private void feedback() {
        getActivity().finish();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DialEvent event) {
        if (!event.isSuccessful()) {
            Toast.makeText(getActivity(), "Dial failed!", Toast.LENGTH_SHORT).show();
            feedback();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AnswerEvent event) {
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(HangupEvent event) {
        setButtonsEnable(false);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnRingingEvent event) {
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnConnectEvent event) {
        isConnected = true;
        startAwakeService();
        setViewAndChildrenEnabled(layout, true);
        if (agent.getDefaultCamera().equals(WebexAgent.CameraCap.CLOSE))
            agent.sendVideo(false);
        setupWidgetStates();

        event.call.setMultiStreamObserver(new MultiStreamObserver() {
            @Override
            public void onAuxStreamChanged(AuxStreamChangedEvent event) {
                WebexAgentEvent.postEvent(new OnAuxStreamEvent(event));
            }

            @Override
            public View onAuxStreamAvailable() {
                Ln.d("onAuxStreamAvailable");
                View auxStreamView = LayoutInflater.from(getActivity()).inflate(R.layout.remote_video_view, null);
                AuxStreamViewHolder auxStreamViewHolder = new AuxStreamViewHolder(auxStreamView);
                //mAuxStreamViewMap.put(auxStreamViewHolder.mediaRenderView, auxStreamViewHolder);
                return auxStreamViewHolder.mediaRenderView;
            }

            @Override
            public View onAuxStreamUnavailable() {
                Ln.d("onAuxStreamUnavailable");
                return null;
            }
        });
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnDisconnectEvent event) {
        isConnected = false;
        stopAwakeService();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnMediaChangeEvent event) {
        if (event.callEvent instanceof RemoteSendingSharingEvent) {
            Ln.d("RemoteSendingSharingEvent: " + ((RemoteSendingSharingEvent) event.callEvent).isSending());
            updateScreenShareView();
        } else if (event.callEvent instanceof SendingSharingEvent) {
            Ln.d("SendingSharingEvent: " + ((SendingSharingEvent) event.callEvent).isSending());
            if (((SendingSharingEvent) event.callEvent).isSending()) {
                sendNotification();
                backToHome();
            }
        }
    }


    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnAuxStreamEvent event) {

    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnCallMembershipEvent event) {
        CallMembership membership = event.callEvent.getCallMembership();
        Ln.d("OnCallMembershipEvent: " + membership);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PermissionAcquiredEvent event) {
        makeCall();
    }

    private void backToHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(intent);
    }

    private void sendNotification() {
        Intent appIntent = new Intent(getActivity(), LauncherActivity.class);
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notifyManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Cisco Kichensink")
                .setContentText("I'm sharing content")
                .setContentIntent(contentIntent);
        notifyManager.notify(1, builder.build());
    }

    private void startAwakeService() {
        getActivity().startService(new Intent(getActivity(), AwakeService.class));
    }

    private void stopAwakeService() {
        getActivity().stopService(new Intent(getActivity(), AwakeService.class));
    }

    @Override
    public void onDestroy() {
        stopAwakeService();
        super.onDestroy();
    }
}
