package io.github.yajuhua.podcast2.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubDetailVO implements Serializable {
    private String uuid;
    private String equal;
    private String title;
    private String link;
    private Integer status;
    private String description;
    private String image;
    private String createTime;
    private String checkTime;
    private String updateTime;
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
