package io.github.yajuhua.podcast2.service;

import io.github.yajuhua.podcast2.pojo.vo.KeyValue;

import java.util.List;

public interface SystemService {
    List<KeyValue> info() throws Exception;
    void refreshJobRunrStatus();
}
