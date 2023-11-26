package com.podcast.update;

import com.podcast.Servlet.UserServlet;
import com.podcast.Utils.Clear;
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
    public static Integer UPDATE_STATUS;//更新状态
    @Override
    public void run() {

            //如果没有创建，则执行清理未完成的资源
            if (UserServlet.CREATE_UUID.size() == 0){
                Clear.clearNotSubscribeResourceData();//清除数据库记录
                Clear.clearNotCompleted();
            }

            UPDATE_STATUS = 1;
            LOGGER.info("系统开始扫描！");
            LOGGER.info("createSize:"+UserServlet.CREATE_UUID.size());
            LOGGER.info("updateStatus:"+UPDATE_STATUS);
            ChannelService service = new ChannelService();
            List<String> list = service.checkForUpdate(System.currentTimeMillis() / 1000);
            for (String u : list) {
                try {
                    //要等待12秒，不然容易风控
                    Thread.sleep(12*1000);
                    while (UserServlet.CREATE_UUID.size()!=0){
                        Thread.sleep(10*1000);//等待10秒
                        LOGGER.info("有多集正在更新,扫描更新等待10秒");
                    }
                        Update update = new Update(u);
                        update.run_();
                } catch (InterruptedException e) {
                    LOGGER.error("在等待下个更新任务时出错，重启服务试试"+" 详细:" +e);
                }
            }
            UPDATE_STATUS = 0;
            LOGGER.info("系统扫描完成！");
            LOGGER.info("createSize:"+UserServlet.CREATE_UUID.size());
            LOGGER.info("updateStatus:"+UPDATE_STATUS);

    }
}
