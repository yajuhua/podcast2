package io.github.yajuhua.podcast2.pojo.entity;
import java.util.List;

public class PluginMetadata {

    private String repoVersion;
    private String updateTime;
    private List<PluginInfo> pluginList;
    public void setRepoVersion(String repoVersion) {
        this.repoVersion = repoVersion;
    }
    public String getRepoVersion() {
        return repoVersion;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
    public String getUpdateTime() {
        return updateTime;
    }

    public void setPluginList(List<PluginInfo> pluginList) {
        this.pluginList = pluginList;
    }
    public List<PluginInfo> getPluginList() {
        return pluginList;
    }

}
