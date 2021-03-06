package nl.whitedove.thespygame.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.inject.Named;

@Api(
        name = "tsgApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.thespygame.whitedove.nl",
                ownerName = "backend.thespygame.whitedove.nl"
        )
)
public class TsgEndpoint {

    private List<Game> CurrentGames = Collections.synchronizedList(new ArrayList<Game>());
    private Random rnd = new Random(DateTime.now().getMillis());
    private static final String OK = "OK";
    private static final int MAX_PLAYERS = 9;
    private static final int MAX_CHATS = 50;
    private static final String MAIN = "MAIN";

    private void SetGameHealthy(Game game) {
        game.setResult(OK);
        game.setLastUsed(DateTime.now());
    }

    private Game DetermineSpyAndPlayer(Game game) {

        SetGameHealthy(game);

        // The location
        Location location = LocationHelper.GetRandomLocation();
        game.setLocation(location.getLocationName());

        ArrayList<String> roles = new ArrayList<>(location.getParticipants());

        ArrayList<Player> players = game.getPlayers();
        for (Player p : players) {
            int nrOfRoles = roles.size();
            int roleNr = rnd.nextInt(nrOfRoles);
            String role = roles.get(roleNr);
            roles.remove(roleNr);
            if (roles.size() == 0) {
                roles = new ArrayList<>(location.getParticipants());
            }
            p.setRole(role);
            p.setIsSpy(false);
            p.setAnswer("");
            p.setIsCorrectAnswer(false);
        }

        int nrOfPlayers = players.size();
        int theSpyNr = rnd.nextInt(nrOfPlayers);
        Player theSpy = players.get(theSpyNr);
        theSpy.setIsSpy(true);
        theSpy.setRole("");

        game.setStartTime(DateTime.now());
        game.setGameStatus(Game.GameStatus.Running);

        return game;
    }

    private Game SearchForGame(String gameName) {
        for (Game g : CurrentGames) {
            if (gameName.equalsIgnoreCase(g.getName())) {
                return g;
            }
        }
        return null;
    }

    private Player SearchForPlayer(Game game, String playerId) {
        for (Player p : game.getPlayers()) {
            if (p.getId().equalsIgnoreCase(playerId)) {
                return p;
            }
        }
        return null;
    }

    private Player SearchForTheSpy(Game game) {
        for (Player p : game.getPlayers()) {
            if (p.getIsSpy()) {
                return p;
            }
        }
        return null;
    }

    private Boolean AllPlayersAnswered(Game game) {
        for (Player p : game.getPlayers()) {
            String answer = p.getAnswer();
            if (answer == null || answer.equalsIgnoreCase("")) {
                return false;
            }
        }
        return true;
    }

    private Boolean AllPlayersReady(Game game) {
        for (Player p : game.getPlayers()) {
            if (!p.getIsReady()) {
                return false;
            }
        }
        return true;
    }

    private void DeleteOldGames() {
        DateTime dtNow = DateTime.now();
        ArrayList<Game> newGames = new ArrayList<>();
        for (Game g : CurrentGames) {
            int minutes = Math.abs(Minutes.minutesBetween(dtNow, g.getLastUsed()).getMinutes());
            if (minutes <= 15) {
                newGames.add(g);
            }
        }
        CurrentGames = newGames;
    }

    private void AddWaitingPlayers(Game game) {
        for (Player p : game.getWaitingPlayers()) {
            AddPlayer(game, p);
        }
    }

    private void AddPlayerToWaitingList(Game game, Player player) {
        SetGameHealthy(game);
        for (Player p : game.getWaitingPlayers()) {
            if (p.getId().equalsIgnoreCase(player.getId())) {
                // Already waiting
                return;
            }
        }

        if (game.getPlayers().size() + game.getWaitingPlayers().size() >= MAX_PLAYERS) {
            game.setResult(String.format("Cannot add you to the waiting players, maximum number of players is %s", Integer.toString(MAX_PLAYERS)));
            return;
        }

        game.getWaitingPlayers().add(player);
    }

    private void AddMessage(Game game, TsgMessage message) {
        ArrayList<TsgMessage> messages = game.getMessages();
        messages.add(message);
        if (messages.size() > MAX_CHATS) {
            messages.remove(0);
        }
        game.setMessages(messages);
    }

