package io.github.yajuhua.podcast2.task;

import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.podcast2.pojo.dto.AppendItemDTO;
import io.github.yajuhua.podcast2.pojo.entity.*;
import io.github.yajuhua.podcast2.pojo.entity.LogMessage;
import io.github.yajuhua.podcast2.pojo.vo.DownloadProgressVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Component
public class TaskRegistry {

    public static Set<DownloadProgressVO> downloadProgressVOSet = new CopyOnWriteArraySet<>();
    public static Boolean addSubStatus = false;//避免添加订阅时更新yt-dlp
    public static Boolean updateStatus = false;//避免更新订阅时更新插件之类的
    public static List<DownloadManager> downloadManagerList = new ArrayList<>();//存放下载管理
    public static Map<String,List<LogMessage>> collectUpdateLogMessagesMap = new HashMap<>();//存放订阅更新日志
    public static List<Items> reDownloadItems = new ArrayList<>();//点击重新下载后会先存放到这
    public static List<AppendItemDTO> appendItemList = new ArrayList<>();//订阅追加节目
    public static final Map<UUID,String> backgroundTask = new HashMap<>();//后台任务

    /**
     * 获取进度
     * @return
     */
    public static Set<DownloadProgressVO> getDownloadProgressVOSet(){
        return downloadProgressVOSet;
    }
}
