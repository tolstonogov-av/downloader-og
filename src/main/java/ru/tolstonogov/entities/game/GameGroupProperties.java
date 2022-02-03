package ru.tolstonogov.entities.game;

import java.util.Objects;

public class GameGroupProperties {
    private final String name;

    public GameGroupProperties(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameGroupProperties that = (GameGroupProperties) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
