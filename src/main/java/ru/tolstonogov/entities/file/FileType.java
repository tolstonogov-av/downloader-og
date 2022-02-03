package ru.tolstonogov.entities.file;

import java.util.Objects;

public class FileType {
    private String name;

    public FileType(String name) {
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
        FileType fileType = (FileType) o;
        return name.equals(fileType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
