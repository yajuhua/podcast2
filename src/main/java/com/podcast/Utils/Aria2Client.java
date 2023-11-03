package com.podcast.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.text.StringEscapeUtils;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * 通过RPO调用aria2
 */
public class Aria2Client {

    /**
     *id可以自定义，用于程序辨识
     */
    private String id;
    /**
     * Aria2开启RPO接口的URL地址
     */
    private String Aria2_RPO_URL;
    /**
     * GID（或 gid）是管理每次下载的关键。 每次下载都将分配一个唯一的 GID
     */
    private String gid;
    /**
     * RPC 授权机密令牌
     */
    private String secret;

    /**
     * Aria2RPOClient构造器
     * @param Aria2_RPO_URL
     */
    public Aria2Client(String Aria2_RPO_URL) {
        this.Aria2_RPO_URL = Aria2_RPO_URL;
        this.id = UUID.randomUUID().toString();
    }

    /**
     * 将封装好的数据通过POST请求发送到Aria2下载器
     * @param url 要下载的链接地址
     * @param method 调用下载器的方法，如addUrl、getOption。详细看Aria2官方文档：https://uaoao.github.io/docs/aria2-chs-docs/aria2c
     * @param params 下载参数设置
     * @return 返回POST请求的响应结果，也就是Aria2的响应数据
     * @throws Exception
     */
    public  String invoke(String url, String method, Object... params) throws Exception {
        return sendPost(url,create_aria2RPO_Data(method,params));
    }
    /**
     * 生成Aria2  RPO的POST数据
     * @param method 调用下载器的方法，如addUrl、getOption。详细看Aria2官方文档：https://uaoao.github.io/docs/aria2-chs-docs/aria2c
     * @param params 下载参数设置
     * @return 返回拼接好的json字符串
     * @throws Exception
     */
    public String create_aria2RPO_Data(String method,Object... params) throws Exception {
        Gson gson = new Gson();
        String json = gson.toJson(params);
        //Gson 会把字符转成Unicode，可能会导致链接无法下载。下面进行转回来
        json = StringEscapeUtils.unescapeJava(json);

        String data = "{\"jsonrpc\": \"2.0\",\n" +
                "      \"method\": \""+ method +"\",\n" +
                "      \"id\": \""+ id +"\",\n" +
                "      \"params\": "+ json+"}";
        return data;
    }

