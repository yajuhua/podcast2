package com.podcast.Servlet;

import com.google.gson.Gson;
import com.podcast.Type.Type;
import com.podcast.Utils.TimeFormat;
import com.podcast.loader.PluginLoader;
import com.podcast.pojo.ChannelDate;
import com.podcast.service.ChannelService;
import com.podcast.service.PodcastUserService;
import com.podcast.update.Update;
import com.podcast.update.UpdateInit;
import org.podcast2.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

@WebServlet("/xmlFactoryServlet")
public class XmlFactoryServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger("XmlFactoryServlet");
    public static Integer CREATE_STATUS;
    private PodcastUserService service = new PodcastUserService();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 获取请求协议（http 或 https）
            String protocol = request.getScheme();

            // 获取服务器主机名
            String serverName = request.getServerName();

            // 获取服务器端口号
            int serverPort = request.getServerPort();

            // 获取应用上下文路径
            String contextPath = request.getContextPath();

            // 拼接完整的请求 URL
            String requestUrl = protocol + "://" + serverName + ":" + serverPort + contextPath;
            LOGGER.debug("requestUrl:"+requestUrl);

            //获取自定义剧集
            String episodesStr = request.getParameter("episodes");
            String[] episodesArray = episodesStr.split(",");
            List<Integer> episodes = new ArrayList<>();
            for (int i = 0; i < episodesArray.length; i++) {
                episodes.add(Integer.parseInt(episodesArray[i]));
            }

            LOGGER.debug("episodes:"+episodes);

            //更新IP地址
            service.UpdateIP(requestUrl);
            LOGGER.info("开始创建！");
            String uuid = create(request);
            LOGGER.info("创建完成！"+uuid);
            response.getWriter().write("ok");
            LOGGER.info("进入首次更新！");

            CREATE_STATUS = 1;
            //开启一个线程进行首次更新
            Update update = new Update(uuid,episodes);
            update.start();
        } catch (Exception e) {
            LOGGER.error("创建失败 "+request.getParameter("url"));
            request.getRequestDispatcher("add.html").forward(request,response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
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
     * 首次创建
     * @return
     * @throws Exception
     */
    public static String create(HttpServletRequest request) throws Exception {

        //获取参数
        String typeStr = request.getParameter("type");
        String url = request.getParameter("url");
        //频道类型："Video、Live、News、Shorts、Post
        String frequency = request.getParameter("frequency");
        ServletContext servletContext = request.getServletContext();
        String webappPath = servletContext.getRealPath("/");
        //节目存活时间
        String survival = request.getParameter("survival");
        long survivalTime = Long.parseLong(survival);
        LOGGER.info("survival:"+survival+"s");
        //更新数据数据库webappPath
        PodcastUserService service = new PodcastUserService();
        service.updateWebappPath(webappPath);
        Class plugin  = null;
        LOGGER.info("检查更新频率:"+frequency);


        //判断格式类型
        Type type = Type.video;;
        if ("audio".equals(typeStr)){
           type =  Type.audio;
        }

        //根据插件名称去匹配url中有没有包含，在进入这里之前，先判断url是否可通
        Map<String, Class> plugins = scanerPlugin(webappPath);
        //插件的名称集合
        Set<String> pluginNames = plugins.keySet();
        LOGGER.info("当前插件数量:"+pluginNames.size());
        String usePluginName = "";
        for (String pluginName : pluginNames) {
            if (url.contains(pluginName)){
                //如果url包含插件的名称，就返回该插件的名称
                usePluginName =  pluginName;
                LOGGER.info("该频道使用的插件:"+usePluginName);
                break;
            }
            usePluginName = null;
        }

        //找插件，先判断是否为null
        if (usePluginName!=null){
            plugin = plugins.get(usePluginName);
        }else {
            //找不到
            LOGGER.error("找不到该插件！");
           throw new Exception("找不到插件");
        }


        //1.根据插件获取构造器
        Constructor constructor = plugin.getConstructor(String.class,String.class);
        Object o = constructor.newInstance(url,type.name());

       /* //2.获取所有方法
        Method[] methods = plugin.getDeclaredMethods();

        //3.执行方法，把返回结果存入Map集合
        Map<String,Object> methodResult = new HashMap<>();
        for (Method method : methods) {
            methodResult.put(method.getName(),method.invoke(o));
        }*/

        //获取channel
        Method methodChannel = plugin.getMethod("channel");
        String channelStr = (String) methodChannel.invoke(o);
        Gson gson = new Gson();
        Channel channel = gson.fromJson(channelStr, Channel.class);

        //4.以uuid命名，保存到webapp/xml/UUID.xml
        String uuid = UUID.randomUUID().toString();
        String savePath = webappPath+ "xml"+File.separator+uuid+".xml";
        PrintStream ps = new PrintStream(new File(savePath));

        Integer getCount = 0;

        //5.将频道信息写入xml文件中
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\" encoding=\"UTF-8\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:itunes=\"http://www.itunes.com/dtds/podcast-1.0.dtd\">\n");
        xml.append("\t<channel>\n");
        xml.append("\t\t<title>").append(channel.getTitle()).append("</title>\n");
        xml.append("\t\t<pubDate>").append(TimeFormat.now()).append("</pubDate>\n");
        xml.append("\t\t<language>").append("zh-CN").append("</language>\n");
        xml.append("\t\t<link>").append(channel.getLink()).append("</link>\n");
        xml.append("\t\t<itunes:image href=\"").append(channel.getImage()).append("\"/>\n");
        xml.append("\t\t<description>").append(channel.getDescription()).append("</description>\n");
        xml.append("\t\t<itunes:author>").append(channel.getAuthor()).append("</itunes:author>\n");
        xml.append("\t\t<itunes:category text=\"").append(channel.getCategory()).append("\"/>\n");
        xml.append("\t\t<type>").append(type.name()).append("</type>\n");//为创建完成后就更新，在totalCount上减一
        xml.append("\t\t<totalCount>").append(getCount-1).append("</totalCount>\n");
        xml.append("\t\t<plugin>").append(usePluginName).append("</plugin>\n");
        xml.append("\t</channel>\n");
        xml.append("\t<update>update</update>\n");
        xml.append("</rss>");



        //6.写入xml文件
        ps.print(xml);
        ps.close();


        ChannelDate channelDate = new ChannelDate();
        channelDate.setUuid(uuid);
        channelDate.setChannelTitle(channel.getTitle());
        channelDate.setUpdateTimestamp(System.currentTimeMillis()/1000);//时间秒数
        int i = Integer.parseInt(frequency);
        channelDate.setFrequency((long) i);
        channelDate.setLatestCheckTimestamp(System.currentTimeMillis()/1000);
        channelDate.setChannelFace(channel.getImage());
        channelDate.setSurvival(survivalTime);
        ChannelService service1 = new ChannelService();
        service1.add(channelDate);


        //6.返回uuid
        return uuid;
    }
}
