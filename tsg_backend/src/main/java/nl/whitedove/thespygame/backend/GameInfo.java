package nl.whitedove.thespygame.backend;

public class GameInfo {

    private String Name;
    private Game.GameStatus GameStatus;
    private String Country;
    private int NrOfPlayers;

    public GameInfo(String name, Game.GameStatus gameStatus, String country, int nrOfPlayers) {
        this.setName(name);
        this.setGameStatus(gameStatus);
        this.setCountry(country);
        this.setNrOfPlayers(nrOfPlayers);
    }

    public String getName() {
        return Name;
    }

    private void setName(String name) {
        Name = name;
    }

    public Game.GameStatus getGameStatus() {
        return GameStatus;
    }

    private void setGameStatus(Game.GameStatus gameStatus) {
        GameStatus = gameStatus;
    }

    public int getNrOfPlayers() {
        return NrOfPlayers;
    }

    private void setNrOfPlayers(int nrOfPlayers) {
        NrOfPlayers = nrOfPlayers;
    }

    public String getCountry() {
        return Country;
    }

    private void setCountry(String country) {
        Country = country;
    }
}
