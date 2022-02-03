package ru.tolstonogov.entities.game;

import java.util.Objects;

public class GameProperty {
    private GameGroupProperties group;

    private String propertyName;

    private String description;

    public GameProperty(GameGroupProperties group, String propertyName, String description) {
        this.group = group;
        this.propertyName = propertyName;
        this.description = description;
    }

    public GameGroupProperties getGroup() {
        return group;
    }

    public void setGroup(GameGroupProperties group) {
        this.group = group;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameProperty that = (GameProperty) o;
        return group.equals(that.group) &&
                propertyName.equals(that.propertyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, propertyName);
    }
}
