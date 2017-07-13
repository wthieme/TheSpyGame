package nl.whitedove.thespygame;

import android.content.Context;

public class ReadyInfo {
    Context context;
    boolean isReady;

    public ReadyInfo(Context context, boolean isReady) {
        this.context = context;
        this.isReady = isReady;
    }

    public boolean getIsReady() {
        return isReady;
    }

    public Context getContext() {
        return context;
    }

}
