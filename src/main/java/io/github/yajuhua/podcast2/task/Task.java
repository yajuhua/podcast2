package io.github.yajuhua.podcast2.task;

import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.utils.DownloaderUtils;
import io.github.yajuhua.podcast2.common.utils.PluginLoader;
import io.github.yajuhua.podcast2.controller.PluginController;
import io.github.yajuhua.podcast2.mapper.*;
import io.github.yajuhua.podcast2.pojo.entity.Downloader;
import io.github.yajuhua.podcast2.pojo.entity.Items;
import io.github.yajuhua.podcast2.pojo.entity.Sub;
import io.github.yajuhua.podcast2.pojo.entity.User;
import io.github.yajuhua.podcast2.pojo.vo.DownloadProgressVO;
import io.github.yajuhua.podcast2.pojo.vo.PluginVO;
import io.github.yajuhua.podcast2.service.SubService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Task {

    public static Set<DownloadProgressVO> downloadProgressVOSet = new CopyOnWriteArraySet<>();
    public static Boolean addSubStatus = false;//避免添加订阅时更新yt-dlp
    public static Boolean updateStatus = false;//避免更新订阅时更新插件之类的
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SubService subService;
    @Autowired
    private ExtendMapper extendMapper;
    @Autowired
    private DataPathProperties dataPathProperties;
    @Autowired
    private DownloaderMapper downloaderMapper;
    @Autowired
    private SubMapper subMapper;
    @Autowired
    private ItemsMapper itemsMapper;
    @Autowired
    private SettingsMapper settingsMapper;
    @Autowired
    private PluginController pluginController;

    /**
     * 获取进度
     * @return
     */
    public static Set<DownloadProgressVO> getDownloadProgressVOSet(){
        return downloadProgressVOSet;
    }

    /**
     * 每隔分钟检查一次频道是否需要更新
     */
    @Scheduled(fixedDelay = 60000)
    public void updateSub(){
        Update update;
        try {
            //1.获取需要更新的订阅
            List<Sub> subList = subService.selectUpdateList();
            for (Sub sub : subList) {
                //标记正在更新
                update = new Update(sub, subService, extendMapper, dataPathProperties, subMapper, itemsMapper, settingsMapper);
                update.run();
            }
        } catch (Exception e) {
            log.error("更新异常:{}详细:{}",e.getMessage(),e.getStackTrace());
        }finally {
            Task.updateStatus = false;
            PluginLoader.closeAll();
            if (Task.downloadProgressVOSet != null){
                Task.downloadProgressVOSet.clear();
            }
        }
    }

    /**
     * 每小时删除过期节目
     */
    @Scheduled(cron = "0 0 * * * *")
    public void clearExpired(){
        try {
            List<Sub> subList = subMapper.list();
            for (Sub sub : subList) {
                //-1是永久的
                if (sub.getSurvivalTime() != -1){
                    Long survivalTime = sub.getSurvivalTime()*24*3600*1000;
                    List<Items> itemsList = itemsMapper.selectByChannelUUid(sub.getUuid());
                    itemsList =  itemsList.stream().filter(new Predicate<Items>() {
                        @Override
                        public boolean test(Items items) {
                            return items.getCreateTime() + survivalTime < System.currentTimeMillis();
                        }
                    }).collect(Collectors.toList());

                    for (Items items : itemsList) {
                        File[] list = new File(dataPathProperties.getResourcesPath()).listFiles();
                        for (File file : list) {
                            if (file.getName().contains(items.getUuid())){
                                log.info("删除过期节目:{}",file.getName());
                                FileUtils.forceDelete(file);
                                itemsMapper.deleteByUuid(items.getUuid());
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            log.error("删除过期文件异常");
        }
    }

    /**
     * 清除数据库未记录的文件,每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * *")
    public void clearNotFoundFile(){
        try {
            List<File> files = Arrays.asList(new File(dataPathProperties.getResourcesPath()).listFiles());
            List<Items> list = itemsMapper.list();
            List<File> deleteList = files.stream().filter(new Predicate<File>() {
                @Override
                public boolean test(File file) {
                    for (Items items : list) {
                        if (file.getName().contains(items.getUuid())){
                            return false;
                        }
                    }
                    return true;
                }
            }).collect(Collectors.toList());

            for (File file : deleteList) {
                if (file.exists()){
                    log.info("删除未记录的文件:{}",file.getName());
                    FileUtils.forceDelete(file);
                }
            }
        } catch (Exception e) {
            log.error("清除数据库未记录的文件异常");
        }
    }

    /**
     * 更新yt-dlp,每周日的23点执行
     */
    @Scheduled(cron = "0 0 23 * * 7 ")
    public void updateYtDlp() {
        try {
            log.info("检查更新yt-dlp");
            if (addSubStatus == true){
                log.info("正在添加订阅中，暂时无法更新yt-dlp");
                Thread.sleep(30*1000);//等待30秒
            }
            if (!addSubStatus){
                Downloader ytDlp = downloaderMapper.selectByName("YtDlp");
                Long latestUpdateTime = ytDlp.getUpdateTime();
                Integer refreshDuration = ytDlp.getRefreshDuration()*3600*1000;
                if ((latestUpdateTime + refreshDuration) > System.currentTimeMillis()){
                    //执行更新命令
                    log.info("执行更新yt-dlp");
                    DownloaderUtils.cmd("yt-dlp-update");
                    //TODO 更新数据库
                }
            }
            log.info("已完成检查更新yt-dlp");
        } catch (Exception e) {
            log.error("检查更新yt-dlp异常");
        }
    }

    /**
     * 每个两分钟检查一次
     */
    @Scheduled(fixedDelay = 120000)
    public void autoUpdatePlugin(){
        try {
            List<User> list = userMapper.list();
            if (list.size() != 0){
                Boolean autoUpdatePlugin = list.get(0).getAutoUpdatePlugin();
                if (autoUpdatePlugin != null){
                    if (autoUpdatePlugin){
                        List<PluginVO> pluginVOS = pluginController.list().getData();
                        //取出有更新的
                        pluginVOS = pluginVOS.stream().filter(new Predicate<PluginVO>() {
                            @Override
                            public boolean test(PluginVO pluginVO) {
                                return pluginVO.getHasUpdate();
                            }
                        }).collect(Collectors.toList());

                        //获取插件uuid
                        List<String> names = new ArrayList<>();
                        for (PluginVO vo : pluginVOS) {
                            log.info("更新插件:{}",vo.getName());
                            names.add(vo.getName());
                        }

                        //更新插件
                        if (names.size() > 0 && !addSubStatus){
                            pluginController.update(names);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查更新插件错误:{}",e.getMessage());
        }
    }
}
