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

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.android.webex.spinsai.actions.IAction;
import com.android.webex.spinsai.actions.events.PermissionAcquiredEvent;
import com.android.webex.spinsai.actions.events.WebexAgentEvent;

/**
 * Created on 30/09/2017.
 */

public class RequirePermissionAction implements IAction {
    Activity activity;

    public RequirePermissionAction(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void execute() {
        int permissionCheck = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE
            };
            ActivityCompat.requestPermissions(activity, permissions, 0);
        } else {
            WebexAgentEvent.postEvent(new PermissionAcquiredEvent());
        }
    }

    public static void PermissionsRequired(int requestCode, int[] grantResults) {
        if (requestCode == 0 && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            WebexAgentEvent.postEvent(new PermissionAcquiredEvent());
        }
    }
}
