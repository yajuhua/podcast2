package com.podcast.Servlet;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.podcast.Type.Type;
import com.podcast.Utils.TimeFormat;
import com.podcast.loader.PluginLoader;
import com.podcast.pojo.*;
import com.podcast.service.ChannelService;
import com.podcast.service.PodcastUserService;
import com.podcast.update.Update;
import com.podcast.update.UpdateInit;
import org.apache.commons.io.FileUtils;
import io.github.yajuhua.podcast2API.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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

/**
 * 关于用户的，如用户名和密码的设置
 * 6
 */
@WebServlet("/user/*")
public class UserServlet  extends BaseServlet{
    public static PodcastUserService service = new PodcastUserService();
    public static ChannelService channelService = new ChannelService();
    public static List<String> CREATE_UUID = new ArrayList<>();//首次更新的UUID
    public static Map<String,Update> FIRST_UPDATE = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger("UserServlet");
    private static final String CERT_PATH="/opt/tomcat/tomcat8/cert/";//证书和密钥存放位置
    private Gson gson = new Gson();


    /**
     * 获取channel表中的所有数据
     * @param request
     * @param response
     * @throws Exception
     */
    public void selectChannelByIdServlet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String uuid = request.getParameter("uuid");
        ChannelDate channelDate = channelService.selectAllByUuid(uuid);
        response.setContentType("text/json;charset=utf-8");
        response.getWriter().write(gson.toJson(channelDate));
    }

    /**
     * 根据uuid更新channel表
     * @param request
     * @param response
     * @throws Exception
     */
    public void updateChannelByIdServlet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boolean flag = false;
        try {
            String uuid = request.getParameter("uuid");
            String type = request.getParameter("type");
            String frequency = request.getParameter("frequency");
            String survival = request.getParameter("survival");
            String status = request.getParameter("status");
            String args = request.getParameter("args");

            //封装更新信息
            ChannelDate channelDate = channelService.selectAllByUuid(uuid);
            if (type!=null){channelDate.setType(type);}
            if (frequency!=null){channelDate.setFrequency(Long.parseLong(frequency));}
            if (survival!=null){channelDate.setSurvival(Long.parseLong(survival));}
            if (status!=null){channelDate.setStatus(Integer.parseInt(status));}
            if (args!=null){channelDate.setArgs(args);}

            //更新
            flag = channelService.updateChannel(channelDate);
        } catch (NumberFormatException e) {
            flag=false;
        }finally {
            response.getWriter().write(flag?"ok":"no");
        }
    }

    /**
     * 上传CA证书
     * 证书文件 podcast2.crt
     * 密钥文件 podcast2.key
     * @param request
     * @param response
     * @throws Exception
     */
    public void uploadCertServlet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //上传CA证书
        LOGGER.debug("certPath:"+CERT_PATH);
        SystemServlet.upload(request,response,CERT_PATH);
    }

    /**
     * 删除CA认证
     * @param request
     * @param response
     * @throws Exception
     */
    public void deleteCertServlet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        File[] certList = new File(CERT_PATH).listFiles();
        String fileName = request.getParameter("fileName");
        System.out.println("fileName" + fileName);
        for (File file : certList) {
            if (file.getName().equals(fileName)){
                boolean flag = FileUtils.deleteQuietly(file);
                response.getWriter().write(flag==true?"deleteCertOK":"deleteCertErr");
                break;
            }
        }
    }

    /**
     * 删除所有CA认证
     * @param request
     * @param response
     * @throws Exception
     */
    public void deleteAllCertServlet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        File[] certList = new File(CERT_PATH).listFiles();
        for (File cert : certList) {
            FileUtils.deleteQuietly(cert);
        }
        //删除成功！
        response.getWriter().write("deleteAllCertOK");
    }

    public void getAllCertInfoServlet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        File[] certList = new File(CERT_PATH).listFiles();
        List<CertFile> certName = new ArrayList<>();
        for (File cert : certList) {
            certName.add(new CertFile(cert.getName()));
        }
        //删除成功！
        response.getWriter().write(gson.toJson(certName));
    }

    /**
     * 申请CA证书
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void certificateServlet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        File ca = new File("/opt/tomcat/tomcat8/webapps/podcast2/tmp/install_ca.sh");
        if (!ca.exists()){
            //新建install_ca.sh
            StringBuffer sb = new StringBuffer();
            sb.append("#!/bin/bash\n")
                    .append("yum install curl -y && yum install openssl -y && yum install cronie -y\n")
                    .append("curl https://get.acme.sh | sh\n")
                    .append("ln -s  /root/.acme.sh/acme.sh /usr/local/bin/acme.sh\n")
                    .append("yum install socat -y\n")
                    .append("acme.sh --register-account -m $(date +%s)@podcast2.com\n")
                    .append("acme.sh  --issue -d $1 --standalone -k ec-256\n")
                    .append("if [ $? -ne 0 ];then\n")
                    .append("\tacme.sh  --force  --issue -d $1 --standalone -k ec-256\n")
                    .append("\tif [ $? -ne 0  ];then\n")
                    .append("\t\treturn 1\n")
                    .append("\tfi\n")
                    .append("fi\n")
                    .append("acme.sh --installcert -d $1 --ecc  --key-file   $2.key   --fullchain-file $2.crt\n")
                    .append("if [ $? -ne 0 ];then\n")
                    .append("\treturn 2\n")
                    .append("fi\n");
            FileUtils.write(ca,sb);//写入
        }
        //域名
        String domain = request.getParameter("domain");
        //执行申请证书脚本
        Process exec = Runtime.getRuntime()
                .exec("sh /opt/tomcat/tomcat8/webapps/podcast2/tmp/install_ca.sh " + domain + " /opt/tomcat/tomcat8/cert/podcast2");
        int exitCode = exec.waitFor();

        //返回退出码
        response.getWriter().write(exitCode+"");

    }

    /**
     * 修改用户名和密码
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void changeServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //获取参数
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        //debug
        LOGGER.debug("username:"+username);
        LOGGER.debug("password:"+password);

        //根据参数进行选择修改
        if (username != null && password == null){
            LOGGER.debug("修改用户名:"+username);
            //修改用户名
            service.changeUsername(username);
        }else if (username == null && password != null){
            LOGGER.debug("修改密码:"+password);
            //修改密码
            service.changePassword(password);
        }else if (username != null && password != null){
            LOGGER.debug("修改用户名:"+username+";修改密码:"+password);
            //修改用户名和密码
            service.changeAll(username,password);
        }

        HttpSession session = request.getSession();
        //销毁session
        session.invalidate();
        response.getWriter().write("ok");
    }

    /**
     * 删除订阅
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void deleteServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uuid = request.getParameter("uuid");

        //也要删除xml文件
        String webappPath = service.getWebappPath();
        String xmlSavePath = webappPath+ "xml"+ File.separator+uuid+".xml";
        File file = new File(xmlSavePath);
        if (FileUtils.deleteQuietly(file)){
            channelService.deleteByUuid(uuid);
            LOGGER.info(xmlSavePath+"删除成功！");
        }else {
            LOGGER.info(xmlSavePath+"删除失败！");
        }

        //删除该频道下的资源
        String videoResourceSavePath = webappPath+ "video"+ File.separator;
        String audioResourceSavePath = webappPath+ "audio"+ File.separator;
        List<String> resourceUuidByXmlUuid = channelService.getResourceUuidByXmlUuid(uuid);
        File videoresourceFile = new File(videoResourceSavePath);
        List<File> vides = Arrays.stream(videoresourceFile.listFiles()).toList();
        File audioresourceFile = new File(audioResourceSavePath);
        List<File> audios = Arrays.stream(audioresourceFile.listFiles()).toList();

        //获取所有资源的文件
        List<File> resources = new ArrayList<>();
        resources.addAll(vides);
        resources.addAll(audios);
        //遍历所有的资源文件
        for (File resource : resources) {
            for (String rUuid : resourceUuidByXmlUuid) {
                if (resource.getName().contains(rUuid)){
                    //删除特定uuid的资源
                    FileUtils.deleteQuietly(resource);
                    //删除数据库
                    channelService.deleteByResourceUuid(rUuid);
                }
            }
        }

        //在首次更新UUID集合中移除此UUID
        UserServlet.CREATE_UUID.remove(uuid);

        //结束正在下载
        Update update = UserServlet.FIRST_UPDATE.get(uuid);
        update.interrupt();//发送中断信息
        UserServlet.FIRST_UPDATE.remove(uuid);

        request.getRequestDispatcher("/index.html").forward(request,response);
    }

    /**
     * 退出登录
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void exitServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        //销毁
        session.invalidate();
        response.getWriter().write("ok");
    }

    /**
     * 登录
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void loginServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        PodcastUserService podcastUserService = new PodcastUserService();
        PodcastUser login = podcastUserService.login(username, password);

        request.setCharacterEncoding("utf-8");
        response.setContentType("application/json;charset=utf-8");
        HttpSession session = request.getSession();

        if (login!=null){
            //提示成功
            response.getWriter().write("ok");

            session.setAttribute("user",login);

            LOGGER.debug("登录状态:"+login);
        }else {
            //提示错误，继续在login页面
            response.getWriter().write("error");

        }
    }

    /**
     * 查询所有
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void selectAllServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ChannelService service = new ChannelService();
        response.setCharacterEncoding("utf-8");
        List<ChannelDate> channelDates = service.seletAll();

        //存入新的集合
        List<ChannelDataShow> channelDataShows = new ArrayList<>();
        for (int i = 0; i < channelDates.size(); i++) {
            ChannelDataShow channelDataShow = new ChannelDataShow();
            channelDataShow.setChannelTitle(channelDates.get(i).getChannelTitle());
            channelDataShow.setUuid(channelDates.get(i).getUuid());
            channelDataShow.setUpdateTimestamp(TimeFormat.formatDate(channelDates.get(i).getUpdateTimestamp()));
            channelDataShow.setChannelFace(channelDates.get(i).getChannelFace());
            channelDataShows.add(channelDataShow);
        }

        String jsonString = JSON.toJSONString(channelDataShows);
        response.setContentType("text/json;charset=utf-8");
        response.getWriter().write(jsonString);
    }

    /**
     * xml创建工厂，用于频道xml文件的生成
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void xmlFactoryServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

            //自定义域名：http://t.podcast2.org:8088 ->域名:端口
            //1.判断数据库的IP详细，如果包含[customize]说明是自定义域名，创建订阅时就不更新域名了
            String ipAddress = service.getIP();
            if (ipAddress.contains("null") || ipAddress == null || !ipAddress.contains("[customize]") ){
                //使用默认IP地址
                //更新IP地址
                service.UpdateIP(requestUrl);//存入数据库
            }


            LOGGER.info("开始创建！");
            String uuid = create(request);
            LOGGER.info("创建完成！"+uuid);
            response.getWriter().write("ok");
            LOGGER.info("进入首次更新！");

            //开启一个线程进行首次更新
            Update update = new Update(uuid,episodes);
            update.start();
            CREATE_UUID.add(uuid);
            FIRST_UPDATE.put(uuid,update);
        } catch (Exception e) {
            LOGGER.error("创建失败 "+request.getParameter("url"));
            request.getRequestDispatcher("add.html").forward(request,response);
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
     * 首次创建
     * @return
     * @throws Exception
     */
    public static String create(HttpServletRequest request) throws Exception {

        //获取参数
        String typeStr = request.getParameter("type");
        String url = request.getParameter("url");
        String episodes = request.getParameter("episodes");
        String args = request.getParameter("args");

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
        LOGGER.info("episodes:"+episodes);
        LOGGER.debug("url:"+url);
        LOGGER.info("args:"+args);

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

        //获取channel
        Method methodChannel = plugin.getMethod("channel");
        String channelStr = (String) methodChannel.invoke(o);
        Gson gson = new Gson();
        Channel channel = gson.fromJson(channelStr, Channel.class);


        //4.以uuid命名，保存到webapp/xml/UUID.xml
        String uuid = UUID.randomUUID().toString();
        String savePath = webappPath+ "xml"+File.separator+uuid+".xml";
        PrintStream ps = new PrintStream(new File(savePath));


        //5.将频道信息写入xml文件中
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\" encoding=\"UTF-8\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:itunes=\"http://www.itunes.com/dtds/podcast-1.0.dtd\">\n");
        xml.append("\t<channel>\n");
        xml.append("\t\t<title><![CDATA[ ").append(channel.getTitle()).append(" ]]></title>\n");
        xml.append("\t\t<pubDate>").append(TimeFormat.now()).append("</pubDate>\n");
        xml.append("\t\t<language>").append("zh-CN").append("</language>\n");
        xml.append("\t\t<link><![CDATA[ ").append(channel.getLink()).append(" ]]></link>\n");
        xml.append("\t\t<itunes:image href=\"").append(channel.getImage()).append("\"/>\n");
        xml.append("\t\t<description><![CDATA[ ").append(channel.getDescription()).append(" ]]></description>\n");
        xml.append("\t\t<itunes:author><![CDATA[ ").append(channel.getAuthor()).append(" ]]></itunes:author>\n");
        xml.append("\t\t<itunes:category text=\"").append(channel.getCategory()).append("\"/>\n");
        xml.append("\t\t<type>").append(type.name()).append("</type>\n");//为创建完成后就更新，在totalCount上减一
        xml.append("\t\t<equal>").append("none").append("</equal>\n");
        xml.append("\t\t<plugin>").append(usePluginName).append("</plugin>\n");
        xml.append("\t<update>update</update>\n");
        xml.append("\t</channel>\n");
        xml.append("</rss>");

        //6.写入xml文件
        ps.print(xml);
        ps.close();

        //将数据封装存入数据库
        ChannelDate channelDate = new ChannelDate();
        channelDate.setUuid(uuid);
        channelDate.setChannelTitle(channel.getTitle());
        channelDate.setUpdateTimestamp(System.currentTimeMillis()/1000);//时间秒数
        int i = Integer.parseInt(frequency);
        channelDate.setFrequency((long) i);
        channelDate.setLatestCheckTimestamp(System.currentTimeMillis()/1000);
        channelDate.setChannelFace(channel.getImage());
        channelDate.setSurvival(survivalTime);
        channelDate.setArgs(args);//下载器选项
        channelDate.setLink(channel.getLink());//频道链接
        channelDate.setEqual("none");//比对更新
        channelDate.setDescription(channel.getDescription());//频道描述
        channelDate.setType(typeStr);//频道资源类型
        channelDate.setPlugin(usePluginName);//频道使用插件
        ChannelService service1 = new ChannelService();
        service1.add(channelDate);

        //6.返回uuid
        return uuid;
    }


    /**
     * 获取完成下载记录
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void selectCompleteDownloadServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Download> completeDownload = channelService.selectCompleteDownload();
        response.setContentType("text/json;charset=utf-8");
        response.getWriter().write(gson.toJson(completeDownload));
    }


    /**
     * 根据id删除下载记录
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void deleteDownloadRecordServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        channelService.deleteDownloadRecord(id);
        response.setContentType("text/*;charset=utf-8");
        response.getWriter().write("ok");
    }

    /**
     * 获取channel数据库表中所有数据
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void channelDataServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<ChannelDate> channelDates = channelService.seletAll();
        response.setContentType("text/*;charset=utf-8");
        response.getWriter().write(gson.toJson(channelDates));
    }


    /**
     * 导入数据
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void importChannelDataServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext servletContext = request.getServletContext();
        String webappPath = servletContext.getRealPath("/");



        //类型
        java.lang.reflect.Type listType = new TypeToken<List<ChannelDate>>(){}.getType();
        String importData = request.getParameter("importData");
        if (importData!=null){

            LOGGER.debug("importData:"+importData);

            List<ChannelDate> channelDates = null;
            try {
                channelDates = gson.fromJson(importData, listType);
            } catch (JsonSyntaxException e) {
                response.getWriter().write("importError");
            }

            for (ChannelDate channelDate : channelDates) {
                //获取基本信息
                String uuid = channelDate.getUuid();
                String savePath = webappPath+ "xml"+File.separator+uuid+".xml";
                PrintStream ps = new PrintStream(new File(savePath));

                //将频道信息写入xml文件中
                StringBuffer xml = new StringBuffer();
                xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<rss version=\"2.0\" encoding=\"UTF-8\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:itunes=\"http://www.itunes.com/dtds/podcast-1.0.dtd\">\n");
                xml.append("\t<channel>\n");
                xml.append("\t\t<title><![CDATA[ ").append(channelDate.getChannelTitle()).append(" ]]></title>\n");
                xml.append("\t\t<pubDate>").append(TimeFormat.now()).append("</pubDate>\n");
                xml.append("\t\t<language>").append("zh-CN").append("</language>\n");
                xml.append("\t\t<link><![CDATA[ ").append(channelDate.getLink()).append(" ]]></link>\n");
                xml.append("\t\t<itunes:image href=\"").append(channelDate.getChannelFace()).append("\"/>\n");
                xml.append("\t\t<description><![CDATA[ ").append(channelDate.getDescription()).append(" ]]></description>\n");
                xml.append("\t\t<itunes:author><![CDATA[ ").append(channelDate.getChannelTitle()).append(" ]]></itunes:author>\n");
                xml.append("\t\t<itunes:category text=\"").append("null").append("\"/>\n");
                xml.append("\t\t<type>").append(channelDate.getType()).append("</type>\n");//为创建完成后就更新，在totalCount上减一

                xml.append("\t\t<equal>").append("none").append("</equal>\n");
                xml.append("\t\t<plugin>").append(channelDate.getPlugin()).append("</plugin>\n");
                xml.append("\t<update>update</update>\n");
                xml.append("\t</channel>\n");
                xml.append("</rss>");

                //写入xml文件
                ps.print(xml);
                ps.close();

                //存入数据库
                channelService.add(channelDate);
            }
            response.getWriter().write("importOk");
        }else {
            response.getWriter().write("importError");
        }


    }


    /**
     * 自定义域名
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void customDomainNameServlet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String customDomainName = request.getParameter("customDomainName");
        service.UpdateIP("[customize]"+customDomainName + "/podcast2");
        response.getWriter().write("ok");
    }

}
