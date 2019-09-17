package com.android.webex.spinsai.actions.events;

import com.ciscowebex.androidsdk.phone.CallObserver;

/**
 * Created by qimdeng on 3/13/18.
 */

public class OnCallMembershipEvent {
    public CallObserver.CallMembershipChangedEvent callEvent;

    public OnCallMembershipEvent(CallObserver.CallMembershipChangedEvent event) {
        this.callEvent = event;
    }
}
