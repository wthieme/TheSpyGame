package nl.whitedove.thespygame;

import java.util.Comparator;

import nl.whitedove.thespygame.backend.tsgApi.model.Player;

class PlayerComparator implements Comparator<Player> {
    public int compare(Player left, Player right) {
        int verschil = right.getPoints() - left.getPoints();
        return verschil == 0 ? left.getName().compareToIgnoreCase(right.getName()) : verschil;
    }
}
