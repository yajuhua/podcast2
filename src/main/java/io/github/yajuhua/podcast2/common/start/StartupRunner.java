package io.github.yajuhua.podcast2.common.start;

import com.google.gson.Gson;
import io.github.yajuhua.podcast2.common.constant.Default;
import io.github.yajuhua.podcast2.common.constant.StatusCode;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.properties.InfoProperties;
import io.github.yajuhua.podcast2.common.utils.DownloaderUtils;
import io.github.yajuhua.podcast2.controller.SystemController;
import io.github.yajuhua.podcast2.mapper.DownloaderMapper;
import io.github.yajuhua.podcast2.mapper.SubMapper;
import io.github.yajuhua.podcast2.mapper.UserMapper;
import io.github.yajuhua.podcast2.pojo.entity.Config;
import io.github.yajuhua.podcast2.pojo.entity.Downloader;
import io.github.yajuhua.podcast2.pojo.entity.Sub;
import io.github.yajuhua.podcast2.pojo.entity.User;
import io.github.yajuhua.podcast2.pojo.vo.DownloadProgressVO;
import io.github.yajuhua.podcast2.task.Task;
import io.github.yajuhua.podcast2.websocket.DownloadWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@Slf4j
public class StartupRunner implements ApplicationRunner{

    private Gson gson;
    private SubMapper subMapper;
    private InfoProperties infoProperties;
    private UserMapper userMapper;
    private DownloaderMapper downloaderMapper;
    private DataPathProperties dataPathProperties;
    private DownloadWebSocketServer downloadWebSocketServer;

    public StartupRunner() {
    }

    @Autowired
    public StartupRunner(Gson gson, SubMapper subMapper, InfoProperties infoProperties, UserMapper userMapper,
                         DownloaderMapper downloaderMapper, DataPathProperties dataPathProperties,
                         DownloadWebSocketServer downloadWebSocketServer) {
        this.gson = gson;
        this.subMapper = subMapper;
        this.infoProperties = infoProperties;
        this.userMapper = userMapper;
        this.downloaderMapper = downloaderMapper;
        this.dataPathProperties = dataPathProperties;
        this.downloadWebSocketServer = downloadWebSocketServer;
    }

    @Autowired


    @Override
    public void run(ApplicationArguments args) throws Exception {

        //记录启动时间
        SystemController.startTime = LocalDateTime.now();

        //首次
        firstConfig();

        //初始化
        initConfig();

        //将订阅状态全部恢复成NO_ACTION
        List<Sub> list = subMapper.list();
        list.forEach(new Consumer<Sub>() {
            @Override
            public void accept(Sub sub) {
                sub.setStatus(StatusCode.NO_ACTION);
            }
        });
        for (Sub sub : list) {
            subMapper.update(sub);
        }

        //如果下载少于3个则写入,检查更新下载器信息
        if (downloaderMapper.list().isEmpty() || downloaderMapper.list().size() < DownloaderUtils.Downloader.values().length){
            log.info("更新下载器信息");
            downloaderMapper.deleteAll();
            for (DownloaderUtils.Downloader downloader : DownloaderUtils.Downloader.values()) {
                Downloader build = Downloader.builder()
                        .name(downloader.toString())
                        .version(DownloaderUtils.getDownloaderVersion(downloader))
                        //只更新yt-dlp
                        .isUpdate(downloader.equals(DownloaderUtils.Downloader.YtDlp) ? 1 : 0)
                        .refreshDuration(24)
                        .updateTime(System.currentTimeMillis())
                        .build();
                downloaderMapper.insert(build);
            }
            log.info("下载器信息更新完成");
        }

        log.info("开启ws推送下载进度");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (Task.getDownloadProgressVOSet().size() != 0){
                    Set<DownloadProgressVO> downloadProgressVOSet = Task.getDownloadProgressVOSet();
                    for (DownloadProgressVO vo : downloadProgressVOSet) {
                        if (DownloaderUtils.endStatusCode().contains(vo.getStatus())){
                            Task.getDownloadProgressVOSet().remove(vo);
                        }
                    }
                    //清空前端
                    downloadWebSocketServer.sendToAllClient(gson.toJson(downloadProgressVOSet));
                }
            }
        },0,1, TimeUnit.SECONDS);
    }

    /**
     * 初始化配置
     */
    public void initConfig() throws Exception{
        File config = new File(dataPathProperties.getConfigPath());
        Config config2 = new Config();
        if (config.exists()){
            String configJson = FileUtils.readFileToString(config);
            Config config1 = gson.fromJson(configJson, Config.class);
            if (config1 != null && config1.isInitUserNameAndPassword()){
                String username = config1.isInitUserNameAndPassword()? Default.USERNAME:null;
                String password = config1.isInitUserNameAndPassword()? Default.PASSWORD:null;
                User user = userMapper.list().get(0);
                user.setUsername(username);
                user.setPassword(password);
                userMapper.update(user);
                log.info("修改为默认用户名和密码");
            }
        }
        //初始化
        FileUtils.write(config,gson.toJson(config2));
    }

    /**
     * 首次配置user表信息
     */
    public void firstConfig(){
        List<User> list = userMapper.list();
        if (list.size() == 0){
            userMapper.clear();
            User user = User.builder()
                    .username(Default.USERNAME)
                    .password(Default.PASSWORD)
                    .createTime( System.currentTimeMillis())
                    .uuid(UUID.randomUUID().toString())
                    .firstVersion(infoProperties.getVersion())
                    .hostname(null)
                    .autoUpdatePlugin(true)
                    .isSsl(false)
                    .hasSsl(false)
                    .build();
            userMapper.insert(user);
        }

    }
}
