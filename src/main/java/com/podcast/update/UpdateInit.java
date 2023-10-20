package com.podcast.update;

import com.podcast.Servlet.UserServlet;
import com.podcast.Utils.N_m3u8DL_RE;
import com.podcast.service.ChannelService;
import com.podcast.service.PodcastUserService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class UpdateInit implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger("UpdateInit");
    public static Long SYSYTEM_START_TIME = System.currentTimeMillis();
    public static String WEBAPP_PATH;
    public static String deletePluginPath;
    public static Properties confProperties;
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        // 获取ServletContext对象
        ServletContext context = servletContextEvent.getServletContext();
        // 获取webapp目录的绝对路径
        String webappPath = context.getRealPath("/");
        WEBAPP_PATH = webappPath;
        deletePluginPath = WEBAPP_PATH + "tmp" + File.separator + "deletePlugins";


        //1.读取要删除的插件
        try {
            List<String> deletePluginList = FileUtils.readLines(new File(deletePluginPath));
            for (String p : deletePluginList) {
                //一行一行读取插件绝对路径，然后删除
                /**
                 * 用JavaAPI无法删除
                 */
                String rmcmd = "rm -rf "+p;
                LOGGER.info("删除插件："+rmcmd);
                N_m3u8DL_RE.Cmd(rmcmd);
            }

            //清空插件删除列表
            FileUtils.write(new File(deletePluginPath)," ","utf-8");

        } catch (IOException e) {
            LOGGER.error("插件删除列表无法读取！");
        }
        //是否有新创建
        UserServlet.CREATE_STATUS = 0;
        PodcastUserService service = new PodcastUserService();
        //存入数据库
        service.updateWebappPath(webappPath);


        //创建必要的文件夹 video、audio、plugin、xml
        File directory  = new File(webappPath);
        new File(directory,"video").mkdir();
        new File(directory,"audio").mkdir();
        new File(directory,"plugin").mkdir();
        new File(directory,"xml").mkdir();
        new File(directory,"tmp").mkdir();


        //定时任务
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable checkForUpdateTarget = new Target();
        // 在0分钟后开始执行任务，每隔一分钟重复执行
        executor.scheduleAtFixedRate(checkForUpdateTarget, 0, 1, TimeUnit.MINUTES);


        //改成定时任务
        ChannelService channelService = new ChannelService();
        Runnable checkForSurvivalTarget = new CheckForSurvival(channelService.getAllUuid());

        // 在0秒钟后开始执行任务，每隔x秒钟重复执行，根据配置信息
        Properties properties = new Properties();
        confProperties = properties;
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
        executor.scheduleAtFixedRate(checkForSurvivalTarget, 0, checkForSurvival, TimeUnit.SECONDS);

        //定期删除日志文件
        ScheduledExecutorService executor2 = Executors.newScheduledThreadPool(1);
        Runnable deleteLogsTarget = new DeleteLogsTarget();
        // 在0分钟后开始执行任务，每隔一个小时重复执行
        executor2.scheduleAtFixedRate(deleteLogsTarget, 0, 1, TimeUnit.HOURS);

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
