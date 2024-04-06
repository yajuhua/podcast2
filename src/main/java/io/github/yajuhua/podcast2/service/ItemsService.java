package io.github.yajuhua.podcast2.service;

import io.github.yajuhua.podcast2.pojo.entity.Items;

import java.util.List;

public interface ItemsService {
    List<Items> selectByChannelUuid(String channelUuid);

    /**
     * 批量插入
     * @param items
     */
    void batchInsert(List<Items> items);
}
