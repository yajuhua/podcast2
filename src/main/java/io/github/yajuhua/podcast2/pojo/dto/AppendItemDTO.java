package io.github.yajuhua.podcast2.pojo.dto;

import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import lombok.*;

import java.util.List;

/**
 * 追加节目 for 订阅
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppendItemDTO {
    private String type;
    private String url;//单个节目链接
    private String channelUuid;//订阅uuid
    private List<InputAndSelectData> inputAndSelectDataList;//扩展选项

    @Override
    public boolean equals(Object o) {
        if (o != null){
            AppendItemDTO appendItemDTO = (AppendItemDTO) o;
            //链接相同就认为是同一个
            return appendItemDTO.getUrl() != null && appendItemDTO.getUrl().equals(this.url);
        }
        return false;
    }


}
