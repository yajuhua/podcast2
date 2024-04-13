package io.github.yajuhua.podcast2.task;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.progress.DownloadProgress;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.podcast2.common.constant.EpisodesStatus;
import io.github.yajuhua.podcast2.common.constant.ReflectionMethodName;
import io.github.yajuhua.podcast2.common.constant.StatusCode;
import io.github.yajuhua.podcast2.common.constant.Unit;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.utils.DownloaderUtils;
import io.github.yajuhua.podcast2.common.utils.PluginLoader;
import io.github.yajuhua.podcast2.mapper.ExtendMapper;
import io.github.yajuhua.podcast2.mapper.ItemsMapper;
import io.github.yajuhua.podcast2.mapper.SettingsMapper;
import io.github.yajuhua.podcast2.mapper.SubMapper;
import io.github.yajuhua.podcast2.pojo.entity.Extend;
import io.github.yajuhua.podcast2.pojo.entity.Items;
import io.github.yajuhua.podcast2.pojo.entity.Settings;
import io.github.yajuhua.podcast2.pojo.entity.Sub;
import io.github.yajuhua.podcast2.pojo.vo.DownloadProgressVO;
import io.github.yajuhua.podcast2.service.SubService;
import io.github.yajuhua.podcast2API.Item;
import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import io.github.yajuhua.podcast2API.setting.Setting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
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


    public Update(Sub sub, SubService subService, ExtendMapper extendMapper, DataPathProperties dataPathProperties
            , SubMapper subMapper, ItemsMapper itemsMapper, SettingsMapper settingsMapper) {
        this.sub = sub;
        this.subService = subService;
        this.extendMapper = extendMapper;
        this.dataPathProperties = dataPathProperties;
        this.subMapper = subMapper;
        this.itemsMapper = itemsMapper;
        this.settingsMapper = settingsMapper;
    }

    @Override
    public void run() {
        Class aClass = null;
        try {
            //修改订阅状态
            sub.setStatus(StatusCode.ACTION_ING);
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

            aClass = PluginLoader.selectByName(sub.getPlugin(), dataPathProperties).get(0);
            Gson gson = new Gson();
            Constructor constructor = aClass.getConstructor(String.class);
            Object o = constructor.newInstance(gson.toJson(params));
            Method settingsMethod = aClass.getMethod(ReflectionMethodName.SETTINGS);

            //返回插件设置用于更新
            Type type1 = new TypeToken<List<Setting>>() {}.getType();
            List<Setting> resultSettings = gson.fromJson(gson.toJson(settingsMethod.invoke(o)),type1);

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
                invoke = aClass.getMethod(ReflectionMethodName.ITEMS).invoke(o);
                json = gson.toJson(invoke);
                Type type = new TypeToken<List<Item>>() {
                }.getType();
                items = gson.fromJson(json, type);
            } else {
                //次次更新，latest
                invoke = aClass.getMethod(ReflectionMethodName.LATEST_ITEM).invoke(o);
                json = gson.toJson(invoke);
                Item item = gson.fromJson(json, Item.class);
                items.add(item);
            }

            DownloadManager downloadManager = new DownloadManager();
            //过滤器
            List<String> titlekeyWords = Arrays.asList(sub.getTitleKeywords());
            List<String> desckeyWords = Arrays.asList(sub.getDescKeywords());
            List<Item> filterItems = new ArrayList<>();
            for (Item item : items) {
                if (item.getEqual().equals(sub.getEqual())) {
                    log.info("{}:暂无更新",sub.getTitle());
                    //更新sub表
                    sub.setCheckTime(System.currentTimeMillis());
                    sub.setStatus(StatusCode.NO_ACTION);
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

                //arm32v7没有N_m3u8DL-RE,将下载请求提交的yt-dlp
                boolean isNm3u8DlRe = request.getDownloader().equals(DownloadManager.Downloader.Nm3u8DlRe);
                String osArch = System.getProperty("os.arch");

                if (isNm3u8DlRe && "arm".equals(osArch)){
                    request.setDownloader(DownloadManager.Downloader.YtDlp);
                }

                item.setRequest(request);
                filterItems.add(item);
                downloadManager.add(request);
            }
            items = filterItems;
            Thread downloadManagerThread = new Thread(downloadManager);
            downloadManagerThread.start();
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
                            .build();
                    Task.getDownloadProgressVOSet().remove(build);
                    Task.getDownloadProgressVOSet().add(build);
                    if (DownloaderUtils.endStatusCode().contains(progress.getStatus())){
                        Items items1 = itemsMapper.selectByUuid(progress.getUuid());
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
                }
            }
            log.info("{}:下载完成",sub.getTitle());

            //更新sub表
            sub.setUpdateTime(System.currentTimeMillis());
            sub.setCheckTime(System.currentTimeMillis());
            sub.setStatus(StatusCode.NO_ACTION);
            sub.setIsFirst(StatusCode.NO);
            log.info("{}:更新完成",sub.getTitle());
        } catch (InvocationTargetException e){
            if (e.getTargetException() != null){
                log.error("{}:更新异常:{}",sub.getTitle(),e.getTargetException().getMessage());
            }
        } catch (Exception e) {
            log.error("异常信息:{}",e.getMessage());
        }finally {
            sub.setStatus(StatusCode.NO_ACTION);
            subMapper.update(sub);
        }
    }
}
