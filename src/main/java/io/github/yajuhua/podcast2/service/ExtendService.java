package io.github.yajuhua.podcast2.service;


import io.github.yajuhua.podcast2.pojo.entity.Extend;

import java.util.List;

public interface ExtendService {
    List<Extend> selectByUuid(String uuid);
    void addExtend(Extend extend);
    void batchExtend(List<Extend> extendList);
}
