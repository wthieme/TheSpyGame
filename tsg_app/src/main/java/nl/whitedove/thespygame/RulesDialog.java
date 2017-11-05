package nl.whitedove.thespygame;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

class RulesDialog extends Dialog implements
        View.OnClickListener {

    RulesDialog(Context ctx) {
        super(ctx);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.rules_dialog);
        Button btOk = findViewById(R.id.btOk);
        btOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}

