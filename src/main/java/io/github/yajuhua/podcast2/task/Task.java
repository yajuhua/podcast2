package io.github.yajuhua.podcast2.task;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.Operation;
import io.github.yajuhua.download.commons.Type;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.utils.DownloaderUtils;
import io.github.yajuhua.podcast2.common.utils.Http;
import io.github.yajuhua.podcast2.controller.PluginController;
import io.github.yajuhua.podcast2.downloader.ytdlp.YtDlpUpdate;
import io.github.yajuhua.podcast2.mapper.*;
import io.github.yajuhua.podcast2.pojo.entity.*;
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
    public static List<DownloadManager> downloadManagerList = new ArrayList<>();//存放下载管理
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
        ExecutorService executor = Executors.newSingleThreadExecutor();
        long timeout;
        try {
            //1.获取需要更新的订阅
            List<Sub> subList = subService.selectUpdateList();
            for (Sub sub : subList) {
                int downloadItemNum;
                String[] customEpisodes = sub.getCustomEpisodes().split(",");
                downloadItemNum = sub.getIsFirst().equals(1) && sub.getEpisodes().equals(-1)?30:1;
                downloadItemNum = sub.getIsFirst().equals(1) && !sub.getCustomEpisodes().isEmpty()?customEpisodes.length:downloadItemNum;
                timeout = downloadItemNum * TimeUnit.MINUTES.toMillis(30);
                Future<?> future = null;
                try {
                    future = executor.submit(new Update(sub, subService, extendMapper, dataPathProperties, subMapper, itemsMapper, settingsMapper));
                    future.get(timeout,TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    future.cancel(true);
                    subMapper.update(sub);//保持原样
                    log.error("更新超时:{}{}",sub.getTitle(),e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("更新异常:{}详细:{}",e.getMessage(),e.getStackTrace());
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
                        UserMoreInfo moreInfo = gson.fromJson(userMapper.list().get(0).getUuid(), UserMoreInfo.class);
                        String githubProxyUrl = moreInfo.getGithubProxyUrl();

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
            log.info("重新下载");
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
                Sub sub = subMapper.selectByUuid(items.getChannelUuid());
                if (sub != null) {
                    Integer isFirst = sub.getIsFirst();
                    log.info("订阅:{}-节目:{}",sub.getTitle(),items.getTitle());
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
                        ReDownload reDownload = new ReDownload(request, itemsMapper, subMapper);
                        pool.execute(reDownload);
                        Thread.sleep(2000);
                    }else {
                        log.info("数据不全:{}-删除该记录",items.getTitle());
                        itemsMapper.deleteByUuid(items.getUuid());
                    }
                }else {
                    //清理掉
                    log.info("未找到所属频道:{}-删除该节目记录",items.getTitle());
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
                    log.error("重新下载超时");
                    return;
                } else if (pool.isTerminated()) {
                    log.info("重新下载结束");
                    return;
                }
            }

        }
    }
}
