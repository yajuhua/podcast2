package io.github.yajuhua.podcast2.common.start;

import com.google.gson.Gson;
import io.github.yajuhua.podcast2.common.constant.Default;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.properties.InfoProperties;
import io.github.yajuhua.podcast2.common.utils.DownloaderUtils;
import io.github.yajuhua.podcast2.controller.SystemController;
import io.github.yajuhua.podcast2.downloader.aria2.Aria2RPC;
import io.github.yajuhua.podcast2.mapper.DownloaderMapper;
import io.github.yajuhua.podcast2.mapper.SubMapper;
import io.github.yajuhua.podcast2.mapper.UserMapper;
import io.github.yajuhua.podcast2.pojo.entity.*;
import io.github.yajuhua.podcast2.pojo.vo.DownloadProgressVO;
import io.github.yajuhua.podcast2.service.UserService;
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
import java.util.stream.Collectors;
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
    private UserService userService;

    public StartupRunner() {
    }

    @Autowired
    public StartupRunner(Gson gson, SubMapper subMapper, InfoProperties infoProperties, UserMapper userMapper,
                         DownloaderMapper downloaderMapper, DataPathProperties dataPathProperties,
                         DownloadWebSocketServer downloadWebSocketServer, UserService userService) {
        this.gson = gson;
        this.subMapper = subMapper;
        this.infoProperties = infoProperties;
        this.userMapper = userMapper;
        this.downloaderMapper = downloaderMapper;
        this.dataPathProperties = dataPathProperties;
        this.downloadWebSocketServer = downloadWebSocketServer;
        this.userService = userService;
    }


    @Override
    public void run(ApplicationArguments args){

        //项目信息
        printProjectInfo();

        //记录启动时间
        SystemController.startTime = LocalDateTime.now();

        //首次
        firstConfig();

        //初始化
        initConfig();

        //启动aria2 RPC
        Aria2RPC.start();


        List<Downloader> downloaderList = downloaderMapper.list();
        if (downloaderList.isEmpty() || downloaderList.size() != 3){
            log.info("更新下载器信息");
            downloaderMapper.deleteAll();

            //yt-dlp
            Downloader downloader = null;
            if (DownloaderUtils.hasDownloader(DownloaderUtils.Downloader.YtDlp)) {
                downloader = Downloader.builder()
                        .name(DownloaderUtils.Downloader.YtDlp.name())
                        .version(DownloaderUtils.getDownloaderVersion(DownloaderUtils.Downloader.YtDlp))
                        //只更新yt-dlp
                        .isUpdate(1)
                        .refreshDuration(24)
                        .updateTime(System.currentTimeMillis())
                        .build();
                downloaderMapper.insert(downloader);
            }


            //aria2
            if (DownloaderUtils.hasDownloader(DownloaderUtils.Downloader.Aria2)) {
                downloader = Downloader.builder()
                        .name(DownloaderUtils.Downloader.Aria2.name())
                        .version(DownloaderUtils.getDownloaderVersion(DownloaderUtils.Downloader.Aria2))
                        .isUpdate(0)
                        .refreshDuration(24)
                        .updateTime(System.currentTimeMillis())
                        .build();
                downloaderMapper.insert(downloader);
            }

            //N_m3u8DL-RE
            if (DownloaderUtils.hasDownloader(DownloaderUtils.Downloader.Nm3u8DlRe)) {
                downloader = Downloader.builder()
                        .name(DownloaderUtils.Downloader.Nm3u8DlRe.name())
                        .version(DownloaderUtils.getDownloaderVersion(DownloaderUtils.Downloader.Nm3u8DlRe))
                        .isUpdate(0)
                        .refreshDuration(24)
                        .updateTime(System.currentTimeMillis())
                        .build();
                downloaderMapper.insert(downloader);
            }

            log.info("下载器信息更新完成");
        }

        log.info("开启ws推送下载进度");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Set<DownloadProgressVO> downloadProgressVOSet = Task.getDownloadProgressVOSet();
                for (DownloadProgressVO vo : downloadProgressVOSet) {
                    if (DownloaderUtils.endStatusCode().contains(vo.getStatus())){
                        Task.getDownloadProgressVOSet().remove(vo);
                    }
                }
                //排序
                List<DownloadProgressVO> collect = downloadProgressVOSet.stream().sorted(new Comparator<DownloadProgressVO>() {
                    @Override
                    public int compare(DownloadProgressVO o1, DownloadProgressVO o2) {
                        UUID u1 = UUID.fromString(o1.getUuid());
                        UUID u2 = UUID.fromString(o2.getUuid());
                        return u1.compareTo(u2);
                    }
                }).collect(Collectors.toList());
                //清空前端
                downloadWebSocketServer.sendToAllClient(gson.toJson(collect));
            }
        },0,300, TimeUnit.MILLISECONDS);
    }

    /**
     * 初始化配置
     */
    public void initConfig(){
        try {
            File configFile = new File(dataPathProperties.getConfigPath());
            if (configFile.exists()){
                User user = userMapper.list().get(0);
                String configJson = FileUtils.readFileToString(configFile);
                Config config1 = gson.fromJson(configJson, Config.class);
                if (config1 != null && config1.isInitUserNameAndPassword()){
                    String username = config1.isInitUserNameAndPassword()? Default.USERNAME:null;
                    String password = config1.isInitUserNameAndPassword()? Default.PASSWORD:null;
                    user.setUsername(username);
                    user.setPassword(password);
                    log.info("修改为默认用户名和密码");
                }
                if (config1 != null && config1.isInitPath()){
                    userService.updateExtendInfo(ExtendInfo.builder().path(null).build());
                    log.info("清空path");
                }

                userMapper.update(user);
            }
            //初始化
            FileUtils.write(configFile,gson.toJson(new Config()));
        } catch (Exception e) {
            log.error("初始化配置错误:{}",e.getMessage());
        }
    }

    /**
     * 首次配置user表信息
     */
    public void firstConfig(){
        List<User> list = userMapper.list();
        ExtendInfo extendInfo = ExtendInfo.builder().uuid(UUID.randomUUID().toString()).build();
        if (list.isEmpty()){
            User user = User.builder()
                    .username(Default.USERNAME)
                    .password(Default.PASSWORD)
                    .createTime( System.currentTimeMillis())
                    .uuid(gson.toJson(extendInfo))
                    .firstVersion(infoProperties.getVersion())
                    .hostname(null)
                    .autoUpdatePlugin(true)
                    .isSsl(false)
                    .hasSsl(false)
                    .build();
            userMapper.insert(user);
        }else {
            try {
                ExtendInfo extendInfo1 = gson.fromJson(list.get(0).getUuid(), ExtendInfo.class);
                if (extendInfo1.getAlistInfo() == null){
                    extendInfo1.setAlistInfo(AlistInfo.builder().build());
                }

                if (extendInfo1.getAddressFilter() == null){
                    extendInfo1.setAddressFilter(AddressFilter.builder()
                            .whitelist(new ArrayList<>())
                            .blacklist(new ArrayList<>())
                            .build());
                }
                userService.updateExtendInfo(extendInfo1);
            } catch (Exception e) {
                log.info("init extendInfo...");
                extendInfo = ExtendInfo.builder().uuid(UUID.randomUUID().toString())
                        .alistInfo(AlistInfo.builder().build())
                        .addressFilter(AddressFilter.builder()
                                .blacklist(new ArrayList<>())
                                .whitelist(new ArrayList<>())
                                .build()).
                        build();
                userService.updateExtendInfo(extendInfo);
            }
        }

    }

    /**
     * 打印项目信息
     */
    public void printProjectInfo(){
        String osArch = System.getProperty("os.arch");
        log.info("系统架构: {}",osArch);
        log.info("项目版本: {} - 更新时间: {}",infoProperties.getVersion(),infoProperties.getUpdate());
    }
}
