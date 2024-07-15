package io.github.yajuhua.podcast2.pojo.entity;

public class PluginInfo {

    private String name;
    private String updateTime;
    private String version;
    private String uuid;
    private String url;
    private String md5;

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
    public String getUpdateTime() {
        return updateTime;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    public String getVersion() {
        return version;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getUuid() {
        return uuid;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "PluginInfo{" +
                "name='" + name + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", version='" + version + '\'' +
                ", uuid='" + uuid + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}