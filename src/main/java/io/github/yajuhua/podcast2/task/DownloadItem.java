package io.github.yajuhua.podcast2.task;

import com.google.gson.Gson;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.progress.DownloadProgress;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.podcast2.bot.telegram.TGBot;
import io.github.yajuhua.podcast2.common.constant.Unit;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.utils.DownloaderUtils;
import io.github.yajuhua.podcast2.common.utils.Http;
import io.github.yajuhua.podcast2.mapper.ItemsMapper;
import io.github.yajuhua.podcast2.mapper.SettingsMapper;
import io.github.yajuhua.podcast2.mapper.SubMapper;
import io.github.yajuhua.podcast2.plugin.PluginManager;
import io.github.yajuhua.podcast2.pojo.dto.AppendItemDTO;
import io.github.yajuhua.podcast2.pojo.entity.Items;
import io.github.yajuhua.podcast2.pojo.entity.Settings;
import io.github.yajuhua.podcast2.pojo.entity.Sub;
import io.github.yajuhua.podcast2.pojo.vo.DownloadProgressVO;
import io.github.yajuhua.podcast2API.Item;
import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.Podcast2;
import io.github.yajuhua.podcast2API.Type;
import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import io.github.yajuhua.podcast2API.setting.Setting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.net.URL;
import java.util.*;


/**
 * 单个节目下载
 */
@Slf4j
public class DownloadItem implements Runnable{


    private Set<DownloadProgress> downloadProgresses;
    private ItemsMapper itemsMapper;
    private SubMapper subMapper;
    private Gson gson;
    private AppendItemDTO appendItemDTO;

    private PluginManager pluginManager;
    private DataPathProperties dataPathProperties;
    private SettingsMapper settingsMapper;
    public DownloadItem() {
    }

    public DownloadItem(AppendItemDTO appendItemDTO, ItemsMapper itemsMapper, SubMapper subMapper,
                        PluginManager pluginManager, DataPathProperties dataPathProperties, SettingsMapper settingsMapper) {
        this.itemsMapper = itemsMapper;
        this.subMapper = subMapper;;
        this.gson = new Gson();
        this.pluginManager = pluginManager;
        this.appendItemDTO = appendItemDTO;
        this.dataPathProperties = dataPathProperties;
        this.settingsMapper = settingsMapper;
    }

    @Override
    public void run() {

        DownloadManager downloadManager = new DownloadManager();
        Task.downloadManagerList.add(downloadManager);

        Item item;
        try {
            //获取二级域名
            String host = new URL(appendItemDTO.getUrl()).getHost();
            String secondLevelDomain = Http.getSecondLevelDomain(host);

            //获取参数
            List<InputAndSelectData> inputAndSelectDataList = appendItemDTO.getInputAndSelectDataList();
            Params params = pluginManager.getParams();
            params.setType(Type.valueOf(appendItemDTO.getType()));
            if (params.getInputAndSelectDataList() != null && !params.getInputAndSelectDataList().isEmpty()){
                params.getInputAndSelectDataList().addAll(inputAndSelectDataList);
            }else {
                params.setInputAndSelectDataList(inputAndSelectDataList);
            }

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

            //获取节目信息
            item = instance.getItem(appendItemDTO.getUrl());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //配置下载请求信息
        Request request = item.getRequest();
        request.setDir(new File(dataPathProperties.getResourcesPath()));
        request.setUuid(UUID.randomUUID().toString());
        request.setChannelUuid(appendItemDTO.getChannelUuid());

        //初始化节目信息
        Items items = new Items();
        BeanUtils.copyProperties(item, items);
        items.setDownloader(item.getRequest().getDownloader().name());
        items.setType(appendItemDTO.getType());
        items.setUuid(request.getUuid());
        items.setChannelUuid(request.getChannelUuid());
        items.setStatus(Context.DOWNLOADING);
        items.setLinks(gson.toJson(Arrays.asList(appendItemDTO.getUrl())));
        items.setArgs(gson.toJson(item.getRequest().getArgs()));
        items.setOperation(item.getRequest().getOperation().name());
        items.setPublicTime(item.getPublicTime());
        items.setInputAndSelectDataList(gson.toJson(appendItemDTO.getInputAndSelectDataList()));
        items.setPlugin(Http.getSecondLevelDomain(Http.getHost(appendItemDTO.getUrl())));
        itemsMapper.insert(items);

        //获取channel信息
        Sub sub = subMapper.selectByUuid(items.getChannelUuid());

        //开始下载
        downloadManager.add(item.getRequest());
        try {
            downloadManager.startDownload();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //获取进度
        this.downloadProgresses = downloadManager.allDownloadProgress();
        while (true){
            for (DownloadProgress progress : downloadProgresses) {
                DownloadProgressVO build = DownloadProgressVO.builder()
                        .channelUuid(progress.getChannelUuid())
                        .uuid(progress.getUuid())
                        .status(progress.getStatus())
                        .downloadProgress(progress.getDownloadProgress())
                        .downloadTimeLeft(DownloaderUtils.duration((int) progress.getDownloadProgress()))
                        .totalSize(DownloaderUtils.byteToMB(progress.getTotalSize()) + Unit.MB)
                        .downloadSpeed(DownloaderUtils.byteToMB(progress.getDownloadSpeed()) + Unit.MB_BY_SECOUND)
                        .operation(progress.getOperation())
                        .type(progress.getType())
                        .finalFormat(progress.getFinalFormat())
                        .downloader(items.getDownloader())
                        .channelName(sub.getTitle())
                        .itemName(item.getTitle())
                        .build();

                Task.getDownloadProgressVOSet().remove(build);//去重
                Task.getDownloadProgressVOSet().add(build);

                //remove操作
                if (progress.getStatus().equals(Context.REMOVE)) {
                    itemsMapper.deleteByUuid(progress.getUuid());
                    log.info("移除下载");
                    return;
                }

                if (DownloaderUtils.endStatusCode().contains(progress.getStatus())){
                    items.setFileName(progress.getUuid() + "." + progress.getFinalFormat());
                    items.setFormat(progress.getFinalFormat());
                    items.setTotalSize(progress.getTotalSize());
                    items.setStatus(progress.getStatus());
                    items.setDownloadProgress(progress.getDownloadProgress());
                    items.setDownloadTimeLeft((double) progress.getDownloadTimeLeft());
                    items.setDownloadSpeed(progress.getDownloadSpeed());
                    itemsMapper.update(items);
                    TGBot.downloadCompletedItems.add(items);
                    log.info("追加节目下载完成: {}",items.getTitle());
                    //结束下载
                    return;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
