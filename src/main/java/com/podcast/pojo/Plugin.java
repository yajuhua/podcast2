package com.podcast.pojo;

public class Plugin {
    private String name;
    private String version;
    private String update;

    public Plugin() {
    }

    public Plugin(String name, String version, String update) {
        this.name = name;
        this.version = version;
        this.update = update;
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

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    @Override
    public String toString() {
        return "Plugin{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", update='" + update + '\'' +
                '}';
    }
}
