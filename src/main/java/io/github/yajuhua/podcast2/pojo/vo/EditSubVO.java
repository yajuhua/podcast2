package io.github.yajuhua.podcast2.pojo.vo;

import io.github.yajuhua.podcast2API.extension.build.ExtendList;
import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 编辑订阅数据
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditSubVO {
    private String uuid;
    private String title;
    private String equal;
    private String type;
    private String description;
    private Long survivalTime;
    private Long cron;
    private Integer isUpdate;
    private Integer isFilter;
    private int maxDuration;
    private int minDuration;
    private List<String> titleKeywords;
    private List<String> descKeywords;
    private Integer isExtend;
    private ExtendList extendList;
    private List<InputAndSelectData> inputListData;
    private List<InputAndSelectData> selectListData;
    private String titleInputValue;
    private boolean titleInputVisible;
    private String descInputValue;
    private boolean descInputVisible;
    private Integer status;
    private String image;
    private Integer keepLast;
    private String survivalWay;//节目存活方式
    private String subType;
}
