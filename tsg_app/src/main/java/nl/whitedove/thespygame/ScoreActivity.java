package nl.whitedove.thespygame;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.Seconds;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import nl.whitedove.thespygame.backend.tsgApi.model.Game;
import nl.whitedove.thespygame.backend.tsgApi.model.Player;


public class ScoreActivity extends AppCompatActivity {

    private BroadcastReceiver mReceiver;
    private ScheduledExecutorService mExecuter = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mFuture;
    private CastContext mCastContext;
    private CastSession mCastSession;
    private static TsgChannel mChannel;

    SessionManagerListener<CastSession> mSessionManagerListener = new SessionManagerListener<CastSession>() {

        @Override
        public void onSessionStarting(CastSession castSession) {
            // ignore
        }

        @Override
        public void onSessionStarted(CastSession castSession, String sessionId) {
            Helper.Log("Session started");
            mCastSession = castSession;
            startCustomMessageChannel();
            CastToTv();
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionStartFailed(CastSession castSession, int error) {
            // ignore
        }

        @Override
        public void onSessionEnding(CastSession castSession) {
            // ignore
        }

        @Override
        public void onSessionEnded(CastSession castSession, int error) {
            Helper.Log("Session ended");
            if (mCastSession == castSession) {
                cleanupSession();
            }
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionSuspended(CastSession castSession, int reason) {
            // ignore
        }

        @Override
        public void onSessionResuming(CastSession castSession, String sessionId) {
            // ignore
        }

        @Override
        public void onSessionResumed(CastSession castSession, boolean wasSuspended) {
            Helper.Log("Session resumed");
            mCastSession = castSession;
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionResumeFailed(CastSession castSession, int error) {
            // ignore
        }
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_actitivty);
        mCastContext = CastContext.getSharedInstance(this);
        InitViews();
        InitReceiver();
        ToonGameInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCastContext.getSessionManager().addSessionManagerListener(mSessionManagerListener, CastSession.class);
        if (mCastSession == null) {
            // Get the current session if there is one
            mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
            startCustomMessageChannel();
        }
        ToonGameInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        StopTimer();
        UnregBroadcastReceiver();
        mCastContext.getSessionManager().removeSessionManagerListener(mSessionManagerListener,
                CastSession.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Setup the menu item for connecting to cast devices
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menu_main bar item clicks here. The menu_main bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.itLocations) {
            ShowLocationssDialog();
            return true;
        }

        if (id == R.id.itRules) {
            ShowRulesDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void cleanupSession() {
        closeCustomMessageChannel();
        mCastSession = null;
    }

    private void startCustomMessageChannel() {
        if (mCastSession != null && mChannel == null) {
            mChannel = new TsgChannel(getString(R.string.cast_namespace));
            try {
                mCastSession.setMessageReceivedCallbacks(mChannel.getNamespace(), mChannel);
                Helper.Log("Message channel started");
            } catch (IOException e) {
                Helper.Log("Error starting message channel");
                mChannel = null;
            }
        }
    }

    private void closeCustomMessageChannel() {
        if (mCastSession != null && mChannel != null) {
            try {
                mCastSession.removeMessageReceivedCallbacks(mChannel.getNamespace());
                Helper.Log("Message channel closed");
            } catch (IOException e) {
                Helper.Log("Error closing message channel");
            } finally {
                mChannel = null;
            }
        }
    }

    void sendMessage(String message) {
        if (mChannel != null)
            mCastSession.sendMessage(mChannel.getNamespace(), message);
    }

    private static class TsgChannel implements Cast.MessageReceivedCallback {
        private final String mNamespace;

        TsgChannel(String namespace) {
            mNamespace = namespace;
        }

        String getNamespace() {
            return mNamespace;
        }

        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
            Helper.Log("onMessageReceived: " + message);
        }
    }


    private void Terug() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    //static inner class doesn't hold an implicit reference to the outer class
    private static class MyHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<ScoreActivity> myClassWeakReference;

        MyHandler(ScoreActivity myClassInstance) {
            myClassWeakReference = new WeakReference<>(myClassInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            ScoreActivity ma = myClassWeakReference.get();
            if (ma != null) {
                ma.updateTimerOnScreen();
            }
        }
    }

    private void InitReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                GetGameStatus();
            }
        };

        registerReceiver(mReceiver, new IntentFilter(MyFcmListenerService.REFRESH_SCORE));
    }

    private void UnregBroadcastReceiver() {
        if (mReceiver != null) {
            try {
                unregisterReceiver(mReceiver);
            } catch (Exception ignored) {
            }
        }
    }

