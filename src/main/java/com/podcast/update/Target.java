package com.podcast.update;

import com.podcast.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 每一分钟检查更新一次
 * 为了避免风控，改用单线程更新
 */
public class Target implements Runnable{
    private static final Logger LOGGER = LoggerFactory.getLogger("Target");
    @Override
    public void run() {
            LOGGER.info("系统开始扫描！");
            ChannelService service = new ChannelService();
            List<String> list = service.checkForUpdate(System.currentTimeMillis() / 1000);
            for (String u : list) {
                Update update = new Update(u);
                update.run_();
                //要等待12秒，不然容易风控
                try {
                    Thread.sleep(12*1000);
                } catch (InterruptedException e) {
                    LOGGER.error("在等待下个更新任务时出错，重启服务试试"+" 详细:" +e);
                }
            }
            LOGGER.info("系统扫描完成！");

    }
}
