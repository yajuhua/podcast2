package io.github.yajuhua.podcast2.downloader.ytdlp;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.yajuhua.podcast2.common.utils.Http;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支持更新方式
 * 1.-U
 * 2.加速站
 */
@Slf4j
public class YtDlpUpdate {
    private String proxyUrl;
    private String filePath;
    private String tmpPath;

    public YtDlpUpdate(String proxyUrl, String filePath, String tmpPath) {
        this.proxyUrl = proxyUrl;
        this.filePath = filePath;
        this.tmpPath = tmpPath;
    }

    public boolean proxy(){

        try {
            //构建下载URL
            String downloadUrl = proxyUrl + "https://github.com/yt-dlp/yt-dlp/releases/download/" + latestTagName() + "/" + getFileName();
            log.info("构建下载URL:{}",downloadUrl);

            //下载
            Http.downloadFile(downloadUrl,tmpPath);

            //是否可用
            String tmpFile = tmpPath + File.separator + getFileName();
            String checksum = getSHA256Checksum(tmpFile);

            boolean check = getYtDlpSHA256Checksum().equals(checksum);
            if (!check){
                log.error("校验失败");
                return false;
            }

            //替换
            String fileName = System.getProperty("os.name").startsWith("Win") ? "yt-dlp.exe.tmp" : "yt-dlp.tmp";
            String finalPathTmp = filePath + File.separator + fileName;
            FileUtils.copyFile(new File(tmpFile),new File(finalPathTmp));
            File originFile = new File(finalPathTmp.substring(0,finalPathTmp.lastIndexOf(".")));
            //删除临时文件
            FileUtils.forceDelete(new File(tmpFile));
            //删除之前的
            FileUtils.forceDelete(originFile);
            //重命名
            FileUtils.moveFile(new File(finalPathTmp),originFile);
            //赋可执行权限
            originFile.setExecutable(true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getFileName(){
        String arch = System.getProperty("os.arch");
        String name = System.getProperty("os.name");
        String fileName = null;

        if (name.contains("Windows")){
            //Windows
           fileName = "yt-dlp.exe";
        }else if(name.contains("Linux")){
            //Linux
            if (arch.contains("amd64") || arch.contains("x86_64")){
                fileName = "yt-dlp_linux";
            } else if (arch.contains("aarch64")) {
                fileName = "yt-dlp_linux_aarch64";
            } else if (arch.contains("arm") || arch.contains("armv7l")) {
                fileName = "yt-dlp_linux_armv7l";
            }
        }else if (name.contains("Mac OS X") || name.contains("macOS")){
            //macOS
            fileName = "yt-dlp_macos_legacy";
        }
        return fileName;
    }


    /**
     * 获取SHA-256 散列值
     * @return
     */
    public static String getSHA256Checksum (String filePath){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(filePath);
            byte[] buffer = new byte[8192];
            int bytesRead;

            // 读取文件内容并更新摘要
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            // 计算摘要值
            byte[] hashBytes = digest.digest();

            // 将摘要值转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            fis.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
       return null;
    }

    public String getYtDlpSHA256Checksum(){

        String last = proxyUrl.substring(proxyUrl.length() -1);
        if (last.equals("/")){
            proxyUrl = proxyUrl.substring(0,proxyUrl.length() - 1);
        }
        List<String> checksum = get(proxyUrl + "/https://github.com/yt-dlp/yt-dlp/releases/download/" + latestTagName() + "/SHA2-256SUMS");

        Map map = new HashMap();

        for (String s : checksum) {
            s = s.trim();
            String[] split = s.split("\\s+");
            map.put(split[1],split[0]);
        }
        return map.get(getFileName()).toString();
    }

    public static List<String> get(String url){
        List<String> lines = new ArrayList<>();
        try {
            HttpURLConnection connection =(HttpURLConnection)new URL(url).openConnection();

            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return  lines;
    }

    /**
     * 获取最新标签
     * @return
     */
    public static String latestTagName(){
        Gson gson = new Gson();

        //获取最新tag
        String apiUrl  = "https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest";
        String json = Http.get(apiUrl);
        String tagName = gson.fromJson(json, JsonObject.class).get("tag_name").getAsString();
        return tagName;
    }
}
