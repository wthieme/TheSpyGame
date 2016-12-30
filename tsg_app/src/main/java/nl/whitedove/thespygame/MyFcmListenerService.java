package nl.whitedove.thespygame;

/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
