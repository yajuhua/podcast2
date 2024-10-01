package io.github.yajuhua.podcast2.pojo.dto;

import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import lombok.Data;

import java.util.List;

/**
 * 追加节目 for 订阅
 */
@Data
public class AppendItemDTO {
    private String type;
    private String url;//单个节目链接
    private String channelUuid;//订阅uuid
    private List<InputAndSelectData> inputAndSelectDataList;//扩展选项
}
