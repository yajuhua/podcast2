package com.podcast.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class N_m3u8DL_RE {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("N_m3u8DL_RE");
    /**
     * 下载器名称
     */
    private final String TOOLNAME = " N_m3u8DL-RE ";
    /**
     * 保存路径
     */
    private final String PATHS = " --save-dir ";
    /**
     * 保存名称
     */
    private final String RENAME = " --save-name ";

    /**
     * 下载链接
     */
    private String url;
    /**
     * 重命名
     */
    private String rename;
    /**
     * 保存路径
     */
    private String paths;

    public N_m3u8DL_RE() {
    }

    public N_m3u8DL_RE(String url) {
        this.url = url;
    }

    public N_m3u8DL_RE(String url, String rename, String paths) {
        this.url = url;
        this.rename = rename;
        this.paths = paths;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRename(String rename) {
        this.rename = RENAME+rename;
    }

    public void setPaths(String paths) {
        this.paths = PATHS+paths;
    }

    /**
     * 获取最终下载的cmd命令
     * @return
     */
    public String getDownloadCmd(){
        String[] options = {this.url,this.paths,this.rename};
        String[] optionCmd = {this.TOOLNAME,this.PATHS,this.RENAME};
        String cmd = "";

        for (int i = 0; i < options.length; i++) {
            if (options[i] != null){
                cmd += optionCmd[i]+options[i];
            }
        }
        //debug
        LOGGER.debug("getDownloadCmd:"+cmd);
        return cmd;
    }

    /**
     * 开始下载
     * @throws Exception
     */
    public void startDownload() throws Exception {
        Cmd(getDownloadCmd());
    }

    /**
     * cmd命令行的操作(字符串类型)
     * @param command 命令
     */
    public static void Cmd(String command) throws IOException {
        try {
            BufferedReader br = null;
            try {
                Process p = Runtime.getRuntime().exec(command);
                //解决中文乱码 GBK是汉字编码
                br = new BufferedReader(new InputStreamReader(p.getInputStream(),"UTF-8"));//解决中文乱码 GBK是汉字编码//二维码会乱码
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
