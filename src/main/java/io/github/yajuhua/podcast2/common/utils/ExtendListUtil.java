package io.github.yajuhua.podcast2.common.utils;

import io.github.yajuhua.podcast2.pojo.entity.Extend;
import io.github.yajuhua.podcast2.pojo.vo.ExtendListVO;
import io.github.yajuhua.podcast2API.extension.build.ExtendList;
import io.github.yajuhua.podcast2API.extension.build.Input;
import io.github.yajuhua.podcast2API.extension.build.Select;
import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class ExtendListUtil {

    /**
     * 构建ExtendListVO
     * @param extendList
     * @return
     */
    public static ExtendListVO buildExtendListVO(ExtendList extendList){
        List<InputAndSelectData> inputListData = new ArrayList<>();
        List<InputAndSelectData> selectListData = new ArrayList<>();

        //避免传入null
        if (extendList.getInputList() != null){
            for (Input input : extendList.getInputList()) {
                inputListData.add(new InputAndSelectData());
            }
        }

        if (extendList.getSelectList() != null){
            for (Select select : extendList.getSelectList()) {
                selectListData.add(new InputAndSelectData());
            }
        }
        return ExtendListVO.builder()
                .extendList(extendList)
                .selectListData(selectListData)
                .inputListData(inputListData)
                .build();
    }
}
