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

package com.android.webex.spinsai.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.android.webex.spinsai.R;
import com.android.webex.spinsai.actions.commands.AppIdLoginAction;
import com.android.webex.spinsai.actions.events.LoginEvent;
import com.android.webex.spinsai.actions.events.OnErrorEvent;
import com.android.webex.spinsai.launcher.LauncherActivity;
import com.android.webex.spinsai.ui.BaseActivity;
import com.android.webex.spinsai.utils.AppPrefs;
import com.android.webex.spinsai.utils.Constants;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.greenrobot.eventbus.Subscribe;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    @BindView(R.id.textView2)
    TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            parseUrl(data);
        } else {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String csnId = bundle.getString("ROOM_NAME");
                String patientId = bundle.getString("PATIENT_ID");

                init(csnId, patientId);

            } else {
                displayDefaultMessage();
            }
        }
    }

    private void parseUrl(Uri referrerUri) {
        try {
            String hostName = referrerUri.getHost();
            String scheme = referrerUri.getScheme();

            if (scheme != null && scheme.equalsIgnoreCase("spinscitelehealth")) {

                String csnId = referrerUri.getQueryParameter("csn");
                String provId = referrerUri.getQueryParameter("provId");
                String provName = referrerUri.getQueryParameter("provName");
                String patId = referrerUri.getQueryParameter("patId");
                String patName = referrerUri.getQueryParameter("patName");

                if (hostName != null && hostName.equalsIgnoreCase("patient")) {
                    init(csnId, patId);
                }
            } else {
                displayDefaultMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayDefaultMessage();
        }
    }

    private void displayDefaultMessage() {
        textView.setText(getString(R.string.please_open_this));
    }

    private void init(String csnId, String patId) {
        Constants.csnId = csnId;

        String[] permissions = {Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO};
        Permissions.check(this/*context*/, permissions, "Please allow these permission, " +
                "We need them to connect webEx"/*rationale*/, null/*options*/, new PermissionHandler() {
            @Override
            public void onGranted() {
                showBusyIndicator("SpinSci", "Connecting to UC Davis Health ...");
                generateJwt(csnId, patId);
            }
        });
    }

    private boolean recordAudio() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED;
    }

    private boolean storageAudio() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    private boolean cameraAudio() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onEventMainThread(LoginEvent event) {
        dismissBusyIndicator();
        if (event.isSuccessful()) {
            toast("AppID logged in.");
            startLauncher();
        } else {
            toast(event.getError().toString());
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onErrorEvent(OnErrorEvent event) {
        dismissBusyIndicator();
        toast(event.getMessage());
        finish();
    }

    private void startLauncher() {
        Log.d(TAG, "startLauncher: " + AppPrefs.getInstance().getRoomId());
        startActivity(new Intent(this, LauncherActivity.class));
        finish();
    }

    private void generateJwt(String csnId, String patId) {

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] secretBytes = Base64.decode(Constants.guestSecret, Base64.URL_SAFE);
        Key signingKey = new SecretKeySpec(secretBytes, signatureAlgorithm.getJcaName());

        String jws = Jwts.builder()
                .setIssuer(Constants.guestId)
                .setSubject(csnId + '/' + patId)
                .signWith(signingKey)
                .compact();

        Log.d(TAG, "generateJwt: " + jws);

        if (jws != null) {
            new AppIdLoginAction(jws).execute();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
