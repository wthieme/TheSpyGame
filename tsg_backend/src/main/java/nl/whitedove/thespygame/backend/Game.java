package nl.whitedove.thespygame.backend;

import org.joda.time.DateTime;

import java.util.ArrayList;

public class Game {

    public DateTime getFinishTime() {
        return FinishTime;
    }

    public void setFinishTime(DateTime finishTime) {
        FinishTime = finishTime;
    }

    public enum GameStatus {UnKnown, Created, Running, WaitingForScore, Finished}

    private String Name;
    private ArrayList<Player> Players;
    private DateTime StartTime;
    private DateTime FinishTime;
    private GameStatus GameStatus;
    private String Result;
    private DateTime LastUsed;
    private String Location;
    private ArrayList<Player> WaitingPlayers;
    private ArrayList<TsgMessage> Messages;
    private String Country;

    public Game(String name) {
        this.setPlayers(new ArrayList<Player>());
        this.GameStatus = this.GameStatus.UnKnown;
        this.Name = name;
        this.LastUsed = DateTime.now();
        this.Result = "OK";
        this.setWaitingPlayers(new ArrayList<Player>());
        this.setMessages(new ArrayList<TsgMessage>());
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public ArrayList<Player> getPlayers() {
        return Players;
    }

    public void setPlayers(ArrayList<Player> players) {
        Players = players;
    }

    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }

    public DateTime getStartTime() {
        return StartTime;
    }

    public void setStartTime(DateTime startTime) {
        StartTime = startTime;
    }

    public DateTime getLastUsed() {
        return LastUsed;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public void setLastUsed(DateTime lastUsed) {
        LastUsed = lastUsed;
    }

    public GameStatus getGameStatus() {
        return GameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        GameStatus = gameStatus;
    }


    public ArrayList<Player> getWaitingPlayers() {
        return WaitingPlayers;
    }

    public void setWaitingPlayers(ArrayList<Player> waitingPlayers) {
        WaitingPlayers = waitingPlayers;
    }

    public ArrayList<TsgMessage> getMessages() {
        return Messages;
    }

    public void setMessages(ArrayList<TsgMessage> messages) {
        Messages = messages;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

}
