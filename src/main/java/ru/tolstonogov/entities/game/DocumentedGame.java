package ru.tolstonogov.entities.game;

import java.util.Objects;

public class DocumentedGame {

    private final int linkId;

    private final String name;

    private final int released;

    public DocumentedGame(int linkId, String name, int released) {
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
        DocumentedGame documentedGame = (DocumentedGame) o;
        return linkId == documentedGame.linkId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkId);
    }
}
