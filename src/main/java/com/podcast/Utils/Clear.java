package com.podcast.Utils;

import com.podcast.service.ChannelService;
import com.podcast.service.PodcastUserService;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 清除类
 */
public class Clear {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("Clear");
    public static ChannelService channelService = new ChannelService();
    public static PodcastUserService podcastUserService = new PodcastUserService();

    /**
     * 清除xml文件中过期的item和相关资源
     * @param uuid xml文件的UUID
     */
    public static void clearPastDue(String uuid) {

        //获取xml文件绝对路径
        PodcastUserService service = new PodcastUserService();
        ChannelService channelService = new ChannelService();
        Long survivalTime = channelService.getChannelSurvivalTime(uuid);
        String webappPath = service.getWebappPath();
        String xmlPath = webappPath+ "xml"+ File.separator+uuid+".xml";

        //debug
        LOGGER.debug("survivalTime:"+survivalTime);
        LOGGER.debug("传入的uuid:"+uuid);
        LOGGER.debug("xmlPath:"+xmlPath);


        Document document = null;
        try {
            SAXReader reader = new SAXReader();
            document = reader.read(new File(xmlPath));
        } catch (DocumentException e) {
            LOGGER.error("读取xml文件时出错！"+" 详细:"+e);
        }
        Element rootElement = document.getRootElement();
        Element channel = rootElement.element("channel");
        List<Element> items = channel.elements("item");

        //获取首个item过期的时间字符串
        Integer number = null;//用于获取问下item中的uuid
        String pastItemPubDate = null;
        for (int i = 0; i < items.size(); i++) {
            String pubDate = items.get(i).element("pubDate").getText();
            long lPubDate = TimeFormat.parseTimeString(pubDate);
            if (((System.currentTimeMillis()/1000)-lPubDate)>survivalTime){
                LOGGER.info("有过期内容:"+uuid);
                pastItemPubDate = pubDate;
                number = i;
                break;
            }
        }

        //获取过期item中的url中的uuid
        Map<String,String> uuidAndType = new HashMap<>();
        List<String> uuids = new ArrayList<>();
        //如果为null说明没有过期item
        if (number!=null){
            LOGGER.debug("number!=null");
            for (int i = number; i < items.size(); i++) {
                String url = items.get(i).element("enclosure").attributeValue("url");
                String type = items.get(i).element("enclosure").attributeValue("type").equals("audio/x-m4a")?"audio":"video";
                int index = url.lastIndexOf(".");
                int index1 = url.lastIndexOf("/");
                String urlUuid = url.substring(index1+1,index);
                uuids.add(urlUuid);
                uuidAndType.put(urlUuid,type);
            }
        }

        //删除资源
        for (String uuid_ : uuids) {
            LOGGER.debug("开始删除资源");
            String resourcePath = webappPath+ uuidAndType.get(uuid_)+File.separator+uuid_+((uuidAndType.get(uuid_).equals("audio"))?".m4a":".mp4");
            if (FileUtils.deleteQuietly(new File(resourcePath))){
                LOGGER.info("删除成功:" + resourcePath);
                //删除数据库信息
                channelService.deleteByResourceUuid(uuid_);
            }else {
                LOGGER.error("删除失败:" + resourcePath);
            }
        }

        //如果找到过期字符串，则进行清除
        if (pastItemPubDate!=null){

            LOGGER.debug("如果找到过期字符串，则进行清除");

            //记录行数
            int line = 1;
            try(BufferedReader br = new BufferedReader(new FileReader(xmlPath));) {

                ArrayList<String> strings = new ArrayList<String>();

                String s;//读取的每一行数据
                while ((s=br.readLine()) != null){
                    if (s.contains(pastItemPubDate)) {
                        line = strings.size();
                    }
                    strings.add(s);//将数据存入集合
                }

                BufferedWriter bw = null;
                FileOutputStream writerStream = new FileOutputStream(xmlPath);
                bw = new BufferedWriter(new OutputStreamWriter(writerStream, "UTF-8"));

                for (int i = 0; i < line-1; i++) {
                    bw.write(strings.get(i));//一行一行写入数据
                    bw.newLine();//换行
                }

                bw.write("\t</channel>\n");
                bw.write("</rss>");
                bw.close();
                writerStream.close();

            }catch (Exception e){
                LOGGER.error("清除过期item时出错！"+" 详细:"+e);
            }
        }
    }

    /**
     * 删除没有完成下载的
     */
    public static void clearNotCompleted(){
        //获取UUID集合
        List<String> allResourceUUID = channelService.getAllResourceUUID();
        //资源位置
        String webappPath = podcastUserService.getWebappPath();
        //获取video和audio目录下的File[]对象
        List<File> videoFiles = Arrays.stream(new File(webappPath + "video").listFiles()).toList();
        List<File> audioFiles = Arrays.stream(new File(webappPath + "audio").listFiles()).toList();
        //全部File
        List<File> allResourceFile = new ArrayList<>();
        allResourceFile.addAll(videoFiles);
        allResourceFile.addAll(audioFiles);

        //将名称包含allResourceUUID的过滤掉
         allResourceFile = allResourceFile.stream().filter(f -> !allResourceUUID.contains(f.getName().substring(0,f.getName().lastIndexOf(".")))).collect(Collectors.toList());

        //删除
        for (File file : allResourceFile) {
            boolean b = FileUtils.deleteQuietly(file);
            LOGGER.info("文件："+file.getName()+(b==true?"删除成功":"删除失败"));
        }
    }

    /**
     * 删除channel表中没有的resource记录
     */
    public static void clearNotSubscribeResourceData(){
        //获取资源uuid
        List<String> channelAllUuid = channelService.getAllUuid();
        List<String> resourcesAllXmlUuid = channelService.getResourcesAllXmlUuid();

        //将resourcesAllXmlUuid存在的订阅uuid过滤掉，剩下就是要删除的订阅资源记录
        resourcesAllXmlUuid = resourcesAllXmlUuid.stream().filter(u -> !channelAllUuid.contains(u)).collect(Collectors.toList());

        //删除资源记录
        for (String uuid : resourcesAllXmlUuid) {
            channelService.deleteResourceByXmlUUID(uuid);
        }
    }

    @Test
    public void t1(){
        clearNotCompleted();
    }
}
