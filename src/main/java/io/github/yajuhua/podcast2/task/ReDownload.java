package io.github.yajuhua.podcast2.task;

import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.progress.DownloadProgress;
import io.github.yajuhua.download.downloader.ytdlp.YtDlp;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.podcast2.common.constant.Unit;
import io.github.yajuhua.podcast2.common.utils.DownloaderUtils;
import io.github.yajuhua.podcast2.mapper.ItemsMapper;
import io.github.yajuhua.podcast2.mapper.SubMapper;
import io.github.yajuhua.podcast2.pojo.entity.Items;
import io.github.yajuhua.podcast2.pojo.entity.Sub;
import io.github.yajuhua.podcast2.pojo.vo.DownloadProgressVO;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Slf4j
public class ReDownload implements Runnable{
    private Request request;
    private Set<DownloadProgress> downloadProgresses;
    private ItemsMapper itemsMapper;
    private SubMapper subMapper;
    public ReDownload(Request request, ItemsMapper itemsMapper, SubMapper subMapper) {
        this.request = request;
        this.itemsMapper  = itemsMapper;
        this.subMapper = subMapper;
    }

    @Override
    public void run() {

        DownloadManager downloadManager = new DownloadManager();
        downloadManager.add(request);
        Task.downloadManagerList.add(downloadManager);
        //更新items状态码
        Items items = itemsMapper.selectByUuid(request.getUuid());
        items.setUuid(request.getUuid());
        items.setStatus(Context.DOWNLOADING);

        //v2.2.0开始移除了aria2c和N_m3u8DL-RE,要将所有请求改成yt-dlp
        if (!request.getDownloader().equals(DownloadManager.Downloader.YtDlp)){
            Map args = new HashMap();
            request.setDownloader(DownloadManager.Downloader.YtDlp);
            request.setArgs(args);
            items.setDownloader(request.getDownloader().toString());
        }

        itemsMapper.update(items);

        //开始下载
        try {
            downloadManager.startDownload();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //获取进度
        this.downloadProgresses = downloadManager.allDownloadProgress();
        Sub sub = subMapper.selectByUuid(request.getChannelUuid());
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
