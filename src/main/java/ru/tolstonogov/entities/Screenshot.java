package ru.tolstonogov.entities;

import java.util.Objects;

public class Screenshot implements Filable {
    private final int nn;

    private final String provided;

    private final String description;

    private final String link;

    private long size;

    private String cause_load;

    private String cause_unload;

    public Screenshot(int nn, String link, String provided, String description) {
        this.nn = nn;
        this.link = link;
        this.provided = provided;
        this.description = description;
    }

    public int getNn() {
        return nn;
    }

    public String getProvided() {
        return provided;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String getName() {
        return link.substring(link.lastIndexOf('/') + 1);
    }

    public String getCause_load() {
        return cause_load;
    }

    public void setCause_load(String cause_load) {
        this.cause_load = cause_load;
    }

    public String getCause_unload() {
        return cause_unload;
    }

    public void setCause_unload(String cause_unload) {
        this.cause_unload = cause_unload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Screenshot that = (Screenshot) o;
        return link.equals(that.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }
}
