package nl.whitedove.thespygame;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class PrivacyDialog extends Dialog implements
        View.OnClickListener {

    private Context context;

    PrivacyDialog(Context ctx) {
        super(ctx);
        this.context = ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.privacy_dialog);
        Button btOk = (Button) findViewById(R.id.btOk);
        btOk.setOnClickListener(this);
        GetPrivacyTxtInBackground();
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    private void GetPrivacyTxtInBackground() {
        new GetPrivacyTxt().execute();
    }

    private class GetPrivacyTxt extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(Helper.PrivacuUrl)
                    .build();

            String privtxt = "";
            Response response;
            try {
                response = client.newCall(request).execute();
                if (response.isSuccessful()) privtxt = response.body().string();
            } catch (IOException ignored) {

            }
            return privtxt;
        }

        @Override
        protected void onPostExecute(String result) {
            ToonPrivacy(result);
        }
    }

    private void ToonPrivacy(String privacyTxt) {
        if (privacyTxt.equals(""))
            Helper.ShowMessage(context, context.getString(R.string.ErrorLoadingPrivacy), true);
        else {
            int startIndex = privacyTxt.indexOf("<h2>");
            int endIndex = privacyTxt.indexOf("</h2>");
            String toBeReplaced = privacyTxt.substring(startIndex + 1, endIndex);
            String showTxt = privacyTxt.replace(toBeReplaced, "");
            TextView tvPrivacyText = (TextView) findViewById(R.id.tvPrivacyText);
            Helper.SetHtmlText(tvPrivacyText, showTxt);
        }
    }
}

