package com.podcast.pojo;

/**
 * 用于数据库交互
 */
public class ChannelDate {
  private String uuid;
  private String channelTitle;
  private Long updateTimestamp;
  private Long frequency;
  private Long latestCheckTimestamp;
  private String channelFace;
  private Long survival;
  private String args;
  private String link;
  private String equal;
  private String description;
  private String type;
  private String plugin;

  private Integer status;



    public ChannelDate() {
    }

    public ChannelDate(String uuid, String channelTitle, Long updateTimestamp,
                       Long frequency, Long latestCheckTimestamp, String channelFace,
                       Long survival, String args, String link, String equal, String description,
                       String type, String plugin, Integer status) {
        this.uuid = uuid;
        this.channelTitle = channelTitle;
        this.updateTimestamp = updateTimestamp;
        this.frequency = frequency;
        this.latestCheckTimestamp = latestCheckTimestamp;
        this.channelFace = channelFace;
        this.survival = survival;
        this.args = args;
        this.link = link;
        this.equal = equal;
        this.description = description;
        this.type = type;
        this.plugin = plugin;
        this.status = status;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public Long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public Long getFrequency() {
        return frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    public Long getLatestCheckTimestamp() {
        return latestCheckTimestamp;
    }

    public void setLatestCheckTimestamp(Long latestCheckTimestamp) {
        this.latestCheckTimestamp = latestCheckTimestamp;
    }

    public String getChannelFace() {
        return channelFace;
    }

    public void setChannelFace(String channelFace) {
        this.channelFace = channelFace;
    }

    public Long getSurvival() {
        return survival;
    }

    public void setSurvival(Long survival) {
        this.survival = survival;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getEqual() {
        return equal;
    }

    public void setEqual(String equal) {
        this.equal = equal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ChannelDate{" +
                "uuid='" + uuid + '\'' +
                ", channelTitle='" + channelTitle + '\'' +
                ", updateTimestamp=" + updateTimestamp +
                ", frequency=" + frequency +
                ", latestCheckTimestamp=" + latestCheckTimestamp +
                ", channelFace='" + channelFace + '\'' +
                ", survival=" + survival +
                ", args='" + args + '\'' +
                ", link='" + link + '\'' +
                ", equal='" + equal + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", plugin='" + plugin + '\'' +
                ", status=" + status +
                '}';
    }
}
