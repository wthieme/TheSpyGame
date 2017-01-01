package nl.whitedove.thespygame;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import nl.whitedove.thespygame.backend.tsgApi.model.Game;
import nl.whitedove.thespygame.backend.tsgApi.model.GameInfo;
import nl.whitedove.thespygame.backend.tsgApi.model.GameListExtra;
import nl.whitedove.thespygame.backend.tsgApi.model.LocationList;
import nl.whitedove.thespygame.backend.tsgApi.model.Player;
import nl.whitedove.thespygame.backend.tsgApi.model.TsgMessage;
import nl.whitedove.thespygame.backend.tsgApi.model.TsgVersion;

public class MainActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_main);
        mCastContext = CastContext.getSharedInstance(this);
        InitButtons();
        InitEdits();
        if (!CheckVersion()) return;
        InitLocation();
        GetLocationList();
        InitReceiver();
        GetGameStatus();
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
        GetGameStatus();
    }

    @Override
    protected void onPause() {
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

    //static inner class doesn't hold an implicit reference to the outer class
    private static class MyHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<MainActivity> myClassWeakReference;

        MyHandler(MainActivity myClassInstance) {
            myClassWeakReference = new WeakReference<>(myClassInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity ma = myClassWeakReference.get();
            if (ma != null) {
                ma.updateTimerOnScreen();
            }
        }
    }

    private void UnregBroadcastReceiver() {
        if (mReceiver != null) {
            try {
                unregisterReceiver(mReceiver);
            } catch (Exception ignored) {
            }
        }
    }

    private void CastTimeColor(int col) {
        sendMessage(Helper.TimeColorMessage(col));
    }

    private void CastTimeValue(String time) {
        sendMessage(Helper.TimeValueMessage(time));
    }

    private void updateTimerOnScreen() {
        Helper.Log("updateTimerOnScreen MAIN");

        DateTime timeNow = DateTime.now().plus(Helper.mOffset);
        Period period = new Period(timeNow, Helper.mEndTimeStart);

        TextView tvTimer = (TextView) findViewById(R.id.tvTimer);
        int totalSeconds = Seconds.secondsBetween(timeNow, Helper.mEndTimeStart).getSeconds();
        int col = ContextCompat.getColor(this, R.color.colorPrimaryDark);

        if (totalSeconds < 60) {
            CastTimeColor(Color.RED);
            tvTimer.setTextColor(Color.RED);
        } else {
            tvTimer.setTextColor(col);
            CastTimeColor(col);
        }

        int minutes = Math.abs(period.getMinutes());
        int seconds = Math.abs(period.getSeconds());
        String timeVal = String.format(getString(R.string.TimeFormat), minutes, seconds);
        CastTimeValue(timeVal);
        tvTimer.setText(timeVal);

        if (totalSeconds <= 0) {
            if (Helper.mGame.getGameStatus().equalsIgnoreCase("Running")) FinishGame();
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

        final MyHandler mHandler = new MyHandler(this);

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

    private void InitReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                GetGameStatus();
            }
        };
        registerReceiver(mReceiver, new IntentFilter(MyFcmListenerService.REFRESH_MAIN));
    }

    private void InitButtons() {

        FloatingActionButton fabCreateGame = (FloatingActionButton) findViewById(R.id.fabCreateGame);
        fabCreateGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateGame();
            }
        });

        FloatingActionButton fabJoinGame = (FloatingActionButton) findViewById(R.id.fabJoinGame);
        fabJoinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JoinGame();
            }
        });

        FloatingActionButton fabLeaveGame = (FloatingActionButton) findViewById(R.id.fabLeaveGame);
        fabLeaveGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LeaveGame();
            }
        });

        FloatingActionButton fabStartGame = (FloatingActionButton) findViewById(R.id.fabStartGame);
        fabStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartGame();
            }
        });

        FloatingActionButton fabFinishGame = (FloatingActionButton) findViewById(R.id.fabFinishGame);
        fabFinishGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FinishGame();
            }
        });

        FloatingActionButton fabChat = (FloatingActionButton) findViewById(R.id.fabChat);
        fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chat("");
            }
        });

        Button btnPlayer1 = (Button) findViewById(R.id.btnPlayer1);
        btnPlayer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                Chat(btn.getText().toString());
            }
        });

        Button btnPlayer2 = (Button) findViewById(R.id.btnPlayer2);
        btnPlayer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                Chat(btn.getText().toString());
            }
        });

        Button btnPlayer3 = (Button) findViewById(R.id.btnPlayer3);
        btnPlayer3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                Chat(btn.getText().toString());
            }
        });

        Button btnPlayer4 = (Button) findViewById(R.id.btnPlayer4);
        btnPlayer4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                Chat(btn.getText().toString());
            }
        });

        Button btnPlayer5 = (Button) findViewById(R.id.btnPlayer5);
        btnPlayer5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                Chat(btn.getText().toString());
            }
        });

        Button btnPlayer6 = (Button) findViewById(R.id.btnPlayer6);
        btnPlayer6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                Chat(btn.getText().toString());
            }
        });

        Button btnPlayer7 = (Button) findViewById(R.id.btnPlayer7);
        btnPlayer7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                Chat(btn.getText().toString());
            }
        });

        Button btnPlayer8 = (Button) findViewById(R.id.btnPlayer8);
        btnPlayer8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                Chat(btn.getText().toString());
            }
        });

        Button btnPlayer9 = (Button) findViewById(R.id.btnPlayer9);
        btnPlayer9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                Chat(btn.getText().toString());
            }
        });

        ImageView ivGameList = (ImageView) findViewById(R.id.ivGameList);
        ivGameList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    GetGameList();
                    NewMenu();
                } catch (IOException ignored) {
                }
            }
        });
    }

    private void ScoreActivity() {
        Intent intent = new Intent(this, ScoreActivity.class);
        startActivity(intent);
    }

    private boolean CheckGame(Context cxt) {
        String game = Helper.GetGame(cxt);
        if (game == null || game.isEmpty()) {
            Helper.ShowMessage(cxt, getString(R.string.NameMustNotBeEmpty), false);
            return false;
        }
        return true;
    }

    private boolean CheckNick(Context cxt) {
        String nick = Helper.GetNick(cxt);
        if (nick == null || nick.isEmpty()) {
            Helper.ShowMessage(cxt, getString(R.string.NicknameMustNotBeEmpty), false);
            return false;
        }
        return true;
    }

    private void Chat(String toPlayer) {
        final Context context = getApplicationContext();
        if (!Helper.TestInternet(context)) return;
        if (!CheckVersion()) return;
        if (!CheckGame(context)) return;
        if (!CheckNick(context)) return;
        if (Helper.mGame == null) return;
        if (Helper.mGame.getPlayers() == null) return;

        LayoutInflater li = LayoutInflater.from(this);
        @SuppressLint("InflateParams") final View chatView = li.inflate(R.layout.chat, null);

        final Spinner spPlayer = (Spinner) chatView.findViewById(R.id.spPlayer);
        final EditText etChatMessage = (EditText) chatView.findViewById(R.id.etChatMessage);

        List<String> players = Helper.GetPlayerList(Helper.mGame.getPlayers(), Helper.GetNick(context));
        if (players == null || players.size() == 0) {
            Helper.ShowMessage(context, getString(R.string.NoOneToChat), false);
            return;
        }

        String[] array = new String[players.size()];
        players.toArray(array);

        ArrayAdapter<String> playerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, players);
        spPlayer.setAdapter(playerAdapter);

        if (toPlayer.equals(""))
            spPlayer.setSelection(0);
        else
            spPlayer.setSelection(playerAdapter.getPosition(toPlayer));

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(chatView);
        builder.setCancelable(false);
        builder.setPositiveButton(Helper.OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String playerFrom = Helper.GetNick(context);
                        String playerTo = spPlayer.getSelectedItem().toString();
                        String chatTxt = etChatMessage.getText().toString();
                        HideKeyboard(chatView);
                        if (chatTxt.trim().equalsIgnoreCase("")) return;
                        TsgMessage mess = new TsgMessage();
                        String title = String.format(getString(R.string.FromTo), playerFrom, playerTo);
                        mess.setTitle(title);
                        mess.setMessageTxt(chatTxt);
                        SaveChatInBackground(mess);
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        HideKeyboard(chatView);
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            WindowManager.LayoutParams wmlp = window.getAttributes();
            wmlp.gravity = Gravity.TOP;
        }

        alertDialog.show();

        Button btnOk = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btnOk.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        Button btnCancel = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnCancel.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    @SuppressWarnings("unchecked")
    private void SaveChatInBackground(TsgMessage mess) {
        Context cxt = getApplicationContext();
        new AsyncSaveChat().execute(Pair.create(cxt, mess));
    }

    private class AsyncSaveChat extends AsyncTask<Pair<Context, TsgMessage>, Void, Pair<Context, Game>> {

        @SafeVarargs
        @Override
        protected final Pair<Context, Game> doInBackground(Pair<Context, TsgMessage>... params) {

            Context context = params[0].first;
            TsgMessage mess = params[0].second;
            String gameName = Helper.GetGame(context);
            String playerId = Helper.GetGuid(context);

            Game game = null;
            try {
                game = Helper.myApiService.chat(gameName, playerId, mess.getTitle(), mess.getMessageTxt()).execute();
            } catch (IOException ignored) {
            }
            return Pair.create(context, game);
        }

        @Override
        protected void onPostExecute(Pair<Context, Game> result) {
            Game game = result.second;
            String err = game.getResult();
            if (!err.equalsIgnoreCase(Helper.OK)) {
                Helper.ShowMessage(result.first, err, false);
            }
            Helper.SetGame(game);
            ToonGameInfo();
        }
    }

    private void GetGameStatus() {

        Context cxt = getApplicationContext();
        if (!Helper.TestInternet(cxt)) return;

        GetGameList();

        String gameName = Helper.GetGame(cxt);
        if (gameName == null || gameName.trim().equals("")) {
            StopTimer();
            return;
        }

        GetGameStatusInBackground();
    }

    private void GetGameStatusInBackground() {
        Context context = getApplicationContext();
        new AsyncGetGameStatus().execute(context);
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

    private void CreateGame() {
        Context cxt = getApplicationContext();
        if (!Helper.TestInternet(cxt)) return;
        if (!CheckVersion()) return;
        if (!CheckGame(cxt)) return;

        CreateGameInBackground();
        GetGameList();
    }

    private void CreateGameInBackground() {
        Context cxt = getApplicationContext();
        new AsyncCreateGame().execute(cxt);
    }

    private class AsyncCreateGame extends AsyncTask<Context, Void, Pair<Context, Game>> {

        @Override
        protected Pair<Context, Game> doInBackground(Context... params) {

            Context context = params[0];
            String gameName = Helper.GetGame(context);

            Game game = null;
            try {
                String country = Helper.GetCountry(context);
                game = Helper.myApiService.createGame(gameName, country).execute();
            } catch (IOException ignored) {
            }
            return Pair.create(context, game);
        }

        @Override
        protected void onPostExecute(Pair<Context, Game> result) {
            Game game = result.second;
            String err = game.getResult();
            if (!err.equalsIgnoreCase(Helper.OK)) {
                Helper.ShowMessage(result.first, err, false);
                return;
            }
            Helper.SetGame(game);
            ToonGameInfo();
        }
    }

    private void JoinGame() {
        Context cxt = getApplicationContext();

        if (!Helper.TestInternet(cxt)) return;
        if (!CheckVersion()) return;
        if (!CheckGame(cxt)) return;
        if (!CheckNick(cxt)) return;

        JoinGameInBackground();
    }

    private void JoinGameInBackground() {
        Context cxt = getApplicationContext();
        new AsyncJoineGame().execute(cxt);
    }

    private class AsyncJoineGame extends AsyncTask<Context, Void, Pair<Context, Game>> {

        @Override
        protected Pair<Context, Game> doInBackground(Context... params) {

            Context context = params[0];
            String gameName = Helper.GetGame(context);
            String guid = Helper.GetGuid(context);
            String nickName = Helper.GetNick(context);
            String token = Helper.GetToken(context);

            Game game = null;
            try {
                game = Helper.myApiService.joinGame(gameName, guid, nickName, token).execute();
            } catch (IOException ignored) {
            }
            return Pair.create(context, game);
        }

        @Override
        protected void onPostExecute(Pair<Context, Game> result) {
            Game game = result.second;
            String err = game.getResult();
            if (!err.equalsIgnoreCase(Helper.OK)) {
                Helper.ShowMessage(result.first, err, false);
            }
            Helper.SetGame(game);
            ToonGameInfo();
        }
    }

    private void LeaveGame() {
        Context context = getApplicationContext();

        if (!Helper.TestInternet(context)) return;
        if (!CheckVersion()) return;
        if (!CheckGame(context)) return;
        if (!CheckNick(context)) return;
        if (Helper.mGame == null) return;
        if (Helper.mGame.getPlayers() == null) return;
        if (Helper.mGame.getPlayers().size() == 0) return;

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        LeaveGameInBackground();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to leave?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button btnOk = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btnOk.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        Button btnCancel = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnCancel.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    private void LeaveGameInBackground() {
        Context cxt = getApplicationContext();
        new AsyncLeaveGame().execute(cxt);
    }

    private class AsyncLeaveGame extends AsyncTask<Context, Void, Pair<Context, Game>> {

        @Override
        protected Pair<Context, Game> doInBackground(Context... params) {

            Context context = params[0];
            String gameName = Helper.GetGame(context);
            String guid = Helper.GetGuid(context);

            Game game = null;
            try {
                game = Helper.myApiService.leaveGame(gameName, guid).execute();
            } catch (IOException ignored) {
            }
            return Pair.create(context, game);
        }

        @Override
        protected void onPostExecute(Pair<Context, Game> result) {
            Game game = result.second;
            String err = game.getResult();
            if (!err.equalsIgnoreCase(Helper.OK)) {
                Helper.ShowMessage(result.first, err, false);
            }
            Helper.SetGame(game);
            ToonGameInfo();
        }
    }

    private void StartGame() {
        Context cxt = getApplicationContext();
        if (!Helper.TestInternet(cxt)) return;
        if (!CheckGame(cxt)) return;
        StartGameInBackground();
    }

    private void StartGameInBackground() {
        Context cxt = getApplicationContext();
        new AsyncStartGame().execute(cxt);
    }

    private class AsyncStartGame extends AsyncTask<Context, Void, Pair<Context, Game>> {

        @Override
        protected Pair<Context, Game> doInBackground(Context... params) {

            Context context = params[0];
            String gameName = Helper.GetGame(context);
            String playerId = Helper.GetGuid(context);

            Game game = null;
            try {
                game = Helper.myApiService.startGame(gameName, playerId).execute();
            } catch (IOException ignored) {
            }
            return Pair.create(context, game);
        }

        @Override
        protected void onPostExecute(Pair<Context, Game> result) {
            Game game = result.second;
            String err = game.getResult();
            if (!err.equalsIgnoreCase(Helper.OK)) {
                Helper.ShowMessage(result.first, err, false);
                return;
            }
            Helper.SetGame(game);
            ToonGameInfo();
            StartTimer();
        }
    }

    private void FinishGame() {
        Helper.Log("Finish aangeroepen");
        StopTimer();
        Context cxt = getApplicationContext();
        if (!Helper.TestInternet(cxt)) return;
        if (!CheckGame(cxt)) return;
        FinishGameInBackground();
    }

    private void FinishGameInBackground() {
        Context cxt = getApplicationContext();
        new AsyncFinishGame().execute(cxt);
    }

    private class AsyncFinishGame extends AsyncTask<Context, Void, Pair<Context, Game>> {

        @Override
        protected Pair<Context, Game> doInBackground(Context... params) {

            Context context = params[0];
            String gameName = Helper.GetGame(context);
            String playerId = Helper.GetGuid(context);

            Game game = null;
            try {
                game = Helper.myApiService.finishGame(gameName, playerId).execute();
            } catch (IOException ignored) {
            }
            return Pair.create(context, game);
        }

        @Override
        protected void onPostExecute(Pair<Context, Game> result) {
            Game game = result.second;
            String err = game.getResult();
            if (!err.equalsIgnoreCase(Helper.OK)) {
                Helper.ShowMessage(result.first, err, false);
                return;
            }
            StopTimer();
            Helper.SetGame(game);
            ToonGameInfo();
        }
    }

    private void GetGameList() {
        Context cxt = getApplicationContext();
        if (!Helper.TestInternet(cxt)) return;

        GetGameListInBackground();
    }

    private void GetGameListInBackground() {
        new AsyncGetGameList().execute();
    }

    private class AsyncGetGameList extends AsyncTask<Void, Void, GameListExtra> {

        @Override
        protected GameListExtra doInBackground(Void... params) {

            GameListExtra gl = null;
            try {
                gl = Helper.myApiService.getGameListExtra().execute();
            } catch (IOException ignored) {
            }
            return gl;
        }

        @Override
        protected void onPostExecute(GameListExtra gameList) {
            Helper.mGameList = gameList;
        }
    }

    private void GetLocationList() {
        Context cxt = getApplicationContext();
        if (!Helper.TestInternet(cxt)) return;

        GetLocationListInBackground();
    }

    private void GetLocationListInBackground() {
        new AsyncGetLocationList().execute();
    }

    private class AsyncGetLocationList extends AsyncTask<Void, Void, LocationList> {

        @Override
        protected LocationList doInBackground(Void... params) {

            LocationList ll = null;
            try {
                ll = Helper.myApiService.getLocationList().execute();
            } catch (IOException ignored) {
            }
            return ll;
        }

        @Override
        protected void onPostExecute(LocationList locationList) {
            Helper.mLocationsList = locationList;
        }
    }

    private boolean CheckVersion() {
        Context cxt = getApplicationContext();
        if (!Helper.TestInternet(cxt)) return false;
        try {
            String v = CheckVersionInBackgroundAndWait();

            if (!v.equalsIgnoreCase(Helper.REQUIRED_VERSION)) {
                Helper.ShowMessage(cxt, getString(R.string.UpdateNeeded), true);
                return false;
            }
            return true;
        } catch (Exception e) {
            Helper.ShowMessage(cxt, e.getMessage(), true);
            return false;
        }
    }

    private String CheckVersionInBackgroundAndWait() throws ExecutionException, InterruptedException, TimeoutException {
        TsgVersion v = new AsyncCheckVersion().execute().get(10000, TimeUnit.MILLISECONDS);
        return v.getVersion();
    }

    private class AsyncCheckVersion extends AsyncTask<Void, Void, TsgVersion> {

        @Override
        protected TsgVersion doInBackground(Void... params) {

            TsgVersion v = null;
            try {
                v = Helper.myApiService.getVersion().execute();
            } catch (IOException ignored) {
            }
            return v;
        }
    }

    private String GetPlayerNameWithScore(Player player) {
        if (player == null) return "";
        return String.format("%s (%s)", player.getName(), Integer.toString(player.getPoints()));
    }

    private void CastToTv() {
        if (mCastContext == null || mCastSession == null) return;
        Context context = getApplicationContext();
        sendMessage(Helper.GameMessage(Helper.mGame, context));
    }

    private void ToonGameInfo() {

        Game game = Helper.mGame;
        if (game == null) return;

        CastToTv();
        TextView tvSpy = (TextView) findViewById(R.id.tvSpy);
        TextView tvLocation = (TextView) findViewById(R.id.tvLocation);
        TextView tvTimer = (TextView) findViewById(R.id.tvTimer);
        TextView tvGameStatus = (TextView) findViewById(R.id.tvGameStatus);
        FloatingActionButton fabStartGame = (FloatingActionButton) findViewById(R.id.fabStartGame);
        FloatingActionButton fabFinishGame = (FloatingActionButton) findViewById(R.id.fabFinishGame);
        LinearLayout llButtonRow2 = (LinearLayout) findViewById(R.id.llButtonRow2);
        LinearLayout llButtonRow3 = (LinearLayout) findViewById(R.id.llButtonRow3);
        ListView lvMessages = (ListView) findViewById(R.id.lvMessages);
        int nrOfPlayers = (game.getPlayers() == null) ? 0 : game.getPlayers().size();

        String packname = this.getPackageName();
        Resources res = this.getResources();

        for (int i = 0; i < Helper.MAX_PLAYERS; i++) {
            String name = Helper.BTN_NAME + Integer.toString(i + 1);
            int id = res.getIdentifier(name, "id", packname);
            Button but = (Button) findViewById(id);
            but.setText("");
        }

        for (int i = 0; i < nrOfPlayers; i++) {
            String name = Helper.BTN_NAME + Integer.toString(i + 1);
            int id = res.getIdentifier(name, "id", packname);
            Button but = (Button) findViewById(id);
            but.setText(GetPlayerNameWithScore(game.getPlayers().get(i)));
        }

        String gameStatus = game.getGameStatus();

        tvGameStatus.setText(String.format(getString(R.string.Status), gameStatus));
        switch (gameStatus) {
            case "Unknown": {
                fabStartGame.setVisibility(View.GONE);
                fabFinishGame.setVisibility(View.GONE);
                tvSpy.setVisibility(View.GONE);
                tvLocation.setVisibility(View.GONE);
                llButtonRow2.setVisibility(View.VISIBLE);
                llButtonRow3.setVisibility(View.VISIBLE);
                break;
            }
            case "Created": {
                fabStartGame.setVisibility(View.VISIBLE);
                fabFinishGame.setVisibility(View.GONE);
                tvSpy.setVisibility(View.GONE);
                tvLocation.setVisibility(View.GONE);
                llButtonRow2.setVisibility(View.VISIBLE);
                llButtonRow3.setVisibility(View.VISIBLE);
                break;
            }
            case "Running": {
                StartTimer();
                fabStartGame.setVisibility(View.GONE);
                fabFinishGame.setVisibility(View.VISIBLE);
                tvSpy.setVisibility(View.VISIBLE);
                Player player = Helper.GetPlayer(game.getPlayers(), Helper.GetGuid(this), this);

                if (player.getIsSpy()) {
                    tvLocation.setVisibility(View.GONE);
                    tvLocation.setText("");
                    tvSpy.setText(getString(R.string.Spy));
                } else {
                    tvLocation.setVisibility(View.VISIBLE);
                    tvLocation.setText(String.format(getString(R.string.Location), player.getRole(), game.getLocation()));
                    tvSpy.setText(getString(R.string.NoSpy));
                }

                if (nrOfPlayers <= 3) llButtonRow2.setVisibility(View.GONE);
                if (nrOfPlayers <= 6) llButtonRow3.setVisibility(View.GONE);
                break;
            }
            case "WaitingForScore": {
                StopTimer();
                tvTimer.setText(getString(R.string.TimeZero));
                tvTimer.setTextColor(Color.RED);
                fabStartGame.setVisibility(View.GONE);
                fabFinishGame.setVisibility(View.VISIBLE);
                tvSpy.setVisibility(View.VISIBLE);
                tvLocation.setVisibility(View.VISIBLE);

                Player player = Helper.GetPlayer(game.getPlayers(), Helper.GetGuid(this), this);

                if (player.getIsSpy()) {
                    tvLocation.setVisibility(View.GONE);
                    tvLocation.setText("");
                    tvSpy.setText(getString(R.string.Spy));
                } else {
                    tvLocation.setVisibility(View.VISIBLE);
                    tvLocation.setText(String.format(getString(R.string.Location), player.getRole(), game.getLocation()));
                    tvSpy.setText(getString(R.string.NoSpy));
                }

                if (Helper.ScoreTime.plusSeconds(1).isBeforeNow()) {
                    ScoreActivity();
                }
                else {
                    CastTimeValue(getString(R.string.TimeZero));
                    CastTimeColor(Color.RED);
                }
                Helper.ScoreTime = DateTime.now();
                break;
            }

            case "Finished": {
                StopTimer();
                CastTimeValue(getString(R.string.TimeZero));
                CastTimeColor(Color.RED);
                tvTimer.setText(getString(R.string.TimeZero));
                tvTimer.setTextColor(Color.RED);
                fabStartGame.setVisibility(View.VISIBLE);
                fabFinishGame.setVisibility(View.GONE);
                tvSpy.setVisibility(View.VISIBLE);
                tvLocation.setVisibility(View.VISIBLE);
                llButtonRow2.setVisibility(View.VISIBLE);
                llButtonRow3.setVisibility(View.VISIBLE);

                Player player = Helper.GetPlayer(game.getPlayers(), Helper.GetGuid(this), this);

                if (player.getIsSpy()) {
                    tvLocation.setVisibility(View.GONE);
                    tvLocation.setText("");
                    tvSpy.setText(getString(R.string.Spy));
                } else {
                    tvLocation.setVisibility(View.VISIBLE);
                    tvLocation.setText(String.format(getString(R.string.Location), player.getRole(), game.getLocation()));
                    tvSpy.setText(getString(R.string.NoSpy));
                }

                break;
            }
        }

        List<TsgMessage> messages = game.getMessages();

        if (messages != null && messages.size() > 0)
            lvMessages.setAdapter(new
                    CustomListAdapterMessages(this, messages)
            );
        else
            lvMessages.setAdapter(new
                    CustomListAdapterMessages(this, new ArrayList<TsgMessage>()
            ));
    }

    private void InitEdits() {
        final Context cxt = getApplicationContext();
        final EditText etGame = (EditText) findViewById(R.id.etGame);
        String game = Helper.GetGame(cxt);
        etGame.setText(game);

        etGame.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void afterTextChanged(Editable s) {
                Helper.SetGame(cxt, etGame.getText().toString());
            }
        });

        final EditText etNickname = (EditText) findViewById(R.id.etNickname);
        String nick = Helper.GetNick(cxt);
        etNickname.setText(nick);

        etNickname.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void afterTextChanged(Editable s) {
                Helper.SetNick(cxt, etNickname.getText().toString());
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void HideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void SetGameName(String name) {
        EditText etGame = (EditText) findViewById(R.id.etGame);
        etGame.setText(name);
    }

    @SuppressLint("InflateParams")
    public void NewMenu() throws IOException {
        Context cxt = getApplicationContext();

        if (Helper.mGameList == null || Helper.mGameList.getGames() == null || Helper.mGameList.getGames().size() == 0) {
            Helper.ShowMessage(cxt, getString(R.string.NoGamesAvailable), false);
            return;
        }
        List<ContextMenuItem> contextMenuItems;
        final Dialog customDialog = new Dialog(this);

        LayoutInflater inflater;
        View child;
        ListView listView;
        ContextMenuAdapter adapter;

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        child = inflater.inflate(R.layout.listview_context_menu, null);
        listView = (ListView) child.findViewById(R.id.listView_context_menu);

        contextMenuItems = new ArrayList<>();

        for (GameInfo g : Helper.mGameList.getGames()) {
            contextMenuItems.add(new ContextMenuItem(String.format(getString(R.string.GameInfo), g.getName(), g.getGameStatus(), g.getCountry(), g.getNrOfPlayers())));
        }

        adapter = new ContextMenuAdapter(this, contextMenuItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                customDialog.dismiss();
                SetGameName(Helper.mGameList.getGames().get(position).getName());
            }
        });

        customDialog.setTitle(getString(R.string.AvailableGames));
        customDialog.setContentView(child);
        customDialog.show();
    }

    private void InitLocation() {

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Helper.ONE_MINUTE, Helper.ONE_KM, locationListener);

        String locationProvider = LocationManager.NETWORK_PROVIDER;

        Location lastLocation = locationManager.getLastKnownLocation(locationProvider);
        makeUseOfNewLocation(lastLocation);
    }

    private void makeUseOfNewLocation(Location location) {
        Helper.mCurrentBestLocation = location;
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

