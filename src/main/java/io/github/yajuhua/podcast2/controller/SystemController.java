package io.github.yajuhua.podcast2.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.yajuhua.download.commons.utils.CommonUtils;
import io.github.yajuhua.podcast2.Podcast2Application;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.properties.InfoProperties;
import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.common.utils.LogUtils;
import io.github.yajuhua.podcast2.mapper.SubMapper;
import io.github.yajuhua.podcast2.pojo.vo.KeyValue;
import io.github.yajuhua.podcast2.service.UserService;
import io.github.yajuhua.podcast2.update.ProjectUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private DataPathProperties dataPathProperties;
    public static ProjectUpdate projectUpdate;
    @Autowired
    private UserService userService;


    /**
     * 重启项目
     * @return
     */
    @ApiOperation("重启项目")
    @GetMapping("/restart")
    public Result restart() {

        //运行在docker环境中重启方式
        String runningInDocker = System.getenv("RUNNING_IN_DOCKER");
        if (runningInDocker != null && "true".equalsIgnoreCase(runningInDocker)){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Podcast2Application.context.close();
                    System.exit(0);
                }
            }).start();
            return Result.success();
        }

        //其他重启方式
        Podcast2Application.restart();
        return Result.success();
    }


    /**
     * 检查更新
     * @return
     */
    @ApiOperation("检查更新")
    @GetMapping("/update/has")
    public Result<ProjectUpdate.UpdateInfo> hasUpdate() throws Exception {
        ProjectUpdate.UpdateInfo updateInfo = ProjectUpdate.getUpdateInfo(infoProperties.getVersion());
        return Result.success(updateInfo);
    }

    /**
     * 下载最新jar包
     * @return
     */
    @ApiOperation("下载最新版本的Jar包")
    @GetMapping("/update/download")
    public Result downloadLatestJarFile(@RequestParam String version) throws Exception {
        try {
            if (ProjectUpdate.isSupportUpdate()){
                if (!ProjectUpdate.isDownload(version,dataPathProperties.getDataPath())){
                    String githubProxyUrl = userService.getExtendInfo().getGithubProxyUrl();
                    projectUpdate = new ProjectUpdate(version,githubProxyUrl,dataPathProperties.getDataPath());
                    projectUpdate.downloadJar();
                }
                return Result.success();
            }else {
                return Result.error("仅支持docker版本在线更新");
            }
        } catch (Exception e){
            log.error("下载异常",e);
            return Result.error("下载异常: " + e.getMessage());
        }
    }

    /**
     * 删除最新版本的Jar文件，如果已经下载的话
     * @param version
     * @return
     */
    @ApiOperation("删除最新版本的Jar文件")
    @GetMapping("/update/delete")
    public Result<Boolean> deleteDownloadLatestJarFile(@RequestParam String version){
        projectUpdate = null;
        boolean deleteDownloadJarFile = ProjectUpdate.deleteDownloadJarFile(version, dataPathProperties.getDataPath());
        return Result.success(deleteDownloadJarFile);
    }

    /**
     * 获取Jar包下载状态
     * @return
     */
    @ApiOperation("获取Jar包下载状态")
    @GetMapping("/update/jarStatus")
    public Result<ProjectUpdate.DownloadStatus> downloadJarFileStatus(@RequestParam String version) throws Exception {
        if (projectUpdate != null){
            return Result.success(projectUpdate.getStatus());
        }else {
            boolean download = ProjectUpdate.isDownload(version, dataPathProperties.getDataPath());
            return Result.success(ProjectUpdate.DownloadStatus.builder().isDownload(download).build());
        }
    }

    /**
     * 取消下载Jar文件
     * @return
     */
    @ApiOperation("取消下载Jar包")
    @GetMapping("/update/cancel")
    public Result cancelDownloadJarFile(){
        if (projectUpdate != null){
            try {
                projectUpdate.remove();
            } catch (Exception e) {
                log.error("取消下载Jar包失败",e);
                return Result.error(e.getMessage());
            }
        } return Result.success();
    }


    /**
     * 系统概况信息
     * @return
     */
    @ApiOperation("系统概况信息")
    @GetMapping("/info")
    public Result info() throws Exception{
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
        keyValueList.add(new KeyValue("commit",getCommitID()));

        return Result.success(keyValueList);
    }

    /**
     * 根据时间区间获取历史日志
     * @return
     */
    @ApiOperation("根据时间区间获取历史日志")
    @GetMapping("/logs/history/between")
    public Result<List<String>> historyLogsByDate(@RequestParam String start, @RequestParam String end, @RequestParam String level) throws Exception{
        //TODO 前端超过两个小时仅提供下载查看
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startLocalDateTime = LocalDateTime.parse(start,formatter);
        LocalDateTime endLocalDateTime = LocalDateTime.parse(end,formatter);
        List<String> logs = LogUtils.logs(startLocalDateTime, endLocalDateTime, new File(dataPathProperties.getLogsPath()), level);
        return Result.success(logs);
    }

    /**
     * 根据时间区间获取历史日志
     * @return
     */
    @ApiOperation("获取最近日志")
    @GetMapping("/logs/history/latest")
    public Result<List<String>> historyLogsByLatest(@RequestParam Long minutes, @RequestParam String level) throws Exception{
        List<String> logs = LogUtils.getRecent(minutes, TimeUnit.MINUTES, new File(dataPathProperties.getLogsPath()), level);
        return Result.success(logs);
    }

    /**
     * 获取git提交信息
     * @return
     * @throws Exception
     */
    public Properties getCommit() throws Exception{
        Resource resource = new DefaultResourceLoader().getResource("classpath:git.properties");
        if (resource.exists()) {
            InputStream inputStream = resource.getInputStream();
            Properties properties = new Properties();
            properties.load(inputStream);
            inputStream.close();
            return properties;
        } else {
            return new Properties();
        }
    }

    /**
     * 获取git提交ID
     * @return
     * @throws Exception
     */
    public String getCommitID() throws Exception{
        if (getCommit().containsKey("git.commit.id.abbrev")){
            return getCommit().get("git.commit.id.abbrev").toString();
        }else {
            return "none";
        }
    }


}
