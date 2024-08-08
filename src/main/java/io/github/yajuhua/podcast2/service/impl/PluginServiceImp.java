package io.github.yajuhua.podcast2.service.impl;

import io.github.yajuhua.podcast2.mapper.PluginMapper;
import io.github.yajuhua.podcast2.pojo.entity.Plugin;
import io.github.yajuhua.podcast2.service.PluginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PluginServiceImp implements PluginService {

    @Autowired
    private PluginMapper pluginMapper;

    @Override
    public void insert(Plugin plugin) {
        pluginMapper.deleteByName(plugin.getName());
        pluginMapper.insert(plugin);
    }
}
