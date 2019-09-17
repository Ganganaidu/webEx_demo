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

package com.android.webex.spinsai.login.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.android.webex.spinsai.R;
import com.android.webex.spinsai.actions.commands.AppIdLoginAction;
import com.android.webex.spinsai.actions.events.LoginEvent;
import com.android.webex.spinsai.actions.events.OnErrorEvent;
import com.android.webex.spinsai.launcher.LauncherActivity;
import com.android.webex.spinsai.ui.BaseFragment;
import com.android.webex.spinsai.utils.AppPrefs;
import com.android.webex.spinsai.utils.Constants;

import org.greenrobot.eventbus.Subscribe;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JwtFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JwtFragment extends BaseFragment {

    private static final String TAG = "JwtFragment";

    private String jws;

    public JwtFragment() {
        // Required empty public constructor
        setLayout(R.layout.fragment_jwt_login);

        generateJwt("","");
    }

    public static JwtFragment newInstance() {
        return new JwtFragment();
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

        getActivity().finish();
    }

    private void startLauncher() {
        Log.d(TAG, "startLauncher: " + AppPrefs.getInstance().getRoomId());
        startActivity(new Intent(getActivity(), LauncherActivity.class));
    }

    private void generateJwt(String csnId,String patId) {
        showBusyIndicator("SpinSci", "Connecting to UC Davis Health ...");

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] secretBytes = Base64.decode(Constants.guestSecret, Base64.URL_SAFE);
        Key signingKey = new SecretKeySpec(secretBytes, signatureAlgorithm.getJcaName());

        jws = Jwts.builder()
                .setIssuer(Constants.guestId)
                .setSubject(csnId + '/' + patId)
                .signWith(signingKey)
                .compact();

        Log.d(TAG, "generateJwt: " + jws);

        if (jws != null) {
            new AppIdLoginAction(jws).execute();
        }
    }
}
