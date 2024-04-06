package io.github.yajuhua.podcast2.pojo.dto;

import lombok.Data;

@Data
public class ItemsDTO {
    private String channelUuid;
    private String uuid;
    private String title;
    private String image;
    private String description;
    private String link;
    private String enclosure;
    private Long createTime;
    private Integer status;
    private Integer duration;
    private String downloader;
    private String fileName;
    private String format;
    private Double downloadProgress;
    private Double downloadTimeLeft;
    private Double totalSize;
    private Double downloadSpeed;
    private String operation;
}
