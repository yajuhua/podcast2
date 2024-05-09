package io.github.yajuhua.podcast2.common.utils;

import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.manager.DownloadManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DownloaderUtils {
    public enum Downloader{
        YtDlp,
        Nm3u8DlRe,
        Aria2
    }

    /**
     * 获取下载器版本版本
     * @return
     */
    public static String getDownloaderVersion(Downloader downloader){
        String version = null;
        switch (downloader) {
            case YtDlp:
                version = cmd("yt-dlp --version");
                break;
            case Aria2:
                try {
                    BufferedReader br = null;
                    try {
                        Process p = Runtime.getRuntime().exec("aria2c --version");

                        br = new BufferedReader(new InputStreamReader(p.getInputStream(),"UTF-8"));
                        String line = null;
                        String reg = "\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}";//匹配版本号
                        Pattern pattern = Pattern.compile(reg);

                        while ((line = br.readLine()) != null) {
                            if (line.contains("aria2 版本") || line.contains("aria2 version")){
                                Matcher matcher = pattern.matcher(line);
                                while (matcher.find()){
                                    line = matcher.group();
                                }
                                //返回结果
                                version = line;
                                return line;
                            }

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
                }
                break;
            case Nm3u8DlRe:
                version = cmd("N_m3u8DL-RE --version");
                break;
        }
        return version;
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
                    log.debug(line);
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
            log.error("执行cmd时出错！"+" 详细:"+e);
        }
        return result;
    }

    /**
     * b转MB
     * @param byteNum
     * @return
     */
    public static String byteToMB(double byteNum){
        BigDecimal a = new BigDecimal(String.valueOf(byteNum));
        BigDecimal b = new BigDecimal("1048576");
        DecimalFormat df = new DecimalFormat("0.00");
        double v = a.divide(b).doubleValue();
        return String.valueOf(df.format(v));
    }

    /**
     * 获取下载状态码描述
     * @return
     */
    public static String downloadStatusDescription(Integer code){
        Map<Integer,String> map = new HashMap<>();
        map.put(-1,"未知错误");
        map.put(1,"下载完成");
        map.put(2,"合并完成");
        map.put(3,"转换mp4成功");
        map.put(4,"转换m4a成功");
        map.put(5,"完成");
        map.put(6,"下载错误");
        map.put(7,"合并错误");
        map.put(8,"转换mp4错误");
        map.put(9,"转换m4a错误");
        map.put(10,"下载中");
        map.put(11,"合并中");
        map.put(12,"转换mp4中");
        map.put(13,"转换m4a中");
        map.put(14,"下载第一部分中");
        map.put(15,"下载第二部分中");
        map.put(16,"第一部分下载完成");
        map.put(17,"第二部分下载完成");
        map.put(18,"第一部分下载错误");
        map.put(19,"第二部分下载错误");
        map.put(20,"移除下载");
        return map.get(code);
    }

    /**
     * 结束下载的状态码
     * @return
     */
    public static List<Integer> endStatusCode(){
        List<Integer> endStatusCode = new ArrayList<>();
        Collections.addAll(endStatusCode, Context.COMPLETED,Context.DOWNLOAD_ERR,Context.MERGE_ERR,
                Context.TO_MP4_ERR,Context.DOWNLOAD_PATH1_ERR,Context.DOWNLOAD_PATH2_ERR,Context.UNKNOWN,
                Context.TO_M4A_ERR,Context.REMOVE);
        return endStatusCode;
    }

    /**
     * 将秒数转换成 00:24:01这种时长形式的字符串
     * @param s 秒数
     * @return
     */
    public static String duration(int s) {
        // 计算小时数
        long hours = s / 3600;
        // 计算分钟数
        long minutes = (s % 3600) / 60;
        // 计算剩余秒数
        long seconds = s % 60;

        // 创建LocalTime对象，表示时间部分
        LocalTime time = LocalTime.of((int) hours, (int) minutes, (int) seconds);

        // 使用DateTimeFormatter将LocalTime格式化为"00:20:00"的字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = time.format(formatter);

        return formattedTime;
    }

}
