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
public class Sub implements Serializable {
    private String uuid;
    private String equal;
    private String title;
    private String link;
    private Integer status;
    private String description;
    private String image;
    private Long createTime;
    private Long checkTime;
    private Long updateTime;
    private String type;
    private Long survivalTime;
    private Long cron;
    private String plugin;
    private Integer episodes;
    private String customEpisodes;
    private Integer isUpdate;
    private Integer isFirst;
    private String pluginUuid;
    private Integer isFilter;
    private Integer minDuration;
    private Integer maxDuration;
    private String titleKeywords;
    private String descKeywords;
    private Integer isExtend;
    private Integer keepLast;
    private String survivalWay;//节目存活方式
}
