package ru.tolstonogov.entities.file;

import java.util.Objects;

public class Unit {
    private String name;

    public Unit(String name) {
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
        Unit unit = (Unit) o;
        return name.equals(unit.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
