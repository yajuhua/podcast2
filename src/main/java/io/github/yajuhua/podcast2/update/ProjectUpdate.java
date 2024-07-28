package io.github.yajuhua.podcast2.update;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.progress.DownloadProgress;
import io.github.yajuhua.download.commons.utils.CommonUtils;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.podcast2.common.utils.Http;
import io.github.yajuhua.podcast2.update.pojo.AppJarFileInfo;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * 项目更新
 */
@Slf4j
public class ProjectUpdate {

    private String version;
    private String githubProxyUrl;
    private String dataPath;
    private DownloadManager downloadManager;


    public ProjectUpdate(String version, String githubProxyUrl, String dataPath) {
        this.version = version;
        this.githubProxyUrl = githubProxyUrl;
        this.dataPath = dataPath;
        this.downloadManager = new DownloadManager();
    }

    /**
     * 下载最新jar包
     */
    public void downloadJar() throws Exception {
        Request request = JarFileDownloader.buildDownloadJarFileRequest(version, githubProxyUrl, dataPath);
        downloadManager.add(request);
        downloadManager.startDownload();
    }

    /**
     * 停止且删除下载
     */
    public void remove() throws Exception {
      downloadManager.killByChannelUuid(version);
    }

    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DownloadStatus{
        double percent;
        boolean isCompleted;
        boolean isError;
        boolean isDownload;
        boolean isDownloading;
        String version;
    }

    /**
     * 获取下载状态
     * @return
     */
    public DownloadStatus getStatus() throws Exception {
        Set<DownloadProgress> progresses = downloadManager.allDownloadProgress();
        for (DownloadProgress progress : progresses) {
            DownloadStatus status = DownloadStatus.builder()
                    .percent(progress.getStatus() == Context.COMPLETED?100:progress.getDownloadProgress())
                    .isError(progress.getStatus() == Context.DOWNLOAD_ERR || progress.getStatus() == 2)
                    .isCompleted(progress.getStatus() == Context.COMPLETED || progress.getStatus() == Context.REMOVE)
                    .isDownload(progress.getStatus() == Context.COMPLETED)
                    .isDownloading(progress.getStatus() == Context.DOWNLOADING)
                    .version(progress.getChannelUuid())
                    .build();
            return status;
        }
        return DownloadStatus.builder().isDownload(isDownload(version,dataPath)).build();
    }

    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateInfo{
        boolean hasUpdate;
        String version;
        String desc;
    }

    /**
     * 获取更新信息
     * @param currentVersion 当前版本
     * @return
     */
    public static UpdateInfo getUpdateInfo(String currentVersion) throws Exception {
        String beta = System.getenv("beta");
        Gson gson = new Gson();
        JsonObject jsonObject = null;
        if (beta != null && beta.equalsIgnoreCase("true")) {
            String json = Http.get("https://api.github.com/repos/yajuhua/podcast2/releases");
            jsonObject = gson.fromJson(json, JsonArray.class).get(0).getAsJsonObject();
        } else {
            //获取latest信息
            String json = Http.get("https://api.github.com/repos/yajuhua/podcast2/releases/latest");
            jsonObject = gson.fromJson(json, JsonObject.class);
        }
        String tagName = jsonObject.get("tag_name").getAsString();
        String body = jsonObject.get("body").getAsString();
        body = "# " + tagName + "\n\n" + body;

        // 解析 Markdown
        Parser parser = Parser.builder().build();
        org.commonmark.node.Node document = parser.parse(body);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);

