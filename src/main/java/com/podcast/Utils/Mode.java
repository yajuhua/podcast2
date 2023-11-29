package com.podcast.Utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.podcast.pojo.Download;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public Mode() {
    }

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
            boolean result = (boolean)startDownload.invoke(o1);
            if (result == false){
                return null;//下载失败
            }
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

        if (fileabsolutePath == null){
            return null;//下载失败
        }

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

        if (fileabsolutePath == null){
            return null;//下载失败
        }

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

    /**
     * 自定义模式
     * @return 返回资源URL
     */
    public  String customize() throws Exception {

        //资源存放位置
        String filePath = this.webappPath+ File.separator+this.type+File.separator;

        //集合类型
        Type listType = new TypeToken<List<String[]>>() {}.getType();

        //将json字符串转换成对象
        Gson gson = new Gson();
        List<String[]> customCmd = gson.fromJson(this.Enclosure, listType);

        Download download = null; //yt-dlp下载信息
        String toolName; //工具名称
        String command; //执行命令
        String result = null; //资源绝对路径
        String format = null; //格式
        List<String> results = new ArrayList<>();//存放返回资源绝对路径

        for (int i = 0; i < customCmd.size(); i++) {
            toolName = customCmd.get(i)[0];
            command = customCmd.get(i)[1].replace("${path}",filePath).replace("${rename}",uuid);
            result = customCmd.get(i)[2].replace("${path}",filePath).replace("${rename}",uuid);
            format = result.substring(result.indexOf("."));
            results.add(result);//收集返回资源绝对路径
            switch (toolName){
                case "yt-dlp":
                    LOGGER.info("yt-dlp执行:"+command);
                    Yt_dlp ytDlp = new Yt_dlp();
                    download = ytDlp.ytDlpCmd(command);
                    if (download.getStatus()!=0){
                        return null;//下载失败
                    }
                    format = download.getDescription().substring(download.getDescription().lastIndexOf("."));
                    LOGGER.info("yt-dlpResult:"+result);
                    break;
                case "N_m3u8DL-RE":
                    LOGGER.info("N_m3u8DL-RE执行:"+command);
                    N_m3u8DL_RE n_m3u8DL_re = new N_m3u8DL_RE();
                    Download download1 = n_m3u8DL_re.nM3u8DLRECmd(command);
                    if (download1.getStatus()!=0){
                        return null;//下载失败
                    }
                    LOGGER.info("N_m3u8DL-REResult:"+result);
                    break;
                case "ffmpeg":
                    LOGGER.info("ffmpeg执行:"+command);
                    N_m3u8DL_RE.Cmd(command);
                    LOGGER.info("ffmpegResult:"+result);
                    break;
                case "cmd":
                    LOGGER.info("执行cmd命令:"+command);
                    N_m3u8DL_RE.Cmd(command);
                    LOGGER.info("cmdResult:"+result);
            }
        }

        //除了最后一个资源
        String lastResult  = results.get(results.size() - 1);
        for (int i = 0; i < results.size(); i++) {
            if (!results.get(i).contains(lastResult)){
                //删除资源,除了最后一个资源
                if (FileUtils.deleteQuietly(new File(results.get(i)))){
                    LOGGER.info("删除成功："+results.get(i));
                }
            }
        }

        //拼接URL
        String url = this.IP+"/"+this.type+"/"+this.uuid+format;
        //返回URL
        return url;
    }

    @Test
    public void t3() throws Exception {
        String IP = "http://podcast2.lighnting.cyou:8088/podcast2";
        String uuid = UUID.randomUUID().toString();
        String[] cmd1 = {"yt-dlp","yt-dlp -f mp4 --path ${path}  --output ${rename}.mp4 https://www.youtube.com/watch?v=YAXTn0E-Zgo&ab_channel=Tranquility","${path}${rename}.mp4"};
        String[] cmd2 = {"ffmpeg","ffmpeg -i ${path}${rename}.mp4 -vn -c:a copy ${path}${rename}.m4a","${path}${rename}.m4a"};
        //String[] cmd3 = {"cmd","del ${path}${rename}.mp4","${path}${rename}.m4a"};
        List<String[]> cmds = new ArrayList<>();
        cmds.add(cmd1);
        cmds.add(cmd2);
        Mode mode = new Mode(new Gson().toJson(cmds),"E:\\webapps",null,uuid,"audio",IP);
        String customize = mode.customize();
        System.out.println(customize);
    }
}
