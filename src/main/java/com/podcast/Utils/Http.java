package com.podcast.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class Http {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("Http");
    /**
     * 获取网页内容
     * @param url 访问的URL地址
     * @return 返回GET请求响应数据
     */
    public static String getHttpContent(String url){
        return WebContentP(url);
    }

    /**
     * 代理版获取网页内容(GET请求)
     * @param URL 访问的URL地址
     * 配置proxyHost和proxyPort即可
     * @return 返回GET请求响应数据
     * @throws Exception
     */
    public static String WebContentP(String URL){
        String Content = null;
        try {
            String proxyHost = "127.0.0.1";
            int proxyPort = 1080; // 替换为你的代理端口

            //到服务器的时候直接注释掉即可关闭代理
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));//到服务器的时候直接注释掉
            java.net.URL url = new URL(URL);

            HttpURLConnection connection = (HttpURLConnection)url.openConnection(proxy);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            Content = "";
            while ((line = reader.readLine()) != null) {
                Content = Content + line;
            }
            reader.close();
        } catch (IOException e) {
            LOGGER.error("获取网页内容时出错！"+" 详细:"+e);
        }
        return  Content;
    }

}
