package ru.tolstonogov.entities.game;

import java.util.Objects;

public class Platform {
    private String name;

    public Platform(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Platform platform = (Platform) o;
        return name.equals(platform.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
