package ru.tolstonogov.entities.game;

import ru.tolstonogov.entities.Screenshot;
import ru.tolstonogov.entities.file.GameFl;

import java.util.List;
import java.util.Objects;

public class Game {
    private String name;

    private List<AltName> altNames;

    private Genre genre;

    private List<Company> developers;

    private List<Company> publishers;

    private int released;

    private Platform platform;

    private int favorites;

    private int completions;

    private int bookmarks;

//    TODO: temporarily, because this review will be in forum "Games on site"
    private String review;

    private List<GameFl> files;

    private List<GameProperty> properties;

    private List<Screenshot> screenshots;

    private int linkId;

    private String cause_load;

    private final boolean wasted;

    private final boolean saved;

    private final boolean documented;

    public Game(String name, List<AltName> altNames, Genre genre, List<Company> developers, List<Company> publishers, int released, Platform platform, int favorites, int completions, int bookmarks, String review, List<GameFl> files, List<GameProperty> properties, List<Screenshot> screenshots, int linkId, boolean wasted, boolean saved, boolean documented) {
        this.name = name;
        this.altNames = altNames;
        this.genre = genre;
        this.developers = developers;
        this.publishers = publishers;
        this.released = released;
        this.platform = platform;
        this.favorites = favorites;
        this.completions = completions;
        this.bookmarks = bookmarks;
        this.review = review;
        this.files = files;
        this.properties = properties;
        this.screenshots = screenshots;
        this.linkId = linkId;
        this.wasted = wasted;
        this.saved = saved;
        this.documented = documented;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AltName> getAltNames() {
        return altNames;
    }

    public void setAltNames(List<AltName> altNames) {
        this.altNames = altNames;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public List<Company> getDevelopers() {
        return developers;
    }

    public void setDevelopers(List<Company> developers) {
        this.developers = developers;
    }

    public List<Company> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<Company> publishers) {
        this.publishers = publishers;
    }

    public int getReleased() {
        return released;
    }

    public void setReleased(int released) {
        this.released = released;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public int getFavorites() {
        return favorites;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }

    public int getCompletions() {
        return completions;
    }

    public void setCompletions(int completions) {
        this.completions = completions;
    }

    public int getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(int bookmarks) {
        this.bookmarks = bookmarks;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public List<GameFl> getFiles() {
        return files;
    }

    public void setFiles(List<GameFl> files) {
        this.files = files;
    }

    public List<GameProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<GameProperty> properties) {
        this.properties = properties;
    }

    public List<Screenshot> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<Screenshot> screenshots) {
        this.screenshots = screenshots;
    }

    public int getLinkId() {
        return linkId;
    }

    public void setLinkId(int linkId) {
        this.linkId = linkId;
    }

    public String getCause_load() {
        return cause_load;
    }

    public void setCause_load(String cause_load) {
        this.cause_load = cause_load;
    }

    public boolean isWasted() {
        return wasted;
    }

    public boolean isSaved() {
        return saved;
    }

    public boolean isDocumented() {
        return documented;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return linkId == game.linkId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkId);
    }
}
