package io.github.yajuhua.podcast2.task;

import com.google.gson.Gson;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.progress.DownloadProgress;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.podcast2.common.constant.*;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.utils.DownloaderUtils;
import io.github.yajuhua.podcast2.mapper.ExtendMapper;
import io.github.yajuhua.podcast2.mapper.ItemsMapper;
import io.github.yajuhua.podcast2.mapper.SettingsMapper;
import io.github.yajuhua.podcast2.mapper.SubMapper;
import io.github.yajuhua.podcast2.plugin.PluginManager;
import io.github.yajuhua.podcast2.pojo.entity.Extend;
import io.github.yajuhua.podcast2.pojo.entity.Items;
import io.github.yajuhua.podcast2.pojo.entity.Settings;
import io.github.yajuhua.podcast2.pojo.entity.Sub;
import io.github.yajuhua.podcast2.pojo.vo.DownloadProgressVO;
import io.github.yajuhua.podcast2.service.SubService;
import io.github.yajuhua.podcast2API.Channel;
import io.github.yajuhua.podcast2API.Item;
import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import io.github.yajuhua.podcast2API.setting.Setting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 每个订阅使用一个更新线程
 */
@Slf4j
public class Update implements Runnable {
    private Sub sub;
    private SubService subService;
    private ExtendMapper extendMapper;
    private DataPathProperties dataPathProperties;
    private SubMapper subMapper;
    private ItemsMapper itemsMapper;
    private SettingsMapper settingsMapper;
    private PluginManager pluginManager;
    private Gson gson;


    public Update(Sub sub, SubService subService, ExtendMapper extendMapper, DataPathProperties dataPathProperties
            , SubMapper subMapper, ItemsMapper itemsMapper, SettingsMapper settingsMapper, PluginManager pluginManager) {
        this.sub = sub;
        this.subService = subService;
        this.extendMapper = extendMapper;
        this.dataPathProperties = dataPathProperties;
        this.subMapper = subMapper;
        this.itemsMapper = itemsMapper;
        this.settingsMapper = settingsMapper;
        this.pluginManager = pluginManager;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        Class aClass = null;
        try {
            //修改订阅状态
            //sub.setStatus(StatusCode.ACTION_ING);
            subMapper.update(sub);
            List<Extend> anExtends = extendMapper.selectByUuid(sub.getUuid());
            List<InputAndSelectData> inputAndSelectDataList = new ArrayList<>();
            Integer isFirst = sub.getIsFirst();
            Integer episodes = sub.getEpisodes();
            String customEpisodes = sub.getCustomEpisodes();
            List<Integer> es = new ArrayList<>();
            log.info("开始检查更新:{}",sub.getTitle());

            //标记更新状态
            Task.updateStatus = true;

            if (isFirst == StatusCode.YES) {
                log.info("进入首次更新：{}",sub.getTitle());
                //进入首次更新
                if (episodes == EpisodesStatus.LATEST) {
                    //最新一集
                    es.add(episodes);
                } else if (episodes == EpisodesStatus.LATEST_30) {
                    //最新30集
                    es.add(EpisodesStatus.LATEST_30);
                } else if (episodes == EpisodesStatus.CUSTOMIZE) {
                    //自定义剧集
                    List<String> list = Arrays.asList(customEpisodes.split(","));
                    for (String s : list) {
                        es.add(Integer.parseInt(s));
                    }
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
            } else {
                //次次更新，latest
                items.add(pluginManager.getPluginInstanceByDomainName(sub.getPlugin(),params).latestItem());
            }

            //添加后又立即删除
            if (subMapper.selectByUuid(sub.getUuid()) == null){
                //已经被删除了
                log.info("已被删除:{}",sub.getTitle());
                return;
            }

            //构建下载
            DownloadManager downloadManager = new DownloadManager(1,1,items.size());
            Task.downloadManagerList.add(downloadManager);
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
                    log.info("{}:暂无更新",sub.getTitle());
                    //加入当前时间浮动，让每次检查时间不一样 往后
                    sub.setCheckTime(nowTimeFloat(1,1,10,Units.Minutes));
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
                        if (!(Pattern.compile(descK).matcher(item.getTitle()).find() || descK == "" || descK == null)) {
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
                        log.info("当前节目已经过滤:{}",item.getTitle());
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
                    Task.getDownloadProgressVOSet().remove(build);
                    Task.getDownloadProgressVOSet().add(build);
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
            e.printStackTrace();
        }finally {
            //更新sub表
            sub.setIsFirst(StatusCode.NO);
            //加入当前时间浮动，让每次检查时间不一样 往后
            sub.setCheckTime(nowTimeFloat(1,1,10,Units.Minutes));
            subMapper.update(sub);
            log.info("{}:更新完成",sub.getTitle());
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
    public static Long nowTimeFloat(int direction, int start, int end, Units unit){
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
}
