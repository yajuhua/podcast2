package io.github.yajuhua.podcast2.pojo.dto;

import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import lombok.Data;

import java.util.List;

@Data
public class AddSubDTO {
    private String url;
    private String type;
    private Long survivalTime;
    private Long cron;
    private Integer episodes;
    private String customEpisodes;
    private String plugin;
    private Integer isUpdate;
    private Integer isFilter;
    private int maxDuration;
    private int minDuration;
    private List<String> titleKeywords;
    private List<String> descKeywords;
    private Integer isExtend;
    private List<InputAndSelectData> inputAndSelectDataList;
    private Integer status;
    private Integer keepLast;//保留最近n节目
}
