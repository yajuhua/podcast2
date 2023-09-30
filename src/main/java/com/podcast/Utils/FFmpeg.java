package com.podcast.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 主要是转换为m4a和mp4
 */
public class FFmpeg {
    private static final Logger LOGGER = LoggerFactory.getLogger("FFmpeg");
    private String filePath;

    public FFmpeg(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 转换为m4a
     */
    public void convertToM4A(){
        //去掉扩展名
        int index = this.filePath.indexOf(".");
        String savePath = filePath.substring(0, index + 1);
        String cmd = "ffmpeg -i " + this.filePath +" -vn -c:a copy " + savePath + "m4a";
        LOGGER.debug("cmd:"+cmd);
        try {
            Cmd(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //删除原文件
        File delFile = new File(this.filePath);
        if (delFile.delete()){
            LOGGER.info("删除成功!");
        }else {
            LOGGER.error("删除失败！");
        }
    }

    /**
     * 转换为mp4
     */
    public void convertToMP4(){
        //去掉扩展名
        int index = this.filePath.indexOf(".");
        String savePath = filePath.substring(0, index + 1);
        String cmd = "ffmpeg -i " +  this.filePath +" -c copy -map 0:v -map 0:a -bsf:a aac_adtstoasc " + savePath + "mp4";
        LOGGER.debug("cmd:"+cmd);
        try {
            Cmd(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //删除原文件
        File delFile = new File(this.filePath);
        if (delFile.delete()){
            LOGGER.info("删除成功!");
        }else {
            LOGGER.error("删除失败！");
        }
    }

    /**
     * cmd命令行的操作(字符串类型)
     * @param command 命令
     */
    private static void Cmd(String command) throws IOException {
        try {
            BufferedReader br = null;
            try {
                Process p = Runtime.getRuntime().exec(command);
                //解决中文乱码 GBK是汉字编码
                br = new BufferedReader(new InputStreamReader(p.getInputStream(),"GBK"));//解决中文乱码 GBK是汉字编码//二维码会乱码
                String line = null;
                while ((line = br.readLine()) != null) {
                    LOGGER.debug(line);
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
    }
}
