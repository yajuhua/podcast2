package com.podcast.pojo;

/**
 * 用于前端页面展示
 */
public class ChannelDataShow {
  private String uuid;
  private String channelTitle;
  private String updateTimestamp;
  private String channelFace;

    public ChannelDataShow(String uuid, String channelTitle, String updateTimestamp, String channelFace) {
        this.uuid = uuid;
        this.channelTitle = channelTitle;
        this.updateTimestamp = updateTimestamp;
        this.channelFace = channelFace;
    }

    public ChannelDataShow() {
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

    public String getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(String updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public String getChannelFace() {
        return channelFace;
    }

    public void setChannelFace(String channelFace) {
        this.channelFace = channelFace;
    }

    @Override
    public String toString() {
        return "ChannelDataShow{" +
                "uuid='" + uuid + '\'' +
                ", channelTitle='" + channelTitle + '\'' +
                ", updateTimestamp='" + updateTimestamp + '\'' +
                ", channelFace='" + channelFace + '\'' +
                '}';
    }
}
