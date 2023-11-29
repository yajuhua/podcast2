package com.podcast.Utils;

import com.google.gson.Gson;
import com.podcast.Progress.WebSocketServerDownload;
import com.podcast.Servlet.UserServlet;
import com.podcast.pojo.Download;
import com.podcast.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Yt_dlp {
    /**
     * 解析json
     */
    private static Gson gson = new Gson();
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("Yt_dlp");
    private ChannelService channelService = new ChannelService();
    /**
     * 下载器名称
     */
    private final String TOOLNAME = " yt-dlp ";
    /**
     * 下载格式
     */
    private final String FORMAT = " --format ";
    /**
     * 保存路径
     */
    private final String PATHS = " --paths ";
    /**
     * 一个空格
     */
    private final String SPACE = " ";
    /**
     * 输出设置
     */
    private final String OUTPUT = " --output ";
    /**
     * 下载视频列表开始
     */
    private final String PLAYLIST_START = " --playlist-start ";
    /**
     * 下载视频列表结束
     */
    private final String PLAYLIST_END = " --playlist-end ";

    private String playlistStart;
    private String playlistEnd;
    private String format;

    private String rename;
    private String paths;

    private String url;

    public Yt_dlp() {
    }

    /**
     * 注意：在Linux下 输出路径和URL周围要添加双引号，不然提示语法有误
     * @param url
     */
    public Yt_dlp(String url) {
        this.url = SPACE+url;
    }

    public Yt_dlp(String url, String rename, String paths) {
        this.rename =  OUTPUT+rename+".mp4";
        this.paths = paths +" -f mp4";
        this.url = url;
    }

    public Yt_dlp(String playlistStart, String playlistEnd, String format, String rename, String paths, String url) {
        this.playlistStart = playlistStart;
        this.playlistEnd = playlistEnd;
        this.format = format;
        this.rename = OUTPUT+rename+".%(ext)s";
        this.paths = paths;
        this.url = url;
    }

    /**
     * 下载链接
     * @param url
     */
    public void setUrl(String url) {
        this.url = SPACE+url;
    }

    /**
     * 视频列表第几个开始
     * @param playlistStart
     */
    public void setPlaylistStart(int playlistStart) {
        this.playlistStart = PLAYLIST_START+String.valueOf(playlistStart);
    }

    /**
     * 视频列表第几个结束
     * @param playlistEnd
     */
    public void setPlaylistEnd(int playlistEnd) {
        this.playlistEnd = PLAYLIST_END+String.valueOf(playlistEnd);
    }

    /**
     * 重命名
     * @param rename
     */
    public void setRename(String rename) {
        this.rename = OUTPUT+rename+".%(ext)s";
    }

    /**
     * 选择下载格式
     * @param format
     */
    public void setFormat(String format) {
        this.format = FORMAT+format;
    }

    /**
     * 保存位置
     * @param paths
     */
    public void setPaths(String paths) {
        this.paths = PATHS+paths;
    }

    /**
     * 获取拼接后的命令
     * @return
     */
    public String getDownloadCmd(){
        String[] options = {this.format,this.paths,this.rename,this.playlistStart,this.playlistEnd};
        String[] optionsCmd = {this.FORMAT,this.PATHS,"",this.PLAYLIST_START,this.PLAYLIST_END};
        String info = TOOLNAME;

        for (int i = 0; i < options.length; i++) {
            if (options[i] != null){
                info += optionsCmd[i]+options[i];
            }
        }

        //debug
        LOGGER.debug("getDownloadCmd:"+(info+" "+url).trim());
        return (info+" "+url).trim();
    }
    /**
     * 根据 getDownloadCmd()下载
     * @return 是否正确下载,错误返回false，否则返回true
     * @throws IOException
     */
    public  boolean startDownload() throws IOException {
        return ytDlpCmd(getDownloadCmd()).getStatus()==0;//等于0说明下载成功
    }

    /**
     * 解析yt-dlp日志信息
     * @param command 命令
     */
    public Download ytDlpCmd(String command) throws IOException {
        Download download  = new Download();
        int exitCode = 0;
        try {
            BufferedReader br = null;
            try {
                Process p = Runtime.getRuntime().exec(command);
                exitCode = p.waitFor(); //状态码：0代表成功

                br = new BufferedReader(new InputStreamReader(p.getInputStream(),"UTF-8"));//解决中文乱码 GBK是汉字编码//二维码会乱码
                String line = null;

                //获取下载进度正则表达式
                String percentageReg = "[0-9]{1,3}\\.[0-9]%";

                //获取总MB正则表达式
                String totalSizeReg = "[0-9]{1,3}\\.[0-9][0-9]MiB ";

                //获取当前下载速度正则表达式
                String currentSpeedReg = "[0-9]{1,3}\\.[0-9][0-9][KM]iB/s";

                //获取剩余时间正则表达式
                String ETArex = "[0-9][0-9]:[0-9][0-9](:[0-9][0-9])?";

                //获取描述
                String DestinationReg = "Destination:.+";

                //获取下载进度
                double percentage;
                //获取总MB
                String totalSize;
                //获取当前速度
                String currentSpeed;
                //获取剩余时间
                String ETA;
                String destination;

                //Download download  = new Download();
                download.setId(UUID.randomUUID().toString());
                download.setDownloaderName("yt-dlp");
                download.setTotalSize("");
                download.setCurrentSpeed("");
                download.setETA("");

                while ((line = br.readLine()) != null) {

                    //判断线程是否中断
                    if (Thread.currentThread().isInterrupted()){
                        //结束下载
                        LOGGER.info("该订阅已删除，结束下载");
                        return download;
                    }

                    //描述
                    Pattern DestinationPattern = Pattern.compile(DestinationReg);
                    Matcher DestinationMatcher = DestinationPattern.matcher(line);

                    while(DestinationMatcher.find()){
                        destination = DestinationMatcher.group().replace("Destination: ","");
                        download.setDescription(destination);
                    }

                    if (!line.contains("in")){
                        //进度百分比
                        Pattern percentagePattern = Pattern.compile(percentageReg);
                        Matcher percentageMatcher = percentagePattern.matcher(line);

                        //获取获取总MB
                        Pattern totalSizePattern = Pattern.compile(totalSizeReg);
                        Matcher totalSizeMatcher = totalSizePattern.matcher(line);

                        //获取当前速度
                        Pattern currentSpeedPattern = Pattern.compile(currentSpeedReg);
                        Matcher currentSpeedMatcher = currentSpeedPattern.matcher(line);

                        //获取剩余时间
                        Pattern ETAPattern = Pattern.compile(ETArex);
                        Matcher ETAMatcher = ETAPattern.matcher(line);

                        if (percentageMatcher.find()){
                            percentage = Double.parseDouble(percentageMatcher.group().replace("%",""));
                            download.setPercentage(percentage);
                        }

                        if (totalSizeMatcher.find()){
                            totalSize = totalSizeMatcher.group();
                            download.setTotalSize(totalSize+"MB");
                        }

                        if (currentSpeedMatcher.find()){
                            currentSpeed = currentSpeedMatcher.group();
                            download.setCurrentSpeed(currentSpeed);
                        }

                        if (ETAMatcher.find()){
                            ETA = ETAMatcher.group();
                            download.setETA(ETA);
                        }

                        //通过WS推送到前端
                        WebSocketServerDownload.send(download);
                    }
                }

                WebSocketServerDownload.send(download);


                //将记录存入数据库
                download.setStatus(exitCode);
                channelService.completeDownload(download);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                if (br != null) {
                    try {
                        br.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {

        }
        return download;
    }

    /**
     * 更新下载器
     */
    public static void update() throws Exception {
        cmd("yt-dlp -U");
        //将版本信息存入数据库
    }


    /**
     * 获取版本
     * @return
     */
    public static String version(){
       return cmd("yt-dlp --version");
    }

    /**
     * cmd命令行的操作(字符串类型)
     * @param command 命令
     */
    public static String cmd(String command){
        String result = "";
        try {
            BufferedReader br = null;
            try {
                Process p = Runtime.getRuntime().exec(command);
                //
                br = new BufferedReader(new InputStreamReader(p.getInputStream(),"UTF-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    LOGGER.debug(line);
                    result+=line;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                if (br != null) {
                    try {
                        br.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("执行cmd时出错！"+" 详细:"+e);
        }
        return result;
    }
}
