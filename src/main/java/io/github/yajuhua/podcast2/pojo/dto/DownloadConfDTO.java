package io.github.yajuhua.podcast2.pojo.dto;

import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DownloadConfDTO {
    private String uuid;
    private List<InputAndSelectData> inputListData;
    private List<InputAndSelectData> selectListData;
    private String type;
}
