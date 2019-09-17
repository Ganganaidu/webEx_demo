package com.android.webex.spinsai.utils;

import android.content.SharedPreferences;

import com.android.webex.spinsai.SpinsciHealthApp;

public class AppPrefs {

    private static class Loader {
        static final AppPrefs INSTANCE = new AppPrefs();
    }

    public static AppPrefs getInstance() {
        return Loader.INSTANCE;
    }

    /**
     * UDP connection state
     * Is called from ConnectionService.class
     */
    public void saveRoomId(String roomId) {
        SharedPreferences udpState = SpinsciHealthApp.getApplication().getSharedPreferences("room_data", 0);
        SharedPreferences.Editor editor = udpState.edit();
        editor.putString("roomId", roomId);

        // Commit the edits!
        editor.apply();
    }

    public String getRoomId() {
        SharedPreferences udpState = SpinsciHealthApp.getApplication().getSharedPreferences("room_data", 0);
        return udpState.getString("roomId", "");
    }
}
