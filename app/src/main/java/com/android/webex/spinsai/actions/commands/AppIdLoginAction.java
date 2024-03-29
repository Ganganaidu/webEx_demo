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

package com.android.webex.spinsai.actions.commands;

import com.android.webex.spinsai.SpinsciHealthApp;
import com.android.webex.spinsai.actions.IAction;
import com.android.webex.spinsai.actions.WebexAgent;
import com.ciscowebex.androidsdk.Webex;
import com.ciscowebex.androidsdk.auth.JWTAuthenticator;

/**
 * Created on 19/09/2017.
 */

public class AppIdLoginAction implements IAction {
    private String jwt;

    public AppIdLoginAction(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public void execute() {
        JWTAuthenticator jwtAuthenticator = new JWTAuthenticator();
        Webex webex = new Webex(SpinsciHealthApp.getApplication(), jwtAuthenticator);
        WebexAgent.getInstance().setWebex(webex);
        jwtAuthenticator.authorize(jwt);
        new RegisterAction(jwtAuthenticator).execute();
    }
}