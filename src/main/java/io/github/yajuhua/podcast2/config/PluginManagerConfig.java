package io.github.yajuhua.podcast2.config;

import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.properties.RepoProperties;
import io.github.yajuhua.podcast2.mapper.PluginMapper;
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
    @Bean
    public PluginManager pluginManager(){
        String remotePluginUrl;
        String remotePluginUrlDefault = repoProperties.getPluginUrl();
        String remotePluginUrlCustomzie = userService.getExtendInfo().getPluginUrl();
        File pluginDir = new File(dataPathProperties.getLocalPluginPath());

        if (remotePluginUrlCustomzie == null || remotePluginUrlCustomzie.isEmpty()){
            remotePluginUrl = remotePluginUrlDefault;
        }else if (PluginManager.remoteRepoIsOK(remotePluginUrlCustomzie)){
            remotePluginUrl = remotePluginUrlCustomzie;
        }else {
            remotePluginUrl = remotePluginUrlDefault;
            userService.updateExtendInfo(ExtendInfo.builder().pluginUrl("").build());
        }

        PluginManager pluginManager = new PluginManager(pluginDir,remotePluginUrl,pluginMapper);
        return pluginManager;
    }

}
