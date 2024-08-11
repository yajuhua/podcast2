package io.github.yajuhua.podcast2.update;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.Operation;
import io.github.yajuhua.download.commons.Type;
import io.github.yajuhua.download.commons.progress.DownloadProgressCallback;
import io.github.yajuhua.download.downloader.Downloader;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.podcast2.downloader.ytdlp.YtDlpUpdate;
import io.github.yajuhua.podcast2.update.pojo.AppJarFileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 下载项目jar的下载器
 */
@Slf4j
public class JarFileDownloader implements Runnable, Downloader {

    private int seconds = 0;
    private double speed = 0;
    private double percent = 0;
    private double totalSize = 0;
    private String finalFormat = "unknown";
    private List<String> links;
    private Operation operation;
    private Type type;
    private String channelUuid;
    private String uuid;
    private Map args;
    private File dir;
    private List<Process> processList;
    private DownloadProgressCallback callback;
    private Aria2Client aria2Client;
    private static Gson gson = new Gson();
    private Integer status = Context.UNKNOWN;
    private String cmd;

    /**
     * 开始下载
     * @throws Exception
     */
    public void startDownload() throws Exception {
        aria2Client = new Aria2Client(Context.ARIA2C_RPC_HOSTS);
        //进行转换
        File out = new File(dir.getAbsolutePath()+File.separator+"app.jar");
        finalFormat="jar";
        download();
        if (status == Context.COMPLETED_DOWNLOAD){
            try {
                //校验md5;
                status = 1;//正在校验
                String md5 = links.get(1);
                String calculateMD5 = calculateMD5(out.getAbsolutePath());
                File newFile = null;
                if (md5.equals(calculateMD5)){
                    String dataPath = links.get(2);
                    List<File> appPackage = jarList(dataPath);
                    if (appPackage.isEmpty()){
                        dir.renameTo(new File(dir.getParent(),"0"));
                        newFile = new File(dir.getParent(),"0");
                    }else {
                        String name = appPackage.get(appPackage.size() - 1).getName();
                        String child = (Integer.parseInt(name) + 1) + "";
                        dir.renameTo(new File(dir.getParent(),child));
                        newFile = new File(dir.getParent(),child);
                    }
                    if (newFile == null){
                        throw new RuntimeException("找不到保存位置");
                    }
                    //写入app.jar信息
                    AppJarFileInfo appJarFileInfo = AppJarFileInfo.builder()
                            .md5(md5)
                            .version(links.get(3))
                            .build();
                    FileUtils.write(new File(newFile,"app.json"),gson.toJson(appJarFileInfo),"UTF-8");
                    updateProgressStatus(Context.COMPLETED);
                    log.info("下载完成: {}",newFile.getAbsolutePath());
                }else{
                    updateProgressStatus(2);
                }
            } catch (Exception e) {
                //校验错误
                updateProgressStatus(2);
                try {
                    FileUtils.forceDelete(dir);
                } catch (IOException ex) {
                    log.warn("删除错误文件异常",e);
                }
            }
        }else if (status == Context.DOWNLOAD_ERR){
            //结束
            aria2Client.remove();
            return;
        }
        //删除下载的
        if (status == Context.REMOVE){
            updateProgressStatus(Context.REMOVE);
            if (dir.exists()){
                try {
                    FileUtils.forceDelete(dir);
                } catch (IOException e) {
                    log.warn("删除临时文件异常",e);
                }
            }
        }
    }

