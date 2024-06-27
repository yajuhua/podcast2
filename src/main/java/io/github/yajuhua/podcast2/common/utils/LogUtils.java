package io.github.yajuhua.podcast2.common.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * 日志查看工具类
 */
public class LogUtils {
    /**
     * 自定义获取日志
     * @param start 开始时间
     * @param end 结束时间
     * @param logsPath 日志目录
     * @param level 日志等级
     * @return
     * @throws Exception
     */
    public static List<String> logs(LocalDateTime start, LocalDateTime end, File logsPath, String level) throws Exception {
        List<String> logs = new ArrayList<>();
        List<String> logLines = new ArrayList<>();
        List<File> logFiles = new ArrayList<>();

        //获取在start和end之间的日期的日志文件
        Map<File, Map<String, LocalDateTime>> logFileWithDate = getLogFileWithDate(logsPath);
        for (File file : logsPath.listFiles()) {
            Map<String, LocalDateTime> map = logFileWithDate.get(file);
            if (map != null && !map.isEmpty()){
                LocalDateTime startTime = map.get("start");
                LocalDateTime endTime = map.get("end");
                //TODO starTime和endTime可能存在null，需要排除
                startTime = startTime==null?endTime:startTime;
                endTime = endTime==null?startTime:endTime;
                if(startTime!= null && endTime != null){
                    if (!(startTime.isAfter(end) || endTime.isBefore(start))){
                        if (file.getName().contains(level.toLowerCase())){
                            logFiles.add(file);
                        }
                    }
                }

            }
        }

        //解压并读取
        for (File file : logFiles) {
            String extension = FilenameUtils.getExtension(file.getName());
            if (extension.equals("log")){
                logLines.addAll(IOUtils.readLines(new FileInputStream(file), "UTF-8"));
            }else {
                GZIPInputStream gzi = new GZIPInputStream(new FileInputStream(file));
                logLines.addAll(IOUtils.readLines(gzi));
            }
        }

        //根据时、分、秒过滤
        //2024-06-23 13:52:41
        Pattern dateTimePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Matcher mn = null;
        for (String line : logLines) {
            Matcher matcher = dateTimePattern.matcher(line);
            if (matcher.find()) {
                LocalDateTime dateTime = LocalDateTime.parse(matcher.group(), formatter);
                if ((dateTime.isAfter(start) && dateTime.isBefore(end)) || dateTime.equals(start) || dateTime.equals(end)) {
                    logs.add(line);
                    int currentIndex = logLines.indexOf(line);
                    // Check the next lines for continuation of logs
                    while (currentIndex + 1 < logLines.size()) {
                        String nextLine = logLines.get(currentIndex + 1);
                        Matcher nextMatcher = dateTimePattern.matcher(nextLine);
                        if (!nextMatcher.find()) {
                            logs.add(nextLine);
                            currentIndex++;
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        return logs;
    }


    /**
     * 获取本地日志文件
     * @param logsPath
     * @return
     */
    public static Map<File,Map<String,LocalDateTime>> getLogFileWithDate(File logsPath){
        Map<File,Map<String,LocalDateTime>> logFileWithDate = new HashMap<>();
        Pattern logInfoPattern = Pattern.compile("podcast2-info.log");
        Pattern logErrorPattern = Pattern.compile("podcast2-error.log");
        Pattern gzInfoPattern = Pattern.compile("podcast2-info\\d+-\\d{4}-\\d{2}-\\d{2}-\\.log\\.gz");
        Pattern gzErrorPattern = Pattern.compile("podcast2-error\\d+-\\d{4}-\\d{2}-\\d{2}-\\.log\\.gz");

        if (logsPath.isDirectory()) {
            for (File f : logsPath.listFiles()) {
                String fileName = f.getName();
                if (gzInfoPattern.matcher(fileName).find() || gzErrorPattern.matcher(fileName).find() ||
                        logInfoPattern.matcher(fileName).find() || logErrorPattern.matcher(fileName).find()) {
                    logFileWithDate.put(f,getStartWithEndLogTime(f));
                }
            }
        }
        return logFileWithDate;
    }

    /**
     * 获取文件创建时间
     * @param filePath
     * @return
     */
    public static LocalDateTime getFileCreateTime(String filePath){
        FileTime creationTime = null;
        try {
            Path path = Paths.get(filePath);
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            creationTime = attributes.creationTime();
        } catch (IOException e) {
            throw new RuntimeException("获取文件创建时间错误：" + e.getMessage());
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(creationTime.toMillis()), ZoneId.systemDefault());
    }

    /**
     * 获取日志文件下首尾日志的时间
     * @return
     */
    public static Map<String,LocalDateTime> getStartWithEndLogTime(File logFile){
        //2024-06-23 13:52:39.651
        Pattern datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{3}");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        List<String> lines;
        Map<String,LocalDateTime> map = new HashMap<>();

        try (FileInputStream fileInputStream = new FileInputStream(logFile)){
            String extension = FilenameUtils.getExtension(logFile.getName());
            if ("log".equals(extension)){
                lines = IOUtils.readLines(fileInputStream,"UTF-8");
            }else {
                GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
                lines = IOUtils.readLines(gzipInputStream, "UTF-8");
                fileInputStream.close();
                gzipInputStream.close();
            }
            //获取end
            for (String line : lines) {
                Matcher matcher = datePattern.matcher(line);
                if (matcher.find()){
                    map.put("end",LocalDateTime.parse(matcher.group(),formatter));
                }
            }
            //获取start
            for (int i = lines.size() - 1; i > 0; i--) {
                Matcher matcher = datePattern.matcher(lines.get(i));
                if (matcher.find()){
                    map.put("start",LocalDateTime.parse(matcher.group(),formatter));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    /**
     * 获取最近的日志
     * @param n
     * @param unit
     * @return
     */
    public static List<String> getRecent(Long n, TimeUnit unit,File logsPath,String level)throws Exception{
        Long time = unit.toSeconds(n);
        //获取最近日志的时间
        Map<File, Map<String, LocalDateTime>> logFileWithDate = getLogFileWithDate(logsPath);
        LocalDateTime max = null;
        for (File file : logsPath.listFiles()) {
            Map<String, LocalDateTime> map = logFileWithDate.get(file);
            if (map != null && !map.isEmpty() && file.getName().contains(level.toLowerCase())){
                LocalDateTime endTime = map.get("end");
                if (max == null){
                    max = endTime;
                }else if (endTime.isAfter(max)){
                    max = endTime;
                }
            }
        }
        return logs(max.plusSeconds(-time),max,logsPath,level);
    }

    @Test
    public void testRecentLog()throws Exception{
        //TODO VO和Controller
        String path = "C:\\Users\\Administrator\\Desktop\\fsdownload\\logs\\logs";
        List<String> info = getRecent(10L, TimeUnit.MINUTES, new File(path), "error");
        for (String s : info) {
            System.out.println(s);
        }
    }
}
