package com.podcast.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Yt_dlp {
    private static final Logger LOGGER = LoggerFactory.getLogger("Yt_dlp");
    /**
     * 1.重命名
     * 2.格式选择
     * 3.保存路径
     * 4.下载范围
     */
    private final String TOOLNAME = " yt-dlp ";
    private final String FORMAT = " --format ";
    private final String PATHS = " --paths ";
    private final String SPACE = " ";
    private final String OUTPUT = " --output ";
    private final String PLAYLIST_START = " --playlist-start ";
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
        this.rename =  OUTPUT+ "\"" +rename+".%(ext)s\"";
        this.paths = paths;
        this.url = "\""+url+"\"";
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
      /*  for (String option : options) {
            if (option!=null){
                info+=option;
            }
        }*/

        for (int i = 0; i < options.length; i++) {
            if (options[i] != null){
                info += optionsCmd[i]+options[i];
            }
        }
        return (info+" "+url).trim();
    }
    /**
     * 根据 getDownloadCmd()下载
     * @return 是否正确下载,错误返回false，否则返回true
     * @throws IOException
     */
    public  boolean startDownload() throws IOException {
        //命令输出行数
        int lineCount = 0;
        try {
            BufferedReader br = null;
            try {
                Process p = Runtime.getRuntime().exec(getDownloadCmd());
                //解决中文乱码 GBK是汉字编码
                br = new BufferedReader(new InputStreamReader(p.getInputStream(),"UTF-8"));//解决中文乱码 GBK是汉字编码//二维码会乱码
                String line = null;

                while ((line = br.readLine()) != null) {
                    //如果命令输出行数小于等于2，说明错误
                    LOGGER.debug(line);
                    lineCount++;
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
        return lineCount>2;
    }
}
