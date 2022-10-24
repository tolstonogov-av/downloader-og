package ru.tolstonogov;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.tolstonogov.entities.FileCheck;
import ru.tolstonogov.entities.Screenshot;
import ru.tolstonogov.entities.file.*;
import ru.tolstonogov.entities.game.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;

public class Parser {
    private static final Logger LOG = LogManager.getLogger(Parser.class.getName());
    private static final String NAME_DELIMITER = ", ";
    private static final String SIZE_DELIMITER = " ";
    private static final String GENRE = "genre";
    private static final String DEVELOPER = "developerCompany";
    private static final String PUBLISHER = "publisherCompany";
    private static final String RELEASED = "year";
    private static final String PLATFORM = "platform";
    private static final String FILES_DIRECTORY_NAME = "files";
    private static final String SCREENSHOTS_DIRECTORY_NAME = "screenshots";
    private static final String NOT_EXIST = "not exist";
    private static final String COPYRIGHT_VIOLATION = "copyright violation";
    private static final String ONLY_DOCUMENTS = "only documents";
    private static final String DESIGNATION_UNIT_B = "B";
    private static final String DESIGNATION_UNIT_KB = "KB";
    private static final String DESIGNATION_UNIT_MB = "MB";
    private static final String DESIGNATION_UNIT_GB = "GB";
    private static final int SIZE_B = 1;
    private static final int SIZE_KB = 1024;
    private static final int SIZE_MB = 1024 * 1024;
    private static final int SIZE_GB = 1024 * 1024 * 1024;
    private static final int ERROR_UNIT_B = 1;
    private static final int ERROR_UNIT_KB = 11;
    private static final int ERROR_UNIT_MB = 10486;
    private static final int ERROR_UNIT_GB = 10737419;

    private final String baseUrl;

    private final DataBaseGames dbV;

    public Parser(String baseUrl, DataBaseGames dbV) {
        this.baseUrl = baseUrl;
        this.dbV = dbV;
    }

