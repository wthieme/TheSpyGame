package nl.whitedove.thespygame.backend;

/**
 * The object model for the data we are sending through endpoints
 */
public class Player {

    private String Id;
    private String Name;
    private boolean IsSpy;
    private String Role;
    private String Token;
    private int Points;
    private String Answer;
    private boolean IsCorrectAnswer;
    private boolean IsReady;

    public Player(String id, String name, String token) {
        this.setId(id);
        this.setName(name);
        this.setIsSpy(false);
        this.setRole("");
        this.setToken(token);
        this.setPoints(0);
        this.setIsReady(false);
        this.setAnswer("");
        this.setCorrectAnswer(false);
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public boolean getIsSpy() {
        return IsSpy;
    }

    public void setIsSpy(boolean isSpy) {
        IsSpy = isSpy;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }

    public int getPoints() {
        return Points;
    }

    public void setPoints(int points) {
        Points = points;
    }

    public String getAnswer() {
        return Answer;
    }

    public void setAnswer(String answer) {
        Answer = answer;
    }

    public boolean getIsCorrectAnswer() {
        return IsCorrectAnswer;
    }

    public void setCorrectAnswer(boolean isCorrectAnswer) {
        this.IsCorrectAnswer = isCorrectAnswer;
    }

    public boolean getIsReady() {
        return IsReady;
    }

    public void setIsReady(boolean ready) {
        IsReady = ready;
    }
}