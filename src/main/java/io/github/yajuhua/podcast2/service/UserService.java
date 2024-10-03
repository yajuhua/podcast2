package io.github.yajuhua.podcast2.service;

import io.github.yajuhua.podcast2.pojo.dto.UserLoginDTO;
import io.github.yajuhua.podcast2.pojo.entity.BotInfo;
import io.github.yajuhua.podcast2.pojo.entity.ExtendInfo;
import io.github.yajuhua.podcast2.pojo.entity.User;
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

    /**
     * 更新bot信息
     * @param botInfo
     */
    void updateBotInfo(BotInfo botInfo);

    /**
     * 获取bot信息
     * @return
     */
    BotInfo getBotInfo();
}
