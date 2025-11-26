package io.github.yajuhua.podcast2.task;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.Operation;
import io.github.yajuhua.download.commons.Type;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.podcast2.alist.Alist;
import io.github.yajuhua.podcast2.alist.dto.fs.PutDTO;
import io.github.yajuhua.podcast2.alist.dto.task.upload.InfoDTO;
import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.constant.SubStatusCode;
import io.github.yajuhua.podcast2.common.exception.ItemNotFoundException;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.properties.InfoProperties;
import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.common.utils.Http;
import io.github.yajuhua.podcast2.controller.DownloadController;
import io.github.yajuhua.podcast2.controller.PluginController;
import io.github.yajuhua.podcast2.downloader.ytdlp.YtDlpUpdate;
import io.github.yajuhua.podcast2.mapper.*;
import io.github.yajuhua.podcast2.plugin.PluginManager;
import io.github.yajuhua.podcast2.pojo.dto.AppendItemDTO;
import io.github.yajuhua.podcast2.pojo.entity.*;
import io.github.yajuhua.podcast2.pojo.vo.DownloadConfVO;
import io.github.yajuhua.podcast2.pojo.vo.DownloadProgressVO;
import io.github.yajuhua.podcast2.pojo.vo.PluginVO;
import io.github.yajuhua.podcast2.service.SubService;
import io.github.yajuhua.podcast2.service.UserService;
import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.Podcast2;
import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import io.github.yajuhua.podcast2API.setting.Setting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    public static List<DownloadManager> downloadManagerList = new ArrayList<>();//存放下载管理
    public static Map<String,List<LogMessage>> collectUpdateLogMessagesMap = new HashMap<>();//存放订阅更新日志
    public static List<Items> reDownloadItems = new ArrayList<>();//点击重新下载后会先存放到这
    public static List<AppendItemDTO> appendItemList = new ArrayList<>();//订阅追加节目
    public static final Map<UUID,String> backgroundTask = new HashMap<>();//后台任务
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
    @Autowired
    private Gson gson;
    @Autowired
    private PluginMapper pluginMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private Alist alist;
    @Autowired
    private InfoProperties infoProperties;

    @Autowired
    private PluginManager pluginManager;
    @Autowired
    private DownloadController downloadController;
    @Autowired
    private CronTaskManager cronTaskManager;

    /**
     * 获取进度
     * @return
     */
    public static Set<DownloadProgressVO> getDownloadProgressVOSet(){
        return downloadProgressVOSet;
    }

    public void updateSub(){
        log.info("开始检查更新订阅...");
        //获取还在更新的订阅
        List<Sub> hasUpdataSubList = subMapper.list().stream().filter(new Predicate<Sub>() {
            @Override
            public boolean test(Sub sub) {
                return sub.getIsUpdate() == 1 && sub.getSubType().equalsIgnoreCase("plugin");
            }
        }).collect(Collectors.toList());

        //获取schedule_type=cron
        List<Sub> cronSubList = hasUpdataSubList.stream().filter(new Predicate<Sub>() {
            @Override
            public boolean test(Sub sub) {
                return sub.getScheduleType().equalsIgnoreCase("cron");
            }
        }).collect(Collectors.toList());
        for (Sub cronSub : cronSubList) {
            long initialDelay = 0;
            long duration = System.currentTimeMillis() - cronSub.getCheckTime();
            if (duration < (cronSub.getCron() * 1000) ){
                initialDelay = ((cronSub.getCron() * 1000) - duration) / 1000;
            }
            Runnable task = new Update(cronSub, subService, extendMapper, dataPathProperties, subMapper, itemsMapper,
                    settingsMapper,pluginManager);
            cronTaskManager.add(cronSub.getUuid(), cronSub.getCron(), task, TimeUnit.SECONDS,
                    calculateUpdateSubTimeout(cronSub), "更新: " + cronSub.getTitle(), initialDelay);
        }

        //schedule_type=cronExpression
        List<Sub> cronExSubList = hasUpdataSubList.stream().filter(new Predicate<Sub>() {
            @Override
            public boolean test(Sub sub) {
                return sub.getScheduleType().equalsIgnoreCase("cron_expression");
            }
        }).collect(Collectors.toList());
        for (Sub cronExSub : cronExSubList) {
            Runnable task = new Update(cronExSub, subService, extendMapper, dataPathProperties, subMapper, itemsMapper,
                    settingsMapper,pluginManager);
            cronTaskManager.add(cronExSub.getUuid(), cronExSub.getCronExpression(),
                    task, TimeUnit.SECONDS, calculateUpdateSubTimeout(cronExSub), "更新: " + cronExSub.getTitle());
        }
    }

    /**
     * 计算更新订阅超时时间
     * @param sub
     * @return
     */
    public static long calculateUpdateSubTimeout(Sub sub){
        if (sub.getCustomEpisodes() == null){
            return 3 * TimeUnit.MINUTES.toMillis(30);
        }
        String[] customEpisodes = sub.getCustomEpisodes().split(",");
        int downloadItemNum = sub.getIsFirst().equals(1) && sub.getEpisodes().equals(-1)?30:1;
        downloadItemNum = sub.getIsFirst().equals(1)
                && !sub.getCustomEpisodes().isEmpty()?customEpisodes.length:downloadItemNum;
        return downloadItemNum * TimeUnit.MINUTES.toMillis(30);
    }

    /**
     * 每小时删除过期节目
     */
    public void clearExpired(){
        log.debug("删除过期节目");
        try {
            List<Sub> subList = subMapper.list();
            for (Sub sub : subList) {
                //-1是永久的
                if (sub.getSurvivalTime() != -1){
                    List<Items> itemsDeleteList = new ArrayList<>();
                    List<Items> itemsList = itemsMapper.selectByChannelUUid(sub.getUuid());
                    if (sub.getSurvivalWay().equalsIgnoreCase("keepTime")){
                        itemsDeleteList =  itemsList.stream().filter(new Predicate<Items>() {
                            @Override
                            public boolean test(Items items) {
                                //保留时间,旧版(2.5.0之前)数据库存放的是天数
                                Long survivalTime = sub.getSurvivalTime()*24*3600*1000;
                                if (sub.getSurvivalTime() > 30){
                                    //说明是以秒数为存活时间
                                    survivalTime = sub.getSurvivalTime()*1000;//转换成毫秒值
                                }
                                return items.getCreateTime() + survivalTime < System.currentTimeMillis();
                            }
                        }).collect(Collectors.toList());
                    }else if (sub.getSurvivalWay().equalsIgnoreCase("keepLast")){
                        //保留最近N集
                        //只有item中publicTime不为Null时支持；排序
                        itemsList = itemsList.stream().filter(new Predicate<Items>() {
                            @Override
                            public boolean test(Items items) {
                                return items.getPublicTime() != null;
                            }
                        }).sorted(new Comparator<Items>() {
                            @Override
                            public int compare(Items o1, Items o2) {
                                return Long.compare(o2.getPublicTime(),o1.getPublicTime());
                            }
                        }).collect(Collectors.toList());
                        //排除信息不完整
                        if ((sub.getKeepLast() != null) && (itemsList.size() > sub.getKeepLast())){
                             itemsDeleteList = itemsList.subList(sub.getKeepLast(),itemsList.size());
                        }
                    }

                    //执行删除操作
                    for (Items items : itemsDeleteList) {
                        Integer status = items.getStatus();
                        //删除本地文件
                        if (Context.COMPLETED == status){
                            File[] list = new File(dataPathProperties.getResourcesPath()).listFiles();
                            for (File file : list) {
                                if (file.getName().contains(items.getUuid())){
                                    log.info("删除过期节目:{}",file.getName());
                                    try {
                                        FileUtils.forceDelete(file);
                                        itemsMapper.deleteByUuid(items.getUuid());
                                    } catch (IOException e) {
                                        log.error("删除过期节目失败：{}",e.getMessage());
                                    }
                                }
                            }
                        }else if (Context.ALIST_UPLOAD_SUCCESS == status){
                            //删除AList的资源
                            log.info("删除过期文件：{}",items.getFileName());
                            alist.deleteFile(items.getFileName());
                            itemsMapper.deleteByUuid(items.getUuid());
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
    public void clearNotFoundFile(){
        log.debug("清除数据库未记录的文件");
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
            log.error("清除数据库未记录的文件异常：{}",e.getMessage());
        }
    }

    /**
     * 更新yt-dlp,每小时执行一次
     */
    public void updateYtDlp() {
        try {
            log.info("检查更新yt-dlp");
            if (addSubStatus == true){
                log.info("正在添加订阅中，暂时无法更新yt-dlp");
                Thread.sleep(30*1000);//等待30秒
            }
            if (!addSubStatus){
                Downloader ytDlp = downloaderMapper.selectByName("YtDlp");
                log.info("开始更新yt-dlp");
                String githubProxyUrl = userService.getExtendInfo().getGithubProxyUrl();
                //使用Github加速站更新yt-dlp,仅支持stable频道
                if (githubProxyUrl != null && (ytDlp.getUpdateArgs() == null || ytDlp.getUpdateArgs().isEmpty())){
                    String tmpPath = dataPathProperties.getTmpPath();
                    YtDlpUpdate.withProxy(githubProxyUrl, tmpPath);
                }else if (ytDlp.getUpdateArgs() != null && !ytDlp.getUpdateArgs().isEmpty()){
                    //更新到指定频道
                    YtDlpUpdate.withUpdateArgs(ytDlp.getUpdateArgs());
                }else {
                    //默认更新到官方stable频道
                    YtDlpUpdate.updateToStable();
                }
                //更新数据库
                ytDlp.setUpdateTime(System.currentTimeMillis());
                ytDlp.setVersion(YtDlpUpdate.getCurrentVersion());
                downloaderMapper.update(ytDlp);
            }
            log.info("已完成检查更新yt-dlp");
        } catch (Exception e) {
            log.error("检查更新yt-dlp异常: {}", e.getMessage());
        }
    }

    /**
     * 每个两分钟检查一次
     */
    public void autoUpdatePlugin(){
        log.debug("扫描插件自动更新");
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


    /**
     * 上传节目资源到AList
     * 每一分钟检查一次
     */
    public void uploadResourcesToAList(){
        log.debug("上传资源到Alist");
        try {
            if (userMapper.list().isEmpty()){
                //首次部署时可能user还没初始化
                log.warn("用户信息未初始化");
                return;
            }
            AlistInfo alistInfo = userService.getExtendInfo().getAlistInfo();
            if (alistInfo != null && alistInfo.isOpen() && alist.isConnect()){
                //需要上传的状态码：5、!28
                List<Items> uploadItems = itemsMapper.list().stream().filter(items ->
                        Context.COMPLETED == items.getStatus()).collect(Collectors.toList());
                uploadItems.addAll(DownloadController.reUploadItems);
                String resourcesPath = dataPathProperties.getResourcesPath();

                for (Items uploadItem : uploadItems) {
                    PutDTO putDTO = null;
                    String filePath = new File(resourcesPath + uploadItem.getFileName()).getPath();
                    try {

                        //移除记录
                        DownloadController.reUploadItems.remove(uploadItem);
                        //订阅状态为21是存放本地，22是存放alist
                        Sub sub = subMapper.selectByUuid(uploadItem.getChannelUuid());
                        if (sub == null || sub.getStatus() != SubStatusCode.SAVE_ALIST){
                            continue;
                        }

                        //1是正在上传 2是上传完成
                        log.info("开始上传：{}",filePath);
                        putDTO = alist.addUploadFileTask(filePath);
                        //排除异常情况
                        Integer aListState = putDTO.getData().getTask().getState();
                        if (200 != putDTO.getCode() || (0!=aListState && 1!=aListState && 2!=aListState)){
                           throw new RuntimeException();
                        }

                        //获取上传任务状态
                        InfoDTO taskInfo;
                        Integer taskState;
                        while (true){
                            taskInfo = alist.getUploadTaskInfo(putDTO.getData().getTask().getId());
                            taskState = taskInfo.getData().getState();
                            if (200 != taskInfo.getCode() || (1!=taskState && 2!=taskState)){
                                uploadItem.setStatus(Context.ALIST_UPLOAD_ERR);
                                itemsMapper.update(uploadItem);
                                break;
                            }else if (200 == taskInfo.getCode() && 2 == taskState){
                                uploadItem.setStatus(Context.ALIST_UPLOAD_SUCCESS);
                                itemsMapper.update(uploadItem);
                                log.info("上传成功：{}",taskInfo.getData().getName());
                                try {
                                    DownloadController.reUploadItems.remove(uploadItem);
                                    FileUtils.forceDelete(new File(filePath));
                                    log.info("删除本地文件成功：{}",filePath);
                                } catch (IOException e) {
                                    log.error("删除本地文件错误：{}",e.getMessage());
                                }
                                break;
                            }
                        }

                    }catch (OutOfMemoryError error){
                        log.error("内存过小，无法上传大文件");
                        uploadItem.setStatus(Context.ALIST_UPLOAD_OUT_OF_MEMORY);
                        itemsMapper.update(uploadItem);
                    } catch (RuntimeException e) {
                        log.error("上传失败 - 文件：{} - 详细：{}",uploadItem.getFileName(),e.getMessage());
                        if (e.getMessage().contains("文件不存在")){
                            uploadItem.setStatus(Context.DOWNLOAD_ERR);
                        }else {
                            uploadItem.setStatus(Context.ALIST_UPLOAD_ERR);
                        }
                        itemsMapper.update(uploadItem);
                    }
                }
            }
        } catch (Exception e) {
            log.error("alist上传任务失败：{}",e.getMessage());
        }


    }

    /**
     * 每24小时刷新一次AList的token
     */
    public void refreshAListToken(){
        log.debug("刷新AList的token");
        try {
            List<User> list = userMapper.list();
            //首次部署时可能user还没初始化
            if (list.isEmpty()){
                log.warn("用户信息未初始化");
               return;
            }
            AlistInfo alistInfo = userService.getExtendInfo().getAlistInfo();
            if (alistInfo.isOpen()){
                alist.refreshToken();
            }
        } catch (Exception e) {
            log.error("刷新AListToken异常：{}",e.getMessage());
        }
    }

    /**
     * 收集订阅更新日志信息
     * @param uuid
     * @param msg
     * @param level
     */
    public static void collectUpdateLogMessages(String uuid, String msg, String level){
        Map<String, List<LogMessage>> sources = collectUpdateLogMessagesMap;
        List<LogMessage> logMessages = sources.get(uuid);
        if (logMessages == null){
            logMessages = new ArrayList<>();
        } else if (logMessages.size() >= 10) {
            logMessages.remove(0);
        }
        logMessages.add(LogMessage.builder().msg(msg).level(level.toLowerCase()).build());
        sources.put(uuid,logMessages);
    }

    /**
     * 点击重新下载后会先提交到reDownloadItems集合中，每分钟轮询一次，如果有就下载
     */
    public void reDownloadTask(){
        log.debug("扫描重新下载队列");
        if (!reDownloadItems.isEmpty()){
            for (Items items : reDownloadItems) {
                try {
                    //先把之前的删除掉，不然在格式转换时出现错误
                    List<File> before = Arrays.stream(new File(dataPathProperties.getResourcesPath()).listFiles())
                            .filter(file -> file.getName().contains(items.getUuid())).collect(Collectors.toList());
                    try {
                        for (File file : before) {
                            log.info("删除文件:{}",file.getName());
                            FileUtils.forceDelete(file);
                        }
                    } catch (Exception e) {
                        throw new ItemNotFoundException(MessageConstant.ITEMS_RESOURCE_DELETE_FAILED);

                    }
                    Map args = gson.fromJson(items.getArgs(), Map.class);
                    List<String> links = gson.fromJson(items.getLinks(),new TypeToken<List<String>>() {}.getType());

                    //解析枚举
                    Type type = Type.valueOf(items.getType());
                    DownloadManager.Downloader downloader = DownloadManager.Downloader.valueOf(items.getDownloader());
                    Operation operation = Operation.valueOf(items.getOperation());
                    Request build = null;
                    if (items.getInputAndSelectDataList() != null && !items.getInputAndSelectDataList().isEmpty()){
                        /* 如果有数据就直接使用items中的 */
                        //获取二级域名
                        String host = new URL(items.getLink()).getHost();
                        String secondLevelDomain = Http.getSecondLevelDomain(host);

                        //获取参数
                        List<InputAndSelectData> inputAndSelectDataList = new ArrayList<>();
                        Result<DownloadConfVO> downloadConf =
                                downloadController.getDownloadConf(items.getUuid());
                        inputAndSelectDataList.addAll(downloadConf.getData().getInputListData());
                        inputAndSelectDataList.addAll(downloadConf.getData().getSelectListData());
                        Params params = pluginManager.getParams();
                        params.setType(io.github.yajuhua.podcast2API.Type
                                .valueOf(downloadConf.getData().getType()));
                        params.setInputAndSelectDataList(inputAndSelectDataList);

                        //插件设置信息
                        List<Settings> settingsFromDB = settingsMapper.selectByPluginName(secondLevelDomain);
                        List<Setting> settings = new ArrayList<>();
                        for (Settings settings1 : settingsFromDB) {
                            Setting setting = new Setting();
                            BeanUtils.copyProperties(settings1,setting);
                            settings.add(setting);
                        }
                        params.setSettings(settings);

                        //获取插件实例
                        Podcast2 instance = pluginManager.getPluginInstanceByDomainName(secondLevelDomain,params);
                        build = instance.getRequest(items.getLink());
                        build.setChannelUuid(items.getChannelUuid());
                        build.setDir(new File(dataPathProperties.getResourcesPath()));
                        build.setUuid(items.getUuid());
                        ReConfDownload reConfDownload = new ReConfDownload(build, itemsMapper, subMapper, pluginManager);
                        reConfDownload.run();
                    }else {
                        /* 构建下载请求 默认是*/
                        build = Request.builder()
                                .links(links)
                                .type(type)
                                .operation(operation)
                                .downloader(downloader)
                                .args(args)
                                .dir(new File(dataPathProperties.getResourcesPath()))
                                .channelUuid(items.getChannelUuid())
                                .uuid(items.getUuid())
                                .build();
                        ReDownload reDownload = new ReDownload(build, itemsMapper, subMapper, pluginMapper, dataPathProperties,
                                settingsMapper, pluginManager);
                        reDownload.run();
                    }

                } catch (Exception e) {
                   log.error("{}重新下载移除: {}",items.getUuid(),e.getMessage());
                }
            }
            //清空
            reDownloadItems.clear();
            //关闭所有类加载器
            PluginManager.closeAllClassLoader();
        }
    }

    /**
     * 下载订阅追加节目
     */
    public void downloadAppendItemList(){
        log.debug("扫描下载订阅追加姐");
        for (AppendItemDTO appendItem : appendItemList) {
            try {
                DownloadItem downloadItem = new DownloadItem(appendItem,itemsMapper,subMapper
                        ,pluginManager,dataPathProperties,settingsMapper);
                downloadItem.run();
            } catch (Exception e) {
                log.error("{}追加节目下载失败: {}",appendItem.getUrl(),e.getMessage());
            }
        }
        //清空列表
        appendItemList.clear();
        //关闭所有插件资源
        PluginManager.closeAllClassLoader();
    }
}
