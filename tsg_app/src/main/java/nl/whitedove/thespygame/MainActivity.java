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
import android.support.annotation.NonNull;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
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
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
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
    private ScheduledExecutorService mExecuterMain = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mFutureMain;
    private ScheduledExecutorService mExecuterScore = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mFutureScore;
    private CastContext mCastContext;
    private CastSession mCastSession;
    private static TsgChannel mChannel;
    static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

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
        StopTimerMain();
        StopTimerScore();
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

        if (id == R.id.itPrivacy) {
            ShowPrivacyDialog();
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

    private static class MyHandlerMain extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<MainActivity> myClassWeakReference;

        MyHandlerMain(MainActivity myClassInstance) {
            myClassWeakReference = new WeakReference<>(myClassInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity ma = myClassWeakReference.get();
            if (ma != null) {
                ma.updateMainTimerOnScreen();
            }
        }
    }

    private static class MyHandlerScore extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<MainActivity> myClassWeakReference;

        MyHandlerScore(MainActivity myClassInstance) {
            myClassWeakReference = new WeakReference<>(myClassInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity ma = myClassWeakReference.get();
            if (ma != null) {
                ma.updateScoreTimerOnScreen();
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

    private void updateScoreTimerOnScreen() {
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

    private void InitViewsScore() {
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

    private void updateMainTimerOnScreen() {
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
            if (Helper.mGame.getGameStatus().equalsIgnoreCase("Running")) Ready(true);
        }
    }

    private void StopTimerMain() {
        if (mExecuterMain != null && mFutureMain != null) {
            mFutureMain.cancel(false);
        }
    }

    private void StartTimerMain() {
        if (mExecuterMain == null) {
            mExecuterMain = Executors.newSingleThreadScheduledExecutor();
        }

        final MyHandlerMain mHandler = new MyHandlerMain(this);

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

        if (mFutureMain != null) {
            mFutureMain.cancel(false);
        }

        mFutureMain = mExecuterMain.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
    }

    private void StopTimerScore() {
        if (mExecuterScore != null && mFutureScore != null) {
            mFutureScore.cancel(false);
        }
    }

    private void StartTimerScore() {
        if (mExecuterScore == null) {
            mExecuterScore = Executors.newSingleThreadScheduledExecutor();
        }

        final MyHandlerScore mHandler = new MyHandlerScore(this);

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

        if (mFutureScore != null) {
            mFutureScore.cancel(false);
        }

        mFutureScore = mExecuterScore.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
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

        FloatingActionButton fabChat = (FloatingActionButton) findViewById(R.id.fabChat);
        fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chat("");
            }
        });

        Switch swReady = (Switch) findViewById(R.id.swReady);
        InitReadySwitch(swReady, false);
        Switch swReadyScore = (Switch) findViewById(R.id.swReadyScore);
        InitReadySwitch(swReadyScore, false);

        Button btnPlayer1 = (Button) findViewById(R.id.btnPlayer1);
        btnPlayer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                String name = btn.getTag() == null ? "" : btn.getTag().toString();
                Chat(name);
            }
        });

        Button btnPlayer2 = (Button) findViewById(R.id.btnPlayer2);
        btnPlayer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                String name = btn.getTag() == null ? "" : btn.getTag().toString();
                Chat(name);
            }
        });

        Button btnPlayer3 = (Button) findViewById(R.id.btnPlayer3);
        btnPlayer3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                String name = btn.getTag() == null ? "" : btn.getTag().toString();
                Chat(name);
            }
        });

        Button btnPlayer4 = (Button) findViewById(R.id.btnPlayer4);
        btnPlayer4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                String name = btn.getTag() == null ? "" : btn.getTag().toString();
                Chat(name);
            }
        });

        Button btnPlayer5 = (Button) findViewById(R.id.btnPlayer5);
        btnPlayer5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                String name = btn.getTag() == null ? "" : btn.getTag().toString();
                Chat(name);
            }
        });

        Button btnPlayer6 = (Button) findViewById(R.id.btnPlayer6);
        btnPlayer6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                String name = btn.getTag() == null ? "" : btn.getTag().toString();
                Chat(name);
            }
        });

        Button btnPlayer7 = (Button) findViewById(R.id.btnPlayer7);
        btnPlayer7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                String name = btn.getTag() == null ? "" : btn.getTag().toString();
                Chat(name);
            }
        });

        Button btnPlayer8 = (Button) findViewById(R.id.btnPlayer8);
        btnPlayer8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                String name = btn.getTag() == null ? "" : btn.getTag().toString();
                Chat(name);
            }
        });

        Button btnPlayer9 = (Button) findViewById(R.id.btnPlayer9);
        btnPlayer9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                String name = btn.getTag() == null ? "" : btn.getTag().toString();
                Chat(name);
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

    private void InitReadySwitch(Switch swReady, boolean checked) {
        swReady.setOnCheckedChangeListener(null);
        swReady.setChecked(checked);
        swReady.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Ready(isChecked);
            }
        });
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
            StopTimerMain();
            StopTimerScore();
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

    private void Ready(boolean isReady) {
        Context cxt = getApplicationContext();
        if (!Helper.TestInternet(cxt)) return;
        if (!CheckGame(cxt)) return;
        StartReadyInBackground(isReady);
    }

    private void StartReadyInBackground(boolean isReady) {
        Context cxt = getApplicationContext();
        new AsyncReady().execute(new ReadyInfo(cxt, isReady));
    }

    private class AsyncReady extends AsyncTask<ReadyInfo, Void, Pair<Context, Game>> {

        @Override
        protected Pair<Context, Game> doInBackground(ReadyInfo... params) {

            Context context = params[0].getContext();
            boolean isReady = params[0].getIsReady();
            String gameName = Helper.GetGame(context);
            String playerId = Helper.GetGuid(context);

            Game game = null;
            try {
                if (isReady) {
                    game = Helper.myApiService.ready(gameName, playerId).execute();
                } else {
                    game = Helper.myApiService.unReady(gameName, playerId).execute();
                }
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
            if (game.getGameStatus().equalsIgnoreCase("Running")) {
                StartTimerMain();
            } else {
                StopTimerMain();
            }
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
        if (mCastContext == null || mCastSession == null || Helper.mGame == null) return;
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
        Switch swReady = (Switch) findViewById(R.id.swReady);
        Switch swReadyScore = (Switch) findViewById(R.id.swReadyScore);
        TextView tvReady = (TextView) findViewById(R.id.tvReady);
        TextView tvReadyPercent = (TextView) findViewById(R.id.tvReadyPercent);
        TextView tvReadyPercentScore = (TextView) findViewById(R.id.tvReadyPercentScore);
        TextView tvWaitingFor = (TextView) findViewById(R.id.tvWaitingFor);
        LinearLayout llButtonRow2 = (LinearLayout) findViewById(R.id.llButtonRow2);
        LinearLayout llButtonRow3 = (LinearLayout) findViewById(R.id.llButtonRow3);
        LinearLayout llReady = (LinearLayout) findViewById(R.id.llReady);
        LinearLayout llReadyScore = (LinearLayout) findViewById(R.id.llReadyScore);
        LinearLayout llWhereAreWe = (LinearLayout) findViewById(R.id.llWhereAreWe);
        LinearLayout llWhoIsSpy = (LinearLayout) findViewById(R.id.llWhoIsSpy);

        RelativeLayout rlMain = (RelativeLayout) findViewById(R.id.rlMain);
        RelativeLayout rlScore = (RelativeLayout) findViewById(R.id.rlScore);
        ListView lvMessages = (ListView) findViewById(R.id.lvMessages);
        TextView tvResultWho = (TextView) findViewById(R.id.tvResultWho);
        TextView tvResultWhere = (TextView) findViewById(R.id.tvResultWhere);
        TextView tvTimerResult = (TextView) findViewById(R.id.tvTimerResult);

        Player theSpy = Helper.SearchForTheSpy(game);
        Player player = Helper.GetPlayer(game.getPlayers(), Helper.GetGuid(this), this);

        int nrOfPlayers = (game.getPlayers() == null) ? 0 : game.getPlayers().size();
        int colRed = ContextCompat.getColor(this, R.color.colorRedBackground);
        int colGreen = ContextCompat.getColor(this, R.color.colorGreenBackground);

        String packname = this.getPackageName();
        Resources res = this.getResources();

        for (int i = 0; i < Helper.MAX_PLAYERS; i++) {
            String name = Helper.BTN_NAME + Integer.toString(i + 1);
            int id = res.getIdentifier(name, "id", packname);
            Button but = (Button) findViewById(id);
            but.setTag("");
            but.setText("");
        }

        int aantalReady = 0;

        for (int i = 0; i < nrOfPlayers; i++) {
            String name = Helper.BTN_NAME + Integer.toString(i + 1);
            int id = res.getIdentifier(name, "id", packname);
            Button but = (Button) findViewById(id);
            but.setTag(game.getPlayers().get(i).getName());
            but.setText(GetPlayerNameWithScore(game.getPlayers().get(i)));
            if (game.getPlayers().get(i).getIsReady()) aantalReady++;
        }

        String gameStatus = game.getGameStatus();

        tvGameStatus.setText(String.format(getString(R.string.Status), gameStatus));
        switch (gameStatus) {
            case "Unknown": {
                StopTimerMain();
                StopTimerScore();
                rlMain.setVisibility(View.VISIBLE);
                rlScore.setVisibility(View.GONE);
                llReady.setVisibility(View.GONE);
                tvSpy.setVisibility(View.GONE);
                tvLocation.setVisibility(View.GONE);
                llButtonRow2.setVisibility(View.VISIBLE);
                llButtonRow3.setVisibility(View.VISIBLE);
                break;
            }
            case "Created": {
                StopTimerMain();
                StopTimerScore();
                rlMain.setVisibility(View.VISIBLE);
                rlScore.setVisibility(View.GONE);
                llReady.setVisibility(View.VISIBLE);
                InitReadySwitch(swReady, player.getIsReady());
                tvReady.setText(getString(R.string.ReadyToStart));
                tvReadyPercent.setText(String.format(getString(R.string.ReadyPercent), Integer.toString(aantalReady), Integer.toString(nrOfPlayers)));
                swReady.setBackgroundColor(player.getIsReady() ? colGreen : colRed);
                tvSpy.setVisibility(View.GONE);
                tvLocation.setVisibility(View.GONE);
                llButtonRow2.setVisibility(View.VISIBLE);
                llButtonRow3.setVisibility(View.VISIBLE);
                break;
            }
            case "Running": {
                StartTimerMain();
                StopTimerScore();
                rlMain.setVisibility(View.VISIBLE);
                rlScore.setVisibility(View.GONE);
                llReady.setVisibility(View.VISIBLE);
                InitReadySwitch(swReady, player.getIsReady());
                tvReady.setText(getString(R.string.ReadyToFinish));
                tvReadyPercent.setText(String.format(getString(R.string.ReadyPercent), Integer.toString(aantalReady), Integer.toString(nrOfPlayers)));
                swReady.setBackgroundColor(player.getIsReady() ? colGreen : colRed);
                tvSpy.setVisibility(View.VISIBLE);

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
                InitViewsScore();
                StopTimerMain();
                StartTimerScore();
                tvWaitingFor.setVisibility(View.VISIBLE);
                llWhoIsSpy.setVisibility(View.VISIBLE);
                llWhereAreWe.setVisibility(View.VISIBLE);
                rlMain.setVisibility(View.GONE);
                rlScore.setVisibility(View.VISIBLE);
                tvTimer.setText(getString(R.string.TimeZero));
                tvTimer.setTextColor(Color.RED);
                llReadyScore.setVisibility(View.GONE);
                tvResultWho.setVisibility(View.GONE);
                tvResultWhere.setVisibility(View.GONE);

                int toAnswer = 0;
                for (Player p : game.getPlayers())
                    if (p.getAnswer().equalsIgnoreCase(""))
                        toAnswer++;

                tvWaitingFor.setText(String.format(getString(R.string.ResultWait), Integer.toString(toAnswer)));
                ToonScores();
                break;
            }

            case "Finished": {
                InitViewsScore();
                StopTimerMain();
                StopTimerScore();
                tvWaitingFor.setVisibility(View.GONE);
                llWhoIsSpy.setVisibility(View.GONE);
                llWhereAreWe.setVisibility(View.GONE);
                rlMain.setVisibility(View.GONE);
                rlScore.setVisibility(View.VISIBLE);
                CastTimeValue(getString(R.string.TimeZero));
                CastTimeColor(Color.RED);
                tvTimer.setText(getString(R.string.TimeZero));
                tvTimer.setTextColor(Color.RED);
                llReadyScore.setVisibility(View.VISIBLE);
                InitReadySwitch(swReadyScore, player.getIsReady());
                tvReadyPercentScore.setText(String.format(getString(R.string.ReadyPercent), Integer.toString(aantalReady), Integer.toString(nrOfPlayers)));
                swReadyScore.setBackgroundColor(player.getIsReady() ? colGreen : colRed);
                CastTimeValue(getString(R.string.TimeZero));
                CastTimeColor(Color.RED);
                tvTimerResult.setText(getString(R.string.TimeZero));
                tvTimerResult.setTextColor(Color.RED);
                tvResultWho.setVisibility(View.VISIBLE);
                tvResultWho.setText(String.format(getString(R.string.SpyResult), theSpy == null ? "Unknown" : theSpy.getName()));
                tvResultWhere.setVisibility(View.VISIBLE);
                tvResultWhere.setText(String.format(getString(R.string.LocationResult), game.getLocation()));
                ToonScores();
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

    private void ToonScores() {
        Game game = Helper.mGame;
        List<Player> pSorted = new ArrayList<>(game.getPlayers());
        Collections.sort(pSorted, new PlayerComparator());
        Resources res = this.getResources();
        String packname = this.getPackageName();
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
                Boolean correct = pSorted.get(i).getIsCorrectAnswer();
                tv4.setText(correct ? "+1" : "-");

                for (int j = 1; j < 6; j++) {
                    name = Helper.TV_NAME + Integer.toString(i + 1) + "_" + Integer.toString(j);
                    id = res.getIdentifier(name, "id", packname);
                    TextView tv = (TextView) findViewById(id);
                    if (correct)
                        tv.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGreenBackground));
                    else
                        tv.setBackgroundColor(ContextCompat.getColor(this, R.color.colorRedBackground));
                }
            }
        }
        for (int i = nrOfPlayers; i < Helper.MAX_PLAYERS; i++) {
            String name = Helper.TR_NAME + Integer.toString(i + 1);
            int id = res.getIdentifier(name, "id", packname);
            View tr = findViewById(id);
            tr.setVisibility(View.GONE);
        }

        TableLayout tlRanking = (TableLayout) findViewById(R.id.tlRanking);
        tlRanking.invalidate();
        tlRanking.setVisibility(View.GONE);
        tlRanking.setVisibility(View.VISIBLE);

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

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }

        Location netLastLocation = null;

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Helper.ONE_MINUTE, Helper.ONE_KM, locationListener);
            netLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (netLastLocation != null) makeUseOfNewLocation(netLastLocation);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    InitLocation();
                } else {
                    Helper.ShowMessage(this, "Without location permission the country will be unknown in the game list", true);
                }
            }
        }
    }

    private void makeUseOfNewLocation(Location location) {
        Helper.mCurrentBestLocation = location;
    }

    private void ShowRulesDialog() {
        RulesDialog rd = new RulesDialog(this);
        ShowBiggerDialog(rd);
    }

    private void ShowPrivacyDialog() {
        PrivacyDialog pd = new PrivacyDialog(this);
        ShowBiggerDialog(pd);
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

