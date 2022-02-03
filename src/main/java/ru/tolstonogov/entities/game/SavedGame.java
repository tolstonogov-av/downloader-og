package ru.tolstonogov.entities.game;

import java.util.Objects;

public class SavedGame {

    private final int linkId;

    private final String name;

    private final int released;

    public SavedGame(int linkId, String name, int released) {
        this.linkId = linkId;
        this.name = name;
        this.released = released;
    }

    public int getLinkId() {
        return linkId;
    }

    public String getName() {
        return name;
    }

    public int getReleased() {
        return released;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedGame savedGame = (SavedGame) o;
        return linkId == savedGame.linkId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkId);
    }
}
