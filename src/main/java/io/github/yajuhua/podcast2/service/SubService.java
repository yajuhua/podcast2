package io.github.yajuhua.podcast2.service;

import io.github.yajuhua.podcast2.pojo.entity.Sub;

import java.util.List;
import java.util.Map;

public interface SubService {
    List<Sub> list();
    void addSub(Sub sub);
     Sub selectByMap(Map map);
    void commitEditSub(Sub sub);
    Sub selectByUuid(String uuid);
    /**
     * 获取需要更新的订阅信息
     * @return
     */
    List<Sub> selectUpdateList();
}
