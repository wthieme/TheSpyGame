package nl.whitedove.thespygame;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFcmListenerService extends FirebaseMessagingService {
    public static final String REFRESH_MAIN = "REFRESH_MAIN";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        if (message.getData().containsValue("MAIN")) Refresh_Main();
    }

    private void Refresh_Main() {
        Intent intent = new Intent(REFRESH_MAIN);
        sendBroadcast(intent);
    }
}
