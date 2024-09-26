package io.github.yajuhua.podcast2.controller;

import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.mapper.SettingsMapper;
import io.github.yajuhua.podcast2.plugin.PluginManager;
import io.github.yajuhua.podcast2.pojo.entity.Plugin;
import io.github.yajuhua.podcast2.pojo.entity.Settings;
import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.Podcast2;
import io.github.yajuhua.podcast2API.setting.Setting;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


/**
 * 通用接口
 */
@RestController
@RequestMapping("/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private SettingsMapper settingsMapper;
    @Autowired
    private PluginManager pluginManager;


    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload/{desc}")
    @ApiOperation("文件上传")
    @Transactional
    public Result<String> upload(@PathVariable("desc") String desc,@RequestParam("files") MultipartFile file) throws Exception {
        log.info("文件上传：{}",file);

        //项目jar包上传和插件上传
        if ("core".equals(desc)){
            //项目zip传
            log.info("上传项目core文件:{}",file.getOriginalFilename());
        }else if ("plugin".equals(desc)){
            try {
                //插件上传
                Plugin plugin = pluginManager.add(file.getInputStream(), file.getOriginalFilename());
                if (plugin == null){
                    return Result.error(MessageConstant.UPLOAD_FAILED);
                }
                //保留配置
                List<Settings> settingsFromDB = settingsMapper.selectByPluginName(plugin.getName());
                settingsMapper.deleteByPlugin(plugin.getName());


                //获取之前设置
                List<Setting> settings = pluginManager.settingsToSetting(settingsFromDB);
                Params params = new Params();
                params.setSettings(settings);
                Podcast2 instance = pluginManager.getPluginInstance(UUID.fromString(plugin.getUuid()),params);

                //更新到最新设置
                pluginManager.initSettings(plugin.getName(),instance.settings());
                log.info("插件上传完成:{}",file.getOriginalFilename());
            } catch (Exception e) {
                log.error("上传插件失败",e);
                return Result.error("上传插件失败，请稍后再试。");
            }
        }else {
           return Result.error(MessageConstant.UPLOAD_FAILED);
        }
        return Result.success();
    }

}
