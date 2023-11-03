package com.podcast.Utils;

import com.google.gson.Gson;
import com.podcast.Progress.WebSocketServerDownload;
import com.podcast.pojo.Download;
import com.podcast.service.ChannelService;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class N_m3u8DL_RE {
    /**
     * 解析json
     */
    private static Gson gson = new Gson();
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("N_m3u8DL_RE");
    private ChannelService channelService = new ChannelService();
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
        nM3u8DLRECmd(getDownloadCmd());
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

    /**
     * 解析N_m3u8DL-RE的日志
     * @param command 命令
     */
    public void nM3u8DLRECmd(String command) throws IOException {
        //封装信息
        Download download = new Download();
        download.setId(UUID.randomUUID().toString());
        download.setETA("暂无");
        download.setDownloaderName("N_m3u8DL-RE");
        download.setCurrentSpeed("暂无");
        download.setTotalSize("暂无 ");//后面得加个空格，不然会报错


        try {
            BufferedReader br = null;
            try {
                Process p = Runtime.getRuntime().exec(command);

                br = new BufferedReader(new InputStreamReader(p.getInputStream(),"UTF-8"));//解决中文乱码 GBK是汉字编码//二维码会乱码
                String line = null;
                while ((line = br.readLine()) != null) {

                    String destinationReg = "保存文件名: .{1,}";

                    //描述
                    Pattern destinationPattern = Pattern.compile(destinationReg);
                    Matcher destinationMatcher = destinationPattern.matcher(line);

                    if (destinationMatcher.find()){
                        download.setDescription(destinationMatcher.group().replace("保存文件名: ",""));
                    }


                    //获取百分比
                    String regex = "Vid Kbps: \\d{1,3}%";
                    Pattern compile = Pattern.compile(regex);
                    Matcher matcher = compile.matcher(line);
                    while (matcher.find()){
                        String regex2 = "\\d{1,3}";
                        Pattern compile2 = Pattern.compile(regex2);
                        Matcher matcher2 = compile2.matcher(matcher.group());
                        while (matcher2.find()){
                            download.setPercentage(Integer.parseInt(matcher2.group()));
                        }
                    }
                    //出现Done说明下载完成了
                    if (line.contains("Done")){
                        download.setPercentage(100);

                        //通过WS推送到前端
                        if (WebSocketServerDownload._session!=null && WebSocketServerDownload._session.isOpen()){
                            WebSocketServerDownload._session.getBasicRemote().sendText(gson.toJson(download));
                        }

                        //将记录存入数据库
                        download.setStatus(1);
                        channelService.completeDownload(download);

                        return;
                    }

                    //通过WS推送到前端
                    if (WebSocketServerDownload._session!=null && WebSocketServerDownload._session.isOpen()){
                        WebSocketServerDownload._session.getBasicRemote().sendText(gson.toJson(download));
                    }
                }

                //将记录存入数据库
                download.setStatus(0);
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
    }
}
