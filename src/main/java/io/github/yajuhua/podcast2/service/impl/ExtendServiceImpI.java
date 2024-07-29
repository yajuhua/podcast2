package io.github.yajuhua.podcast2.service.impl;

import io.github.yajuhua.podcast2.mapper.ExtendMapper;
import io.github.yajuhua.podcast2.pojo.entity.Extend;
import io.github.yajuhua.podcast2.service.ExtendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExtendServiceImpI implements ExtendService {

    @Autowired
    private ExtendMapper extendMapper;

    /**
     * 根据订阅uuid查询
     * @param uuid
     * @return
     */
    public List<Extend> selectByUuid(String uuid) {
        return extendMapper.selectByUuid(uuid);
    }

    /**
     * 添加扩展信息
     * @param extend
     */
    @Transactional
    public void addExtend(Extend extend) {
        extendMapper.addExtend(extend);
    }

    /**
     * 批量统计扩展信息
     * @param extendList
     */
    @Transactional
    public void batchExtend(List<Extend> extendList) {
        if (extendList != null && extendList.size() > 0){
            extendMapper.batchExtend(extendList);
        }
    }
}
