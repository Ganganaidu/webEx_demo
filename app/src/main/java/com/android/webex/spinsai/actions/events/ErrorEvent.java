package com.android.webex.spinsai.actions.events;

import org.greenrobot.eventbus.EventBus;

public class ErrorEvent {

    String message;

    public ErrorEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static void postEvent(Object event) {
        EventBus.getDefault().post(event);
    }

    public void post() {
        postEvent(this);
    }

}
