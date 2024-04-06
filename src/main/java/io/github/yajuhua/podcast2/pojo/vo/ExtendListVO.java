package io.github.yajuhua.podcast2.pojo.vo;

import io.github.yajuhua.podcast2API.extension.build.ExtendList;
import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExtendListVO {
    private ExtendList extendList;
    private List<InputAndSelectData> inputListData;
    private List<InputAndSelectData> selectListData;
}