    public void parse(int rangeBegin, int rangeEnd, File parentDirectory, String nameOg, String nameOgWasted, String nameOgUnrelated, boolean clean, String nameOgCleaned) {
        long startGamesDownload = System.currentTimeMillis();
        long gamesDownloadSize = 0;
        long finishGamesDownload;
        long startFilesDownload;
        long filesDownloadSize;
        long finishFilesDownload;
        float filesDownloadSpeed;
        long startScreenshotsDownload;
        long screenshotsDownloadSize;
        long finishScreenshotsDownload;
        float screenshotsDownloadSpeed;
        float gamesDownloadSpeed;
        File gamesDirectory = null;
        File gameDirectory;
        File gamesWastedDirectory = null;
        File gameWastedDirectory;
        File gamesUnrelatedDirectory = null;
        File gamesCleanedDirectory = null;
        boolean proper = true;
        int page = 0;
        List<String> gamesDirectoryName = new ArrayList<>();
        if (parentDirectory != null) {
            gamesDirectory = new File(parentDirectory, nameOg);
            gamesWastedDirectory = new File(parentDirectory, nameOgWasted);
            gamesUnrelatedDirectory = new File(parentDirectory, nameOgUnrelated);
            gamesCleanedDirectory = new File(parentDirectory, new StringBuilder(nameOgCleaned).append("(").append(rangeBegin).append("-").append(rangeEnd).append(")").toString());
            try {
                if (!gamesDirectory.exists() && !gamesDirectory.mkdir()) {
                    LOG.error(new StringBuilder("Directory ").append(gamesDirectory.getCanonicalPath()).append(" doesn't created."));
                }
                if (!gamesWastedDirectory.exists() && !gamesWastedDirectory.mkdir()) {
                    LOG.error(new StringBuilder("Directory ").append(gamesWastedDirectory.getCanonicalPath()).append(" doesn't created."));
                }
                if (!gamesUnrelatedDirectory.exists() && !gamesUnrelatedDirectory.mkdir()) {
                    LOG.error(new StringBuilder("Directory ").append(gamesUnrelatedDirectory.getCanonicalPath()).append(" doesn't created."));
                }
                if (!gamesCleanedDirectory.exists() && !gamesCleanedDirectory.mkdir()) {
                    LOG.error(new StringBuilder("Directory ").append(gamesCleanedDirectory.getCanonicalPath()).append(" doesn't created."));
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
//        TODO: move outside of the loop variable's declaration.
        while (proper) {
            if (++page < rangeBegin) {
                continue;
            }
            if (page == rangeEnd) {
                proper = false;
            }
            Document doc = getGamePage(page);
            if (doc == null) {
                continue;
            }
            Game game = new Game(
                    getGameName(doc),
                    getGameAltNames(doc),
                    getGameGenre(doc),
                    getGameDevelopers(doc),
                    getGamePublishers(doc),
                    getGameReleased(doc),
                    getGamePlatform(doc),
                    getGameFavorites(doc),
                    getGameCompletions(doc),
                    getGameBookmarks(doc),
                    getGameReview(doc),
                    getGameFiles(page),
                    getGameProperties(doc),
                    getGameScreenshots(page),
                    page,
                    dbV.checkWasted(page),
                    dbV.checkSaved(page),
                    dbV.checkDocumented(page));
            // TODO: https://www.old-games.ru/game/covers/7499.html - the example of good covers
//            try {
//                if (Jsoup.connect(new StringBuilder(this.baseUrl).append("game/covers/")
//                        .append(page).append(".html").toString()).get()
//                        .getElementById("maintable")
//                        .child(0)
//                        .child(0)
//                        .child(1)
//                        .child(3)
//                        .child(0)
//                        .children().size() > 1) {
//                    LOG.info("covers");
//                }
//            } catch (IOException e) {
//                LOG.error(e.getMessage(), e);
//            }
            String gameDirectoryName = game.getName() +
                    " (" +
                    game.getReleased() +
                    ", id_" +
                    page +
                    ")";
            LOG.info("+ " +
                    gameDirectoryName);
            if (parentDirectory != null) {
                // TODO: extra directory -> keep (for review), extra files - to remove (without copyright violation).
                gameDirectory = new File(gamesDirectory, gameDirectoryName
                        .replaceAll("[:/*?\"]", ""));
                startFilesDownload = System.currentTimeMillis();
                if (game.isWasted()) {
                    if (gameDirectory.exists()) {
                        gameWastedDirectory = new File(gamesWastedDirectory, gameDirectoryName
                                .replaceAll("[:/*?\"]", ""));
                        if (gameWastedDirectory.exists()) {
                            delDirectory(gameWastedDirectory);
                        }
                        try {
                            Files.move(gameDirectory.toPath(), gameWastedDirectory.toPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    filesDownloadSize = 0;
                } else {
                    filesDownloadSize = downloadFiles(game, gameDirectory, gamesUnrelatedDirectory, gameDirectoryName);
                }
                gamesDownloadSize += filesDownloadSize;
                finishFilesDownload = System.currentTimeMillis();
                if (finishFilesDownload == startFilesDownload) {
                    filesDownloadSpeed = 0;
                } else {
                    filesDownloadSpeed = ((float) filesDownloadSize / SIZE_MB)
                            / ((float) (finishFilesDownload - startFilesDownload) / 1000);
                }
                if (filesDownloadSpeed != 0) {
                    LOG.info(new StringBuilder("\t")
                            .append("files download speed - ")
                            .append(BigDecimal.valueOf(filesDownloadSpeed).setScale(2, RoundingMode.HALF_UP))
                            .append(" Mb/sec."));
                }
                startScreenshotsDownload = System.currentTimeMillis();
                if (game.isWasted()) {
                    screenshotsDownloadSize = 0;
                } else {
                    screenshotsDownloadSize = downloadScreenshots(game.getScreenshots(), parentDirectory, nameOg, gameDirectoryName, gameDirectory, gamesUnrelatedDirectory);
                }
                gamesDownloadSize += screenshotsDownloadSize;
                finishScreenshotsDownload = System.currentTimeMillis();
                if (finishScreenshotsDownload == startScreenshotsDownload) {
                    screenshotsDownloadSpeed = 0;
                } else {
                    screenshotsDownloadSpeed = ((float) screenshotsDownloadSize / SIZE_MB)
                            / ((float) (finishScreenshotsDownload - startScreenshotsDownload) / 1000);
                }
                if (screenshotsDownloadSpeed != 0) {
                    LOG.info(new StringBuilder("\t")
                            .append("screenshots download speed - ")
                            .append(BigDecimal.valueOf(screenshotsDownloadSpeed).setScale(2, RoundingMode.HALF_UP))
                            .append(" Mb/sec."));
                }
            }
            if (!game.isWasted()) {
                gamesDirectoryName.add(gameDirectoryName);
            }
            // TODO: whether need add to DB wasted game?
            int idGenre = dbV.addGenre(game.getGenre());
            int idPlatform = dbV.addPlatform(game.getPlatform());
            int idGame = dbV.addGame(game, idGenre, idPlatform);
            List<AltName> altNames = game.getAltNames();
            for (AltName altName : altNames) {
                dbV.addAltName(altName, idGame);
            }
            List<Company> developers = game.getDevelopers();
            for (Company company : developers) {
                dbV.addGameDeveloper(company, idGame);
            }
            // TODO: for (Company company : game.getPublishers()) {
            List<Company> publishers = game.getPublishers();
            for (Company company : publishers) {
                dbV.addGamePublisher(company, idGame);
            }
            List<GameProperty> properties = game.getProperties();
            for (GameProperty property : properties) {
                int idGameGroupProperties = dbV.addGroupOfPropertyOfGame(property.getGroup());
                dbV.addGameProperty(property, idGame, idGameGroupProperties);
            }
            List<GameFl> files = game.getFiles();
            for (GameFl file : files) {
                int idType = dbV.addType(file.getType());
                int idFile = dbV.addFile(file, idGame, idType);
                List<FileProperty> fileProperties = file.getProperties();
                for (FileProperty property : fileProperties) {
                    int idFileGroupProperties = dbV.addGroupOfPropertyOfFile(property.getGroup());
                    dbV.addFileProperty(property, idFile, idFileGroupProperties);
                }
            }
            for (Screenshot screenshot : game.getScreenshots()) {
                dbV.addGameScreenshot(screenshot, idGame);
            }
        }
        if (parentDirectory != null) {
            finishGamesDownload = System.currentTimeMillis();
            if (finishGamesDownload == startGamesDownload) {
                gamesDownloadSpeed = 0;
            } else {
                gamesDownloadSpeed = ((float) gamesDownloadSize / SIZE_MB)
                        / ((float) (finishGamesDownload - startGamesDownload) / 1000);
            }
            LOG.info(new StringBuilder("games download speed - ")
                    .append(BigDecimal.valueOf(gamesDownloadSpeed).setScale(2, RoundingMode.HALF_UP))
                    .append(" Mb/sec."));
        }
        if (clean) {
            LOG.info(new StringBuilder("games clean start."));
            long startCleanTime = System.currentTimeMillis();
            cleanGames(gamesDirectory, gamesCleanedDirectory, gamesDirectoryName);
            long cleanTime = System.currentTimeMillis() - startCleanTime;
            long hours = TimeUnit.MILLISECONDS.toHours(cleanTime);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(cleanTime);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(cleanTime);
            LOG.info(new StringBuilder("games clean finish, time - ")
                    .append(hours).append("h ")
                    .append(minutes).append("m ")
                    .append(seconds).append("s."));
        }
    }

    private void cleanGames(File gamesDirectory, File gamesCleanedDirectory, List<String> gamesNamesDownloaded) {
        File[] files = gamesDirectory.listFiles(File::isFile);
        File fileToClean;
        if (files != null) {
            for (File f : files) {
                fileToClean = new File(gamesCleanedDirectory, f.getName());
                if (fileToClean.exists()) {
                    fileToClean.delete();
                }
                try {
                    Files.move(f.toPath(), fileToClean.toPath());
                    LOG.info(new StringBuilder("\t")
                            .append("- file \"")
                            .append(f.getName())
                            .append("\" cleaned"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        files = gamesDirectory.listFiles(File::isDirectory);
        if (files != null) {
            for (File f : files) {
                if (checkGameForClean(f, gamesNamesDownloaded)) {
                    fileToClean = new File(gamesCleanedDirectory, f.getName());
                    if (fileToClean.exists()) {
                        delDirectory(fileToClean);
                    }
                    try {
                        Files.move(f.toPath(), fileToClean.toPath());
                        LOG.info(new StringBuilder("\t")
                                .append("- directory \"")
                                .append(f.getName())
                                .append("\" cleaned"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private long downloadFiles(Game game, File gameDirectory, File gamesUnrelatedDirectory, String gameDirectoryName) {
        List<GameFl> files = game.getFiles();
        File filesDirectory = new File(gameDirectory, FILES_DIRECTORY_NAME);
        // TODO: gameDirectory.mkdir() move out of the method.
        try {
            if (!gameDirectory.exists()) {
                if (gameDirectory.mkdir()) {
                    game.setCause_load(NOT_EXIST);
                } else {
                    LOG.error(new StringBuilder("Directory ").append(gameDirectory.getCanonicalPath()).append(" doesn't created."));
                }
            }
            if (!filesDirectory.exists() && !filesDirectory.mkdir()) {
                LOG.error(new StringBuilder("Directory ").append(filesDirectory.getCanonicalPath()).append(" doesn't created."));
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        long startFileDownload;
        long finishFileDownload;
        long gameDownloadSize = 0;
        File[] dirs = filesDirectory.listFiles(File::isDirectory);
        for (File dir : dirs) {
            File[] fls = dir.listFiles();
            for (File fl : fls) {
                fl.delete();
            }
            dir.delete();
        }
        for (GameFl gameFl : files) {
            if (gameFl.isCopyrightViolation()) {
                gameFl.setCause_unload(COPYRIGHT_VIOLATION);
                continue;
            }
            if (game.isDocumented() && !gameFl.getType().getName().equals("Документация")) {
                gameFl.setCause_unload(ONLY_DOCUMENTS);
                continue;
            }
            File file = new File(filesDirectory, gameFl.getName());
            FileCheck fileCheck = fileExists(file, gameFl);
            if (!fileCheck.isNeedToDownload()) {
                gameFl.setSize(file.length());
                continue;
            }
            if (fileCheck.isExist()) {
                gameFl.setCause_load(fileCheck.getCause());
            } else {
                gameFl.setCause_load(NOT_EXIST);
            }
            LOG.info(new StringBuilder("\t")
                    .append("+ file: ")
                    .append(gameFl.getName())
                    .append(" (")
                    .append(getApproxSizeInUnits(gameFl))
                    .append(")"));
            startFileDownload = System.currentTimeMillis();
            gameFl.setSize(downloadFile(file, gameFl));
            gameDownloadSize += gameFl.getSize();
            finishFileDownload = System.currentTimeMillis();
            LOG.info(new StringBuilder("\t\t")
                    .append("file download speed - ")
                    .append(BigDecimal.valueOf(
                            ((float) gameFl.getSize() / SIZE_MB)
                                    / ((float) (finishFileDownload - startFileDownload) / 1000))
                            .setScale(2, RoundingMode.HALF_UP))
                    .append(" Mb/sec."));
        }
        moveUnrelatedFiles(gamesUnrelatedDirectory, gameDirectoryName, files, filesDirectory);
        return gameDownloadSize;
    }

    private void moveUnrelatedFiles(File gamesUnrelatedDirectory, String gameDirectoryName, List<GameFl> files, File filesDirectory) {
        File[] filesDownloaded = filesDirectory.listFiles();
        List<File> filesUnrelated = new ArrayList<>();
        if (filesDownloaded != null) {
            for (File fl : filesDownloaded) {
                if (checkFileForUnrelated(fl, files)) {
                    filesUnrelated.add(fl);
                }
            }
        }
        if (!filesUnrelated.isEmpty()) {
            File gameUnrelatedDirectory = new File(gamesUnrelatedDirectory, gameDirectoryName
                    .replaceAll("[:/*?\"]", ""));
            File filesUnrelatedDirectory = new File(gameUnrelatedDirectory, FILES_DIRECTORY_NAME);
            try {
                if (!gameUnrelatedDirectory.exists()) {
                    if (!gameUnrelatedDirectory.mkdir()) {
                        LOG.error(new StringBuilder("Directory ").append(gameUnrelatedDirectory.getCanonicalPath()).append(" doesn't created."));
                    }
                }
                if (!filesUnrelatedDirectory.exists() && !filesUnrelatedDirectory.mkdir()) {
                    LOG.error(new StringBuilder("Directory ").append(filesUnrelatedDirectory.getCanonicalPath()).append(" doesn't created."));
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
            File file;
            for (File fl : filesUnrelated) {
                file = new File(filesUnrelatedDirectory, fl.getName());
                try {
                    Files.move(fl.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LOG.info(new StringBuilder("\t")
                            .append("- file: ")
                            .append(fl.getName())
                            .append(" is related, moved"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void moveUnrelatedScreenshots(File gamesUnrelatedDirectory, String gameDirectoryName, List<Screenshot> screenshots, File screenshotDirectory) {
        File[] screenshotsDownloaded = screenshotDirectory.listFiles();
        List<File> screenshotsUnrelated = new ArrayList<>();
        if (screenshotsDownloaded != null) {
            for (File ss : screenshotsDownloaded) {
                if (checkScreenshotForUnrelated(ss, screenshots)) {
                    screenshotsUnrelated.add(ss);
                }
            }
        }
        if (!screenshotsUnrelated.isEmpty()) {
            File gameUnrelatedDirectory = new File(gamesUnrelatedDirectory, gameDirectoryName
                    .replaceAll("[:/*?\"]", ""));
            File screenshotsUnrelatedDirectory = new File(gameUnrelatedDirectory, SCREENSHOTS_DIRECTORY_NAME);
            try {
                if (!gameUnrelatedDirectory.exists()) {
                    if (!gameUnrelatedDirectory.mkdir()) {
                        LOG.error(new StringBuilder("Directory ").append(gameUnrelatedDirectory.getCanonicalPath()).append(" doesn't created."));
                    }
                }
                if (!screenshotsUnrelatedDirectory.exists() && !screenshotsUnrelatedDirectory.mkdir()) {
                    LOG.error(new StringBuilder("Directory ").append(screenshotsUnrelatedDirectory.getCanonicalPath()).append(" doesn't created."));
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
            File screenshot;
            for (File ss : screenshotsUnrelated) {
                screenshot = new File(screenshotsUnrelatedDirectory, ss.getName());
                try {
                    Files.move(ss.toPath(), screenshot.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LOG.info(new StringBuilder("\t")
                            .append("- screenshot: ")
                            .append(ss.getName())
                            .append(" is related, moved"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean checkGameForClean(File game, List<String> gamesNames) {
        return gamesNames.stream().noneMatch(f -> f.equals(game.getName()));
    }

    private boolean checkFileForUnrelated(File fl, List<GameFl> files) {
        return files.stream().map(GameFl::getName).noneMatch(f -> f.equals(fl.getName()));
    }

    private boolean checkScreenshotForUnrelated(File fl, List<Screenshot> files) {
        return files.stream().map(Screenshot::getName).noneMatch(f -> f.equals(fl.getName()));
    }

    private long downloadScreenshots(List<Screenshot> screenshots, File parentDirectory, String nameOg, String gameDirectoryName, File gameDirectory, File gamesUnrelatedDirectory) {
        File screenshotsDirectory = new File(gameDirectory, SCREENSHOTS_DIRECTORY_NAME);
        try {
            if (!screenshotsDirectory.exists() && !screenshotsDirectory.mkdir()) {
                LOG.error(new StringBuilder("Directory ").append(screenshotsDirectory.getCanonicalPath()).append(" doesn't created."));
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        long screenshotsDownloadSize = 0;
        List <Screenshot> toCheckDuplicate = new ArrayList<>();
        for (Screenshot gameScrn : screenshots) {
            File file = new File(screenshotsDirectory, gameScrn.getName());
            if (toCheckDuplicate.contains(gameScrn)) {
                gameScrn.setCause_unload("duplicate with nn = " + toCheckDuplicate.get(toCheckDuplicate.indexOf(gameScrn)).getNn());
                continue;
            }
            toCheckDuplicate.add(gameScrn);
            if (file.exists()) {
                gameScrn.setSize(file.length());
                continue;
            }
            gameScrn.setCause_load(NOT_EXIST);
            gameScrn.setSize(downloadScreen(file, gameScrn));
            LOG.info(new StringBuilder("\t")
                    .append("+ screenshot: ")
                    .append(gameScrn.getName())
                    .append(" (")
                    .append(gameScrn.getSize())
                    .append(" B)"));
            screenshotsDownloadSize += gameScrn.getSize();
        }
        moveUnrelatedScreenshots(gamesUnrelatedDirectory, gameDirectoryName, screenshots, screenshotsDirectory);
        return screenshotsDownloadSize;
    }

    private String getApproxSizeInUnits(GameFl gameFl) {
        return BigDecimal.valueOf((float) gameFl.getApproxSize() / getSizeUnit(gameFl.getUnit()))
                .setScale(2, RoundingMode.HALF_UP) +
                " " +
                gameFl.getUnit().getName();
    }

    private long getSizeUnit(Unit unit) {
        long result = 0;
        if (new Unit(DESIGNATION_UNIT_B).equals(unit)) {
            result = SIZE_B;
        } else if (new Unit(DESIGNATION_UNIT_KB).equals(unit)) {
            result = SIZE_KB;
        } else if (new Unit(DESIGNATION_UNIT_MB).equals(unit)) {
            result = SIZE_MB;
        } else if (new Unit(DESIGNATION_UNIT_GB).equals(unit)) {
            result = SIZE_GB;
        }
        return result;
    }

    private FileCheck fileExists(File file, GameFl gameFl) {
        FileCheck result;
        boolean exist;
        boolean needToDownload;
        String cause;
        if (file.exists()) {
            exist = true;
            GregorianCalendar lastModified = new GregorianCalendar();
            lastModified.setTimeInMillis(file.lastModified());
            if (abs(gameFl.getApproxSize() - file.length()) > getSizeError(gameFl.getUnit())) {
                needToDownload = true;
                cause = "abs("
                        + "gameFl.approxSize "
                        + gameFl.getApproxSize()
                        + " - "
                        + "file.length "
                        + file.length()
                        + ") = "
                        + abs(gameFl.getApproxSize() - file.length())
                        + " > sizeError "
                        + getSizeError(gameFl.getUnit());
            } else if (!gameFl.getDate().before(lastModified)) {
                needToDownload = true;
                cause = "gameFl.getDate "
                        + gameFl.getDate().getTime()
                        + " >= file.lastModified "
                        + lastModified.getTime();
            } else {
                needToDownload = false;
                cause = "";
            }
        } else {
            exist = false;
            needToDownload = true;
            cause = "";
        }
        result = new FileCheck(exist, needToDownload, cause);
        return result;
//        boolean result = false;
//        GregorianCalendar lastModified;
//        if (file.exists()
//                && abs(gameFl.getApproxSize() - file.length()) <= getSizeError(gameFl.getUnit())) {
//            lastModified = new GregorianCalendar();
//            lastModified.setTimeInMillis(file.lastModified());
//        if (gameFl.getDate()
//                .before(new GregorianCalendar(
//                        lastModified.get(Calendar.YEAR),
//                        lastModified.get(Calendar.MONTH),
//                        lastModified.get(Calendar.DAY_OF_MONTH)))) {
//            result = true;
//        }
//        }
//        return result;
    }

    private int getSizeError(Unit unit) {
        int result = 0;
        if (new Unit(DESIGNATION_UNIT_B).equals(unit)) {
            result = ERROR_UNIT_B;
        } else if (new Unit(DESIGNATION_UNIT_KB).equals(unit)) {
            result = ERROR_UNIT_KB;
        } else if (new Unit(DESIGNATION_UNIT_MB).equals(unit)) {
            result = ERROR_UNIT_MB;
        } else if (new Unit(DESIGNATION_UNIT_GB).equals(unit)) {
            result = ERROR_UNIT_GB;
        }
        return result;
    }

    private long downloadFile(File file, GameFl gameFl) {
        String fileTempLink = getFileTempLink(gameFl.getLink(), "\t");
        byte[] buffer;
        int bytes;
        try (BufferedInputStream in = new BufferedInputStream(new URL(fileTempLink.replaceAll(" ", "%20")).openStream());
             FileOutputStream out = new FileOutputStream(file)) {
            buffer = new byte[1024];
            bytes = in.read(buffer, 0, 1024);
            while (bytes != -1) {
                out.write(buffer, 0, bytes);
                bytes = in.read(buffer, 0, 1024);
            }
        } catch (IOException e) {
            // TODO: clear message.
            LOG.error(e.getClass().getName() + ": " + e.getMessage());
            gameFl.setCause_unload(e.getClass().getName());
        }
        return file.length();
    }


    private long downloadScreen(File file, Screenshot gameScrn) {
        byte[] buffer;
        int bytes;
        try (BufferedInputStream in = new BufferedInputStream(
                new URL(gameScrn.getLink().replaceAll(" ", "%20")).openStream());
             FileOutputStream out = new FileOutputStream(file)) {
            buffer = new byte[1024];
            bytes = in.read(buffer, 0, 1024);
            while (bytes != -1) {
                out.write(buffer, 0, bytes);
                bytes = in.read(buffer, 0, 1024);
            }
        } catch (IOException e) {
            // TODO: clear message.
            LOG.error(e.getMessage());
            gameScrn.setCause_unload(e.getClass().getName());
        }
        return file.length();
    }

    private String getFileTempLink(String link, String tab) {
        String result;
        Document docFile = getGameFilePage(link, tab);
        result = docFile
                .getElementsByClass("gamelink").get(0)
                .attr("href");
        return result;
    }

    private Document getGameFilePage(String link, String tab) {
        Document result = null;
        boolean message = false;
//        TODO: include timeout.
        while (result == null) {
            try {
                result = Jsoup.connect(link).get();
            } catch (IOException e) {
                if (!message) {
                    LOG.info(new StringBuilder(tab).append("File with link ")
                            .append(link).append(" isn't available. Access attempt now."));
                    message = true;
                }
            }
        }
        return result;
    }

    private List<GameFl> getGameFiles(int page) {
        List<GameFl> result = new ArrayList<>();
        Document docDl = getGameFilesPage(page);
//        TODO: java.lang.NullPointerException. Already handled, need to remove "errorAccess".
        boolean errorAccess = docDl.getElementById("main") == null;
        if (errorAccess) {
            LOG.error(new StringBuilder(this.baseUrl).append("game/download/")
                    .append(page).append(".html isn't accessible."));
        }
        Elements files = docDl
                .getElementsByClass("game_downloads_container")
                .get(0)
                .children();
        FileType fileType;
        String fileLink;
        String[] fileDlText;
        long fileApproxSize = 0;
        String fileDateText;
        String fileDesc;
        String fileTempLink;
        String fileName;
        String specifyFileLink;
        boolean copyrightViolation;
        Unit fileUnit = null;
        String fileProvided;
        Elements groups;
        String description;
        for (Element file : files) {
            groups = file
                    .child(1)
                    .child(0)
                    .child(4)
                    .children();
            List<FileProperty> fileProperties = new ArrayList<>();
            if (groups.size() > 1) {
                FileGroupProperties groupName;
                String propertyName;
                Elements properties;
                for (Element group : groups) {
                    if (group.textNodes().get(0).text().isBlank()) {
                        break;
                    }
                    groupName = new FileGroupProperties(group.textNodes().get(0).text().substring(0, group.textNodes().get(0).text().indexOf(':')));
                    properties = group.getElementsByClass("file-property-value tip-list");
                    for (Element property : properties) {
                        propertyName = property.text();
                        description = property.attr("title");
                        fileProperties.add(new FileProperty(groupName, propertyName, description));
                    }
                }
            }
            fileType = new FileType(file
                    .child(0)
                    .text());
            fileDlText = file
                    .child(1)
                    .child(0)
                    .child(1).text().split(SIZE_DELIMITER);
            if (DESIGNATION_UNIT_B.equals(fileDlText[2])) {
                fileApproxSize = (long) Double.parseDouble(fileDlText[1]);
                fileUnit = new Unit(DESIGNATION_UNIT_B);
            } else if (DESIGNATION_UNIT_KB.equals(fileDlText[2])) {
                fileApproxSize = (long) (Double.parseDouble(fileDlText[1]) * 1024);
                fileUnit = new Unit(DESIGNATION_UNIT_KB);
            } else if (DESIGNATION_UNIT_MB.equals(fileDlText[2])) {
                fileApproxSize = (long) (Double.parseDouble(fileDlText[1]) * 1024 * 1024);
                fileUnit = new Unit(DESIGNATION_UNIT_MB);
            } else if (DESIGNATION_UNIT_GB.equals(fileDlText[2])) {
                fileApproxSize = (long) (Double.parseDouble(fileDlText[1]) * 1024 * 1024 * 1024);
                fileUnit = new Unit(DESIGNATION_UNIT_GB);
            }
            fileDateText = fileDlText[4].substring(0, 10);
            fileDesc = file
                    .child(1)
                    .child(0)
                    .child(3)
                    .html();
            if (file.children().size() > 2) {
                fileProvided = file.child(2).child(2).text();
            } else {
                fileProvided = "";
            }
            specifyFileLink = file
                    .child(1)
                    .child(0)
                    .child(0)
                    .attr("href");
            copyrightViolation = specifyFileLink.isEmpty();
            if (!copyrightViolation) {
                fileLink = this.baseUrl +
                        "game/download/" +
                        specifyFileLink;
                fileTempLink = getFileTempLink(fileLink, "");
                fileName = fileTempLink.substring(fileTempLink.lastIndexOf('/') + 1);
            } else {
                fileLink = null;
                fileName = null;
            }
            result.add(new GameFl(
                    fileType,
                    fileApproxSize,
                    new GregorianCalendar(Integer.parseInt(fileDateText.substring(6)),
                            Integer.parseInt(fileDateText.substring(3, 5)) - 1,
                            Integer.parseInt(fileDateText.substring(0, 2))),
                    fileDesc,
                    fileName,
                    fileUnit,
                    fileProvided,
                    fileLink,
                    fileProperties,
                    copyrightViolation));
        }
        return result;
    }

    private List<Screenshot> getGameScreenshots(int page) {
        List<Screenshot> result = new ArrayList<>();
        Document docScrn = getGameSreenshotsPage(page);
//        TODO: java.lang.NullPointerException. Already handled, need to remove "errorAccess".
        boolean errorAccess = docScrn.getElementById("main") == null;
        if (errorAccess) {
            LOG.error(new StringBuilder(this.baseUrl).append("game/screenshots/")
                    .append(page).append(".html isn't accessible."));
        }
        Elements screenshots = docScrn
                .getElementsByClass("game_screen_container middlesmall");
        String screenLink;
        int screenNn = 0;
        String screenProvided;
        String screenDesc;
        for (Element screen : screenshots) {
            screenDesc = screen.getElementsByClass("screen_comment").text();
            screenProvided = screen.getElementsByClass("screen_author").get(0).child(0).text();
            screenLink = this.baseUrl
                    + screen.getElementsByClass("screen_img_container")
                    .get(0).child(0).attr("href").substring(1);
            result.add(new Screenshot(
                    ++screenNn,
                    screenLink,
                    screenProvided,
                    screenDesc
            ));
        }
        return result;
    }

    private int getGameFavorites(Document doc) {
        return Integer.parseInt(doc
                .getElementById("main")
                .child(1)
                .child(3)
                .child(0)
                .child(0)
                .text());
    }

    private int getGameCompletions(Document doc) {
        return Integer.parseInt(doc
                .getElementById("main")
                .child(1)
                .child(3)
                .child(0)
                .child(1)
                .text());
    }

    private int getGameBookmarks(Document doc) {
        return Integer.parseInt(doc
                .getElementById("main")
                .child(1)
                .child(3)
                .child(0)
                .child(2)
                .text());
    }

    private Document getGamePage(int page) {
        Document result = null;
        boolean message = false;
//        TODO: include timeout.
        while (result == null) {
            try {
                result = Jsoup.connect(this.baseUrl + "game/" + page + ".html").get();
            } catch (UnknownHostException e) {
                if (!message) {
                    LOG.info(new StringBuilder("Game with page ").append(page).append(" isn't available. Access attempt now."));
                    message = true;
                }
            } catch (HttpStatusException e) {
                break;
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
        return result;
    }

    private Document getGameFilesPage(int page) {
        Document result = null;
        boolean message = false;
//        TODO: include timeout.
        while (result == null) {
            try {
                result = Jsoup.connect(this.baseUrl + "game/download/" +
                        page + ".html").get();
            } catch (IOException e) {
                if (!message) {
                    LOG.info(new StringBuilder("Files with page ")
                            .append(page).append(" aren't available. Access attempt now."));
                    message = true;
                }
            }
        }
        return result;
    }

    private Document getGameSreenshotsPage(int page) {
        Document result = null;
        boolean message = false;
//        TODO: include timeout.
        while (result == null) {
            try {
                result = Jsoup.connect(this.baseUrl + "game/screenshots/" +
                        page + ".html").get();
            } catch (IOException e) {
                if (!message) {
                    LOG.info(new StringBuilder("Screenshots with page ")
                            .append(page).append(" aren't available. Access attempt now."));
                    message = true;
                }
            }
        }
        return result;
    }

    private String getGameName(Document doc) {
        return doc
                .getElementById("main")
                .child(1)
                .child(1)
                .getElementsByClass("game_title")
                .get(0)
                .text();
    }

    private List<AltName> getGameAltNames(Document doc) {
        List<AltName> result = new ArrayList<>();
        Elements alt_names = doc
                .getElementById("main")
                .child(1)
                .child(1)
                .getElementsByClass("game_alt_names");
        for (Element altName : alt_names) {
            result.add(new AltName(altName.text()));
        }
        return result;
    }

    private Genre getGameGenre(Document doc) {
        Genre result = null;
        Element attrs = getGameAttribute(doc, GENRE);
        if (attrs != null) {
            result = new Genre(attrs
                    .child(1)
                    .child(0)
                    .child(0).text());
        }
        return result;
    }

    private Element getGameAttribute(Document doc, String attr){
        Element result = null;
        Elements attrs = doc
                .getElementById("main")
                .child(1)
                .child(2)
                .child(0)
                .child(0)
                .child(1)
                .child(0)
                .child(0)
                .children();
        for (Element child : attrs) {
            String hrefAttribute = child
                    .child(1)
                    .child(0)
                    .attr("href");
            String nameAttribute = hrefAttribute.substring(hrefAttribute.indexOf('?') + 1, hrefAttribute.indexOf('='));
            if (nameAttribute.equals(attr)) {
                result = child;
                break;
            }
        }
        return result;
    }

    private Platform getGamePlatform(Document doc) {
        Platform result = null;
        Element attrs = getGameAttribute(doc, PLATFORM);
        if (attrs != null) {
            result = new Platform(attrs
                    .child(1)
                    .child(0)
                    .child(0).text());
        }
        return result;
    }

    private List<Company> getGameDevelopers(Document doc) {
        //TODO: Company, The -> 2 companies: Company; The
        List<Company> result = new ArrayList<>();
        Element attrs = getGameAttribute(doc, DEVELOPER);
        if (attrs != null) {
//            List<String> names = nameSplit(attrs
//                    .child(1)
//                    .child(0).text());
            String[] names = attrs
                    .child(1).text().split(NAME_DELIMITER);
            for (String name : names) {
                result.add(new Company(name));
            }
        }
        return result;
    }

    private List<Company> getGamePublishers(Document doc) {
        //TODO: Company, The -> 2 companies: Company; The
        List<Company> result = new ArrayList<>();
        Element attrs = getGameAttribute(doc, PUBLISHER);
        if (attrs != null) {
//            List<String> names = nameSplit(attrs
//                    .child(1).text());
            String[] names = attrs
                    .child(1).text().split(NAME_DELIMITER);
            for (String name : names) {
                result.add(new Company(name));
            }
        }
        return result;
    }

    private List<GameProperty> getGameProperties(Document doc) {
        List<GameProperty> result = new ArrayList<>();
        GameGroupProperties groupName;
        String propertyName;
        String description;
        Elements properties;
        if (doc.getElementsByClass("game-groups").size() > 0) {
            Elements groups = doc
                    .getElementsByClass("game-groups").get(0)
                    .children();
            for (Element group : groups) {
                groupName = new GameGroupProperties(group.textNodes().get(0).text().substring(0, group.textNodes().get(0).text().length() - 1));
                properties = group.getElementsByClass("tip-list");
                for (Element property : properties) {
                    propertyName = property.text();
                    description = property.attr("title");
                    result.add(new GameProperty(groupName, propertyName, description));
                }
            }
        }
        return result;
    }

    private int getGameReleased(Document doc) {
        int result = 0;
        Element attrs = getGameAttribute(doc, RELEASED);
        if (attrs != null) {
            try {
                result = Integer.parseInt(attrs
                        .child(1).text());
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    private String getGameReview(Document doc) {
        return doc
                .getElementById("main")
                .child(3)
                .child(0)
                .child(0)
                .child(1)
                .child(0)
                .html();
    }

    private void delDirectory(File directory) {
        try {
            Files.walk(directory.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }
}