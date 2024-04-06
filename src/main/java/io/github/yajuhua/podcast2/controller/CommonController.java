package io.github.yajuhua.podcast2.controller;

import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.constant.PluginPropertiesKey;
import io.github.yajuhua.podcast2.common.exception.PluginNotFoundException;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.common.utils.PluginLoader;
import io.github.yajuhua.podcast2.mapper.PluginMapper;
import io.github.yajuhua.podcast2.pojo.entity.Plugin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * 通用接口
 */
@RestController
@RequestMapping("/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private DataPathProperties dataPathProperties;
    @Autowired
    private PluginMapper pluginMapper;
    public static List<File> tmpFileList = new ArrayList<>();//临时文件


    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload/{desc}")
    @ApiOperation("文件上传")
    @Transactional
    public Result<String> upload(@PathVariable("desc") String desc,@RequestParam("file") MultipartFile file) throws Exception {
        log.info("文件上传：{}",file);

        //项目jar包上传和插件上传
        if ("core".equals(desc)){
            //项目zip传
            log.info("上传项目core文件:{}",file.getOriginalFilename());
        }else if ("plugin".equals(desc)){
            //插件上传
            InputStream inputStream1 = file.getInputStream();
            InputStream inputStream2 = file.getInputStream();
            log.info("上传插件文件:{}",file.getOriginalFilename());
            String savePath = dataPathProperties.getTmpPath() + file.getOriginalFilename();
            IOUtils.copyLarge(inputStream1,new FileOutputStream(savePath));
            inputStream1.close();

            //插件信息
            //读取插件properties
            Properties properties = PluginLoader.getProperties(savePath);
            String uuid = properties.getProperty(PluginPropertiesKey.UUID);
            String version = properties.getProperty(PluginPropertiesKey.VERSION);
            String name = properties.getProperty(PluginPropertiesKey.NAME);
            String update = properties.getProperty(PluginPropertiesKey.UPDATE);

            List<Class> classList = null;
            try {
                classList = PluginLoader.selectByName(name, dataPathProperties);
            } catch (PluginNotFoundException e) {
                log.info(e.getMessage());
            }
            if (classList != null){
                //有重复的，删除之前的
                List<File> files = PluginLoader.selectPluginFileByName(name, dataPathProperties);
                PluginLoader.closeAll();
                for (File file1 : files) {
                    FileUtils.forceDelete(file1);
                    pluginMapper.deleteByName(name);
                }
            }

            // 并写入数据库
            pluginMapper.insert(Plugin.builder()
                    .uuid(uuid)
                    .version(version)
                    .name(name)
                    .updateTime(update)
                    .build());

            //移到plugin目录，并删除当前的
            savePath = dataPathProperties.getLocalPluginPath() + file.getOriginalFilename();
            IOUtils.copyLarge(inputStream2,new FileOutputStream(savePath));
            savePath = dataPathProperties.getTmpPath() + file.getOriginalFilename();
            PluginLoader.closeAll();
            inputStream2.close();
            inputStream1.close();
            System.gc();
            tmpFileList.add(new File(savePath));
            log.info("插件上传完成:{}",file.getOriginalFilename());
        }else {
           return Result.error(MessageConstant.UPLOAD_FAILED);
        }
        return Result.success();
    }

}
