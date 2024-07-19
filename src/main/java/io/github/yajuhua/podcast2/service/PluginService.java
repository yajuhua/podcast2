package io.github.yajuhua.podcast2.service;

import io.github.yajuhua.podcast2.pojo.entity.Plugin;

public interface PluginService {
    /**
     * 插入之前先删除同名的
     * @param plugin
     */
    void insert(Plugin plugin);
}
