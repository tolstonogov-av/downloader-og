package ru.tolstonogov.entities.file;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

public class GameFl {
    private FileType type;

    private long approxSize;

    private GregorianCalendar date;

    private String description;

    private String name;

    private long size;

    private Unit unit;

    private String provided;

    // TODO: replace with linkId.
    private String link;

    private List<FileProperty> properties;

    private String cause_load;

    private String cause_unload;

    private final boolean copyrightViolation;

    public GameFl(FileType type, long approxSize, GregorianCalendar date, String description, String name, Unit unit, String provided, String link, List<FileProperty> properties, boolean copyrightViolation) {
        this.type = type;
        this.approxSize = approxSize;
        this.date = date;
        this.description = description;
        this.name = name;
        this.unit = unit;
        this.provided = provided;
        this.link = link;
        this.properties = properties;
        this.copyrightViolation = copyrightViolation;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public long getApproxSize() {
        return approxSize;
    }

    public void setApproxSize(long approxSize) {
        this.approxSize = approxSize;
    }

    public GregorianCalendar getDate() {
        return date;
    }

    public void setDate(GregorianCalendar date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        String result;
        if (this.getLink() == null) {
            result = "";
        } else {
            result = "id_"
                    + this.getLink().substring(this.getLink().indexOf('=') + 1, this.getLink().indexOf('&'))
                    + "_"
                    + this.name;
        }
        return result;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getProvided() {
        return provided;
    }

    public void setProvided(String provided) {
        this.provided = provided;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<FileProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<FileProperty> properties) {
        this.properties = properties;
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

    public boolean isCopyrightViolation() {
        return copyrightViolation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameFl gameFl = (GameFl) o;
        return link.equals(gameFl.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }
}