        UpdateInfo updateInfo = new UpdateInfo();
        if (tagName != null) {
            if (currentVersion.startsWith("v") && tagName.startsWith("v")) {
                //大版本不支持更新
                if (!currentVersion.substring(0,2).equals(tagName.substring(0,2))){
                    updateInfo.setHasUpdate(false);
                    updateInfo.setVersion(tagName);
                    updateInfo.setDesc(currentVersion + "无法直接更新到" + tagName + "，可将数据导入新版本。");
                    return updateInfo;
                }
                String latestVersion = tagName.split("v")[1];
                currentVersion = currentVersion.split("v")[1];
                int compareVersion = CommonUtils.compareVersion(latestVersion, currentVersion);
                if (compareVersion == 0) {
                    updateInfo.setHasUpdate(false);
                    updateInfo.setDesc("当前版本已是最新版");
                    updateInfo.setVersion(tagName);
                    return updateInfo;
                } else if (compareVersion == 1) {
                    updateInfo.setHasUpdate(true);
                    updateInfo.setDesc(html);
                    updateInfo.setVersion(tagName);
                    return updateInfo;
                } else {
                    updateInfo.setHasUpdate(false);
                    updateInfo.setDesc("当前版本为待发布版本");
                    updateInfo.setVersion(currentVersion);
                    return updateInfo;
                }
            } else if (currentVersion.startsWith("beta") && tagName.startsWith("v")) {
                if (beta != null && beta.equalsIgnoreCase("true")) {
                    updateInfo.setHasUpdate(true);
                    updateInfo.setDesc(body);
                    updateInfo.setVersion(tagName);
                    return updateInfo;
                } else {
                    updateInfo.setHasUpdate(false);
                    updateInfo.setDesc("当前版本为测试版，不支持在线更新");
                    updateInfo.setVersion(currentVersion);
                    return updateInfo;
                }
            } else if (currentVersion.startsWith("beta") && tagName.startsWith("beta")) {
                if (beta != null && beta.equalsIgnoreCase("true") && !currentVersion.equals(tagName)) {
                    updateInfo.setHasUpdate(true);
                    updateInfo.setDesc(html);
                    updateInfo.setVersion(tagName);
                    return updateInfo;
                } else if (beta != null && beta.equalsIgnoreCase("true") && currentVersion.equals(tagName)) {
                    updateInfo.setHasUpdate(false);
                    updateInfo.setDesc("目前已经是最新测试版");
                    updateInfo.setVersion(currentVersion);
                    return updateInfo;
                } else {
                    updateInfo.setHasUpdate(false);
                    updateInfo.setDesc("当前版本为测试版，不支持在线更新");
                    updateInfo.setVersion(currentVersion);
                    return updateInfo;
                }
            } else if (currentVersion.startsWith("dev")) {
                updateInfo.setHasUpdate(false);
                updateInfo.setDesc("当前版本为开发版，不支持在线更新");
                updateInfo.setVersion(currentVersion);
                return updateInfo;
            }
        }
        updateInfo.setHasUpdate(false);
        updateInfo.setDesc("无法获取新版本信息");
        updateInfo.setVersion(currentVersion);
        return updateInfo;
    }

    /**
     * 在线更新仅支持Docker版本
     * @return
     */
    public static boolean isSupportUpdate(){
        String runningInDocker = System.getenv("RUNNING_IN_DOCKER");
        if (runningInDocker != null && runningInDocker.equalsIgnoreCase("true")){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 判断Jar包是否已经下载
     * @return
     */
    public static boolean isDownload(String version,String dataPath) throws Exception {
        List<File> files = JarFileDownloader.jarList(dataPath);
        if (!files.isEmpty()){
            File jarFile = new File(files.get(files.size() - 1),"app.jar");
            File infoFile = new File(files.get(files.size() - 1),"app.json");
            if (jarFile.exists() && infoFile.exists()){
                String json = FileUtils.readFileToString(infoFile, "UTF-8");
                Gson gson = new Gson();
                AppJarFileInfo appJarFileInfo = gson.fromJson(json, AppJarFileInfo.class);
                return appJarFileInfo.getVersion().equals(version);
            }
        }
        return false;
    }

    /**
     * 删除下载的Jar文件，排除正在运行的
     * @param version 版本号
     * @return
     */
    public static boolean deleteDownloadJarFile(String version,String dataPath){
        //排除当前所以的Jar文件
        File appCurrentDir = new File(System.getProperty("user.dir"));
        List<File> packageDirs = JarFileDownloader.jarList(dataPath);
        File infoFile;
        File jarFile;
        Gson gson = new Gson();
        for (File packageDir : packageDirs) {
            infoFile = new File(packageDir,"app.json");
            jarFile = new File(packageDir,"app.jar");
            if (infoFile.exists() && jarFile.exists() && !packageDir.equals(appCurrentDir)){
                AppJarFileInfo appJarFileInfo = null;
                try {
                    String json = FileUtils.readFileToString(infoFile, "UTF-8");
                    appJarFileInfo = gson.fromJson(json, AppJarFileInfo.class);
                } catch (Exception e) {
                    log.error("读取信息失败: {}",e.getMessage());
                }
                if (appJarFileInfo.getVersion().equals(version)){
                    log.info("删除{}",packageDir);
                    try {
                        FileUtils.forceDelete(packageDir);
                        return true;
                    } catch (Exception e) {
                        log.error("删除失败: {}",e.getMessage());
                        return false;
                    }
                }
            }else {
                log.warn("不存在");
            }
        }
        return false;
    }


}
