package com.podcast.pojo;

/**
 * 封装下载器信息
 */
public class Download {
    private String id;
    private String downloaderName; //下载器名称
    private String description; //描述
    private String totalSize; //文件大小
    private double percentage; //下载进度百分比
    private String currentSpeed; //当前速度
    private String ETA; //预计完成时间

    private int status;

    private String finalAt;



    public Download() {
    }

    public Download(String id, String downloaderName, String description, String totalSize, double percentage, String currentSpeed, String ETA, int status) {
        this.id = id;
        this.downloaderName = downloaderName;
        this.description = description;
        this.totalSize = totalSize;
        this.percentage = percentage;
        this.currentSpeed = currentSpeed;
        this.ETA = ETA;
        this.status = status;
    }

    public Download(String id, String downloaderName, String description, String totalSize, double percentage, String currentSpeed, String ETA) {
        this.id = id;
        this.downloaderName = downloaderName;
        this.description = description;
        this.totalSize = totalSize;
        this.percentage = percentage;
        this.currentSpeed = currentSpeed;
        this.ETA = ETA;
    }

    public String getFinalAt() {
        return finalAt;
    }

    public void setFinalAt(String finalAt) {
        this.finalAt = finalAt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDownloaderName() {
        return downloaderName;
    }

    public void setDownloaderName(String downloaderName) {
        this.downloaderName = downloaderName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(String currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public String getETA() {
        return ETA;
    }

    public void setETA(String ETA) {
        this.ETA = ETA;
    }

    @Override
    public String toString() {
        return "Download{" +
                "id='" + id + '\'' +
                ", downloaderName='" + downloaderName + '\'' +
                ", description='" + description + '\'' +
                ", totalSize='" + totalSize + '\'' +
                ", percentage=" + percentage +
                ", currentSpeed='" + currentSpeed + '\'' +
                ", ETA='" + ETA + '\'' +
                ", status=" + status +
                '}';
    }
}
