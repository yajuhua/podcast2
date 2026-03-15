package io.github.yajuhua.podcast2.task.scheduler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.Operation;
import io.github.yajuhua.download.commons.Type;
import io.github.yajuhua.download.commons.progress.DownloadProgress;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.podcast2.alist.Alist;
import io.github.yajuhua.podcast2.alist.dto.fs.PutDTO;
import io.github.yajuhua.podcast2.alist.dto.task.upload.InfoDTO;
import io.github.yajuhua.podcast2.annotation.JobTimeout;
import io.github.yajuhua.podcast2.common.constant.*;
import io.github.yajuhua.podcast2.common.context.JobTimeoutContext;
import io.github.yajuhua.podcast2.common.exception.ItemNotFoundException;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.common.utils.CronUtils;
import io.github.yajuhua.podcast2.common.utils.DownloaderUtils;
import io.github.yajuhua.podcast2.common.utils.Http;
import io.github.yajuhua.podcast2.controller.DownloadController;
import io.github.yajuhua.podcast2.controller.PluginController;
import io.github.yajuhua.podcast2.downloader.ytdlp.YtDlpUpdate;
import io.github.yajuhua.podcast2.handler.ws.SystemInfoWebSocketHandler;
import io.github.yajuhua.podcast2.mapper.*;
import io.github.yajuhua.podcast2.plugin.PluginManager;
import io.github.yajuhua.podcast2.pojo.dto.AppendItemDTO;
import io.github.yajuhua.podcast2.pojo.entity.*;
import io.github.yajuhua.podcast2.pojo.vo.DownloadConfVO;
import io.github.yajuhua.podcast2.pojo.vo.DownloadProgressVO;
import io.github.yajuhua.podcast2.pojo.vo.PluginVO;
import io.github.yajuhua.podcast2.service.DownloadService;
import io.github.yajuhua.podcast2.service.JobRunrService;
import io.github.yajuhua.podcast2.service.SystemService;
import io.github.yajuhua.podcast2.service.UserService;
import io.github.yajuhua.podcast2.task.TaskRegistry;
import io.github.yajuhua.podcast2.task.runnable.Update;
import io.github.yajuhua.podcast2.task.runnable.DownloadItem;
import io.github.yajuhua.podcast2.task.runnable.ReConfDownload;
import io.github.yajuhua.podcast2.task.runnable.ReDownload;
import io.github.yajuhua.podcast2API.Channel;
import io.github.yajuhua.podcast2API.Item;
import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.Podcast2;
import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import io.github.yajuhua.podcast2API.setting.Setting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 任务
 */
@Component
@Slf4j
public class Jobs {
    @Autowired
    private DataPathProperties dataPathProperties;
    @Autowired
    private ItemsMapper itemsMapper;
    @Autowired
    private SubMapper subMapper;
    @Autowired
    private Alist alist;
    public static Boolean addSubStatus = false;//避免添加订阅时更新yt-dlp
    @Autowired
    private DownloaderMapper downloaderMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PluginController pluginController;
    @Autowired
    private Gson gson;
    @Autowired
    private PluginManager pluginManager;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private SettingsMapper settingsMapper;
    @Autowired
    private PluginMapper pluginMapper;
    @Autowired
    private ExtendMapper extendMapper;
    @Autowired
    private JobScheduler jobScheduler;
    @Autowired
    private JobRunrService jobRunrService;
    @Autowired
    private SystemInfoWebSocketHandler systemInfoWebSocketHandler;
    @Autowired
    private SystemService systemService;


