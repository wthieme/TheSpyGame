package nl.whitedove.thespygame.backend;

import java.util.ArrayList;

/** The object model for the data we are sending through endpoints */
public class Location {

    private String LocationName;
    private ArrayList<String> Participants;

    public String getLocationName() {
        return LocationName;
    }

    public void setLocationName(String locationName) {
        LocationName = locationName;
    }

    public ArrayList<String> getParticipants() {
        return Participants;
    }

    public void setParticipants(ArrayList<String> participants) {
        Participants = participants;
    }
}