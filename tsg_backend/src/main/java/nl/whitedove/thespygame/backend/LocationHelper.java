package nl.whitedove.thespygame.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;


public class LocationHelper {

    private static final ArrayList<Location> mLocationList = new ArrayList<>();
    private static final Random rnd = new Random();

    private static Location newLocation(String location, ArrayList<String> participants) {
        Location loc = new Location();
        loc.setLocationName(location);
        loc.setParticipants(participants);
        return loc;
    }

    public static Location GetRandomLocation() {
        MaakLijst();
        int locNr = rnd.nextInt(getLocationList().size());
        return mLocationList.get(locNr);
    }

    private static void MaakLijst() {

        if (mLocationList.size() > 0) {
            return;
        }

        mLocationList.add(newLocation("in a hotel", new ArrayList(Arrays.asList("the director of Finance", "a guest", "a security officer", "a receptionist", "a bartender", "a housekeeper", "a concierge", "a restaurant Manager", "an operations Manager"))));
        mLocationList.add(newLocation("in a school", new ArrayList(Arrays.asList("a music teacher", "a student", "a concierge", "the headmaster", "a math teacher"))));
        mLocationList.add(newLocation("in a casino", new ArrayList(Arrays.asList("a gambler", "a black jack player", "a slot machine junky", "a security camera operator", "a crouper", "a door man", "a car park assistant"))));
        mLocationList.add(newLocation("in a university", new ArrayList(Arrays.asList("a professor", "a student", "a researcher"))));
        mLocationList.add(newLocation("on a cruise ship", new ArrayList(Arrays.asList("the captain", "an entertainer", "a singer", "a sailor", "a cocktail drink mixer"))));
        mLocationList.add(newLocation("in a space station", new ArrayList(Arrays.asList("an astronaut", "a teacher", "a scientist"))));
        mLocationList.add(newLocation("in a hospital", new ArrayList(Arrays.asList("a doctor", "a nurse", "a patient", "a receptionist", "a visitor"))));
        mLocationList.add(newLocation("in the army", new ArrayList(Arrays.asList("a soldier", "a medic", "a tank driver", "a sergeant"))));
        mLocationList.add(newLocation("at home", new ArrayList(Arrays.asList("a mum", "a dad", "a dog", "a child", "the houskeeper", "the gardener", "the pool cleaner", "the cook"))));
        mLocationList.add(newLocation("at Hogwarts", new ArrayList(Arrays.asList("Dumbledore", "Hagrid", "Dobby", "Harry", "Ron", "Hermoine"))));
        mLocationList.add(newLocation("in a graveyard", new ArrayList(Arrays.asList("a grave digger", "a dead body", "a zombie", "a weeping widow", "a priest", "an undertaker"))));
        mLocationList.add(newLocation("in a church", new ArrayList(Arrays.asList("God", "Jesus", "Maria", "the priest", "the organ player"))));
        mLocationList.add(newLocation("in heaven", new ArrayList(Arrays.asList("God", "Petrus", "an angel", "Adam", "Eve"))));
        mLocationList.add(newLocation("in a supermarket", new ArrayList(Arrays.asList("a manager", "a customer", "the butcher", "the guy who is here for the free coffee"))));
        mLocationList.add(newLocation("in a theme park", new ArrayList(Arrays.asList("the ice cream man", "a scared child", "a thrilled teenager", "an amused adult"))));
        mLocationList.add(newLocation("in a left twix factory", new ArrayList(Arrays.asList("a worker", "a supervisor", "the boss", "a coffee girl"))));
        mLocationList.add(newLocation("in a movie studio", new ArrayList(Arrays.asList("an actor", "an actress", "the production assistant", "the sound man", "the camera man", "the director", "the catering guy", "the props girl", "the make-up artist", "the clothing designer", "a stuntman"))));
        mLocationList.add(newLocation("in a swimming pool", new ArrayList(Arrays.asList("a water quality manager", "a visitor", "a diver", "a swimming instructor"))));
        mLocationList.add(newLocation("in a library", new ArrayList(Arrays.asList("a librarian", "a student (not to be disturbed!)", "a reader", "the guy who is here for the free WiFi"))));
        mLocationList.add(newLocation("in a zoo", new ArrayList(Arrays.asList("the monkey feeder", "a visitor", "an aquarium cleaner", "a park assistant"))));
        mLocationList.add(newLocation("at the beach", new ArrayList(Arrays.asList("a wind surfer", "a sun bather", "a swimmer", "a sand castle builder", "a kite surfer", "a baywatcher"))));
    }

    public static ArrayList<String> getLocationList() {
        MaakLijst();
        ArrayList<String> ll = new ArrayList<>();
        for (Location l : mLocationList) {
            String ln = l.getLocationName();
            ln = ln.replace("in a ", "");
            ln = ln.replace("in the ", "");
            ln = ln.replace("in ", "");
            ln = ln.replace("on a ", "");
            ln = ln.replace("at the ", "");
            ln = ln.replace("at ", "");
            ll.add(capitalize(ln));
        }
        Collections.sort(ll, String.CASE_INSENSITIVE_ORDER);
        return ll;
    }

    private static String capitalize(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }
}