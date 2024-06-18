package io.github.yajuhua.podcast2.controller;

import io.github.yajuhua.podcast2.Podcast2Application;
import io.github.yajuhua.podcast2.common.constant.StatusCode;
import io.github.yajuhua.podcast2.common.properties.InfoProperties;
import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.common.utils.Http;
import io.github.yajuhua.podcast2.common.utils.PluginLoader;
import io.github.yajuhua.podcast2.mapper.SubMapper;
import io.github.yajuhua.podcast2.pojo.entity.Sub;
import io.github.yajuhua.podcast2.pojo.vo.KeyValue;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@Slf4j
@Api(tags = "系统相关接口")
@RequestMapping("/system")
public class SystemController {

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    private InfoProperties infoProperties;
    public static LocalDateTime startTime;
    @Autowired
    private SubMapper subMapper;


    /**
     * 重启项目
     * @return
     */
    @ApiOperation("重启项目")
    @GetMapping("/restart")
    public Result restart() {
        Podcast2Application.restart();
        return Result.success();
    }


    /**
     * 检查更新
     * @return
     */
    @ApiOperation("检查更新")
    @GetMapping("/hasUpdate")
    public Result hasUpdate(){
        String versionUrl = "https://img.shields.io/github/v/release/yajuhua/podcast2";
        String html = Http.get(versionUrl);
        Pattern compile = Pattern.compile("\\<title\\>(?<versionStr>.*?)\\</title\\>");
        Matcher matcher = compile.matcher(html);
        String versionStr = null;
        String version = null;
        if (matcher.find()){
            versionStr = matcher.group("versionStr");
        }

        if (versionStr != null){
            version = versionStr.split("v")[1];
            int compareVersion = PluginLoader.compareVersion(version,infoProperties.getVersion().substring(1));
            if (compareVersion == 0){
                return Result.success("当前版本已是最新版");
            }else if(compareVersion == 1){
                return Result.success("目前最新版：" + version + "可拉取最新镜像进行更新。");
            }else if (infoProperties.getVersion().contains("beta")){
                return Result.success("当前版本为测试版");
            }else if(infoProperties.getVersion().contains("dev")){
                return Result.success("当前版本为开发版");
            }else if (compareVersion == -1){
                return Result.success("当前版本为待发布版本");
            }
        }

        return Result.success("无法获取新版本信息");
    }

    /**
     * 系统概况信息
     * @return
     */
    @ApiOperation("系统概况信息")
    @GetMapping("/info")
    public Result info(){
        Duration duration = Duration.between(startTime, LocalDateTime.now());
        long millis = duration.toMillis();
        long totalSecond = millis / 1000;
        long days = totalSecond / (24 * 3600);
        long hours = (totalSecond % (24 * 3600)) / 3600;
        long minutes = ((totalSecond % (24 * 3600)) % 3600) / 60;
        long seconds = ((totalSecond % (24 * 3600)) % 3600) % 60;

        String runningTime = days + "天 " + hours + "小时 " + minutes + "分钟 " + seconds + "秒";

        List<KeyValue> keyValueList = new ArrayList<>();
        keyValueList.add(new KeyValue("版本",infoProperties.getVersion()));
        keyValueList.add(new KeyValue("更新时间",infoProperties.getUpdate()));
        keyValueList.add(new KeyValue("运行时间",runningTime));

        return Result.success(keyValueList);
    }


}
