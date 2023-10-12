package com.podcast.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 音视频模式
 */
public class Mode {
    private static final Logger LOGGER = LoggerFactory.getLogger("Mode");
    /**
     * 资源地址
     */
    private String Enclosure;
    /**
     * web服务器资源的根目录
     */
    private String webappPath;
    /**
     * 下载器
     */
    private Class downloader;
    /**
     * 资源的UUID
     */
    private String uuid;
    /**
     * 资源类型
     */
    private String type;
    /**
     * 服务器IP地址
     */
    private String IP;

    /**
     * 参数：资源链接、保存位置：webapp 、 下载器 、 uuid 、type
     */
    public Mode(String enclosure, String webappPath, Class downloader, String uuid, String type, String IP) {
        Enclosure = enclosure;
        this.webappPath = webappPath;
        this.downloader = downloader;
        this.uuid = uuid;
        this.type = type;
        this.IP = IP;
    }



    /**
     * 下载
     * @return 保存位置
     */
    private String startDownload(){
        try {
            Class downLoderClass = this.downloader;

            //获取构造器
            Constructor downLoderConstructor = downLoderClass.getConstructor(String.class, String.class, String.class);

            //资源保存位置
            String enclosureSavePath = this.webappPath+this.type+File.separator;
            //aria2 目前只支持斜杠，不支持反斜杠
            enclosureSavePath = enclosureSavePath.replaceAll("\\\\","/");
            LOGGER.debug("enclosureSavePath:" + enclosureSavePath);
            Object o1 = downLoderConstructor.newInstance(this.Enclosure, this.uuid, enclosureSavePath);

            //获取startDownload方法
            Method startDownload = downLoderClass.getMethod("startDownload");
            Method getDownloadCmd = downLoderClass.getMethod("getDownloadCmd");

            //debug
            LOGGER.debug("cmd:"+getDownloadCmd.invoke(o1));
            LOGGER.debug("enclosureSavePath:"+enclosureSavePath);

            //执行startDownload方法
            startDownload.invoke(o1);
        } catch (Exception e) {
            LOGGER.error("开始下载时出错！"+" 详细:"+e);
        }

        return getFileName();
    }

    /**
     *  0.getFileName
     * 提供UUID获取文件名和格式的字符串，用于格式转换和删除
     * 参数：this.UUID
     * @return 文件的绝对路径
     */
    private String getFileName(){
        String filePath = this.webappPath+ File.separator+this.type+File.separator;
        File file = new File(filePath);
        File[] files = file.listFiles();
        for (File file1 : files) {
           boolean flag =  file1.getName().contains(this.uuid);
           if (flag){
              return file1.getAbsolutePath();
           }
        }
        return null;
    }

    /**
     * 下载后进行格式转换
     * @return 返回web服务器资源路径的URL
     */
    public String A1(){
        //1.下载
        String fileabsolutePath = startDownload();
        //判断是否需要转换，否则会误删
        int index = fileabsolutePath.lastIndexOf(".");
        String format = fileabsolutePath.substring(index+1);

        //判断格式
        if (!"m4a".equals(format)){
            //不是m4a格式的
            //2.格式转换
            FFmpeg fFmpeg = new FFmpeg(fileabsolutePath);
            fFmpeg.convertToM4A();
        }
        //3.返回资源URL
        String url = this.IP+"/"+this.type+"/"+this.uuid+".m4a";
        //debug
        LOGGER.debug("资源url(A1):"+url);
        return url;
    }

    /**
     * 直接提供原链接
     * @return 返回原链接
     */
    public String A2(){
        //debug
        LOGGER.debug("资源url(A2):"+this.Enclosure);
        return this.Enclosure;
    }

    /**
     * 下载后进行格式转换
     * @return 返回web服务器资源路径的URL
     */
    public String V1(){
        //1.下载
        String fileabsolutePath = startDownload();

        //判断是否需要转换，否则会误删
        int index = fileabsolutePath.lastIndexOf(".");
        String format = fileabsolutePath.substring(index+1);

        //判断格式
        if (!"mp4".equals(format)){
            //不是m4a格式的
            //2.格式转换
            FFmpeg fFmpeg = new FFmpeg(fileabsolutePath);
            fFmpeg.convertToMP4();
        }
        //3.返回资源URL
        String url = this.IP+"/"+this.type+"/"+this.uuid+".mp4";
        //debug
        LOGGER.debug("资源url(V1):"+url);
        return url;
    }


    /**
     *  直接提供原链接
     * @return 返回原链接
     */
    public String V2(){
        //debug
        LOGGER.debug("资源url(V2):"+this.Enclosure);
        return this.Enclosure;
    }
}
