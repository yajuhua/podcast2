package com.podcast.pojo;
/**
 * 封装下载器的版本、名称
 */
public class Downloader {
    private String name;
    private String version;

    public Downloader() {

    }

    public Downloader(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Downloader{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
