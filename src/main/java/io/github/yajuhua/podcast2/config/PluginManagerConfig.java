package io.github.yajuhua.podcast2.config;

import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.properties.InfoProperties;
import io.github.yajuhua.podcast2.common.properties.RepoProperties;
import io.github.yajuhua.podcast2.mapper.*;
import io.github.yajuhua.podcast2.plugin.PluginManager;
import io.github.yajuhua.podcast2.pojo.entity.ExtendInfo;
import io.github.yajuhua.podcast2.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * 插件管理配置类
 */
@Configuration
@Slf4j
public class PluginManagerConfig {

    @Autowired
    private PluginMapper pluginMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RepoProperties repoProperties;

    @Autowired
    private DataPathProperties dataPathProperties;

    @Autowired
    private ExtendMapper extendMapper;

    @Autowired
    private SubMapper subMapper;

    @Autowired
    private SettingsMapper settingsMapper;

    @Autowired
    private InfoProperties infoProperties;



    @Bean
    public PluginManager pluginManager(){
        String remotePluginUrl;
        String remotePluginUrlDefault = repoProperties.getPluginUrl();
        String remotePluginUrlCustomzie = null;
        try {
            remotePluginUrlCustomzie = userService.getExtendInfo().getPluginUrl();
        } catch (Exception e) {
            log.warn("无法获取自定义插件仓库,使用默认插件仓库: {}",e.getMessage());
        }
        File pluginDir = new File(dataPathProperties.getLocalPluginPath());

        if (remotePluginUrlCustomzie == null || remotePluginUrlCustomzie.isEmpty()){
            remotePluginUrl = remotePluginUrlDefault;
        }else if (PluginManager.remoteRepoIsOK(remotePluginUrlCustomzie)){
            remotePluginUrl = remotePluginUrlCustomzie;
        }else {
            remotePluginUrl = remotePluginUrlDefault;
            userService.updateExtendInfo(ExtendInfo.builder().pluginUrl("").build());
        }

        PluginManager pluginManager = new PluginManager(pluginDir,remotePluginUrl,pluginMapper,subMapper,
                settingsMapper,extendMapper,userService,infoProperties);
        return pluginManager;
    }

}