    private void download() throws Exception{
        //下载选择
        args.put("out","app.jar");
        args.put("dir",this.dir.getAbsolutePath().replace("\\","/"));
        String response = aria2Client.addUri(Context.ARIA2C_RPC_SECRET, Arrays.asList(links.get(0)),args);
        log.info("aria2响应:{}",response);

        String[] keys = {"status","totalLength","completedLength","downloadSpeed","dir"};
        String tellStatusStr;
        while ((tellStatusStr=aria2Client.tellStatus(keys))!=null){

            JsonObject jsonObject = gson.fromJson(tellStatusStr, JsonObject.class);

            int totalLength = jsonObject.get("result").getAsJsonObject().get("totalLength").getAsInt();
            int completedLength = jsonObject.get("result").getAsJsonObject().get("completedLength").getAsInt();
            int downloadSpeed = jsonObject.get("result").getAsJsonObject().get("downloadSpeed").getAsInt();
            String downloadStatus = jsonObject.get("result").getAsJsonObject().get("status").getAsString();
            String dir = jsonObject.get("result").getAsJsonObject().get("dir").getAsString();
            String id = jsonObject.get("id").getAsString();

            //把情况排除掉
            if (downloadStatus!=null && !downloadStatus.contains("error") && !id.contains(" ") && totalLength!=0 && completedLength!=0){

                if (totalLength!=0 && completedLength!=0 && downloadSpeed!=0){
                    //获取下载进度 totalLength-completedLength
                    BigDecimal totalLengthBigDecimal = BigDecimal.valueOf(totalLength);
                    BigDecimal completedLengthBigDecimal = BigDecimal.valueOf(completedLength);
                    BigDecimal hundred = BigDecimal.valueOf(100);
                    BigDecimal divide = completedLengthBigDecimal.divide(totalLengthBigDecimal, 2, RoundingMode.HALF_UP);
                    BigDecimal percentage = divide.multiply(hundred);
                    percent = percentage.doubleValue();
                    //求剩余时间
                    //(totalLength-completedLength)/downloadSpeed
                    BigDecimal surplusLength = totalLengthBigDecimal.subtract(completedLengthBigDecimal);
                    BigDecimal surplusSecond = surplusLength.divide(BigDecimal.valueOf(downloadSpeed), 0, RoundingMode.HALF_UP);
                    seconds = surplusSecond.intValue();
                    totalSize = totalLength;
                    speed = downloadSpeed;
                    status=Context.DOWNLOADING;
                    updateProgressStatus(Context.DOWNLOADING);
                }
            }
            if (downloadStatus.contains("error")){
                status=Context.DOWNLOAD_ERR;
                updateProgressStatus(Context.DOWNLOAD_ERR);
                break;
            }
            if (downloadStatus.contains("complete")){
                status=Context.COMPLETED_DOWNLOAD;
                updateProgressStatus(Context.COMPLETED_DOWNLOAD);
                break;
            }
            if (downloadStatus.contains("removed")){
                status=Context.REMOVE;
                updateProgressStatus(Context.REMOVE);
                return;
            }

            //等待1s
            TimeUnit.SECONDS.sleep(1);
        }

        //
        if (aria2Client.tellStatus(keys) == null){
            status=Context.DOWNLOAD_ERR;
            updateProgressStatus(Context.DOWNLOAD_ERR);
        }
    }

    /**
     * 结束下载
     * @throws Exception
     */
    public void kill() throws Exception {
        log.info("结束下载：channelUuid:{} uuid:{}",channelUuid,uuid);
        if (aria2Client != null){
            aria2Client.forceRemove();
        }
        this.updateProgressStatus(Context.REMOVE);
    }

    /**
     * 下载进度
     * @param callback
     */
    public void callback(DownloadProgressCallback callback){
        this.callback=callback;
    }

