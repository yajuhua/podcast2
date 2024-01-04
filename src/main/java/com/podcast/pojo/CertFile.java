package com.podcast.pojo;

public class CertFile {
    private String fileName;

    public CertFile(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "CertFile{" +
                "fileName='" + fileName + '\'' +
                '}';
    }
}
