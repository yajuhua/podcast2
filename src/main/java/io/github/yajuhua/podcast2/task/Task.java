package io.github.yajuhua.podcast2.task;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.Operation;
import io.github.yajuhua.download.commons.Type;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.podcast2.alist.Alist;
import io.github.yajuhua.podcast2.alist.dto.fs.PutDTO;
import io.github.yajuhua.podcast2.alist.dto.task.upload.InfoDTO;
import io.github.yajuhua.podcast2.common.constant.SubStatusCode;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.properties.InfoProperties;
import io.github.yajuhua.podcast2.common.utils.DownloaderUtils;
import io.github.yajuhua.podcast2.common.utils.Http;
import io.github.yajuhua.podcast2.controller.DownloadController;
import io.github.yajuhua.podcast2.controller.PluginController;
import io.github.yajuhua.podcast2.downloader.ytdlp.YtDlpUpdate;
import io.github.yajuhua.podcast2.mapper.*;
import io.github.yajuhua.podcast2.plugin.PluginManager;
import io.github.yajuhua.podcast2.pojo.dto.GithubActionWorkflowsDTO;
import io.github.yajuhua.podcast2.pojo.entity.*;
import io.github.yajuhua.podcast2.pojo.vo.DownloadProgressVO;
import io.github.yajuhua.podcast2.pojo.vo.PluginVO;
import io.github.yajuhua.podcast2.service.SubService;
import io.github.yajuhua.podcast2.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
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

    /**
     * 获取进度
     * @return
     */
    public static Set<DownloadProgressVO> getDownloadProgressVOSet(){
        return downloadProgressVOSet;
    }
    public static List<GithubActionWorkflowsDTO> actionWorkflowsDTOList = new ArrayList<>();
    /**
     * 每隔分钟检查一次频道是否需要更新
     */
    @Scheduled(fixedDelay = 60000)
    public void updateSub(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        long timeout;
        String uuid = null;
        try {
            //1.获取需要更新的订阅
            List<Sub> subList = subService.selectUpdateList();
            for (Sub sub : subList) {
                uuid = sub.getUuid();
                int downloadItemNum;
                String[] customEpisodes = sub.getCustomEpisodes().split(",");
                downloadItemNum = sub.getIsFirst().equals(1) && sub.getEpisodes().equals(-1)?30:1;
                downloadItemNum = sub.getIsFirst().equals(1) && !sub.getCustomEpisodes().isEmpty()?customEpisodes.length:downloadItemNum;
                timeout = downloadItemNum * TimeUnit.MINUTES.toMillis(30);
                Future<?> future = null;
                try {
                    future = executor.submit(new Update(sub, subService, extendMapper, dataPathProperties, subMapper, itemsMapper, settingsMapper,pluginManager));
                    future.get(timeout,TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    future.cancel(true);
                    subMapper.update(sub);//保持原样
                    log.error("更新超时:{}{}",sub.getTitle(),e.getMessage());
                    collectUpdateLogMessages(sub.getUuid(),"更新超时","error");
                }
                collectUpdateLogMessages(sub.getUuid(),"更新成功","info");
            }
        } catch (Exception e) {
            log.error("更新异常:{}详细:{}",e.getMessage(),e.getStackTrace());
            collectUpdateLogMessages(uuid,e.getMessage(),"error");
        }finally {
            Task.updateStatus = false;
            if (Task.downloadProgressVOSet != null){
                Task.downloadProgressVOSet.clear();
            }
            if(Task.downloadManagerList != null){
                Task.downloadManagerList.clear();
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
            log.error("清除数据库未记录的文件异常：{}",e.getMessage());
        }
    }

    /**
     * 更新yt-dlp,每小时执行一次
     */
    @Scheduled(fixedDelay = 3600000)
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
                if ((latestUpdateTime + refreshDuration) < System.currentTimeMillis()){

                    //获取最新tag
                    String apiUrl  = "https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest";
                    String json = Http.get(apiUrl);
                    String tagName = gson.fromJson(json, JsonObject.class).get("tag_name").getAsString();

                    if (!ytDlp.getVersion().contains(tagName)){
                        log.info("开始更新yt-dlp");
                        String githubProxyUrl = userService.getExtendInfo().getGithubProxyUrl();

                        if (githubProxyUrl != null){
                            log.info("使用Github加速站更新yt-dlp");
                            File filePath = System.getProperty("os.name").contains("Linux") ? new File("/usr/sbin") : new File(System.getProperty("user.dir"));
                            String tmpPath = dataPathProperties.getTmpPath();
                            YtDlpUpdate ytDlpUpdate = new YtDlpUpdate(githubProxyUrl,filePath.getAbsolutePath(),tmpPath);
                            boolean rs = ytDlpUpdate.proxy();
                            log.info("更新yt-dlp{}",rs?"成功":"失败");
                        }else {
                            //执行更新
                            int exitCode = Runtime.getRuntime().exec("yt-dlp -U").waitFor();
                            log.info("更新yt-dlp{}",exitCode==0?"成功":"失败");
                        }
                        //更新数据库
                        ytDlp.setUpdateTime(System.currentTimeMillis());
                        ytDlp.setVersion(DownloaderUtils.cmd("yt-dlp --version"));
                        downloaderMapper.update(ytDlp);
                    }else {
                        log.info("当前版本:{}是最新版",tagName);
                    }
                }else {
                    log.info("未到更新时间");
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

    /**
     * 检查未下载完成的节目，可能是服务突然停止导致的
     * 每个两分钟检查一次
     */
    @Scheduled(fixedDelay = 120000)
    public void checkForUndownload() throws Exception{
        Sub sub = null;
        Items itemsT = null;
        try {
            List<Items> itemsList = itemsMapper.list();
            List<Items> unsuccessfulDownloads = itemsList.stream().filter(new Predicate<Items>() {
                @Override
                public boolean test(Items items) {
                    //正在下载中且没有文件名为null的
                    return Context.DOWNLOADING.equals(items.getStatus()) && items.getFileName() == null || items.getFileName().equals("");
                }
            }).collect(Collectors.toList());

            if (!unsuccessfulDownloads.isEmpty()) {
                //重新下载
                ThreadPoolExecutor pool = new ThreadPoolExecutor(
                        1,    //核心线程数有1个
                        1,  //最大线程数
                        8,    //临时线程存活的时间8秒。 意思是临时线程8秒没有任务执行，就会被销毁掉。
                        TimeUnit.SECONDS,//时间单位（秒）
                        new ArrayBlockingQueue<>(unsuccessfulDownloads.size()), //任务阻塞队列，没有来得及执行的任务在，任务队列中等待
                        Executors.defaultThreadFactory(), //用于创建线程的工厂对象
                        new ThreadPoolExecutor.CallerRunsPolicy() //拒绝策略
                );
                for (Items items : unsuccessfulDownloads) {
                    //如果是首次更新就不用
                     sub = subMapper.selectByUuid(items.getChannelUuid());
                     itemsT = items;
                    if (sub != null) {
                        Integer isFirst = sub.getIsFirst();
                        log.info("开始重新下载 - 订阅：{} - 节目：{}",sub.getTitle(),items.getTitle());
                        //之前版本中type和operation是null，要排除这种情况
                        if (isFirst !=null && isFirst == 0 && items.getType() != null && items.getOperation() != null) {
                            Map args = gson.fromJson(items.getArgs(), Map.class);
                            List<String> links = gson.fromJson(items.getLinks(),new TypeToken<List<String>>() {}.getType());
                            Request request = Request.builder()
                                    .args(args)
                                    .uuid(items.getUuid())
                                    .channelUuid(items.getChannelUuid())
                                    .type(Type.valueOf(items.getType()))
                                    .operation(Operation.valueOf(items.getOperation()))
                                    .downloader(DownloadManager.Downloader.YtDlp)
                                    .dir(new File(dataPathProperties.getResourcesPath()))
                                    .links(links)
                                    .build();
                            ReDownload reDownload = new ReDownload(request,itemsMapper,subMapper,pluginMapper,dataPathProperties,settingsMapper,pluginManager);
                            pool.execute(reDownload);
                            Thread.sleep(2000);
                        }else {
                            log.error("重新下载失败 - 数据不全 - 订阅：{} - 节目：{}",sub.getTitle(),items.getTitle());
                            //订阅equal改成none,一般重新下载失败都是突然中断更新导致数据不全。
                            Sub none = Sub.builder().uuid(sub.getUuid()).equal("none").build();
                            subMapper.update(none);
                            itemsMapper.deleteByUuid(items.getUuid());
                        }
                    }else {
                        //清理掉
                        log.error("未找到该节目所属订阅 - 删除该节目记录：{}",items.getTitle());
                        itemsMapper.deleteByUuid(items.getUuid());
                    }
                }
                pool.shutdown();
                Date start = new Date();
                while (true){
                    Date end = new Date();
                    long minutes = (end.getTime() - start.getTime())/1000/60;
                    //每个下载最多是30分钟
                    if (minutes > unsuccessfulDownloads.size()*30){
                        log.error("重新下载超时 - 订阅：{} - 节目：{}",sub.getTitle(),itemsT.getTitle());
                        return;
                    } else if (pool.isTerminated()) {
                        log.info("重新下载结束 - 订阅：{} - 节目：{}",sub.getTitle(),itemsT.getTitle());
                        return;
                    }
                }

            }
        } catch (Exception e) {
            log.error("检查未下载完成的节目异常：{}",e.getMessage());
        }
    }


    /**
     * 获取GithubActionWorkflows状态
     */
    @Scheduled(fixedDelay = 24,timeUnit = TimeUnit.HOURS)
    public void getGithubActionWorkflowsStatus(){
        log.info("Github Action Status 开始更新...");
        List<GithubActionWorkflowsDTO> tmp = new ArrayList<>();

        //获取Github Action 链接
        List<Plugin> pluginList = pluginMapper.list();
        List<String> githubActionLinkList = new ArrayList<>();
        String actionApi = "https://api.github.com/repos/yajuhua/podcast2/actions/workflows/";
        for (Plugin plugin : pluginList) {
            String link = actionApi + "plugin-status-" + plugin.getName() + ".yml/runs";
            githubActionLinkList.add(link);
        }

        //获取数据并转对象
        for (String link : githubActionLinkList) {
            try {
                if (link != null){
                    String json = Http.get(link);
                    GithubActionWorkflowsDTO actionWorkflowsDTO = gson.fromJson(json, GithubActionWorkflowsDTO.class);
                    if (actionWorkflowsDTO.getTotalCount() != null && actionWorkflowsDTO.getTotalCount() != 0){
                        tmp.add(actionWorkflowsDTO);
                    }
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                log.error("获取插件状态错误：",e.getMessage());
            }
        }

        //清空之前的并添加新的
        if (!tmp.isEmpty()){
            actionWorkflowsDTOList.clear();
            actionWorkflowsDTOList.addAll(tmp);
            log.info("Github Action Status 更新完成");
        }

    }

    /**
     * 上传节目资源到AList
     * 每一分钟检查一次
     */

    @Scheduled(fixedRate = 1,timeUnit = TimeUnit.MINUTES)
    public void uploadResourcesToAList(){
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
    @Scheduled(fixedRate = 24,timeUnit = TimeUnit.HOURS)
    public void refreshAListToken(){
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
}
