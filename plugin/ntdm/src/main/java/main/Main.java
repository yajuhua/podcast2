package main;


import Utils.*;
import com.google.gson.Gson;
import io.github.yajuhua.download.commons.Operation;
import io.github.yajuhua.download.commons.Type;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.podcast2API.extension.build.ExtendList;
import io.github.yajuhua.podcast2API.extension.build.Input;
import io.github.yajuhua.podcast2API.extension.build.Select;
import io.github.yajuhua.podcast2API.setting.Setting;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import io.github.yajuhua.podcast2API.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class Main implements Podcast2 {
    private static final Logger LOGGER = LoggerFactory.getLogger("Main");
    public static String DOMAIN_NAME = "https://www.ntdm.tv";//网站域名
    private List<Integer> episode;
    private Document doc;
    private Gson gson = new Gson();
    private Elements playList;
    private Params params;

    public Main() {
    }

    public Main(String paramsStr) {

        if (paramsStr != null) {
            this.params = gson.fromJson(paramsStr, Params.class);
            if (params.getEpisodes() != null){
                if (params.getEpisodes().size() != 0){
                    Collections.sort(params.getEpisodes());
                    episode = params.getEpisodes();
                }
            }

            if (params.getUrl() != null) {
                try {
                    doc = Jsoup.connect(params.getUrl()).get();

                    //获取视频列表
                    String xpath = "//*[@id=\"main0\"]/div[1]/ul/li/a";
                    playList = doc.selectXpath(xpath);
                } catch (IOException e) {
                    LOGGER.error("Jsoup获取Document时出错了,"+e.toString());
                }
            }
        }

    }
    @Override
    public List<Item> items() {

       List<Item> items = new ArrayList<>();

        if (episode.get(0) < 0){
            //下载最近30集
            for (int i = 0; i < playList.size(); i++) {
                //获取信息
                String link = DOMAIN_NAME + playList.get(i).attr("href");
                String title = playList.get(i).attr("title");

                //获取视频源地址供下载
                String sourceAddress = null;
                try {
                    sourceAddress = ParseYhdm.parseSourceAddress(link);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                //封装信息
                Item item = new Item();
                item.setEnclosure(sourceAddress);
                item.setTitle(title);
                item.setLink(link);
                item.setDuration(FFmpeg.getDuration(sourceAddress));
                item.setImage(doc.selectXpath("//*[@id=\"container\"]/div[3]/div[1]/div/img").get(0).attr("src"));
                item.setDescription(doc.getElementsByClass("detail_imform_desc_pre").text());
                item.setEqual((i+1)+"");
                item.setCreateTime(System.currentTimeMillis());

                //添加到集合中
                items.add(item);

            }

        }else if (episode.get(0) == 0){
            //下载最新一集
            //获取信息
            String link = DOMAIN_NAME + playList.get(playList.size()-1).attr("href");
            String title = playList.get(playList.size()-1).attr("title");

            //获取视频源地址供下载
            String sourceAddress = null;
            try {
                sourceAddress = ParseYhdm.parseSourceAddress(link);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //封装信息
            Item item = new Item();
            item.setEnclosure(sourceAddress);
            item.setTitle(title);
            item.setDuration(FFmpeg.getDuration(sourceAddress));
            item.setImage(doc.selectXpath("//*[@id=\"container\"]/div[3]/div[1]/div/img").get(0).attr("src"));
            item.setDescription(doc.getElementsByClass("detail_imform_desc_pre").text());
            item.setEqual(playList.size()+"");
            item.setCreateTime(System.currentTimeMillis());
            item.setLink(link);

            //添加到集合中
            items.add(item);


        }else if (episode.get(0) > 0){
            //选择下载
            for (int i = 0; i < episode.size(); i++) {
                //获取信息
                String link = DOMAIN_NAME + playList.get(episode.get(i)-1).attr("href");
                String title = playList.get(episode.get(i)-1).attr("title");

                //获取视频源地址供下载
                String sourceAddress = null;
                try {
                    sourceAddress = ParseYhdm.parseSourceAddress(link);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                //封装信息
                Item item = new Item();
                item.setEnclosure(sourceAddress);
                item.setTitle(title);
                item.setDuration(FFmpeg.getDuration(sourceAddress));
                item.setImage(doc.selectXpath("//*[@id=\"container\"]/div[3]/div[1]/div/img").get(0).attr("src"));
                item.setDescription(doc.getElementsByClass("detail_imform_desc_pre").text());
                item.setLink(link);
                item.setEqual((episode.get(i)-1)+"");
                item.setCreateTime(System.currentTimeMillis());

                //添加到集合中
                items.add(item);

            }
        }

        Map args = new HashMap();
        for (Item item : items) {
            io.github.yajuhua.podcast2API.Type type = params.getType();
            Request request = new Request();
            request.setArgs(args);
            request.setDownloader(DownloadManager.Downloader.Aria2);
            request.setType(Type.valueOf(type.name()));//都转换成视频类型的
            request.setOperation(Operation.Single);
            List<String> links = new ArrayList<>();
            links.add(item.getEnclosure());
            request.setLinks(links);
            item.setRequest(request);
        }

        //返回所选节目集合
        return items;
    }

    @Override
    public Channel channel() {
        //获取信息
        String title = doc.selectXpath("//*[@id=\"container\"]/div[4]/div[1]/div/h4").get(0).text();
        String link = params.getUrl();
        Integer status =   doc.selectXpath("//*[@id=\"container\"]/div[3]/div[2]/div/div/ul/li[1]").get(0).getElementsByClass("detail_imform_value").get(0).text().equals("完结") ? 0 : 1;
        String image = doc.selectXpath("//*[@id=\"container\"]/div[3]/div[1]/div/img").get(0).attr("src");
        String description = doc.getElementsByClass("detail_imform_desc_pre").text();
        String category = null;
        String author = title;
        //封装信息
        Channel channel = new Channel();
        channel.setTitle(title);
        channel.setLink(link);
        channel.setStatus(status);
        channel.setImage(image);
        channel.setDescription(description);
        channel.setCategory(category);
        channel.setAuthor(author);

        return channel;
    }

    @Override
    public Item latestItem() {
        //下载最新一集
        //获取信息
        String link = DOMAIN_NAME + playList.get(playList.size()-1).attr("href");
        String title = playList.get(playList.size()-1).attr("title");

        //获取视频源地址供下载
        String sourceAddress = null;
        try {
            sourceAddress = ParseYhdm.parseSourceAddress(link);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //封装信息
        Item item = new Item();
        item.setEnclosure(sourceAddress);
        item.setTitle(title);
        item.setDuration(FFmpeg.getDuration(sourceAddress));
        item.setImage(doc.selectXpath("//*[@id=\"container\"]/div[3]/div[1]/div/img").get(0).attr("src"));
        item.setDescription(doc.getElementsByClass("detail_imform_desc_pre").text());
        item.setEqual(playList.size()+"");
        item.setCreateTime(System.currentTimeMillis());
        item.setLink(link);

        Map args = new HashMap();
        List<String> links = new ArrayList<>();
        links.add(item.getEnclosure());
        Request request = new Request();
        request.setLinks(links);
        request.setArgs(args);
        request.setType(Type.valueOf(params.getType().name()));
        request.setDownloader(DownloadManager.Downloader.Aria2);
        request.setOperation(Operation.Single);
        item.setRequest(request);
        return item;
    }

    @Override
    public ExtendList getExtensions() throws Exception {
        ExtendList extendList = new ExtendList();
        List<Select> selectList = new ArrayList<>();
        List<Input> inputList  = new ArrayList<>();
        extendList.setSelectList(selectList);
        extendList.setInputList(inputList);
        return extendList;
    }

    @Override
    public Map getInfo() throws Exception {
        Map map = new HashMap();
        map.put("名称","ntdm");
        map.put("示例","https://www.ntdm9.com/video/5775.html");
        return map;
    }

    @Override
    public List<Setting> settings() throws Exception {
        List<Setting> settings = new ArrayList<>();
        return settings;
    }
}