    /**
     * 清除数据库未记录的文件
     */
    @JobTimeout
    public void clearNotFoundFile(JobTimeoutContext jobTimeoutContext){
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
     * 删除过期节目
     */
    @JobTimeout
    public void clearExpired(JobTimeoutContext jobTimeoutContext){
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
     * 更新yt-dlp
     */
    @JobTimeout
    public void updateYtDlp(JobTimeoutContext jobTimeoutContext) {

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
    @JobTimeout
    public void autoUpdatePlugin(JobTimeoutContext jobTimeoutContext){
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
    @JobTimeout
    public void uploadResourcesToAList(JobTimeoutContext jobTimeoutContext){
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
                //每个item超时时间设置为30分钟
                jobTimeoutContext.setTimeout(Duration.ofMinutes(uploadItems.size() * 30L));
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
    @JobTimeout
    public void refreshAListToken(JobTimeoutContext jobTimeoutContext){
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
     * 重新下载
     */
    public void reDownloadTask(String uuid){
        log.info("重新下载: {}", uuid);
        Items items = itemsMapper.selectByUuid(uuid);
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
                        downloadService.getDownloadConf(items.getUuid());
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
            log.error("{}重新下载异常: {}",items.getUuid(),e.getMessage());
        }
        //关闭所有类加载器
        PluginManager.closeAllClassLoader();
    }


    /**
     * 重新下载
     */
    @JobTimeout
    public void reDownloadTaskList(JobTimeoutContext jobTimeoutContext){
        //每个超时时间设置为30分钟
        jobTimeoutContext.setTimeout(Duration.ofMinutes(TaskRegistry.reDownloadItems.size() * 30));
        for (Items item : TaskRegistry.reDownloadItems) {
            reDownloadTask(item.getUuid());
        }
        TaskRegistry.reDownloadItems.clear();
    }

    /**
     * 下载订阅追加节目
     */
    @JobTimeout("PT30M")
    public void downloadAppendItemList(AppendItemDTO appendItem, JobTimeoutContext jobTimeoutContext){
        log.debug("下载订阅追加节目");
        try {
            DownloadItem downloadItem = new DownloadItem(appendItem,itemsMapper,subMapper
                    ,pluginManager,dataPathProperties,settingsMapper);
            downloadItem.run();
        } catch (Exception e) {
            log.error("{}追加节目下载失败: {}",appendItem.getUrl(),e.getMessage());
        }
        //关闭所有插件资源
        PluginManager.closeAllClassLoader();
    }

    /**
     * 更新订阅
     * @param uuid
     */
    @JobTimeout
    public void updateSub(String uuid, JobTimeoutContext jobTimeoutContext){
        Sub sub = null;
        try {
            sub = subMapper.selectByUuid(uuid);
            if (sub == null){
                log.error("订阅: {} 不存在", uuid);
                jobRunrService.deleteJob(uuid);
                return;
            }
            List<Extend> anExtends = extendMapper.selectByUuid(sub.getUuid());
            List<InputAndSelectData> inputAndSelectDataList = new ArrayList<>();
            Integer isFirst = sub.getIsFirst();
            Integer episodes = sub.getEpisodes();
            String customEpisodes = sub.getCustomEpisodes();
            List<Integer> es = new ArrayList<>();
            log.info("开始检查更新: {}",sub.getTitle());

            //标记更新状态
            TaskRegistry.updateStatus = true;

            if (isFirst == StatusCode.YES) {
                log.info("进入首次更新: {}",sub.getTitle());
                //进入首次更新
                if (episodes == EpisodesStatus.LATEST) {
                    //最新一集
                    es.add(episodes);
                    jobTimeoutContext.setTimeout(Duration.ofMinutes(30));
                } else if (episodes == EpisodesStatus.LATEST_30) {
                    //最新30集
                    es.add(EpisodesStatus.LATEST_30);
                    jobTimeoutContext.setTimeout(Duration.ofMinutes(30 * 30));
                } else if (episodes == EpisodesStatus.CUSTOMIZE) {
                    //自定义剧集
                    List<String> list = Arrays.asList(customEpisodes.split(","));
                    for (String s : list) {
                        es.add(Integer.parseInt(s));
                    }
                    jobTimeoutContext.setTimeout(Duration.ofMinutes(es.size() * 30));
                }
            }


            for (Extend extend : anExtends) {
                InputAndSelectData inputAndSelectData = new InputAndSelectData();
                inputAndSelectData.setContent(extend.getContent());
                inputAndSelectData.setName(extend.getName());
                inputAndSelectDataList.add(inputAndSelectData);
            }
            //构建参数
            Params params = new Params();
            params.setUrl(sub.getLink());
            params.setType(io.github.yajuhua.podcast2API.Type.valueOf(sub.getType()));
            params.setInputAndSelectDataList(inputAndSelectDataList);
            params.setEpisodes(es);
            //传入插件设置
            List<Settings> settingsFromDB = settingsMapper.selectByPluginName(sub.getPlugin());
            List<Setting> settings = new ArrayList<>();
            for (Settings settings1 : settingsFromDB) {
                Setting setting = new Setting();
                BeanUtils.copyProperties(settings1,setting);
                settings.add(setting);
            }
            params.setSettings(settings);

            //返回插件设置用于更新
            List<Setting> resultSettings = pluginManager.getPluginInstanceByDomainName(sub.getPlugin(),params).settings();

            //避免清空之前的设置数据
            if (resultSettings != null && resultSettings.size() > 0){
                settingsMapper.deleteByPlugin(sub.getPlugin());
            }
            for (Setting resultSetting : resultSettings) {
                Settings settings1 = new Settings();
                BeanUtils.copyProperties(resultSetting,settings1);
                settings1.setPlugin(sub.getPlugin());
                settingsMapper.insert(settings1);
            }

            //获取items
            List<Item> items = new ArrayList<>();
            Object invoke;
            String json;
            if (isFirst == StatusCode.YES) {
                //首次更新，获取Items
                items = pluginManager.getPluginInstanceByDomainName(sub.getPlugin(),params).items();
            }else if (sub.getSyncWay().equalsIgnoreCase("recent")){
                //同步最近
                items.addAll(recentItems(sub.getUuid(),params,sub));
            } else {
                //次次更新，latest
                items.add(pluginManager.getPluginInstanceByDomainName(sub.getPlugin(),params).latestItem());
            }

            //添加后又立即删除
            if (subMapper.selectByUuid(sub.getUuid()) == null){
                //已经被删除了
                log.info("已被删除: {}",sub.getTitle());
                return;
            }

            //构建下载
            DownloadManager downloadManager = new DownloadManager(1,1,items.size());
            TaskRegistry.downloadManagerList.add(downloadManager);
            //过滤器
            List<String> titlekeyWords = Arrays.asList(sub.getTitleKeywords());
            List<String> desckeyWords = Arrays.asList(sub.getDescKeywords());

            //检查该订阅是否继续更新
            Channel channel = pluginManager.getPluginInstanceByDomainName(sub.getPlugin(), params).channel();
            sub.setIsUpdate(channel.getStatus());

            //获取节目列表
            List<Item> filterItems = new ArrayList<>();
            for (Item item : items) {
                if (item.getEqual().equals(sub.getEqual())) {
                    log.info("{}: 暂无更新",sub.getTitle());
                    //加入当前时间浮动，让每次检查时间不一样 往后
                    sub.setCheckTime(nowTimeFloat(1,1,10, Update.Units.Minutes));
                    //更新sub表
                    subMapper.update(sub);
                    return;
                }
                if (sub.getIsFilter() == StatusCode.YES) {
                    boolean hasFilter = true;
                    //标题过滤
                    for (String titleK : titlekeyWords) {
                        if (!(Pattern.compile(titleK).matcher(item.getTitle()).find() || titleK == "" || titleK == null)) {
                            hasFilter = false;
                        }
                    }

                    //描述过滤
                    for (String descK : desckeyWords) {
                        if (!(Pattern.compile(descK).matcher(item.getDescription()).find() || descK == "" || descK == null)) {
                            hasFilter = false;
                        }
                    }

                    //过滤时长
                    Integer maxDuration = sub.getMaxDuration();
                    Integer minDuration = sub.getMinDuration();
                    int duration = item.getDuration();
                    if (maxDuration >= 1 && minDuration >= 0) {
                        if (!(duration <= maxDuration && duration >= minDuration)) {
                            hasFilter = false;
                        }
                    } else if (minDuration <= 0 && maxDuration <= 0) {
                        //无过滤
                    } else if (minDuration <= 0) {
                        if (!(duration >= minDuration)) {
                            hasFilter = false;
                        }
                    } else if (maxDuration <= 0) {
                        if (!(duration <= maxDuration)) {
                            hasFilter = false;
                        }
                    }

                    //如果包含则跳过当前item
                    if (hasFilter == false) {
                        log.info("当前节目已经过滤: {}",item.getTitle());
                        continue;
                    }
                }

                Request request = item.getRequest();
                request.setUuid(UUID.randomUUID().toString());
                request.setChannelUuid(sub.getUuid());
                request.setDir(new File(dataPathProperties.getResourcesPath()));

                boolean isArm = System.getProperty("os.arch").equals("arm");
                if (isArm && request.getDownloader().equals(DownloadManager.Downloader.Nm3u8DlRe)){
                    //arm32不支持N_m3u8DL-RE
                    Map args = request.getArgs();
                    if (args == null){
                        args = new HashMap();
                    }
                    args.put("-N","10");//多线程用于m3u8下载
                    request.setArgs(args);
                    request.setDownloader(DownloadManager.Downloader.YtDlp);
                }
                if (!isArm && request.getDownloader().equals(DownloadManager.Downloader.Nm3u8DlRe)){
                    //以在非ansi环境显示进度信息 & 用于移除ANSI颜色 https://github.com/RikaCelery/N_m3u8DL-RE
                    Map args = request.getArgs();
                    if (args == null){
                        args = new HashMap();
                    }
                    args.put("--force-ansi-console","");
                    request.setArgs(args);
                }
                item.setRequest(request);
                filterItems.add(item);
                downloadManager.add(request);
            }
            items = filterItems;
            downloadManager.startDownload();
            log.info("{}:{}下载",sub.getTitle(),items.size() > 0 ? "开始" : "无");
            Set<DownloadProgress> downloadProgresses = downloadManager.allDownloadProgress();

            //初始化节目数据
            for (Item item : filterItems) {
                Items items1 = new Items();
                BeanUtils.copyProperties(item, items1);
                items1.setDownloader(item.getRequest().getDownloader().name());
                items1.setType(item.getRequest().getType().name());
                items1.setUuid(item.getRequest().getUuid());
                items1.setChannelUuid(item.getRequest().getChannelUuid());
                items1.setStatus(Context.DOWNLOADING);
                items1.setLinks(gson.toJson(item.getRequest().getLinks()));
                items1.setArgs(gson.toJson(item.getRequest().getArgs()));
                items1.setOperation(item.getRequest().getOperation().name());
                items1.setPublicTime(item.getPublicTime());
                sub.setEqual(item.getEqual());
                itemsMapper.insert(items1);
            }


            int closeSize = 0;
            while (items.size() != closeSize) {
                closeSize = downloadProgresses.stream().filter(new Predicate<DownloadProgress>() {
                    @Override
                    public boolean test(DownloadProgress progress) {
                        Integer status = progress.getStatus();
                        return DownloaderUtils.endStatusCode().contains(status);
                    }
                }).collect(Collectors.toList()).size();


                //1.转换成VO
                for (DownloadProgress progress : downloadProgresses) {
                    List<Item> collect = items.stream().filter(new Predicate<Item>() {
                        @Override
                        public boolean test(Item item) {
                            return item.getRequest().getUuid().equals(progress.getUuid());
                        }
                    }).collect(Collectors.toList());
                    String itemName = collect.isEmpty() ? null : collect.get(0).getTitle();
                    if (itemName.length() > 30){
                        itemName = itemName.substring(0,30) + "...";
                    }
                    //构建进度vo推送到前端
                    DownloadProgressVO build = DownloadProgressVO.builder()
                            .channelUuid(progress.getChannelUuid())
                            .uuid(progress.getUuid())
                            .status(progress.getStatus())
                            .downloadProgress(progress.getDownloadProgress())
                            .downloadTimeLeft(DownloaderUtils.duration((int) progress.getDownloadTimeLeft()))
                            .totalSize(DownloaderUtils.byteToMB(progress.getTotalSize()) + Unit.MB)
                            .downloadSpeed(DownloaderUtils.byteToMB(progress.getDownloadSpeed()) + Unit.MB_BY_SECOUND)
                            .operation(progress.getOperation())
                            .type(progress.getType())
                            .finalFormat(progress.getFinalFormat())
                            .downloader(items.get(0).getRequest().getDownloader().toString())
                            .channelName(sub.getTitle())
                            .itemName(itemName)
                            .build();
                    TaskRegistry.getDownloadProgressVOSet().remove(build);
                    TaskRegistry.getDownloadProgressVOSet().add(build);
                    if (DownloaderUtils.endStatusCode().contains(progress.getStatus())){

                        Items items1 = itemsMapper.selectByUuid(progress.getUuid());
                        if (items1 != null) {
                            items1.setFileName(progress.getUuid() + "." + progress.getFinalFormat());
                            items1.setStatus(progress.getStatus());
                            items1.setDownloadSpeed((double) progress.getDownloadTimeLeft());
                            items1.setDownloadTimeLeft((double) progress.getDownloadTimeLeft());
                            items1.setDownloadProgress(progress.getDownloadProgress());
                            items1.setFormat(progress.getFinalFormat());
                            items1.setType(progress.getType());
                            items1.setTotalSize(progress.getTotalSize());
                            items1.setOperation(progress.getOperation());
                            itemsMapper.update(items1);
                        }

                        //remove操作
                        if (progress.getStatus().equals(Context.REMOVE)) {
                            itemsMapper.deleteByUuid(progress.getUuid());
                        }
                    }
                }
            }
            log.info("{}:下载完成",sub.getTitle());
            sub.setUpdateTime(System.currentTimeMillis());

        } catch (InvocationTargetException e){
            if (e.getTargetException() != null){
                log.error("{}:更新异常:{}",sub.getTitle(),e.getTargetException().getMessage());
            }
        } catch (Exception e) {
            log.error("{}:更新异常",sub.getTitle(),e);
        }finally {
            //更新sub表
            sub.setIsFirst(StatusCode.NO);
            //加入当前时间浮动，让每次检查时间不一样 往后
            sub.setCheckTime(nowTimeFloat(1,1,10, Update.Units.Minutes));
            subMapper.update(sub);
            TaskRegistry.updateStatus = false;
            log.info("{}:更新完成",sub.getTitle());
        }
    }

    /**
     * 更新订阅集合
     * @throws Exception
     */
    public void updateSubList(List<Sub> subList) throws Exception {
        for (Sub sub : subList) {
            String scheduleType = sub.getScheduleType();
            if (scheduleType.equalsIgnoreCase("cron")){
                jobScheduler.scheduleRecurrently(sub.getUuid(), Duration.ofSeconds(sub.getCron()),
                        (JobLambda) () -> updateSub(sub.getUuid(), new JobTimeoutContext()));
                jobRunrService.toScheduledJob(sub.getUuid());
            }else if (scheduleType.equalsIgnoreCase("cron_expression")){
                jobScheduler.scheduleRecurrently(sub.getUuid(), CronUtils.fromQuartzToUnix(sub.getCronExpression()),
                        (JobLambda) () -> updateSub(sub.getUuid(), new JobTimeoutContext()));
                jobRunrService.toScheduledJob(sub.getUuid());
            }else {
                log.error("未知scheduleType: {}", scheduleType);
            }
        }
    }

    /**
     * 时间单位
     */
    public enum Units{
        Second,
        Minutes,
        Hour
    }

    /**
     * 当前时间浮动
     * @param direction -1 是往前；0是不变；>0是往后
     * @param start
     * @param end
     * @Units unit 单位
     * @return 时间毫秒值
     */
    public static Long nowTimeFloat(int direction, int start, int end, Update.Units unit){
        long currentTimeMillis = System.currentTimeMillis();
        Random random = new Random();
        int rn = random.nextInt(end - start + 1) + start;
        Long rs = 0L;
        switch (unit) {
            case Second:
                rs = rn*1000L;
                break;
            case Minutes:
                rs = rn*60*1000L;
                break;
            case Hour:
                rs = rn*60*60*1000L;
                break;
        }
        if (direction == -1){
            //往前
            return currentTimeMillis - rs;

        } else if (direction > 0) {
            //往后
            return  currentTimeMillis + rs;
        }else {
            //默认是当前时间
            return currentTimeMillis;
        }
    }

    /**
     * 获取最近的节目
     * 应对收藏夹可能短时间内添加多个视频
     * @param channelUuid
     * @return
     */
    private List<Item> recentItems(String channelUuid,Params params, Sub sub) throws Exception {
        //1.获取本地最新的equal
        String localLatestEqual = subMapper.selectByUuid(channelUuid).getEqual();
        //2.获取最近30集节目并按发布时间升序
        params.setEpisodes(Arrays.asList(-1));
        List<Item> recentItems = pluginManager.getPluginInstanceByDomainName(sub.getPlugin(),params).items()
                .stream().sorted(new Comparator<Item>() {
                    @Override
                    public int compare(Item o1, Item o2) {
                        return Long.compare(o1.getPublicTime(),o2.getPublicTime());
                    }
                }).collect(Collectors.toList());

        //比较equal
        List<String> equalList = recentItems.stream().map(Item::getEqual).collect(Collectors.toList());
        if (localLatestEqual != null
                && !localLatestEqual.equalsIgnoreCase("none")
                && equalList.contains(localLatestEqual)){
            int index = equalList.indexOf(localLatestEqual);
            return recentItems.subList(index + 1,recentItems.size());
        }else {
            //该订阅在本地还没节目
            return recentItems;
        }
    }

    /**
     * ws推送系统信息
     * @throws Exception
     */
    public void pushSystemInfoByWs() throws Exception {
        log.info("ws推送系统信息...");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    systemInfoWebSocketHandler.sendToAllClient(gson.toJson(systemService.info()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            //过快会导致无法获取到backgroundJobServers
        },0, 1, TimeUnit.SECONDS);

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                systemService.refreshJobRunrStatus();
            }
        }, 0, 10, TimeUnit.SECONDS);

    }

}
