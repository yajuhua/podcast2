package io.github.yajuhua.podcast2.downloader.ytdlp;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.yajuhua.podcast2.common.utils.DownloaderUtils;
import io.github.yajuhua.podcast2.common.utils.Http;
import io.github.yajuhua.podcast2.pojo.entity.Downloader;
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

    public static boolean withProxy(String proxyUrl, String tmpPath){
        log.info("使用Github加速站更新yt-dlp stable频道");
        String currentVersion = getCurrentVersion();
        if (currentVersion.equalsIgnoreCase(latestTagName())){
            log.info("当前是stable频道最新版本: {}", currentVersion);
            return true;
        }
        try {
            //构建下载URL
            String downloadUrl = proxyUrl + "https://github.com/yt-dlp/yt-dlp/releases/download/" + latestTagName() + "/" + getFileName();
            log.info("构建下载URL:{}",downloadUrl);

            //下载
            Http.downloadFile(downloadUrl,tmpPath);

            //是否可用
            String tmpFile = tmpPath + File.separator + getFileName();
            String checksum = getSHA256Checksum(tmpFile);

            boolean check = getYtDlpSHA256Checksum(proxyUrl).equals(checksum);
            if (!check){
                log.error("yt-dlp文件校验失败!");
                return false;
            }

            //替换
            String fileName = System.getProperty("os.name").startsWith("Win") ? "yt-dlp.exe.tmp" : "yt-dlp.tmp";
            File filePath = System.getProperty("os.name").contains("Linux") ? new File("/usr/sbin") : new File(System.getProperty("user.dir"));
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
            log.info("更新yt-dlp成功!");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("更新yt-dlp失败!");
            return false;
        }
    }

    private static String getFileName(){
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
    private static String getSHA256Checksum (String filePath){
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

    private static String getYtDlpSHA256Checksum(String proxyUrl){

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

    /**
     * 获取当前版本
     * @return
     */
    public static String getCurrentVersion(){
        return DownloaderUtils.cmd("yt-dlp --version");
    }

    /**
     * 携带更新参数 --update-to 切换到不同频道
     * @param args
     * @return
     */
    public static void withUpdateArgs(String args){
        String rs = DownloaderUtils.cmd("yt-dlp --update-to " + args.trim());
        boolean b = (rs == null || rs.isEmpty());
        if (b){
            log.error("yt-dlp 更新错误: {}", args.trim());
        }
    }

    /**
     * 更新至稳定版
     */
    public static void updateToStable(){
        withUpdateArgs("stable@latest");
    }

    /**
     * 是否到更新时间
     * @param downloader
     * @return
     */
    public static boolean isUpdate(Downloader downloader){
        Long latestUpdateTime = downloader.getUpdateTime();
        Integer refreshDuration = downloader.getRefreshDuration()*3600*1000;
        return (latestUpdateTime + refreshDuration) < System.currentTimeMillis();
    }

}