    private void updateTimerOnScreen() {
        Helper.Log("updateTimerOnScreen SCORE");

        DateTime timeNow = DateTime.now().plus(Helper.mOffset);
        Period period = new Period(timeNow, Helper.mEndTimeFinish);

        TextView tvTimerResult = (TextView) findViewById(R.id.tvTimerResult);
        int totalSeconds = Seconds.secondsBetween(timeNow, Helper.mEndTimeFinish).getSeconds();
        int col = ContextCompat.getColor(this, R.color.colorPrimaryDark);

        if (totalSeconds < 10) {
            CastTimeColor(Color.RED);
            tvTimerResult.setTextColor(Color.RED);
        } else {
            tvTimerResult.setTextColor(col);
            CastTimeColor(col);
        }

        int minutes = Math.abs(period.getMinutes());
        int seconds = Math.abs(period.getSeconds());
        String timeVal = String.format(getString(R.string.TimeFormat), minutes, seconds);
        CastTimeValue(timeVal);
        tvTimerResult.setText(timeVal);

        if (totalSeconds <= 0) {
            ProcessResults();
        }
    }

    private void StopTimer() {
        if (mExecuter != null && mFuture != null) {
            mFuture.cancel(false);
            Helper.Log("Task gestopt");
        }
    }

    private void StartTimer() {
        if (mExecuter == null) {
            mExecuter = Executors.newSingleThreadScheduledExecutor();
            Helper.Log("Executer gemaakt");
        }

        final ScoreActivity.MyHandler mHandler = new ScoreActivity.MyHandler(this);

        Runnable task = new Runnable() {
            public void run() {
                Context cxt = getApplicationContext();
                try {
                    mHandler.obtainMessage(1).sendToTarget();
                } catch (Exception e) {
                    Helper.ShowMessage(cxt, e.getMessage(), false);
                }
            }
        };

        if (mFuture != null) {
            mFuture.cancel(false);
            Helper.Log("Task gestopt");
        }

        mFuture = mExecuter.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
        Helper.Log("Task geactiveerd");
    }

