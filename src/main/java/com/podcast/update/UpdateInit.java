package com.podcast.update;

import com.podcast.service.ChannelService;
import com.podcast.service.PodcastUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class UpdateInit implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger("UpdateInit");
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        PodcastUserService service = new PodcastUserService();
        // 获取ServletContext对象
        ServletContext context = servletContextEvent.getServletContext();
        // 获取webapp目录的绝对路径
        String webappPath = context.getRealPath("/");
        //存入数据库
        service.updateWebappPath(webappPath);


        //创建必要的文件夹 video、audio、plugin、xml
        File directory  = new File(webappPath);
        new File(directory,"video").mkdir();
        new File(directory,"audio").mkdir();
        new File(directory,"plugin").mkdir();
        new File(directory,"xml").mkdir();


        //定时任务
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable task = new Target();
        // 在0分钟后开始执行任务，每隔一分钟重复执行
        executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.MINUTES);



        //改成定时任务
        ChannelService channelService = new ChannelService();
        Runnable target2 = new CheckForSurvival(channelService.getAllUuid());

        // 在0秒钟后开始执行任务，每隔x秒钟重复执行，根据配置信息
        Properties properties = new Properties();
        try {
            // 使用ClassLoader加载Properties文件
            InputStream inputStream = CheckForSurvival.class.getClassLoader().getResourceAsStream("conf.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("使用ClassLoader加载Properties文件时出错！"+" 详细:"+e);
        }

        //读取检查间隔时间
        long checkForSurvival = Long.parseLong((String) properties.get("checkForSurvival"));
        LOGGER.info("开始检查item存活,间隔：" + checkForSurvival+"s");
        executor.scheduleAtFixedRate(target2, 0, checkForSurvival, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