    /**
     * 发送post请求
     * @param url 要发送POST请求的URL
     * @param data 请求数据
     * @return 返回响应数据
     * @throws Exception
     */
    public static String sendPost(String url,String data) throws Exception {

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");

        // 设置超时时间、是否允许输入输出等属性
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(10000);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        // 设置请求头部信息
        connection.setRequestProperty("Accept"," application/json, text/plain, */*");
        connection.setRequestProperty("Content-Type","application/json;charset=UTF-8");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));

        // 写入 POST 请求的参数
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(data);
        outputStream.flush();
        outputStream.close();

        // 获取服务器返回的数据
        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();
        inputStream.close();

        // 处理服务器返回的数据
        return result.toString();
    }


    /**
     * 将中文资源路径进行URL编码
     * @param url 进行URL编码的URL字符串
     * @return 返回编码的URL字符串
     */
    public static String URLencode(String url){
        //编码后的URL
        String codingURL = null;
        try {
            URI uri = new URI(url);
            String resourcePath = uri.getPath();
            String protocol = uri.getScheme();
            String domainName = uri.getHost();
            String requestParams =  uri.getRawQuery();//请求参数

            //将请求资源路径进行URL中文编码
            resourcePath = URLEncoder.encode(resourcePath,"UTF-8");

            //重新拼接成url
            StringBuffer urlSb = new StringBuffer();
            //编码后的URL进行拼接

            //判断是否有请求参数
            if (requestParams==null){
                codingURL = urlSb.append(protocol).append("://").append(domainName).append(resourcePath).toString();
            }
            //一般都请求参数的
            codingURL = urlSb.append(protocol).append("://").append(domainName).append(resourcePath).append("?").append(requestParams).toString();

            //分隔符不要编码，所有替换回来
            codingURL =  codingURL.replaceAll("%2F","/");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return codingURL;
    }

    /**
     * 此方法添加一个新的下载。
     * 此方法返回新注册下载的 GID。
     * @param params 下载链接和下载参数设置
     * @return
     * @throws Exception
     */
    public String addUri(Object... params) throws Exception {
        Gson gson = new Gson();
        //获取参数中的secret
        String json = gson.toJson(params);
        JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        // secret
        this.secret = jsonArray.get(0).getAsString();

        //执行
        String response = invoke(this.Aria2_RPO_URL,"aria2.addUri",params);

        Gson gson2 = new Gson();
        //获取返回的gid
        JsonObject jsonObject2 = gson2.fromJson(response, JsonObject.class);
        this.gid = jsonObject2.get("result").getAsString();
        return response;
    }

    /**
     * aria2.remove([secret, ]gid)
     * 此方法删除由 gid（字符串）表示的下载。
     * 此方法返回已删除下载的 GID。
     * @return
     */
    public boolean remove() throws Exception {
        return invoke(this.Aria2_RPO_URL,"aria2.remove",this.secret,this.gid).contains(this.gid);
    }

    /**
     * aria2.removeDownloadResult([secret, ]gid)
     * 此方法从内存中删除由 gid 表示的已完成／错误／已删除的下载。
     * 如果成功，此方法返回 OK。
     * @return
     * @throws Exception
     */
    public boolean removeDownloadResult() throws Exception {
        return invoke(this.Aria2_RPO_URL,"aria2.removeDownloadResult",this.secret,this.gid).contains("OK");
    }

    /**
     * aria2.pause([secret, ]gid)
     * 此方法暂停由 gid（字符串）表示的下载。
     * 此方法返回暂停下载的 GID。
     * @return
     */
    public boolean pause() throws Exception {
        return invoke(this.Aria2_RPO_URL,"aria2.pause",this.secret,this.gid).contains(this.gid);
    }


    /**
     * 根据key获取相对应的值
     * @param keys
     * @return 如{"totalLength","completedLength"}
     * @throws Exception
     */
    public String tellStatus(String[] keys) throws Exception {
        return invoke(this.Aria2_RPO_URL,"aria2.tellStatus",this.secret,this.gid,keys);
    }

    @Test
    public void t1() throws Exception {
       /* Aria2Client aria2Client = new Aria2Client("http://192.168.123.169:6800/jsonrpc");
        aria2Client.secret = "token:aria2";
        aria2Client.tellStatus(new String[]{"totalLength", "completedLength"});*/
        String url = "https://v16m-default.akamaized.net/1d78252e8457be5562a0d0d91f4e096a/6544a04c/video/tos/alisg/tos-alisg-v-0000/ogNzQyEMAPHffApsawEF24AeeGAE6ojD2X5LRg/?a=2011&ch=0&cr=0&dr=0&net=5&cd=0%7C0%7C0%7C0&br=974&bt=487&bti=MzhALjBg&cs=0&ds=3&ft=iJOG.y7oZZv0PD1TxS0xg9wz.DKlBEeC~&mime_type=video_mp4&qs=0&rc=MzY6Z2Q7NWQ8NzRkaTVlOEBpajVlaDo6ZnFvZzMzODYzNEAwY19jMDAtXjYxXzEuLTQyYSMzczNkcjRfc3BgLS1kMC1zcw%3D%3D&l=202311030118450B095A82CF0F0356707C&btag=e000a0000";
        Aria2c aria2c = new Aria2c(url,UUID.randomUUID().toString(),"D:\\Download\\Aria2");
        aria2c.startDownload();
    }
}
