package ru.tolstonogov;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.tolstonogov.entities.Screenshot;
import ru.tolstonogov.entities.file.FileGroupProperties;
import ru.tolstonogov.entities.file.FileProperty;
import ru.tolstonogov.entities.file.FileType;
import ru.tolstonogov.entities.file.GameFl;
import ru.tolstonogov.entities.game.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class DataBaseGames {

    private static final String DELIMITER_IN_GAME_RESOURCES = "\t";

    private static final Logger LOG = LogManager.getLogger(DataBaseGames.class.getName());

    private final String properties;

    private Connection connection;

    private final String gamesWastedResource;

    private final String gamesSavedResource;

    private final String gamesDocumentedResource;

    public DataBaseGames(String properties, String gamesWastedResource, String gamesSavedResource, String gamesDocumentedResource) {
        this.properties = properties;
        this.gamesWastedResource = gamesWastedResource;
        this.gamesSavedResource = gamesSavedResource;
        this.gamesDocumentedResource = gamesDocumentedResource;
        this.init();
    }

    private void init() {
        try (InputStream in = ParserGames.class.getClassLoader().getResourceAsStream(properties)) {
            Properties config = new Properties();
            if (in != null) {
                config.load(in);
            }
            Class.forName(config.getProperty("jdbc.driver"));
            connection = DriverManager.getConnection(
                    config.getProperty("jdbc.url"),
                    config.getProperty("jdbc.username"),
                    config.getProperty("jdbc.password")
            );
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        dropTables();
        createTables();
        fillGamesWasted(this.gamesWastedResource);
        fillGamesSaved(this.gamesSavedResource);
        fillGamesDocumented(this.gamesDocumentedResource);
    }

    private void fillGamesWasted(String gamesWastedResource) {
        try (InputStream in = DataBaseGames.class.getClassLoader().getResourceAsStream(gamesWastedResource)) {
            if (in != null) {
                Scanner scanWasted = new Scanner(in);
                String[] wasteGameLine;
                while (scanWasted.hasNextLine()) {
                    wasteGameLine = scanWasted.nextLine().split(DELIMITER_IN_GAME_RESOURCES);
                    if (wasteGameLine.length != 3) {
                        continue;
                    }
                    addWastedGame(new WastedGame(
                            Integer.parseInt(wasteGameLine[0]),
                            wasteGameLine[1],
                            Integer.parseInt(wasteGameLine[2])
                    ));
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    private void fillGamesSaved(String gamesSavedResource) {
        try (InputStream in = DataBaseGames.class.getClassLoader().getResourceAsStream(gamesSavedResource)) {
            if (in != null) {
                Scanner scanSaved = new Scanner(in);
                String[] saveGameLine;
                while (scanSaved.hasNextLine()) {
                    saveGameLine = scanSaved.nextLine().split(DELIMITER_IN_GAME_RESOURCES);
                    if (saveGameLine.length != 3) {
                        continue;
                    }
                    addSavedGame(new SavedGame(
                            Integer.parseInt(saveGameLine[0]),
                            saveGameLine[1],
                            Integer.parseInt(saveGameLine[2])
                    ));
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    private void fillGamesDocumented(String gamesDocumentedResource) {
        try (InputStream in = DataBaseGames.class.getClassLoader().getResourceAsStream(gamesDocumentedResource)) {
            if (in != null) {
                Scanner scanDocumented = new Scanner(in);
                String[] docsGameLine;
                while (scanDocumented.hasNextLine()) {
                    docsGameLine = scanDocumented.nextLine().split(DELIMITER_IN_GAME_RESOURCES);
                    if (docsGameLine.length != 3) {
                        continue;
                    }
                    addDocumentedGame(new DocumentedGame(
                            Integer.parseInt(docsGameLine[0]),
                            docsGameLine[1],
                            Integer.parseInt(docsGameLine[2])
                    ));
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    private void dropTables() {
        try (PreparedStatement psGamesAltNames = connection.prepareStatement("DROP TABLE IF EXISTS games_alt_names");
             PreparedStatement psGamesDevelopers = connection.prepareStatement("DROP TABLE IF EXISTS games_developers");
             PreparedStatement psGamesPublishers = connection.prepareStatement("DROP TABLE IF EXISTS games_publishers");
             PreparedStatement psGamesProperties = connection.prepareStatement("DROP TABLE IF EXISTS games_properties");
             PreparedStatement psGamesScreenshots = connection.prepareStatement("DROP TABLE IF EXISTS games_screenshots");
             PreparedStatement psFilesProperties = connection.prepareStatement("DROP TABLE IF EXISTS files_properties");
             PreparedStatement psGamesFiles = connection.prepareStatement("DROP TABLE IF EXISTS games_files");
             PreparedStatement psTypesFiles = connection.prepareStatement("DROP TABLE IF EXISTS types_files");
             PreparedStatement psGames = connection.prepareStatement("DROP TABLE IF EXISTS games");
             PreparedStatement psGamesWasted = connection.prepareStatement("DROP TABLE IF EXISTS games_wasted");
             PreparedStatement psGamesSaved = connection.prepareStatement("DROP TABLE IF EXISTS games_saved");
             PreparedStatement psGamesDocumented = connection.prepareStatement("DROP TABLE IF EXISTS games_documented");
             PreparedStatement psGenres = connection.prepareStatement("DROP TABLE IF EXISTS genres");
             PreparedStatement psCompanies = connection.prepareStatement("DROP TABLE IF EXISTS companies");
             PreparedStatement psPlatforms = connection.prepareStatement("DROP TABLE IF EXISTS platforms");
             PreparedStatement psPropertiesFiles = connection.prepareStatement("DROP TABLE IF EXISTS properties_files");
             PreparedStatement psGroupsPropertiesFiles = connection.prepareStatement("DROP TABLE IF EXISTS groups_properties_files");
             PreparedStatement psPropertiesGames = connection.prepareStatement("DROP TABLE IF EXISTS properties_games");
             PreparedStatement psGroupsPropertiesGames = connection.prepareStatement("DROP TABLE IF EXISTS groups_properties_games")) {
            psGamesAltNames.executeUpdate();
            psGamesDevelopers.executeUpdate();
            psGamesPublishers.executeUpdate();
            psGamesProperties.executeUpdate();
            psGamesScreenshots.executeUpdate();
            psFilesProperties.executeUpdate();
            psGamesFiles.executeUpdate();
            psTypesFiles.executeUpdate();
            psGames.executeUpdate();
            psGamesWasted.executeUpdate();
            psGamesSaved.executeUpdate();
            psGamesDocumented.executeUpdate();
            psGenres.executeUpdate();
            psCompanies.executeUpdate();
            psPlatforms.executeUpdate();
            psPropertiesFiles.executeUpdate();
            psGroupsPropertiesFiles.executeUpdate();
            psPropertiesGames.executeUpdate();
            psGroupsPropertiesGames.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    private void createTables() {
        try (PreparedStatement psGenres = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS genres (%s, %s)",
                "id SERIAL PRIMARY KEY",
                "name varchar(200)"));
             PreparedStatement psPlatforms = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS platforms (%s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "name varchar(200)"));
             PreparedStatement psGroupsPropertiesGames = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS groups_properties_games (%s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "name varchar(200)"));
             PreparedStatement psPropertiesGames = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS properties_games (%s, %s, %s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "id_group int REFERENCES groups_properties_games(id)",
                     "name_property varchar(200)",
                     "description varchar(1000)"));
             PreparedStatement psGames = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS games (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "name varchar(200)",
                     "id_genre int REFERENCES genres(id)",
                     "released int",
                     "id_platform int REFERENCES platforms(id)",
                     "favorites int",
                     "completions int",
                     "bookmarks int",
                     "review text",
                     "link_id int",
                     "cause_load varchar(200)",
                     "wasted boolean",
                     "saved boolean",
                     "documented boolean"));
             PreparedStatement psGamesWasted = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS games_wasted (%s, %s, %s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "link_id int",
                     "name varchar(200)",
                     "released int"));
             PreparedStatement psGamesSaved = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS games_saved (%s, %s, %s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "link_id int",
                     "name varchar(200)",
                     "released int"));
             PreparedStatement psGamesDocumented = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS games_documented (%s, %s, %s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "link_id int",
                     "name varchar(200)",
                     "released int"));
             PreparedStatement psGamesAltNames = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS games_alt_names (%s, %s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "id_game int REFERENCES games(id)",
                     "name varchar(200)"));
             PreparedStatement psCompanies = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS companies (%s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "name varchar(200)"));
             PreparedStatement psGamesDevelopers = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS games_developers (%s, %s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "id_game int REFERENCES games(id)",
                     "id_company int REFERENCES companies(id)"));
             PreparedStatement psGamesPublishers = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS games_publishers (%s, %s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "id_game int REFERENCES games(id)",
                     "id_company int REFERENCES companies(id)"));
             PreparedStatement psTypesFiles = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS types_files (%s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "name varchar(200)"));
             PreparedStatement psGroupsPropertiesFiles = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS groups_properties_files (%s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "name varchar(200)"));
             PreparedStatement psPropertiesFiles = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS properties_files (%s, %s, %s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "id_group int REFERENCES groups_properties_files(id)",
                     "name_property varchar(200)",
                     "description varchar(1000)"));
             PreparedStatement psGamesFiles = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS games_files (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "id_game int REFERENCES games(id)",
                     "id_type int REFERENCES types_files(id)",
                     "approx_size bigint",
                     "date date",
                     "description text",
                     "name varchar(200)",
                     "size bigint",
                     "provided varchar(200)",
                     "link varchar(200)",
                     "cause_load varchar(200)",
                     "cause_unload varchar(200)"));
             PreparedStatement psFilesProperties = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS files_properties (%s, %s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "id_file int REFERENCES games_files(id)",
                     "id_property int REFERENCES properties_files(id)"));
             PreparedStatement psGamesProperties = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS games_properties (%s, %s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "id_game int REFERENCES games(id)",
                     "id_property int REFERENCES properties_games(id)"));
             PreparedStatement psGamesScreenshots = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS games_screenshots (%s, %s, %s, %s, %s, %s, %s, %s, %s)",
                     "id SERIAL PRIMARY KEY",
                     "id_game int REFERENCES games(id)",
                     "name varchar(200)",
                     "nn int",
                     "provided varchar(200)",
                     "description text",
                     "size bigint",
                     "cause_load varchar(200)",
                     "cause_unload varchar(200)"))) {
            psGenres.executeUpdate();
            psPlatforms.executeUpdate();
            psGroupsPropertiesGames.executeUpdate();
            psPropertiesGames.executeUpdate();
            psCompanies.executeUpdate();
            psGames.executeUpdate();
            psGamesWasted.executeUpdate();
            psGamesSaved.executeUpdate();
            psGamesDocumented.executeUpdate();
            psGamesAltNames.executeUpdate();
            psGamesDevelopers.executeUpdate();
            psGamesPublishers.executeUpdate();
            psTypesFiles.executeUpdate();
            psGroupsPropertiesFiles.executeUpdate();
            psPropertiesFiles.executeUpdate();
            psGamesFiles.executeUpdate();
            psFilesProperties.executeUpdate();
            psGamesProperties.executeUpdate();
            psGamesScreenshots.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    public void addWastedGame(WastedGame wastedGame) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO games_wasted (link_id, name, released) VALUES (?, ?, ?)")) {
            ps.setInt(1, wastedGame.getLinkId());
            ps.setString(2, wastedGame.getName());
            ps.setInt(3, wastedGame.getReleased());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    public void addSavedGame(SavedGame savedGame) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO games_saved (link_id, name, released) VALUES (?, ?, ?)")) {
            ps.setInt(1, savedGame.getLinkId());
            ps.setString(2, savedGame.getName());
            ps.setInt(3, savedGame.getReleased());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    public void addDocumentedGame(DocumentedGame documentedGame) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO games_documented (link_id, name, released) VALUES (?, ?, ?)")) {
            ps.setInt(1, documentedGame.getLinkId());
            ps.setString(2, documentedGame.getName());
            ps.setInt(3, documentedGame.getReleased());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    public boolean checkWasted(int idGame) {
        boolean result = false;
        //TODO: maybe without id
        try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM games_wasted WHERE link_id=?")) {
            ps.setInt(1, idGame);
            ResultSet rs = ps.executeQuery();
            result = rs.next();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public boolean checkSaved(int idGame) {
        boolean result = false;
        //TODO: maybe without id
        try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM games_saved WHERE link_id=?")) {
            ps.setInt(1, idGame);
            ResultSet rs = ps.executeQuery();
            result = rs.next();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public boolean checkDocumented(int idGame) {
        boolean result = false;
        //TODO: maybe without id
        try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM games_documented WHERE link_id=?")) {
            ps.setInt(1, idGame);
            ResultSet rs = ps.executeQuery();
            result = rs.next();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public int addGame(Game game, int idGenre, int idPlatform) {
        int result = 0;
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO games (name, id_genre, released, id_platform, favorites, completions, bookmarks, review, link_id, cause_load, wasted, saved, documented) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
             PreparedStatement psId = connection.prepareStatement("SELECT id FROM games WHERE link_id=?")) {
            ps.setString(1, game.getName());
            ps.setInt(2, idGenre);
            ps.setInt(3, game.getReleased());
            ps.setInt(4, idPlatform);
            ps.setInt(5, game.getFavorites());
            ps.setInt(6, game.getCompletions());
            ps.setInt(7, game.getBookmarks());
            ps.setString(8, game.getReview());
            ps.setInt(9, game.getLinkId());
            ps.setString(10, game.getCause_load());
            ps.setBoolean(11, game.isWasted());
            ps.setBoolean(12, game.isSaved());
            ps.setBoolean(13, game.isDocumented());
            ps.executeUpdate();
            psId.setInt(1, game.getLinkId());
            ResultSet rs = psId.executeQuery();
            if (rs.next()) {
                result = rs.getInt("id");
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public int addGenre(Genre genre) {
        int result = 0;
        try (PreparedStatement psSelect = connection.prepareStatement(
                "SELECT id FROM genres WHERE name=?");
             PreparedStatement psInsert = connection.prepareStatement(
                     "INSERT INTO genres (name) VALUES (?)")) {
            psSelect.setString(1, genre.getName());
            ResultSet rs = psSelect.executeQuery();
            if (rs.next()) {
                result = rs.getInt("id");
            } else {
                psInsert.setString(1, genre.getName());
                psInsert.executeUpdate();
                rs = psSelect.executeQuery();
                if (rs.next()) {
                    result = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public void addGameScreenshot(Screenshot screenshot, int idGame) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO games_screenshots (id_game, name, nn, provided, description, size, cause_load, cause_unload) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, idGame);
            ps.setString(2, screenshot.getName());
            ps.setInt(3, screenshot.getNn());
            ps.setString(4, screenshot.getProvided());
            ps.setString(5, screenshot.getDescription());
            ps.setLong(6, screenshot.getSize());
            ps.setString(7, screenshot.getCause_load());
            ps.setString(8, screenshot.getCause_unload());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    public int addPlatform(Platform platform) {
        int result = 0;
        try (PreparedStatement psSelect = connection.prepareStatement(
                "SELECT id FROM platforms WHERE name=?");
             PreparedStatement psInsert = connection.prepareStatement(
                     "INSERT INTO platforms (name) VALUES (?)")) {
            psSelect.setString(1, platform.getName());
            ResultSet rs = psSelect.executeQuery();
            if (rs.next()) {
                result = rs.getInt("id");
            } else {
                psInsert.setString(1, platform.getName());
                psInsert.executeUpdate();
                rs = psSelect.executeQuery();
                if (rs.next()) {
                    result = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public void addGameDeveloper(Company company, int idGame) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO games_developers (id_game, id_company) VALUES (?, ?)")) {
            ps.setInt(1, idGame);
            ps.setInt(2, addCompany(company));
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    public void addGamePublisher(Company company, int idGame) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO games_publishers (id_game, id_company) VALUES (?, ?)")) {
            ps.setInt(1, idGame);
            ps.setInt(2, addCompany(company));
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    public void addGameProperty(GameProperty property, int idGame, int idGameGroupProperties) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO games_properties (id_game, id_property) VALUES (?, ?)")) {
            ps.setInt(1, idGame);
            ps.setInt(2, addPropertyOfGame(property, idGameGroupProperties));
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    public void addFileProperty(FileProperty property, int idFile, int idFileGroupProperties) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO files_properties (id_file, id_property) VALUES (?, ?)")) {
            ps.setInt(1, idFile);
            ps.setInt(2, addPropertyOfFile(property, idFileGroupProperties));
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    public int addGroupOfPropertyOfGame(GameGroupProperties group) {
        int result = 0;
        try (PreparedStatement psSelect = connection.prepareStatement(
                "SELECT id FROM groups_properties_games WHERE name=?");
             PreparedStatement psInsert = connection.prepareStatement(
                     "INSERT INTO groups_properties_games (name) VALUES (?)")) {
            psSelect.setString(1, group.getName());
            ResultSet rs = psSelect.executeQuery();
            if (rs.next()) {
                result = rs.getInt("id");
            } else {
                psInsert.setString(1, group.getName());
                psInsert.executeUpdate();
                rs = psSelect.executeQuery();
                if (rs.next()) {
                    result = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public int addGroupOfPropertyOfFile(FileGroupProperties group) {
        int result = 0;
        try (PreparedStatement psSelect = connection.prepareStatement(
                "SELECT id FROM groups_properties_files WHERE name=?");
             PreparedStatement psInsert = connection.prepareStatement(
                     "INSERT INTO groups_properties_files (name) VALUES (?)")) {
            psSelect.setString(1, group.getName());
            ResultSet rs = psSelect.executeQuery();
            if (rs.next()) {
                result = rs.getInt("id");
            } else {
                psInsert.setString(1, group.getName());
                psInsert.executeUpdate();
                rs = psSelect.executeQuery();
                if (rs.next()) {
                    result = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    private int addPropertyOfGame(GameProperty property, int idGameGroupProperties) {
        int result = 0;
        try (PreparedStatement psSelect = connection.prepareStatement(
                "SELECT id FROM properties_games WHERE id_group=? and name_property=?");
             PreparedStatement psInsert = connection.prepareStatement(
                     "INSERT INTO properties_games (id_group, name_property, description) VALUES (?, ?, ?)")) {
            psSelect.setInt(1, idGameGroupProperties);
            psSelect.setString(2, property.getPropertyName());
            ResultSet rs = psSelect.executeQuery();
            if (rs.next()) {
                result = rs.getInt("id");
            } else {
                psInsert.setInt(1, idGameGroupProperties);
                psInsert.setString(2, property.getPropertyName());
                psInsert.setString(3, property.getDescription());
                psInsert.executeUpdate();
                rs = psSelect.executeQuery();
                if (rs.next()) {
                    result = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    private int addPropertyOfFile(FileProperty property, int idFileGroupProperties) {
        int result = 0;
        try (PreparedStatement psSelect = connection.prepareStatement(
                "SELECT id FROM properties_files WHERE id_group=? and name_property=?");
             PreparedStatement psInsert = connection.prepareStatement(
                     "INSERT INTO properties_files (id_group, name_property, description) VALUES (?, ?, ?)")) {
            psSelect.setInt(1, idFileGroupProperties);
            psSelect.setString(2, property.getPropertyName());
            ResultSet rs = psSelect.executeQuery();
            if (rs.next()) {
                result = rs.getInt("id");
            } else {
                psInsert.setInt(1, idFileGroupProperties);
                psInsert.setString(2, property.getPropertyName());
                psInsert.setString(3, property.getDescription());
                psInsert.executeUpdate();
                rs = psSelect.executeQuery();
                if (rs.next()) {
                    result = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    private int addCompany(Company company) {
        int result = 0;
        try (PreparedStatement psSelect = connection.prepareStatement(
                "SELECT id FROM companies WHERE name=?");
             PreparedStatement psInsert = connection.prepareStatement(
                     "INSERT INTO companies (name) VALUES (?)")) {
            psSelect.setString(1, company.getName());
            ResultSet rs = psSelect.executeQuery();
            if (rs.next()) {
                result = rs.getInt("id");
            } else {
                psInsert.setString(1, company.getName());
                psInsert.executeUpdate();
                rs = psSelect.executeQuery();
                if (rs.next()) {
                    result = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public int addType(FileType type) {
        int result = 0;
        try (PreparedStatement psSelect = connection.prepareStatement(
                "SELECT id FROM types_files WHERE name=?");
             PreparedStatement psInsert = connection.prepareStatement(
                     "INSERT INTO types_files (name) VALUES (?)")) {
            psSelect.setString(1, type.getName());
            ResultSet rs = psSelect.executeQuery();
            if (rs.next()) {
                result = rs.getInt("id");
            } else {
                psInsert.setString(1, type.getName());
                psInsert.executeUpdate();
                rs = psSelect.executeQuery();
                if (rs.next()) {
                    result = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public void addAltName(AltName altName, int idGame) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO games_alt_names (id_game, name) VALUES (?, ?)")) {
            ps.setInt(1, idGame);
            ps.setString(2, altName.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }

    public int addFile(GameFl gameFl, int idGame, int idType) {
        int result = 0;
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO games_files (id_game, id_type, approx_size, date, description, name, size, provided, link, cause_load, cause_unload) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
             PreparedStatement psId = connection.prepareStatement("SELECT id FROM games_files WHERE link=?")) {
            ps.setInt(1, idGame);
            ps.setInt(2, idType);
            ps.setLong(3, gameFl.getApproxSize());
            ps.setDate(4, new Date(gameFl.getDate().getTimeInMillis()));
            ps.setString(5, gameFl.getDescription());
            ps.setString(6, gameFl.getName());
            ps.setLong(7, gameFl.getSize());
            ps.setString(8, gameFl.getProvided());
            ps.setString(9, gameFl.getLink());
            ps.setString(10, gameFl.getCause_load());
            ps.setString(11, gameFl.getCause_unload());
            ps.executeUpdate();
            psId.setString(1, gameFl.getLink());
            ResultSet rs = psId.executeQuery();
            if (rs.next()) {
                result = rs.getInt("id");
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }
}
