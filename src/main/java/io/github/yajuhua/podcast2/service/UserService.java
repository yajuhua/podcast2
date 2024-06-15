package io.github.yajuhua.podcast2.service;

import io.github.yajuhua.podcast2.pojo.dto.UserLoginDTO;
import io.github.yajuhua.podcast2.pojo.entity.ExtendInfo;
import io.github.yajuhua.podcast2.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;
public interface UserService {

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    User login(UserLoginDTO userLoginDTO);

    /**
     * 获取扩展信息
     * @return
     */
    ExtendInfo getExtendInfo();

    /**
     * 更新扩展信息
     * @param extendInfo
     */
    void updateExtendInfo(ExtendInfo extendInfo);

    /**
     * 为null时删除
     * @param extendInfo
     */
    void deleteExtendInfo(ExtendInfo extendInfo);
}
