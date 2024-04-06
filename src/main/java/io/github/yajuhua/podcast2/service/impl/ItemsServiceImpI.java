package io.github.yajuhua.podcast2.service.impl;

import io.github.yajuhua.podcast2.mapper.ItemsMapper;
import io.github.yajuhua.podcast2.pojo.entity.Items;
import io.github.yajuhua.podcast2.service.ItemsService;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemsServiceImpI implements ItemsService {

    @Autowired
    private ItemsMapper itemsMapper;

    /**
     * 根据channelUuid查询
     * @param channelUuid
     * @return
     */
    public List<Items> selectByChannelUuid(String channelUuid) {
        Map map = new HashMap<>();
        map.put("channelUuid",channelUuid);
        List<Items> items = itemsMapper.selectByMap(map);
        return items;
    }


    /**
     * 批量插入
     * @param items
     */
    public void batchInsert(List<Items> items) {
        for (Items item : items) {
            itemsMapper.insert(item);
        }
    }
}
