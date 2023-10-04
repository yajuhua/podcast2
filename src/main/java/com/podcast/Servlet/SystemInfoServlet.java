package com.podcast.Servlet;

import com.google.gson.Gson;
import com.podcast.pojo.Plugin;
import com.podcast.pojo.SystemInfo;
import com.podcast.update.Update;
import com.podcast.update.UpdateInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

import static com.podcast.Servlet.XmlFactoryServlet.scanerPlugin;

@WebServlet("/systemInfoServlet")
public class SystemInfoServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger("SystemInfoServlet");
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //配置文件的名称
        String PROPERTIES_NAME = "plugin.properties";
        //获取所有插件信息
        Map<String, Class> pluginsClassMap = null;
        try {
            pluginsClassMap = scanerPlugin(UpdateInit.WEBAPP_PATH);
        } catch (Exception e) {
            LOGGER.error("扫描插件时出错了："+e);
        }
        //存放插件信息集合
        List<Plugin> pluginList = new ArrayList<>();

        //遍历插件信息
        Set<String> pluginsKey = pluginsClassMap.keySet();
        for (String plugin : pluginsKey) {
            Class pluginClass = pluginsClassMap.get(plugin);
            Properties pluginProperties = XmlFactoryServlet.getProperties(pluginClass.getClassLoader(), PROPERTIES_NAME);
            String name = (String) pluginProperties.get("name");
            String version = (String) pluginProperties.get("version");
            String update = (String) pluginProperties.get("update");
            Plugin plugin1 = new Plugin(name,version,update);
            pluginList.add(plugin1);
        }

        // 使用ClassLoader加载Properties文件
        InputStream inputStream = Update.class.getClassLoader().getResourceAsStream("conf.properties");
        Properties mainProperties = new Properties();
        mainProperties.load(inputStream);
        inputStream.close();
        //系统信息
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setSystemCode((String) mainProperties.getProperty("code"));
        systemInfo.setSystemUpdate((String) mainProperties.getProperty("update"));
        systemInfo.setSystemVersion((String) mainProperties.getProperty("version"));
        Long runTime = (System.currentTimeMillis() - UpdateInit.SYSYTEM_START_TIME)/1000;

        //转换成0天07时03分这种形式
        Instant instant = Instant.ofEpochMilli(UpdateInit.SYSYTEM_START_TIME);
        LocalDateTime systemStartTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        Duration between = Duration.between(systemStartTime, LocalDateTime.now());
        long days = between.toDays();
        long hours = between.toHours() - between.toDays() * 24;
        long minutes = between.toMinutes() - (days*24*60) - (hours*60);
        systemInfo.setSystemRuntime(days+"天"+hours+"时"+minutes+"分");
        systemInfo.setPluginList(pluginList);

        //转换成json字符串
        Gson gson = new Gson();
        String systemInfoJson = gson.toJson(systemInfo);

        //响应到前端
        response.setContentType("text/json;charset=utf-8");
        response.getWriter().write(systemInfoJson);


    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    @Test
    public void t1(){
        LocalDateTime of = LocalDateTime.of(2023, 10, 1, 10, 5, 1, 1);
        //Instant instant = Instant.ofEpochMilli(1633311599000L);
        //LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        Duration between = Duration.between(of, LocalDateTime.now());
        long days = between.toDays();
        long hours = between.toHours() - between.toDays() * 24;
        long minutes = between.toMinutes() - (days*24*60) - (hours*60);
        System.out.println(days + "天" + hours + "时" + minutes + "分");
    /*    System.out.println(days);
        System.out.println(hours);
        System.out.println(minutes);*/
    }


}
