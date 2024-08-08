package io.github.yajuhua.podcast2.service.impl;

import io.github.yajuhua.podcast2.mapper.SubMapper;
import io.github.yajuhua.podcast2.pojo.entity.Sub;
import io.github.yajuhua.podcast2.service.SubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SubServiceImpI implements SubService {

    @Autowired
    private SubMapper subMapper;

    /**
     * 获取所有订阅
     * @return
     */
    public List<Sub> list() {
        return subMapper.list();
    }

    /**
     * 添加订阅
     * @param sub
     */
    public void addSub(Sub sub){
        subMapper.addSub(sub);
    }

    /**
     * 根据map条件查询
     * @param map
     * @return
     */
    public Sub selectByMap(Map map){
      return subMapper.selectByMap(map);
    }


    /**
     * 更新编辑后的sub
     * @param sub
     */
    public void commitEditSub(Sub sub) {
        subMapper.commitEditSub(sub);
    }

    /**
     * 根据uuid查询
     * @param uuid
     * @return
     */
    public Sub selectByUuid(String uuid){
        Map map = new HashMap();
        map.put("uuid",uuid);
        return subMapper.selectByMap(map);
    }


    /**
     * 获取需要更新的订阅
     * @return
     */
    public List<Sub> selectUpdateList() {
        return subMapper.selectUpdateList(System.currentTimeMillis());
    }

}