    /**
     * 是否完成
     * @return
     */
    public boolean isCompleted(){
        return status == Context.COMPLETED;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getChannelUuid() {
        return channelUuid;
    }

    @Override
    public void run() {
        try {
            startDownload();
        } catch (Exception e) {
            updateProgressStatus(Context.DOWNLOAD_ERR);
            log.error("下载错误",e);
        }
    }


    /**
     * 更新状态码
     * @param status
     */
    private void updateProgressStatus(Integer status){
        callback.onProgressUpdate(channelUuid,uuid,
                status,percent,seconds,totalSize,speed
                ,operation.toString(),type.toString(),finalFormat);
    }

    /**
     * 构建下载jar包的Request
     * @return
     */
    public static Request buildDownloadJarFileRequest(String version,String githubProxyUrl,String dataPath){

        //1.构建下载jar包链接 方式一:直接下载;方式二:使用Gihub加速站
        String jarFileUrl = "https://github.com/yajuhua/podcast2/releases/download/"+ version +"/app.jar";
        String md5FileUrl = "https://github.com/yajuhua/podcast2/releases/download/"+ version +"/md5.txt";

        if (githubProxyUrl != null && !githubProxyUrl.isEmpty()){
            jarFileUrl = githubProxyUrl + jarFileUrl;
            md5FileUrl = githubProxyUrl + md5FileUrl;
        }
        //1.1 读取jar包目录 创建目录
        List<File> jarDirList = jarList(dataPath);

        //清理之前的
        if (jarDirList.size() > 1){
            //留当前的，其他删除
            for (int i = 0; i < jarDirList.size() - 1; i++) {
                try {
                    FileUtils.forceDelete(jarDirList.get(i));
                } catch (Exception e) {
                    log.warn("删除之前jar包失败",e);
                }
            }
        }

        //jar存放目录
        File savePackageDir = new File(dataPath,"package");
        //创建临时目录
        File tmpPackageDir = new File(savePackageDir,"tmp");
        if (tmpPackageDir.exists()){
            try {
                FileUtils.forceDelete(tmpPackageDir);
            } catch (Exception e) {
                log.error("删除临时目录失败",e);
                throw new RuntimeException("删除临时目录失败",e);
            }
        }

        //1.1开始下载 使用自定义下载器
        String jarFileMD5 = getJarFileMD5(md5FileUrl);
        List<String> links = new ArrayList<>();
        links.add(jarFileUrl);//jar链接
        links.add(jarFileMD5);//校验码
        links.add(dataPath);//项目数据目录
        links.add(version);//jar包版本
        Request request = new Request();
        request.setCustomizeDownloader(new JarFileDownloader());
        request.setDownloader(DownloadManager.Downloader.Customize);
        request.setOperation(Operation.Single);
        request.setArgs(new HashMap());
        request.setType(Type.Video);
        request.setDir(tmpPackageDir);
        request.setUuid(jarFileMD5);
        request.setChannelUuid(version);
        request.setLinks(links);

        return request;
    }

    /**
     * 获取jar包集合
     * @return
     */
    public static List<File> jarList(String dataPath){
        File savePackageDir = new File(dataPath,"package");
        if (!savePackageDir.exists() && !savePackageDir.isDirectory()){
            return new ArrayList<>();
        }
        Pattern patternDir = Pattern.compile("\\d{1}");//一位数字的目录
        List<File> jarDirList = Arrays.stream(savePackageDir.listFiles()).filter(new Predicate<File>() {
            @Override
            public boolean test(File file) {
                if (!file.isDirectory()) {
                    return false;
                }
                return patternDir.matcher(file.getName()).find();
            }
        }).sorted(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.compare(Integer.parseInt(o1.getName()), Integer.parseInt(o2.getName()));
            }
        }).collect(Collectors.toList());
        return jarDirList;
    }

    /**
     * 获取jar包md5值
     * @param md5FileUrl md5.txt的url
     * @return md5值
     */
    public static String getJarFileMD5(String md5FileUrl){
        List<String> list = YtDlpUpdate.get(md5FileUrl);
        if (list.isEmpty()){
            throw new RuntimeException("无法获取jar包MD5值");
        }
        Map map = new HashMap();
        for (String s : list) {
            s = s.trim();
            String[] split = s.split("\\s+");
            map.put(split[1],split[0]);
        }
        return map.get("app.jar").toString();
    }

    /**
     * 计算文件的 MD5 哈希值
     * @param filePath
     * @return
     * @throws Exception
     */
    public static String calculateMD5(String filePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] dataBytes = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, bytesRead);
            }

            byte[] mdBytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : mdBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }
}
