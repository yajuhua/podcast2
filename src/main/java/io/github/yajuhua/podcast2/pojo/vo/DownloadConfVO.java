package io.github.yajuhua.podcast2.pojo.vo;

import io.github.yajuhua.podcast2API.extension.build.ExtendList;
import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 下载配置VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DownloadConfVO {
    private Integer isExtend;
    private ExtendList extendList;
    private List<InputAndSelectData> inputListData;
    private List<InputAndSelectData> selectListData;
    private String type;
}
