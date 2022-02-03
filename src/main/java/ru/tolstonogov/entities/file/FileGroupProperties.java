package ru.tolstonogov.entities.file;

import java.util.Objects;

public class FileGroupProperties {
    private final String name;

    public FileGroupProperties(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileGroupProperties that = (FileGroupProperties) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