    private Game AddPlayer(Game game, Player player) {
        SetGameHealthy(game);
        for (Player p : game.getPlayers()) {
            if (p.getName().equalsIgnoreCase(player.getName())
                    && !p.getId().equalsIgnoreCase(player.getId())) {

                game.setResult("There is already another player with the name '" + player.getName() + "'");
                return game;
            }
        }

        Player p = SearchForPlayer(game, player.getId());
        if (p != null) {
            p.setName(player.getName());
            p.setToken(player.getToken());
            return game;
        }

        if (game.getPlayers().size() >= MAX_PLAYERS) {
            game.setResult(String.format("Maximum number of players is %s", Integer.toString(MAX_PLAYERS)));
            return game;
        }

        game.getPlayers().add(player);
        return game;
    }

    private static void SendMessagesToClients(ArrayList<Player> players, String data) {
        String host = "https://fcm.googleapis.com/fcm/send";

        for (Player p : players) {
            if (p.getToken() == null || p.getToken().trim().equals("")) continue;
            String q = "\"";
            String message = "{" + q + "to" + q + ":" + q + p.getToken() + q + "," +
                    q + "data" + q + ":{" + q + "tsg" + q + ":" + q + data + q + ",}}";

            //String message = String.format("{ \"to\" : \"%s\" }", p.getToken());

            try {
                URLFetchService url_service = URLFetchServiceFactory.getURLFetchService();
                HTTPRequest request = new HTTPRequest(new URL(host), HTTPMethod.POST);
                request.setHeader(new HTTPHeader("Authorization", "key=AIzaSyDw1CU4cCNeaWsgMQcJLCVwEAfzZM3umTc"));
                request.setHeader(new HTTPHeader("Content-Type", "application/json; charset=UTF-8"));
                request.setPayload(message.getBytes("utf8"));

                HTTPResponse response = url_service.fetch(request);
                if (response.getResponseCode() != 200) {
                    throw new IOException(new String(response.getContent()));
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void ProcessResults(Game game) {
        SetGameHealthy(game);

        if (game.getGameStatus() != Game.GameStatus.WaitingForScore) {
            // Return the running game
            return;
        }

        Player theSpy = SearchForTheSpy(game);
        if (theSpy == null) {
            // Return the running game
            return;
        }

        for (Player p : game.getPlayers()) {
            // Check if answer is correct
            Boolean correct;
            String answer = p.getAnswer().toLowerCase();
            if (p.getIsSpy())
                correct = (!answer.equalsIgnoreCase("") && game.getLocation().toLowerCase().contains(answer));
            else
                correct = theSpy.getName().equalsIgnoreCase(answer);

            p.setIsCorrectAnswer(correct);
            if (correct) p.setPoints(p.getPoints() + 1);
        }
        game.setGameStatus(Game.GameStatus.Finished);

        TsgMessage mess = new TsgMessage("The game has finished", "");
        AddMessage(game, mess);

        SendMessagesToClients(game.getPlayers(), MAIN);
    }

    private Game StartGame(Game game) {
        if (game.getPlayers().size() <= 2) {
            game.setResult("A minimum of 3 players is required to start the game");
            return game;
        }

        game = DetermineSpyAndPlayer(game);

        // All unready
        for (Player p : game.getPlayers()) {
            p.setIsReady(false);
        }

        TsgMessage mess = new TsgMessage("A new game has been started. Have fun!", "");
        AddMessage(game, mess);
        return game;
    }

    private Game FinishGame(Game game) {

        AddWaitingPlayers(game);
        game.setWaitingPlayers(new ArrayList<Player>());
        game.setFinishTime(DateTime.now());
        game.setGameStatus(Game.GameStatus.WaitingForScore);

        // All unready
        for (Player p : game.getPlayers()) {
            p.setIsReady(false);
        }

        TsgMessage mess = new TsgMessage("Waiting for answers", "");
        AddMessage(game, mess);

        return game;
    }

    @ApiMethod(name = "Ready")
    public Game Ready(@Named("GameName") String gameName, @Named("PlayerId") String playerId) {
        Game game = SearchForGame(gameName);
        if (game == null) {
            // Unexpected situation, game not found
            game = new Game(gameName);
            game.setResult("Game '" + gameName + "' not found");
            return game;
        }

        SetGameHealthy(game);

        if (game.getGameStatus() != Game.GameStatus.Created &&
                game.getGameStatus() != Game.GameStatus.Running &&
                game.getGameStatus() != Game.GameStatus.Finished) {
            // Ready function is not applicable
            return game;
        }

        Player player = SearchForPlayer(game, playerId);
        if (player == null) {
            game.setResult("You cannot set yourself ready for a game when you're not joined");
            return game;
        }

        player.setIsReady(true);

        // Check, if all players are ready
        if (!AllPlayersReady(game)) {
            SendMessagesToClients(game.getPlayers(), MAIN);
            return game;
        }

        // All ready, finish game if it was running
        if (game.getGameStatus() == Game.GameStatus.Running) {
            game = FinishGame(game);
            SendMessagesToClients(game.getPlayers(), MAIN);
            return game;
        }

        // All ready, start game if it was not running
        if (game.getGameStatus() == Game.GameStatus.Created || game.getGameStatus() == Game.GameStatus.Finished) {
            SendMessagesToClients(game.getPlayers(), MAIN);
            game = StartGame(game);
            return game;
        }

        return game;
    }

    @ApiMethod(name = "UnReady")
    public Game UnReady(@Named("GameName") String gameName, @Named("PlayerId") String playerId) {
        Game game = SearchForGame(gameName);
        if (game == null) {
            // Unexpected situation, game not found
            game = new Game(gameName);
            game.setResult("Game '" + gameName + "' not found");
            return game;
        }

        SetGameHealthy(game);

        if (game.getGameStatus() != Game.GameStatus.Created &&
                game.getGameStatus() != Game.GameStatus.Running &&
                game.getGameStatus() != Game.GameStatus.Finished) {
            // Ready function is not applicable
            return game;
        }

        Player player = SearchForPlayer(game, playerId);
        if (player == null) {
            game.setResult("You cannot set yourself unready for a game when you're not joined");
            return game;
        }

        player.setIsReady(false);
        SendMessagesToClients(game.getPlayers(), MAIN);
        return game;
    }

    @ApiMethod(name = "CreateGame")
    public Game CreateGame(@Named("GameName") String gameName, @Named("Country") String country) {
        DeleteOldGames();
        Game game = SearchForGame(gameName);
        if (game == null) {
            game = new Game(gameName);
            game.setCountry(country);
            game.setGameStatus(Game.GameStatus.Created);
            TsgMessage mess = new TsgMessage(String.format("Game %s has been created", gameName), "");
            AddMessage(game, mess);
            CurrentGames.add(game);
        } else {
            SetGameHealthy(game);
            game.setResult("Game '" + gameName + "' already exists");
        }

        return game;
    }

    @ApiMethod(name = "JoinGame")
    public Game JoinGame(@Named("GameName") String gameName, @Named("PlayerId") String
            playerId, @Named("PlayerName") String playerName, @Named("PlayerToken") String playerToken) {
        DeleteOldGames();
        Game game = SearchForGame(gameName);
        if (game == null) {
            game = new Game(gameName);
            game.setResult("Game '" + gameName + "' not found");
            return game;
        }

        SetGameHealthy(game);

        Player player = SearchForPlayer(game, playerId);
        if (player != null) {
            // Already in game, just set the name
            player.setName(playerName);
            SendMessagesToClients(game.getPlayers(), MAIN);
            return game;
        }

        player = new Player(playerId, playerName, playerToken);

        if (game.getGameStatus() == Game.GameStatus.Running || game.getGameStatus() == Game.GameStatus.WaitingForScore) {
            AddPlayerToWaitingList(game, player);
            game.setResult("You cannot join a running game, wait for the game to Finish");
            return game;
        }

        game = AddPlayer(game, player);

        TsgMessage mess = new TsgMessage(String.format("%s has joined", playerName), "");
        AddMessage(game, mess);

        SendMessagesToClients(game.getPlayers(), MAIN);
        return game;
    }

    @ApiMethod(name = "LeaveGame")
    public Game LeaveGame(@Named("GameName") String gameName, @Named("PlayerId") String
            playerId) {
        DeleteOldGames();
        Game game = SearchForGame(gameName);
        if (game == null) {
            game = new Game(gameName);
            game.setResult("Game '" + gameName + "' not found");
            return game;
        }

        SetGameHealthy(game);

        Player player = SearchForPlayer(game, playerId);
        if (player == null) {
            // Player not in game, ignore
            return game;
        }

        if (game.getGameStatus() == Game.GameStatus.Running || game.getGameStatus() == Game.GameStatus.WaitingForScore) {
            game.setResult("You cannot leave a running game, wait for the game to Finish");
            return game;
        }

        String playerName = player.getName();
        game.getPlayers().remove(player);

        TsgMessage mess = new TsgMessage(String.format("%s has left", playerName), "");
        AddMessage(game, mess);

        SendMessagesToClients(game.getPlayers(), MAIN);
        return game;
    }

    @ApiMethod(name = "Chat")
    public Game Chat(@Named("GameName") String gameName, @Named("playerID") String
            playerId, @Named("title") String title, @Named("ChatTxt") String chatTxt) {
        DeleteOldGames();
        Game game = SearchForGame(gameName);
        if (game == null) {
            game = new Game(gameName);
            game.setResult("Game '" + gameName + "' not found");
            return game;
        }

        SetGameHealthy(game);

        Player p = SearchForPlayer(game, playerId);
        if (p == null) {
            game.setResult("You cannot chat in a game when you're not joined");
            return game;
        }

        AddMessage(game, new TsgMessage(title, chatTxt));
        SendMessagesToClients(game.getPlayers(), MAIN);
        return game;
    }

    @ApiMethod(name = "GetGameStatus")
    public Game GetGameStatus(@Named("GameName") String gameName) {

        Game game = SearchForGame(gameName);
        if (game == null) {
            // Unexpected situation, game not found
            game = new Game(gameName);
            game.setResult("Game '" + gameName + "' not found");
            return game;
        }

        SetGameHealthy(game);
        return game;
    }

    @ApiMethod(name = "GetGameList")
    public GameList GetGameList() {
        DeleteOldGames();
        ArrayList<String> gnl = new ArrayList<>();

        for (Game g : CurrentGames) gnl.add(g.getName());

        GameList gl = new GameList();
        gl.setGameNames(gnl);
        return gl;
    }

    @ApiMethod(name = "GetLocationList")
    public LocationList GetLocationList() {
        DeleteOldGames();
        ArrayList<String> lnl = new ArrayList<>();

        lnl.addAll(LocationHelper.getLocationList());

        LocationList ll = new LocationList();
        ll.setLocationNames(lnl);
        return ll;
    }

    @ApiMethod(name = "GetGameListExtra")
    public GameListExtra GetGameListExtra() {
        DeleteOldGames();
        ArrayList<GameInfo> gnl = new ArrayList<>();

        for (Game g : CurrentGames) {
            gnl.add(new GameInfo(g.getName(), g.getGameStatus(), g.getCountry(), g.getPlayers().size()));
        }

        GameListExtra gl = new GameListExtra();
        gl.setGames(gnl);
        return gl;
    }

    @ApiMethod(name = "GetVersion")
    public TsgVersion GetVersion() {

        TsgVersion v = new TsgVersion();
        v.setVersion("V6");
        return v;
    }

    @ApiMethod(name = "AddAnswer")
    public Game AddAnswer(@Named("GameName") String gameName, @Named("PlayerId") String
            playerId, @Named("PlayerAnswer") String answer) {

        Game game = SearchForGame(gameName);
        if (game == null) {
            // Unexpected situation, game not found
            game = new Game(gameName);
            game.setResult("Game '" + gameName + "' not found");
            return game;
        }

        SetGameHealthy(game);

        if (game.getGameStatus() != Game.GameStatus.WaitingForScore) {
            game.setResult("Answer not accepted, game is finished");
            return game;
        }

        Player p = SearchForPlayer(game, playerId);
        if (p == null) {
            game.setResult("Cannot answer, player not not joined");
            return game;
        }

        p.setAnswer(answer);
        Boolean allAnswersAreIn = AllPlayersAnswered(game);
        if (allAnswersAreIn) ProcessResults(game);
        else SendMessagesToClients(game.getPlayers(), MAIN);

        return game;
    }

    @ApiMethod(name = "ProcessResults")
    public Game ProcessResults(@Named("GameName") String gameName) {
        Game game = SearchForGame(gameName);
        if (game == null) {
            // Unexpected situation, game not found
            game = new Game(gameName);
            game.setResult("Game '" + gameName + "' not found");
            return game;
        }

        SetGameHealthy(game);
        ProcessResults(game);
        return game;
    }
}