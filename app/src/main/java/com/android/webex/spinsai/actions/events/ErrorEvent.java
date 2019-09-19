package com.android.webex.spinsai.actions.events;

import org.greenrobot.eventbus.EventBus;

public class ErrorEvent {

    private String message;
    private int status;

    ErrorEvent(String message, int status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    private static void postEvent(Object event) {
        EventBus.getDefault().post(event);
    }

    public void post() {
        postEvent(this);
    }

}
