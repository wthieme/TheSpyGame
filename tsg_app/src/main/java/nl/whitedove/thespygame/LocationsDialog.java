package nl.whitedove.thespygame;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

class LocationsDialog extends Dialog implements
        View.OnClickListener {

    private Context context;
    ListView lvLocations;

    LocationsDialog(Context ctx) {
        super(ctx);
        this.context = ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.locations_dialog);
        Button btOk = (Button) findViewById(R.id.btOk);
        btOk.setOnClickListener(this);
        lvLocations = (ListView) findViewById(R.id.lvLocations);
        InitListView();
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    private void InitListView() {
        lvLocations.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view.findViewById(R.id.tvPossibleLocation);
                int flags = tv.getPaintFlags();
                if (flags == (flags | Paint.STRIKE_THRU_TEXT_FLAG)) {
                    tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    tv.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                } else {
                    tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    tv.setTextColor(ContextCompat.getColor(context, R.color.colorTextLight));
                }
            }
        });
    }
}

