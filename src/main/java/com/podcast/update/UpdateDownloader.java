package com.podcast.update;

import com.podcast.Servlet.UserServlet;
import com.podcast.Utils.Yt_dlp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 更新下载器
 */
public class UpdateDownloader implements Runnable{
    private static final Logger LOGGER = LoggerFactory.getLogger("UpdateDownload");

    @Override
    public void run() {
        try {
            while (!(Target.UPDATE_STATUS == 0 && UserServlet.CREATE_UUID.size() == 0)){
                LOGGER.info("有订阅更新无法更新yt-dlp,等待10s");
                Thread.sleep(10*1000);
            }
                LOGGER.info("更新yt-dlp");
                Yt_dlp.update();
                LOGGER.info("yt-dlp更新完成");

        } catch (Exception e) {
            LOGGER.error("yt-dlp更新失败:"+e);
        }
    }
}
