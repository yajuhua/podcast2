package io.github.yajuhua.podcast2.config;

import io.github.yajuhua.podcast2.alist.Alist;
import io.github.yajuhua.podcast2.pojo.entity.AlistInfo;
import io.github.yajuhua.podcast2.pojo.entity.ExtendInfo;
import io.github.yajuhua.podcast2.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * alist配置
 */
@Configuration
@Slf4j
public class AlistConfig {

    @Autowired
    private UserService userService;

    @Bean
    public Alist alist(){
        Alist alist = new Alist();
        try {
            ExtendInfo extendInfo = userService.getExtendInfo();
            if (extendInfo != null && extendInfo.getAlistInfo() != null){
                AlistInfo alistInfo = extendInfo.getAlistInfo();
                if (alistInfo.isOpen()){
                    alist.setUrl(alistInfo.getUrl());
                    alist.setUsername(alistInfo.getUsername());
                    alist.setPassword(alistInfo.getPassword());
                    alist.setPath(alistInfo.getPath());
                    alist.refreshToken();
                }
            }
        } catch (Exception e) {
            log.error("alist配置错误：{}",e.getMessage());
            userService.updateExtendInfo(ExtendInfo.builder().
                    alistInfo(AlistInfo.
                            builder().open(false).build()).build());
        }
        return alist;
    }
}
