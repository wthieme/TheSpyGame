package nl.whitedove.thespygame;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Html;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import nl.whitedove.thespygame.backend.tsgApi.TsgApi;
import nl.whitedove.thespygame.backend.tsgApi.model.Game;
import nl.whitedove.thespygame.backend.tsgApi.model.GameListExtra;
import nl.whitedove.thespygame.backend.tsgApi.model.LocationList;
import nl.whitedove.thespygame.backend.tsgApi.model.Player;
import nl.whitedove.thespygame.backend.tsgApi.model.TsgMessage;

class Helper {

    private static final String ApiUrl = "https://6-dot-thespygame-142522.appspot.com/_ah/api/";
    static final String PrivacuUrl = "https://wthieme.github.io/privacytsg.html";

    static TsgApi myApiService = new TsgApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
            .setRootUrl(Helper.ApiUrl)
            .build();


    private static final boolean DEBUG = false;
    static final String OK = "OK";
    static DateTimeFormatter tFormat = DateTimeFormat.forPattern("HH:mm:ss").withLocale(Locale.getDefault());
    static Location mCurrentBestLocation;
    static final int ONE_MINUTE = 1000 * 60;
    static final int ONE_KM = 1000;
    static final String REQUIRED_VERSION = "V6";
    static final String BTN_NAME = "btnPlayer";
    static final String TR_NAME = "tr";
    static final String TV_NAME = "tv";
    static final int MAX_PLAYERS = 9;
    static GameListExtra mGameList;
    static LocationList mLocationsList;
    static Game mGame;
    static Long mOffset;
    static DateTime mEndTimeStart = DateTime.now();
    static DateTime mEndTimeFinish = DateTime.now();
    static DateTime ScoreTime = DateTime.now();

    static void Log(String log) {
        if (Helper.DEBUG) {
            System.out.println(log);
        }
    }

    static Boolean TestInternet(Context ctx) {
        Boolean result;

        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) result = false;
        else {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            result = netInfo != null && netInfo.isConnectedOrConnecting();
        }

        if (!result) {
            Helper.ShowMessage(ctx, ctx.getString(R.string.NoInternet), false);
        }

