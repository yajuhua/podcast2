package com.podcast.Servlet;

import com.google.gson.Gson;
import com.podcast.Utils.N_m3u8DL_RE;
import com.podcast.loader.PluginLoader;
import com.podcast.pojo.Plugin;
import com.podcast.pojo.SystemInfo;
import com.podcast.update.Update;
import com.podcast.update.UpdateInit;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 关于系统的servlet
 * 4
 */
@WebServlet("/system/*")
public class SystemServlet extends BaseServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger("SystemServlet");

    /**
     * 控制tomcat服务器，比如重启
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void controlSystemServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /**
         * 控制码:controlCode
         *       0:关机
         *       1:重启
         *       2:更新系统，执行updateSystem.sh脚本
         */
        String controlCode = request.getParameter("controlCode");
        if (controlCode.equals("0")){
            //关机
        }else if (controlCode.equals("1")){
            //重启
            LOGGER.info("系统重启");
            N_m3u8DL_RE.Cmd("/restartTomat.sh");
        }else if (controlCode.equals("2")){
            //更新系统
            LOGGER.info("更新系统");
            N_m3u8DL_RE.Cmd("/updateSystem.sh");
        }
    }

    public static Map<String,Class> scanerPlugin(String webappPath) throws Exception {
        //配置文件的名称
        String PROPERTIES_NAME = "plugin.properties";

        //所有插件的位置
        String pluginsPath = webappPath+"plugin";
        File pluginFile = new File(pluginsPath);

        //获取所有的插件文件
        File[] pluginList = pluginFile.listFiles();

        //将所有插件存入map集合
        Map<String,Class> pluginsClass = new HashMap<>();

        //遍历
        for (File plugin : pluginList) {
            String pluginPath = plugin.getAbsolutePath();
            ClassLoader classLoader = getClassLoader(pluginPath);
            LOGGER.debug("pluginPath:"+pluginPath);
            Properties properties = getProperties(classLoader,PROPERTIES_NAME);
            //获取插件名称
            String name = (String) properties.get("name");
            LOGGER.info("插件名称:"+name);

            //加载插件的类
            Class mainClass = PluginLoader.loadJar(pluginPath);

            //存入Map集合
            pluginsClass.put(name,mainClass);
        }
        return pluginsClass;
    }


    /**
     * 获得jar中的properties
     *
     * @param classLoader    classLoader
     * @param propertiesName 文件名称
     * @return
     * @throws IOException
     */
    public static Properties getProperties(ClassLoader classLoader, String propertiesName) throws IOException {
        InputStream propertiesStream = classLoader.getResourceAsStream(propertiesName);
        Properties properties = new Properties();
        properties.load(propertiesStream);
        propertiesStream.close();
        return properties;
    }

    /**
     * 获得ClassLoader
     *
     * @param jarFilePath jar文件路径
     * @return
     * @throws MalformedURLException
     */
    public static final ClassLoader getClassLoader(String jarFilePath) throws MalformedURLException {
        File jarFile = new File(jarFilePath);
        if (!jarFile.exists()) {
            return null;
        }
        URL url = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url}, null);
        return classLoader;
    }


    /**
     * 删除插件
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void deletePluginServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pluginName = request.getParameter("name");
        String pluginVersion = request.getParameter("version");
        /**
         * 根据插件名称和版本号进行删除
         * 读取plugin包下的所有插件配置文件，符合的删除
         */
        //配置文件的名称
        String PROPERTIES_NAME = "plugin.properties";

        //所有插件的位置
        String pluginsPath = UpdateInit.WEBAPP_PATH+"plugin";
        File pluginFile = new File(pluginsPath);

        //获取所有的插件文件
        File[] pluginList = pluginFile.listFiles();


        //遍历,将要删除的插件绝对路径存入插件删除列表
        for (File plugin : pluginList) {
            String pluginPath = plugin.getAbsolutePath();
            ClassLoader classLoader = getClassLoader(pluginPath);
            Properties properties = getProperties(classLoader,PROPERTIES_NAME);
            String name = (String) properties.get("name");
            String version = (String) properties.get("version");
            if (pluginName.equals(name) && pluginVersion.equals(version)){
                FileUtils.write(new File(UpdateInit.deletePluginPath),plugin.getAbsolutePath(),"utf-8",true);
            }
        }

        //重启系统
        LOGGER.info("系统重启");
        N_m3u8DL_RE.Cmd("/restartTomat.sh");
    }


    /**
     * 获取系统信息：插件信息、系统运行时间、系统版本信息
     */
    public void systemInfoServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            Properties pluginProperties = getProperties(pluginClass.getClassLoader(), PROPERTIES_NAME);
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

    /**
     * 上传war包的服务器用于更新系统
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void uploadWarServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String warPath = UpdateInit.WEBAPP_PATH+"tmp"+File.separator;
        //先删除之前的war包
        File war = new File(warPath + "podcast2.war");

        //判断是否存在war包
        if (war.exists()){
            //
            boolean deleteWar = FileUtils.deleteQuietly(new File(warPath + "podcast2.war"));
            if (deleteWar){
                LOGGER.error("war包删除失败！");
                return;
            }else {
                LOGGER.debug("war删除成功！");
            }
        }

        //上传插件
        LOGGER.debug("warPath:"+warPath);
        upload(request,response,warPath);
    }

    /**
     * 通过war包更新系统
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void updateSystem (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String warPath = UpdateInit.WEBAPP_PATH+"tmp"+File.separator;
        File war = new File(warPath + "podcast2.war");
        //判断是否存在war包
        if (!war.exists()) {
            //找不到war包
            LOGGER.error("找不到war包！");
        }

        //执行系统更新脚本
        N_m3u8DL_RE.Cmd("/updateSystem.sh");

    }

    /**
     * 插件上传
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void uploadServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //上传插件
        String pluginsPath = UpdateInit.WEBAPP_PATH+"plugin"+File.separator;
        LOGGER.debug("pluginsPath:"+pluginsPath);
        upload(request,response,pluginsPath);
    }

    /**
     * 文件上传
     * @param request
     * @param response
     * @param savePath
     */
    public  static void upload(HttpServletRequest request, HttpServletResponse response,String savePath) throws IOException {
        // 检查请求是否是上传文件
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            // 返回错误信息
            return;
        }

        // 创建文件上传工厂和上传组件
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        String fileName = null;
        try {
            // 解析请求
            List<FileItem> items = upload.parseRequest(request);

            // 处理上传文件
            for (FileItem item : items) {
                if (!item.isFormField()) {
                    fileName = item.getName();
                    String contentType = item.getContentType();
                    long fileSize = item.getSize();

                    File uploadedFile = new File(savePath + fileName);
                    item.write(uploadedFile);

                    // 处理上传结果
                    LOGGER.info(fileName+"上传成功！");
                    response.setContentType("text/*; charset=utf-8");
                    response.getWriter().write("uploadok");
                }
            }
        } catch (Exception e) {
            // 处理异常情况
            LOGGER.error(fileName+"上传失败！");
            response.setContentType("text/*; charset=utf-8");
            response.getWriter().write("uploadno");
        }
    }
}
