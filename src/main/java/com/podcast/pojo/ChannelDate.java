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

    public ChannelDate() {
    }

    public ChannelDate(String uuid, String channelTitle, Long updateTimestamp, Long frequency, Long latestCheckTimestamp, String channelFace, Long survival) {
        this.uuid = uuid;
        this.channelTitle = channelTitle;
        this.updateTimestamp = updateTimestamp;
        this.frequency = frequency;
        this.latestCheckTimestamp = latestCheckTimestamp;
        this.channelFace = channelFace;
        this.survival = survival;
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
                '}';
    }
}