    private void InitViews() {

        FloatingActionButton fabTerug = (FloatingActionButton) findViewById(R.id.fabBack);
        fabTerug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Terug();
            }
        });

        Spinner spWhere = (Spinner) findViewById(R.id.spWhere);
        Spinner spWho = (Spinner) findViewById(R.id.spWho);
        TextView tvWhereAreWe = (TextView) findViewById(R.id.tvWhereAreWe);
        TextView tvWhoIsSpy = (TextView) findViewById(R.id.tvWhoIsSpy);
        Context context = this.getApplicationContext();

        Player player = Helper.GetPlayer(Helper.mGame.getPlayers(), Helper.GetGuid(context), context);

        if (player.getIsSpy()) {
            spWho.setVisibility(View.GONE);
            tvWhoIsSpy.setVisibility(View.GONE);
            spWhere.setVisibility(View.VISIBLE);
            tvWhereAreWe.setVisibility(View.VISIBLE);

            if (Helper.mLocationsList != null && Helper.mLocationsList.getLocationNames() != null && Helper.mLocationsList.getLocationNames().size() > 0) {
                ArrayList<String> locList = new ArrayList<>();
                locList.addAll(Helper.mLocationsList.getLocationNames());
                locList.add(0, "");
                ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, locList);
                spWhere.setAdapter(locationAdapter);
                spWhere.setSelection(0, false);
            }

            spWhere.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    SendWhereAnswer();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    SendWhereAnswer();
                }

            });

        } else {
            spWho.setVisibility(View.VISIBLE);
            tvWhoIsSpy.setVisibility(View.VISIBLE);
            spWhere.setVisibility(View.GONE);
            tvWhereAreWe.setVisibility(View.GONE);

            if (Helper.mGame != null && Helper.mGame.getPlayers() != null && Helper.mGame.getPlayers().size() > 0) {
                ArrayList<String> players = Helper.GetPlayerList(Helper.mGame.getPlayers(), Helper.GetNick(this));
                String[] array = new String[players.size()];
                players.toArray(array);
                players.add(0, "");
                ArrayAdapter<String> playerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, players);
                spWho.setAdapter(playerAdapter);
                spWho.setSelection(0, false);
            }

            spWho.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    SendWhoAnswer();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    SendWhoAnswer();
                }

            });
        }
    }

    private void CastTimeColor(int col) {
        sendMessage(Helper.TimeColorMessage(col));
    }

    private void CastTimeValue(String time) {
        sendMessage(Helper.TimeValueMessage(time));
    }

    private void CastToTv() {
        if (mCastContext == null || mCastSession == null) return;
        Context context = getApplicationContext();
        sendMessage(Helper.GameMessage(Helper.mGame, context));
    }

    private void ProcessResults() {
        Context context = getApplicationContext();
        new AsyncProcessResults().execute(context);
    }

    private class AsyncProcessResults extends AsyncTask<Context, Void, Pair<Context, Game>> {

        @Override
        protected Pair<Context, Game> doInBackground(Context... params) {

            Context context = params[0];
            String gameName = Helper.GetGame(context);

            Game game = null;
            try {
                game = Helper.myApiService.processResults(gameName).execute();
            } catch (IOException ignored) {
            }
            return Pair.create(context, game);
        }

        @Override
        protected void onPostExecute(Pair<Context, Game> result) {
            Game game = result.second;
            Helper.SetGame(game);
            ToonGameInfo();
        }
    }

    private void SendWhereAnswer() {
        Spinner spWhere = (Spinner) findViewById(R.id.spWhere);
        String answer = spWhere.getSelectedItem().toString();
        if (!answer.equalsIgnoreCase("")) SendAnswerInBackground(answer);
    }

    private void SendWhoAnswer() {
        Spinner spWho = (Spinner) findViewById(R.id.spWho);
        String answer = spWho.getSelectedItem().toString();
        if (!answer.equalsIgnoreCase("")) SendAnswerInBackground(answer);
    }

    @SuppressWarnings("unchecked")
    private void SendAnswerInBackground(String answer) {
        Context cxt = this.getApplicationContext();
        new AsyncSendAnswer().execute(Pair.create(cxt, answer));
    }

    private class AsyncSendAnswer extends AsyncTask<Pair<Context, String>, Void, Pair<Context, Game>> {

        @SafeVarargs
        @Override
        protected final Pair<Context, Game> doInBackground(Pair<Context, String>... params) {

            Context context = params[0].first;
            String answer = params[0].second;
            String gameName = Helper.GetGame(context);
            String playerId = Helper.GetGuid(context);

            Game game = null;
            try {
                game = Helper.myApiService.addAnswer(gameName, playerId, answer).execute();
            } catch (IOException ignored) {
            }
            return Pair.create(context, game);
        }

        @Override
        protected void onPostExecute(Pair<Context, Game> result) {
            Game game = result.second;
            Context context = result.first;
            String err = game.getResult();
            if (!err.equalsIgnoreCase(Helper.OK)) {
                Helper.ShowMessage(result.first, err, false);
                return;
            }

            int toAnswer = 0;
            for (Player p : game.getPlayers())
                if (p.getAnswer().equalsIgnoreCase(""))
                    toAnswer++;

            if (toAnswer == 0)
                Helper.ShowMessage(context, "Answer sent", false);
            else
                Helper.ShowMessage(context, String.format("Answer sent, waiting for %s more players", Integer.toString(toAnswer)), true);
        }
    }

    private void GetGameStatus() {

        Context context = getApplicationContext();
        if (!Helper.TestInternet(context)) return;

        String gameName = Helper.GetGame(context);
        if (gameName == null || gameName.trim().equals("")) {
            StopTimer();
            return;
        }

        GetGameStatusInBackground();
    }

    private void GetGameStatusInBackground() {
        Context context = getApplicationContext();
        new ScoreActivity.AsyncGetGameStatus().execute(context);
    }

    private class AsyncGetGameStatus extends AsyncTask<Context, Void, Pair<Context, Game>> {

        @Override
        protected Pair<Context, Game> doInBackground(Context... params) {

            Context context = params[0];
            String gameName = Helper.GetGame(context);

            Game game = null;
            try {
                game = Helper.myApiService.getGameStatus(gameName).execute();
            } catch (IOException ignored) {
            }
            return Pair.create(context, game);
        }

        @Override
        protected void onPostExecute(Pair<Context, Game> result) {
            Game game = result.second;
            Helper.SetGame(game);
            ToonGameInfo();
        }
    }

    @SuppressLint("SetTextI18n")
    private void ToonGameInfo() {
        Game game = Helper.mGame;
        CastToTv();

        TextView tvResultWho = (TextView) findViewById(R.id.tvResultWho);
        TextView tvResultWhere = (TextView) findViewById(R.id.tvResultWhere);
        Player theSpy = Helper.SearchForTheSpy(game);
        Context context = getApplicationContext();
        TextView tvTimerResult = (TextView) findViewById(R.id.tvTimerResult);

        if (game.getGameStatus().equalsIgnoreCase("Finished")) {
            StopTimer();
            CastTimeValue(getString(R.string.TimeZero));
            CastTimeColor(Color.RED);
            tvTimerResult.setText(getString(R.string.TimeZero));
            tvTimerResult.setTextColor(Color.RED);
            tvResultWho.setText(String.format(context.getString(R.string.SpyResult), theSpy == null ? "Unknown" : theSpy.getName()));
            tvResultWhere.setVisibility(View.VISIBLE);
            tvResultWhere.setText(String.format(context.getString(R.string.LocationResult), game.getLocation()));
        } else {
            tvResultWhere.setVisibility(View.GONE);
        }

        if (game.getGameStatus().equalsIgnoreCase("WaitingForScore")) {
            int toAnswer = 0;
            for (Player p : game.getPlayers())
                if (p.getAnswer().equalsIgnoreCase(""))
                    toAnswer++;

            tvResultWho.setText(String.format(context.getString(R.string.ResultWait), Integer.toString(toAnswer)));
            StartTimer();
        }

        List<Player> pSorted = new ArrayList<>(game.getPlayers());
        Collections.sort(pSorted, new PlayerComparator());

        Resources res = context.getResources();
        String packname = context.getPackageName();
        int nrOfPlayers = pSorted.size();

        for (int i = 0; i < nrOfPlayers; i++) {
            // Position
            String name = Helper.TV_NAME + Integer.toString(i + 1) + "_1";
            int id = res.getIdentifier(name, "id", packname);
            TextView tv1 = (TextView) findViewById(id);
            tv1.setText(Integer.toString(i + 1));

            //Player name
            name = Helper.TV_NAME + Integer.toString(i + 1) + "_2";
            id = res.getIdentifier(name, "id", packname);
            TextView tv2 = (TextView) findViewById(id);
            tv2.setText(pSorted.get(i).getName());

            //Total points
            name = Helper.TV_NAME + Integer.toString(i + 1) + "_5";
            id = res.getIdentifier(name, "id", packname);
            TextView tv5 = (TextView) findViewById(id);
            tv5.setText(Integer.toString(pSorted.get(i).getPoints()));

            if (game.getGameStatus().equalsIgnoreCase("Finished")) {
                // Answer
                name = Helper.TV_NAME + Integer.toString(i + 1) + "_3";
                id = res.getIdentifier(name, "id", packname);
                TextView tv3 = (TextView) findViewById(id);
                tv3.setText(pSorted.get(i).getAnswer());

                // +1 or -
                name = Helper.TV_NAME + Integer.toString(i + 1) + "_4";
                id = res.getIdentifier(name, "id", packname);
                TextView tv4 = (TextView) findViewById(id);
                Boolean correct = pSorted.get(i).getCorrectAnswer();
                tv4.setText(correct ? "+1" : "-");

                for (int j = 1; j < 6; j++) {
                    name = Helper.TV_NAME + Integer.toString(i + 1) + "_" + Integer.toString(j);
                    id = res.getIdentifier(name, "id", packname);
                    TextView tv = (TextView) findViewById(id);
                    if (correct)
                        tv.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGreenBackground));
                    else
                        tv.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRedBackground));
                }
            }
        }

        for (int i = nrOfPlayers; i < Helper.MAX_PLAYERS; i++) {
            String name = Helper.TR_NAME + Integer.toString(i + 1);
            int id = res.getIdentifier(name, "id", packname);
            View tr = findViewById(id);
            tr.setVisibility(View.GONE);
        }
    }

    private void ShowRulesDialog() {
        RulesDialog rd = new RulesDialog(this);
        ShowBiggerDialog(rd);
    }

    private void ShowLocationssDialog() {
        LocationsDialog ld = new LocationsDialog(this);
        ShowBiggerDialog(ld);

        if (Helper.mLocationsList != null && Helper.mLocationsList.getLocationNames() != null && Helper.mLocationsList.getLocationNames().size() > 0)
            ld.lvLocations.setAdapter(new CustomListAdapterLocations(this, Helper.mLocationsList.getLocationNames()));
        else
            ld.lvLocations.setAdapter(new CustomListAdapterLocations(this, new ArrayList<String>()));
    }

    @SuppressWarnings("ConstantConditions")
    static private void ShowBiggerDialog(Dialog d) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        d.show();
        d.getWindow().setAttributes(lp);
    }
}