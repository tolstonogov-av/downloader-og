package ru.tolstonogov.entities;

public class FileCheck {
    private final boolean exist;

    private final boolean needToDownload;

    private final String cause;

    public FileCheck(boolean exist, boolean needToDownload, String cause) {
        this.exist = exist;
        this.needToDownload = needToDownload;
        this.cause = cause;
    }

    public boolean isExist() {
        return exist;
    }

    public boolean isNeedToDownload() {
        return needToDownload;
    }

    public String getCause() {
        return this.cause;
    }
}