        return result;
    }

    static String GetCountry(Context cxt) {
        String country = LocationHelper.GetCountry(cxt);
        String unknown = cxt.getString(R.string.CountryUnknown);
        if (country == null || country.equalsIgnoreCase(unknown)) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);
            return preferences.getString("country", unknown);
        }
        return country;
    }

    static String GetGame(Context cxt) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        return preferences.getString("game", "");
    }

    static void SetGame(Context cxt, String game) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("game", game);
        editor.apply();
    }

    static String GetToken(Context cxt) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        return preferences.getString("token", "");
    }

    static void SetToken(Context cxt, String token) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    static String GetNick(Context cxt) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        return preferences.getString("nick", "");
    }

    static void SetNick(Context cxt, String nick) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("nick", nick);
        editor.apply();
    }

    static String GetGuid(Context cxt) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        SharedPreferences.Editor editor = preferences.edit();

        String guid = preferences.getString("guid", "");
        if (guid.isEmpty()) {
            guid = UUID.randomUUID().toString();
            editor.putString("guid", guid);
            editor.apply();
        }
        return guid;
    }

    static void ShowMessage(Context cxt, String melding, boolean isLong) {
        Helper.Log(melding);
        int duration = isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(cxt, melding, duration);
        toast.show();
    }

    static ArrayList<String> GetPlayerList(List<Player> players, String nick) {
        ArrayList<String> pList = new ArrayList<>();
        for (Player p : players) {
            if (!p.getName().equalsIgnoreCase(nick)) pList.add(p.getName());
        }
        Collections.sort(pList, String.CASE_INSENSITIVE_ORDER);
        return pList;
    }

    static Player GetPlayer(List<Player> players, String id, Context context) {
        Player player = new Player();
        player.setId(id);
        player.setName(Helper.GetNick(context));
        player.setRole("");
        player.setIsSpy(false);
        player.setIsReady(false);
        if (players == null || players.size() == 0)
            return player;

        for (Player p : players) {
            if (p.getId().equalsIgnoreCase(id)) {
                return p;
            }
        }
        return player;
    }

    static void SetGame(Game game) {
        if (game == null) return;
        mGame = game;
        mOffset = game.getLastUsed().getMillis() - DateTime.now().getMillis();
        int nrOfPlayers = game.getPlayers() == null ? 0 : game.getPlayers().size();
        DateTime dts = game.getStartTime() == null ? new DateTime() : new DateTime(game.getStartTime().getMillis());
        mEndTimeStart = dts.plusMinutes(nrOfPlayers);
        DateTime dtf = game.getFinishTime() == null ? new DateTime() : new DateTime(game.getFinishTime().getMillis());
        mEndTimeFinish = dtf.plusSeconds(30);
    }

    static Player SearchForTheSpy(Game game) {
        if (game.getPlayers() == null || game.getPlayers().size() == 0) return null;

        for (Player p : game.getPlayers()) {
            if (p.getIsSpy()) {
                return p;
            }
        }
        return null;
    }

    static String TimeColorMessage(int col) {
        String hexColor = String.format("#%06X", (0xFFFFFF & col));
        return String.format("ct:%s", hexColor);
    }

    static String TimeValueMessage(String time) {
        return String.format("tv:%s", time);
    }

    static String GameMessage(Game game, Context context) {
        JSONObject gjson = new JSONObject();
        try {

            // Only show the location when the game is finished
            if (game.getGameStatus().equalsIgnoreCase("Finished")) {
                gjson.put("loc", String.format(context.getString(R.string.LocationResult), game.getLocation()));
            } else {
                gjson.put("loc", "");
                gjson.put("spy", "");
            }

            if (game.getGameStatus().equalsIgnoreCase("WaitingForScore")) {
                int toAnswer = 0;
                for (Player p : game.getPlayers())
                    if (p.getAnswer().equalsIgnoreCase(""))
                        toAnswer++;

                gjson.put("status", String.format(context.getString(R.string.ResultWait), Integer.toString(toAnswer)));
            } else {
                gjson.put("status", game.getGameStatus());
            }

            int nrOfPlayers = 0;

            if (game.getPlayers() != null) {
                List<Player> pSorted = new ArrayList<>(game.getPlayers());
                Collections.sort(pSorted, new PlayerComparator());

                nrOfPlayers = pSorted.size();

                for (int i = 0; i < nrOfPlayers; i++) {
                    Player p = pSorted.get(i);
                    gjson.put("name" + Integer.toString(i + 1), p.getName());

                    // Only show the spy and the role when the game is finished
                    if (game.getGameStatus().equalsIgnoreCase("Finished")) {

                        if (p.getCorrectAnswer()) {
                            gjson.put("cor" + Integer.toString(i + 1), "+1");
                        } else {
                            gjson.put("cor" + Integer.toString(i + 1), "-");
                        }

                        gjson.put("ans" + Integer.toString(i + 1), p.getAnswer());
                        if (p.getIsSpy()) {
                            gjson.put("spy", String.format(context.getString(R.string.SpyResult), p.getName()));
                            gjson.put("rol" + Integer.toString(i + 1), context.getString(R.string.TheSpy));
                        } else {
                            gjson.put("rol" + Integer.toString(i + 1), p.getRole());
                        }
                    } else {
                        gjson.put("rol" + Integer.toString(i + 1), "");
                        gjson.put("ans" + Integer.toString(i + 1), "");
                        gjson.put("cor" + Integer.toString(i + 1), "");
                    }

                    gjson.put("pts" + Integer.toString(i + 1), Integer.toString(p.getPoints()));
                }
            }

            for (int i = nrOfPlayers; i < Helper.MAX_PLAYERS; i++) {
                gjson.put("name" + Integer.toString(i + 1), "");
                gjson.put("rol" + Integer.toString(i + 1), "");
                gjson.put("ans" + Integer.toString(i + 1), "");
                gjson.put("cor" + Integer.toString(i + 1), "");
                gjson.put("pts" + Integer.toString(i + 1), "");
            }

            int nrMess = 0;
            if (game.getMessages() != null) {

                for (TsgMessage m : Lists.reverse(game.getMessages())) {
                    gjson.put("ttl" + Integer.toString(nrMess + 1), m.getTitle());
                    gjson.put("txt" + Integer.toString(nrMess + 1), m.getMessageTxt());
                    DateTime datum = new DateTime(m.getMessageDt().getMillis());
                    gjson.put("dt" + Integer.toString(nrMess + 1), Helper.tFormat.print(datum));
                    nrMess++;
                    if (nrMess >= 10) break;
                }
            }

            for (int i = nrMess; i < 10; i++) {
                gjson.put("ttl" + Integer.toString(i + 1), "");
                gjson.put("txt" + Integer.toString(i + 1), "");
                gjson.put("dt" + Integer.toString(i + 1), "");
            }

        } catch (JSONException ignored) {
        }
        return gjson.toString();
    }

    @SuppressWarnings("deprecation")
    static void SetHtmlText(TextView view, String htmlTxt) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            view.setText(Html.fromHtml(htmlTxt, Html.FROM_HTML_MODE_LEGACY));
        else
            view.setText(Html.fromHtml(htmlTxt));
    }
}