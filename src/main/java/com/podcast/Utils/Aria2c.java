package com.podcast.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Aria2c {
    private static final Logger LOGGER = LoggerFactory.getLogger("Aria2c");
    /**
     * 1.重命名
     * 2.格式选择
     * 3.保存路径
     * 4.下载范围
     */

    private String rename;
    private String paths;

    private String url;

    public Aria2c() {
    }


    public Aria2c(String url, String rename, String paths) {
        this.rename = rename+".format";
        this.paths = paths;
        this.url = url;
    }

    /**
     * 获取拼接后的命令
     * @return
     */
    public String getDownloadCmd(){
        return null;
    }
    /**
     *
     * @return 是否正确下载,错误返回false，否则返回true
     * @throws IOException
     */
    public  boolean startDownload() throws IOException {
        Aria2Client aria2Client = null;
        boolean status = false;
        //读取Properties文件中的配置信息
        // aria2_RPO_URL
        // aira2_RPO_secret
        Properties mainProperties = new Properties();
        try {
            // 使用ClassLoader加载Properties文件
            InputStream inputStream = Aria2c.class.getClassLoader().getResourceAsStream("conf.properties");
            mainProperties.load(inputStream);
            inputStream.close();

            //相关配置
            String aria2_RPO_URL = mainProperties.getProperty("aria2_RPO_URL");
            Object aira2_rpo_secret = mainProperties.get("aira2_RPO_secret");

            //下载选择
            Map options = new HashMap<>();
            options.put("out",rename);
            options.put("dir",paths);
            aria2Client = new Aria2Client(aria2_RPO_URL);
            String response = aria2Client.addUri(aira2_rpo_secret, Arrays.asList(url),options);
            System.out.println(response);

            String[] keys = {"status"};
            while (!aria2Client.tellStatus(keys).contains("error")){
                if (aria2Client.tellStatus(keys).contains("complete")){
                    return true;
                }
            }

        } catch (Exception e) {
            LOGGER.error("使用startDownload时出错！"+"详细:"+e);
        }
       return status;
    }

    @Test
    public void t1() throws Exception {
        String url = "https://video.31dm.com/sda1/pikpak%20x/补1-7v2/网络胜利组/网络胜利组%2011.mp4?sign=6fb575879ada5a0f3245bf7b89e16a97&t=1696348112";
        String name = "t200";
        String path = "D:/Download/Aria2/t1";
        Aria2c aria2c = new Aria2c(url,name,"/opt/");
        aria2c.startDownload();
    }

}
