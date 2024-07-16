package io.github.yajuhua.podcast2.task;

import com.google.gson.Gson;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.progress.DownloadProgress;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.podcast2.common.constant.Unit;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.utils.DownloaderUtils;
import io.github.yajuhua.podcast2.mapper.ItemsMapper;
import io.github.yajuhua.podcast2.mapper.PluginMapper;
import io.github.yajuhua.podcast2.mapper.SettingsMapper;
import io.github.yajuhua.podcast2.mapper.SubMapper;
import io.github.yajuhua.podcast2.plugin.PluginManager;
import io.github.yajuhua.podcast2.pojo.entity.Items;
import io.github.yajuhua.podcast2.pojo.entity.Settings;
import io.github.yajuhua.podcast2.pojo.entity.Sub;
import io.github.yajuhua.podcast2.pojo.vo.DownloadProgressVO;
import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.Podcast2;
import io.github.yajuhua.podcast2API.Type;
import io.github.yajuhua.podcast2API.setting.Setting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.*;


@Slf4j
public class ReDownload implements Runnable{

    private Request request;
    private Set<DownloadProgress> downloadProgresses;
    private ItemsMapper itemsMapper;
    private SubMapper subMapper;
    private PluginMapper pluginMapper;
    private DataPathProperties dataPathProperties;
    private SettingsMapper settingsMapper;
    private Gson gson;

    private PluginManager pluginManager;
    public ReDownload() {
    }

    public ReDownload(Request request, ItemsMapper itemsMapper, SubMapper subMapper, PluginMapper pluginMapper,
                      DataPathProperties dataPathProperties, SettingsMapper settingsMapper, PluginManager pluginManager) {
        this.request = request;
        this.itemsMapper = itemsMapper;
        this.subMapper = subMapper;
        this.pluginMapper = pluginMapper;
        this.dataPathProperties = dataPathProperties;
        this.settingsMapper = settingsMapper;
        this.gson = new Gson();
        this.pluginManager = pluginManager;
    }

    @Override
    public void run() {

        DownloadManager downloadManager = new DownloadManager();
        Task.downloadManagerList.add(downloadManager);
        Sub sub = subMapper.selectByUuid(request.getChannelUuid());
        //更新items状态码
        Items items = itemsMapper.selectByUuid(request.getUuid());
        items.setUuid(request.getUuid());
        items.setStatus(Context.DOWNLOADING);

        //根据channelUuid获取插件
        String pluginName = subMapper.selectByUuid(request.getChannelUuid()).getPlugin();

        //构建Params
/*        List<Settings> settingsFromDB = settingsMapper.selectByPluginName(pluginName);
        List<Setting> settings = new ArrayList<>();
        for (Settings settings1 : settingsFromDB) {
            Setting setting = new Setting();
            BeanUtils.copyProperties(settings1,setting);
            settings.add(setting);
        }
        Params params = Params.builder()
                .settings(settings)
                .url(sub.getLink())
                .type(Type.valueOf(sub.getType()))
                .build();*/
        Params params = pluginManager.getSubParams(sub.getUuid());


        //获取插件getRequest方法
        String link = items.getLink();
        Request getRequest;
        try {
            //旧版没有getRequest方法
            if (pluginManager.hasRequestMethod(pluginName)){
                Podcast2 instance = pluginManager.getPluginInstanceByDomainName(pluginName, pluginManager.getParams(params));
                getRequest = instance.getRequest(link);
                getRequest.setUuid(request.getUuid());
                getRequest.setChannelUuid(request.getChannelUuid());
                getRequest.setDir(request.getDir());
                request = getRequest;
            }else {
                log.warn("该{}插件不支持getRequest",pluginName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //开始下载
        itemsMapper.update(items);
        downloadManager.add(request);
        try {
            downloadManager.startDownload();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //获取进度
        this.downloadProgresses = downloadManager.allDownloadProgress();
        Items items1 = itemsMapper.selectByUuid(request.getUuid());
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
                        .itemName(items1.getTitle())
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
                    log.info("重新下载完成");
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
