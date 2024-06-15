package io.github.yajuhua.podcast2.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.exception.AccountNotFoundException;
import io.github.yajuhua.podcast2.mapper.UserMapper;
import io.github.yajuhua.podcast2.pojo.dto.UserLoginDTO;
import io.github.yajuhua.podcast2.pojo.entity.ExtendInfo;
import io.github.yajuhua.podcast2.pojo.entity.User;
import io.github.yajuhua.podcast2.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiConsumer;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private Gson gson;

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    public User login(UserLoginDTO userLoginDTO) {
        User user = userMapper.getByUsername(userLoginDTO.getUsername());

        if (user == null){
            //说明账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        if (!user.getPassword().equals(userLoginDTO.getPassword())){
            //密码错误
            throw new AccountNotFoundException(MessageConstant.PASSWORD_ERROR);
        }
        //返回实体对象
        return user;
    }

    @Override
    public ExtendInfo getExtendInfo() {
        List<User> list = userMapper.list();
        if (list == null || list.isEmpty()){
            throw new AccountNotFoundException("找不到用户信息");
        }
        User user = list.get(0);
        ExtendInfo extendInfo = null;
        try {
            extendInfo = gson.fromJson(user.getUuid(), ExtendInfo.class);
        } catch (Exception e) {
            extendInfo = ExtendInfo.builder()
                    .uuid(user.getUuid())
                    .build();
        }
        return extendInfo;
    }

    @Override
    public void updateExtendInfo(ExtendInfo extendInfo) {
        ExtendInfo update = getExtendInfo();
        if (extendInfo.getAlistInfo() != null){
            update.setAlistInfo(extendInfo.getAlistInfo());
        }
        if (extendInfo.getPath() != null){
            update.setPath(extendInfo.getPath());
        }
        if (extendInfo.getPluginUrl() != null){
            update.setPluginUrl(extendInfo.getPluginUrl());
        }
        if (extendInfo.getGithubProxyUrl() != null){
            update.setGithubProxyUrl(extendInfo.getGithubProxyUrl());
        }
        if (extendInfo.getUuid() != null){
            update.setUuid(extendInfo.getUuid());
        }
        String extendInfoJson = gson.toJson(update);
        userMapper.update(User.builder().uuid(extendInfoJson).build());
    }

    /**
     * 为""时删除该字段信息
     * @param extendInfo
     */
    @Override
    public void deleteExtendInfo(ExtendInfo extendInfo) {
        ExtendInfo update = getExtendInfo();
        if (extendInfo.getAlistInfo() != null){
            update.setAlistInfo(null);
        }
        if (extendInfo.getPath() != null){
            update.setPath(null);
        }
        if (extendInfo.getPluginUrl() != null){
            update.setPluginUrl(null);
        }
        if (extendInfo.getGithubProxyUrl() != null){
            update.setGithubProxyUrl(null);
        }
        if (extendInfo.getUuid() != null){
            update.setUuid(null);
        }
        String extendInfoJson = gson.toJson(update);
        userMapper.update(User.builder().uuid(extendInfoJson).build());
    }
}
