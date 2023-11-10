package com.podcast.update;

import com.google.gson.Gson;
import com.podcast.Servlet.UserServlet;
import com.podcast.Utils.Mode;
import com.podcast.Utils.TimeFormat;
import com.podcast.loader.PluginLoader;
import com.podcast.service.ChannelService;
import com.podcast.service.PodcastUserService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.podcast2.Channel;
import org.podcast2.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class Update extends Thread{
    private String uuid;
    private List<Integer> episodes = null;

    public Update() {
    }

    public Update(String uuid) {
        this.uuid = uuid;
    }
    public Update(String uuid,List<Integer> episodes) {
        this.uuid = uuid;
        this.episodes = episodes;
    }
    private static final Logger LOGGER = LoggerFactory.getLogger("Update");
    private static ChannelService channelService = new ChannelService();
    private static PodcastUserService service = new PodcastUserService();

    @Override
    public void run() {
        run_();
    }

    /**
     * 这个是走main线程的
     */
    public void run_() {
        LOGGER.info("频道开始检查更新");
        String PROPERTIES_NAME = "plugin.properties";

        String webappPath = service.getWebappPath();
        Properties pluginProperties = null;
        Class channelPlugin = null;
        Properties mainProperties = new Properties();
        try {
            channelPlugin = getChannelPlugin(uuid, webappPath);
            pluginProperties = getProperties(channelPlugin.getClassLoader(), PROPERTIES_NAME);

            // 使用ClassLoader加载Properties文件
            InputStream inputStream = Update.class.getClassLoader().getResourceAsStream("conf.properties");
            mainProperties.load(inputStream);
            inputStream.close();
        } catch (Exception e) {
           LOGGER.error("使用ClassLoader加载Properties文件时出错！"+"详细:"+e);
        }
        //1.根据xml中的<plugin>ganjing</plugin>获取插件类，

        try {
            if (episodes==null || episodes.size()==0){
                //首次单集更新
                LOGGER.debug("进入单集更新");
                boolean update = update(uuid, channelPlugin, pluginProperties, mainProperties, webappPath);
                if (update){
                    //写入最新检查更新的时间
                    channelService.UpdateForChannel(System.currentTimeMillis()/1000,uuid);
                    LOGGER.info("新内容已更新完成！");
                }else {
                    //写入最新检查更新的时间
                    channelService.UpdateLatestCheckTimestampByUuid(System.currentTimeMillis()/1000,uuid);
                    LOGGER.info("检查更新完成，无内容更新！");
                }
            }else {
                //首次多集更新
                LOGGER.debug("进入多集更新");
                boolean update = update(uuid, channelPlugin, pluginProperties, mainProperties, webappPath,episodes);
                if (update){
                    //写入最新检查更新的时间
                    channelService.UpdateForChannel(System.currentTimeMillis()/1000,uuid);
                    LOGGER.info("新内容已更新完成！");
                    UserServlet.CREATE_STATUS = 0;
                    LOGGER.info("createStatus:"+UserServlet.CREATE_STATUS);
                }else {
                    //写入最新检查更新的时间
                    channelService.UpdateLatestCheckTimestampByUuid(System.currentTimeMillis()/1000,uuid);
                    LOGGER.info("检查更新完成，无内容更新！");
                }
            }

        } catch (Exception e) {
            LOGGER.error("检查更新失败！"+"uuid:"+ uuid +" 详细:"+e);
            UserServlet.CREATE_STATUS = 0;
        }

    }


    /**
     * 首次多集更新
     * @param uuid
     * @param plugin
     * @param pluginProperties
     * @param mainProperties
     * @param webappPath
     * @param episodes
     * @return
     */
    public static boolean update(String uuid, Class plugin, Properties pluginProperties, Properties mainProperties, String webappPath, List<Integer> episodes) throws Exception{
        /**
         * 常量
         */

        //0.根据uuid获取xml文件进行读取type
        String xmlPath = webappPath+ File.separator+"xml"+File.separator+uuid+".xml";

        //xml的uuid
        String xmlUuid = uuid;

        //需要读取的数据
        String type = null;
        String link = null;


        //解析xml
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new File(xmlPath));

            //获取根元素
            Element rootElement = document.getRootElement();
            Element channel = rootElement.element("channel");
            type = channel.element("type").getText();
            link = channel.element("link").getText();
        } catch (DocumentException e) {
            LOGGER.error("解析xml时出错！"+"详细:"+e);
        }

        //获取args
        String args = channelService.selectArgs(uuid);

        //通过插件获取items
        Constructor constructor = plugin.getConstructor(String.class, String.class, List.class,String.class);
        Object o = constructor.newInstance(link, type, episodes,args);
        Method methodItems = plugin.getMethod("items");
        List<String> itemsStr = (List<String>)methodItems.invoke(o);
        List<Item> items = new ArrayList<>();
        Gson gson = new Gson();
        for (int i = 0; i < itemsStr.size(); i++) {
            items.add(gson.fromJson(itemsStr.get(i),Item.class));
        }

        LOGGER.debug(items.toString());

        //读取数据库的IP地址
        String IP = service.getIP();

        //获取插件mode
        String audioMode = (String)pluginProperties.get("audioMode");
        String videoMode = (String) pluginProperties.get("videoMode");
        LOGGER.info("type:"+type);
        LOGGER.debug("audio:"+audioMode);
        LOGGER.debug("video:"+videoMode);

        //读取下载器类
        String downloader = (String)pluginProperties.get("downloader");
        downloader = "com.podcast.Utils."+downloader;
        LOGGER.info("downloader:"+downloader);
        Class downLoderClass = null;
        try {
            downLoderClass = Class.forName(downloader);
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.toString());
        }


        Item item_ = null;


        for (int i = 0; i < items.size(); i++) {
            /**
             * 变量
             */
            //生成资源的uuid
            String resourceUuid = UUID.randomUUID().toString();
            //解析item对象
            Item item = items.get(i);
            item_ = item;
            //创建模式
            Mode mode = new Mode(item.getEnclosure(),webappPath,downLoderClass,resourceUuid,type,IP);

            //最终的资源链接
            String enclosureLink = "";


            if ("video".equals(type)){
                //视频
                switch (videoMode){
                    case "V1":
                        enclosureLink = mode.V1();
                        writeItem(item,type,enclosureLink,xmlPath);
                        //将资源uuid添加到数据库
                        channelService.addResource(xmlUuid,resourceUuid);
                        break;

                    case "V2" :
                        enclosureLink = mode.V2();
                        writeItem(item,type,enclosureLink,xmlPath);
                        break;

                    case "customize" :
                        enclosureLink = mode.customize();
                        writeItem(item,type,enclosureLink,xmlPath);
                        //将资源uuid添加到数据库
                        channelService.addResource(xmlUuid,resourceUuid);
                        break;
                }
            }else {
                //音频
                switch (audioMode){
                    case "A1":
                        enclosureLink = mode.A1();
                        writeItem(item,type,enclosureLink,xmlPath);
                        //将资源uuid添加到数据库
                        channelService.addResource(xmlUuid,resourceUuid);
                        break;

                    case "A2" :
                        enclosureLink = mode.A2();
                        writeItem(item,type,enclosureLink,xmlPath);
                        break;

                    case "customize" :
                        enclosureLink = mode.customize();
                        writeItem(item,type,enclosureLink,xmlPath);
                        //将资源uuid添加到数据库
                        channelService.addResource(xmlUuid,resourceUuid);
                        break;
                }
            }
        }

        //更新对比
        updateCountOrEqual(item_,xmlPath);

        //更新status
        Method methodChannel = plugin.getMethod("channel");
        String channelStr = (String) methodChannel.invoke(o);
        Channel channel = gson.fromJson(channelStr, Channel.class);
        int getChannelStatus = channel.getStatus();
        channelService.UpdateForStatus(getChannelStatus,uuid);
        channelService.UpdateForEqual(item_.getEqual(),uuid);

        return true;
    }



    /**
     * 更新
     * @param uuid
     * @param plugin*/
    public static boolean update(String uuid, Class plugin, Properties pluginProperties, Properties mainProperties, String webappPath) throws Exception {

        //0.根据uuid获取xml文件进行读取count、type和link
        String xmlPath = webappPath+ File.separator+"xml"+File.separator+uuid+".xml";

        //xml的uuid
        String xmlUuid = uuid;

        //需要读取的数据
        Integer count = null;
        String equal = null;
        String type = null;
        String link = null;


        //解析xml
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new File(xmlPath));

            //获取根元素
            Element rootElement = document.getRootElement();
            Element channel = rootElement.element("channel");
            link = channel.element("link").getText();
            type = channel.element("type").getText();

            //获取countOrEqual
            List<Element> channels = channel.elements();
            for (Element element : channels) {
                if (element.getName().equals("totalCount")){
                    count = Integer.parseInt(element.getText());
                }else if (element.getName().equals("equal")){
                    equal = element.getText();
                }
            }
        } catch (DocumentException e) {
            LOGGER.error("解析xml时出错！"+"详细:"+e);
        }
        Constructor constructor = null;
        Object o = null;
        Item item = null;
        Channel channel = null;
        String args = channelService.selectArgs(uuid);//args
        try {

            //1.获取构造器
            constructor = plugin.getConstructor(String.class,String.class,String.class);
            o = constructor.newInstance(link,type,args);

            //获取最新
            Method methodLatestItem = plugin.getMethod("latestItem");
            String latestItemStr = (String)methodLatestItem.invoke(o);
            Gson gson = new Gson();
            item = gson.fromJson(latestItemStr, Item.class);

            //获取channel
            Method methodChannel = plugin.getMethod("channel");
            String channelStr = (String)methodChannel.invoke(o);
            channel = gson.fromJson(channelStr, Channel.class);


        } catch (NoSuchMethodException e) {
            LOGGER.error(e.toString());
        } catch (InstantiationException e) {
            LOGGER.error(e.toString());
        } catch (IllegalAccessException e) {
            LOGGER.error(e.toString());
        } catch (InvocationTargetException e) {
           LOGGER.error(e.getTargetException().toString());
        }

        //4.比对count是否需要更新
        Integer latestCount = null;
        String lastesEqual = null;
        Object[] equalOrCount = equalOrCount(item);

        if (equalOrCount[0].equals("getCount")){
            latestCount = (int)equalOrCount[1];
        }else if (equalOrCount[0].equals("getEqual")){
            lastesEqual = (String) equalOrCount[1];
        }

        /**
         *  1.读取配置文件plugin.properties中的downloader值
         *  2.调用对应的下载器进行下载，返回uuid
         *  3.将item内容写入xml文件
         * */

        //()
        if (((latestCount!=null) && latestCount>count) || (lastesEqual!=null && (!lastesEqual.equals(equal)))){

            //更新status
            int getChannelStatus = channel.getStatus();
            channelService.UpdateForStatus(getChannelStatus,uuid);

            //有更新内容,获取最新视频链接
            String getItemEnclosure = item.getEnclosure();
            LOGGER.debug("getItemEnclosure"+getItemEnclosure);

            //生成资源的UUID
            String resourceUuid = UUID.randomUUID().toString();

            //获取IP地址
            String IP = service.getIP();

            //读取下载器类
            String downloader = (String)pluginProperties.get("downloader");
            downloader = "com.podcast.Utils."+downloader;
            LOGGER.info("downloader:"+downloader);
            Class downLoderClass = null;
            try {
                downLoderClass = Class.forName(downloader);
            } catch (ClassNotFoundException e) {
                LOGGER.error(e.toString());
            }


            //获取插件mode
            String audioMode = (String)pluginProperties.get("audioMode");
            String videoMode = (String) pluginProperties.get("videoMode");
            Mode mode = new Mode(getItemEnclosure,webappPath,downLoderClass,resourceUuid,type,IP);

            LOGGER.info("type:"+type);
            LOGGER.debug("audio:"+audioMode);
            LOGGER.debug("video:"+videoMode);


            //最终的资源链接
            String enclosureLink = "";


            if ("video".equals(type)){
                //视频
                switch (videoMode){
                    case "V1":
                        enclosureLink = mode.V1();
                        LOGGER.info("转换后的链接："+enclosureLink);
                        writeItem(item,type,enclosureLink,xmlPath);
                        //将资源uuid添加到数据库
                        channelService.addResource(xmlUuid,resourceUuid);
                        channelService.UpdateForEqual(item.getEqual(),uuid);
                        return true;
                    case "V2" :
                        enclosureLink = mode.V2();
                        writeItem(item,type,enclosureLink,xmlPath);
                        channelService.UpdateForEqual(item.getEqual(),uuid);
                        return true;
                    case "customize" :
                        enclosureLink = mode.customize();
                        writeItem(item,type,enclosureLink,xmlPath);
                        //将资源uuid添加到数据库
                        channelService.addResource(xmlUuid,resourceUuid);
                        channelService.UpdateForEqual(item.getEqual(),uuid);
                        break;
                    default:
                        return false;
                }
            }else {
                //音频
                switch (audioMode){
                    case "A1":
                        enclosureLink = mode.A1();
                        writeItem(item,type,enclosureLink,xmlPath);
                        //将资源uuid添加到数据库
                        channelService.addResource(xmlUuid,resourceUuid);
                        channelService.UpdateForEqual(item.getEqual(),uuid);
                        return true;
                    case "A2" :
                        enclosureLink = mode.A2();
                        writeItem(item,type,enclosureLink,xmlPath);
                        channelService.UpdateForEqual(item.getEqual(),uuid);
                        return true;
                    case "customize" :
                        enclosureLink = mode.customize();
                        writeItem(item,type,enclosureLink,xmlPath);
                        //将资源uuid添加到数据库
                        channelService.addResource(xmlUuid,resourceUuid);
                        channelService.UpdateForEqual(item.getEqual(),uuid);
                        break;
                    default:
                        return false;
                }
            }
        }
        return false;
    }

    /**
     * 新版1.2.0
     * @param item_
     * @param type
     * @param enclosureLink
     * @param xmlPath
     */
    public static void writeItem(Item item_,String type,String enclosureLink,String xmlPath) throws Exception {
        LOGGER.info("开始写入item");

        //写入item
        StringBuffer item = new StringBuffer();
        item.append("\t<update>update</update>\n");
        item.append("\t<item>\n");
        long time = item_.getCreateTime();
        item.append("\t\t<pubDate>").append(TimeFormat.change(String.valueOf(time))).append("</pubDate>\n");
        item.append("\t\t<title><![CDATA[ ").append(item_.getTitle()).append(" ]]></title>\n");
        item.append("\t\t<link><![CDATA[ ").append(item_.getLink()).append(" ]]></link>\n");
        if ("video".equals(type)){
            item.append("\t\t<enclosure url=\"").append(enclosureLink).append("\" type=\"video/mp4\" />\n");
        }else {
            item.append("\t\t<enclosure url=\"").append(enclosureLink).append("\" type=\"audio/x-m4a\"/>\n");
        }
        //需要一个时长的时间格式
        item.append("\t\t<itunes:duration>").append(TimeFormat.duration(item_.getDuration())).append("</itunes:duration>\n");
        item.append("\t\t<description><![CDATA[ ").append(item_.getDescription()).append(" ]]></description>\n");
        item.append("\t\t<itunes:image href=\"").append(item_.getImage()).append("\"/>\n");
        item.append("\t</item>\n");

        //把null替换成item
        String itemStr = item.toString();
        replaceUpdate(itemStr,xmlPath);
        //更新count
        //updateCount(latestCount,xmlPath);
        updateCountOrEqual(item_,xmlPath);
        LOGGER.info("写入item完成");

    }

    /*
     *
     * 把xml里的null替换成新的item
     * @param newItem  新的item内容
     * @param FilePath TXT和xml文件的路径
     */
    private static void replaceUpdate(String newItem, String filePath) {

        try(BufferedReader br = new BufferedReader(new FileReader(filePath));) {

            ArrayList<String> strings = new ArrayList<String>();
            String s;//读取的每一行数据
            while ((s=br.readLine()) != null){
                if (s.contains("<update>update</update>")) {
                    s = s.replace("<update>update</update>",newItem);
                }
                strings.add(s);//将数据存入集合
            }
            BufferedWriter bw = null;
            FileOutputStream writerStream = new FileOutputStream(filePath);
            bw = new BufferedWriter(new OutputStreamWriter(writerStream, "UTF-8"));

            for (String string : strings) {
                bw.write(string);//一行一行写入数据
                bw.newLine();//换行
            }
            bw.close();
            writerStream.close();

        }catch (Exception e){

        }
    }

    /**
     * 把比对标签<totalCount>和<equal>更新
     * @param FilePath xml文件的路径*/
    private static void updateCountOrEqual(Item item,String FilePath) throws Exception {

     /* //
        Object[] equalOrCount = equalOrCount(item);

        String tag = null;
        if (equalOrCount[0].equals("getCount")){
            tag = "totalCount";
            LOGGER.info("更新count");
        }else {
            tag = "equal";
            LOGGER.info("更新equal");
        }*/

        String tag = "equal";

        try(BufferedReader br = new BufferedReader(new FileReader(FilePath));) {

            ArrayList<String> strings = new ArrayList<String>();
            String s;//读取的每一行数据
            while ((s=br.readLine()) != null){
                if (s.contains("<"+ tag +">")) {
                    s = "\t\t<"+tag+">"+ item.getEqual() +"</"+tag+">";
                }
                strings.add(s);//将数据存入集合
            }
            BufferedWriter bw = null;
            FileOutputStream writerStream = new FileOutputStream(FilePath);
            bw = new BufferedWriter(new OutputStreamWriter(writerStream, "UTF-8"));

            for (String string : strings) {
                bw.write(string);//一行一行写入数据
                bw.newLine();//换行
            }
            bw.close();
            writerStream.close();


        }catch (Exception e){

        }
    }

    /**
     * 把xml里的null替换成新的item
     * @param FilePath TXT和xml文件的路径*/


    private static void updateCount(int lastCount,String FilePath) {

        try(BufferedReader br = new BufferedReader(new FileReader(FilePath));) {

            ArrayList<String> strings = new ArrayList<String>();
            String s;//读取的每一行数据
            while ((s=br.readLine()) != null){
                if (s.contains("<totalCount>")) {
                    s = "\t\t<totalCount>"+ lastCount +"</totalCount>";
                }
                strings.add(s);//将数据存入集合
            }
            BufferedWriter bw = null;
            FileOutputStream writerStream = new FileOutputStream(FilePath);
            bw = new BufferedWriter(new OutputStreamWriter(writerStream, "UTF-8"));

            for (String string : strings) {
                bw.write(string);//一行一行写入数据
                bw.newLine();//换行
            }
            bw.close();
            writerStream.close();


        }catch (Exception e){

        }
    }

    private static Class getChannelPlugin(String uuid,String webappPath) throws Exception {
        //1.需要webappPath
        //2.根据uuid获取xml文件中的插件名称
        String xmlPath = webappPath+File.separator+"xml"+File.separator+uuid+".xml";
        String pluginName = null;
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new File(xmlPath));

            //获取根元素
            Element rootElement = document.getRootElement();
            Element channel = rootElement.element("channel");
            pluginName = channel.element("plugin").getText();
        } catch (DocumentException e) {
            LOGGER.error("根据uuid获取xml文件中的插件名称时出错！"+"详细:"+e);
        }

        //根据插件名称去匹配url中有没有包含，在进入这里之前，先判断url是否可通
        Map<String, Class> plugins = scanerPlugin(webappPath);

        try {
            Class pluginClass = plugins.get(pluginName);
            return pluginClass;
        } catch (Exception e) {
            LOGGER.error("找不到插件");
            return null;
        }
    }

    private static Map<String,Class> scanerPlugin(String webappPath) throws Exception {
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
     * @throws IOException*/


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
     * @throws MalformedURLException */


    private static final ClassLoader getClassLoader(String jarFilePath) throws MalformedURLException {
        File jarFile = new File(jarFilePath);
        if (!jarFile.exists()) {
            return null;
        }
        URL url = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url}, null);
        return classLoader;
    }


    /**
     * 判断使用哪种方式更新
     * @param item
     * @return
     * @throws Exception
     */
    public static Object[] equalOrCount(Item item) throws Exception {
        Class<? extends Item> aClass = item.getClass();
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals("getEqual") && method.invoke(item)!=null) {
                return new Object[]{"getEqual",method.invoke(item)};
            }
        }
        return new Object[]{"getCount",aClass.getMethod("getCount").invoke(item)};
    }
}
