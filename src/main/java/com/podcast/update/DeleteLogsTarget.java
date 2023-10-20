package com.podcast.update;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 删除过期日志（七天）
 */
public class DeleteLogsTarget implements Runnable{
    private static final Logger LOGGER = LoggerFactory.getLogger("DeleteLogsTarget");
    @Override
    public void run() {
        LOGGER.info("进入日志过期检查");
        //删除过期日志
        try {
            File logsPosition = new File(UpdateInit.confProperties.getProperty("logsPosition"));
            File[] allLog = logsPosition.listFiles();

            long duration = 1000*60*60*24*7;//七天
            long now = System.currentTimeMillis();
            for (File log : allLog) {
                long lastModified = log.lastModified();
                if (now-lastModified>duration){
                    //删除
                    boolean deleteQuietly = FileUtils.deleteQuietly(log);
                    if (deleteQuietly){
                        LOGGER.info(log.getName()+":日志文件删除成功！");
                    }else {
                        LOGGER.error(log.getName()+":日志文件删除失败！");
                    }

                }
            }
        } catch (Exception e) {
            LOGGER.error("找不到日志文件位置");
            throw new RuntimeException(e);
        }

        LOGGER.info("日志过期检查结束");
    }
}
