package io.github.yajuhua.podcast2.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Items implements Serializable {
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
    private String type;
    private String links;
    private String args;
}
