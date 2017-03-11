package nl.whitedove.thespygame;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFcmListenerService extends FirebaseMessagingService {
    public static final String REFRESH_MAIN = "REFRESH_MAIN";
    public static final String REFRESH_SCORE = "REFRESH_SCORE";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        if (message.getData().containsValue("MAIN")) Refresh_Main();
        if (message.getData().containsValue("SCORE")) Refresh_Score();
    }

    private void Refresh_Main() {
        Intent intent = new Intent(REFRESH_MAIN);
        sendBroadcast(intent);
    }

    private void Refresh_Score() {
        Intent intent = new Intent(REFRESH_SCORE);
        sendBroadcast(intent);
    }
}
