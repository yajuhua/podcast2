package io.github.yajuhua.podcast2.alist;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.yajuhua.podcast2.alist.dto.auth.LoginDTO;
import io.github.yajuhua.podcast2.alist.dto.fs.FileInfoDTO;
import io.github.yajuhua.podcast2.alist.dto.fs.PutDTO;
import io.github.yajuhua.podcast2.alist.dto.fs.RemoveFileDTO;
import io.github.yajuhua.podcast2.alist.dto.task.upload.CancelDTO;
import io.github.yajuhua.podcast2.alist.dto.task.upload.DeleteDTO;
import io.github.yajuhua.podcast2.alist.dto.task.upload.InfoDTO;
import io.github.yajuhua.podcast2.alist.dto.task.upload.RetryDTO;
import io.github.yajuhua.podcast2.alist.utils.Http;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
public class Alist {
    @Getter
    private String token;
    private String path;
    private String username;
    private String password;
    private String url;
    @Getter
    private Gson gson;
    private Map heads;
    private Long createTime;

    public Alist() {
       gson = new Gson();
       heads = new HashMap();
       createTime = System.currentTimeMillis();
    }

    public Alist(String path, String username, String password, String url) {
        this.path = path;
        this.username = username;
        this.password = password;
        this.url = url;
        heads = new HashMap();
        gson = new Gson();
        createTime = System.currentTimeMillis();
    }

    /**
     * 删除文件
     * @param fileName
     * @return
     */
    public boolean deleteFile(String fileName){
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();
        File file = new File(fileName);
        array.add(URLEncoder.encode(file.getName()));
        object.add("names",array);
        object.addProperty("dir",path + "/" + file.getParent());
        heads.put("Content-Type", "application/json");
        String json = Http.post(url + "/api/fs/remove", gson.toJson(object), heads);
        RemoveFileDTO fileDTO = gson.fromJson(json, RemoveFileDTO.class);
        if (200 != fileDTO.getCode()){
            throw new RuntimeException("删除文件文件失败：" + fileDTO.getMessage());
        }
        return true;
    }

    /**
     * 添加上传文件任务
     * @param filePath
     * @return
     */
    public PutDTO addUploadFileTask(String filePath){
        File file = new File(filePath);
        if (!file.exists() && !file.isFile()){
            throw new RuntimeException("仅支持文件上传");
        }
        heads.put("File-Path", URLEncoder.encode(path + "/" + file.getName()));
        heads.put("As-Task","true");
        String json = Http.put(url + "/api/fs/put", filePath, heads);
        PutDTO putDTO = gson.fromJson(json, PutDTO.class);

        if (200 != putDTO.getCode()){
            throw new RuntimeException("文件上传失败：" + putDTO.getMessage());
        }
        return putDTO;
    }

    /**
     * 取消上传任务
     * @param tid
     */
    public boolean cancelUploadFileTask(String tid){
        String json = Http.post(url + "/api/admin/task/upload/cancel?tid=" + tid, null, heads);
        CancelDTO cancelDTO = gson.fromJson(json, CancelDTO.class);
        if (200 != cancelDTO.getCode()){
            return false;
        }
        return true;
    }

    /**
     * 删除上传任务
     * @param tid
     */
    public boolean deleteUploadFileTask(String tid){
        String json = Http.post(url + "/api/admin/task/upload/delete?tid=" + tid, null, heads);
        DeleteDTO deleteDTO = gson.fromJson(json, DeleteDTO.class);
        if (200 != deleteDTO.getCode()){
            return false;
        }
        return true;
    }

    /**
     * 重新上传任务
     * @param tid
     */
    public boolean retryUploadFileTask(String tid){
        String json = Http.post(url + "/api/admin/task/upload/retry?tid=" + tid, null, heads);
        RetryDTO retryDTO = gson.fromJson(json, RetryDTO.class);
        if (200 != retryDTO.getCode()){
            return false;
        }
        return true;
    }

    /**
     * 获取任务信息
     * @return
     */
    public InfoDTO getUploadTaskInfo(String tid){
        String json = Http.post(url + "/api/admin/task/upload/info?tid=" + tid, null, heads);
        return gson.fromJson(json, InfoDTO.class);
    }

    /**
     * 文件是否存在
     * @param fileName
     * @return
     */
    public boolean exist(String fileName){
        try {
            getFileUrl(fileName);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 获取文件链接
     * @param fileName
     * @return
     */
    public String getFileUrl(String fileName){
        JsonObject object = new JsonObject();
        object.addProperty("path",this.path + "/" + fileName);
        object.addProperty("password",this.password);
        heads.put("Content-Type", "application/json");
        String json = Http.post(url + "/api/fs/get", gson.toJson(object), heads);
        FileInfoDTO infoDTO = gson.fromJson(json, FileInfoDTO.class);
        if (200 != infoDTO.getCode()){
            throw new RuntimeException("获取文件链接失败：" + infoDTO.getMessage());
        }
        if (infoDTO.getData().getIsDir()){
            throw new RuntimeException("不支持目录获取");
        }
        return infoDTO.getData().getRawUrl();
    }

    /**
     * 刷新token
     * @return
     */
    public boolean refreshToken(){
        JsonObject object = new JsonObject();
        object.addProperty("username",username);
        object.addProperty("password",password);
        Map heads = new HashMap();
        heads.put("Content-Type", "application/json");
        String json = Http.post(url + "/api/auth/login", gson.toJson(object), heads);
        LoginDTO loginDTO = gson.fromJson(json, LoginDTO.class);
        if (200 != loginDTO.getCode()){
            throw new RuntimeException("刷新token错误：" + loginDTO.getMessage());
        }
        this.token = loginDTO.getData().getToken();
        this.heads.clear();
        this.heads.put("Authorization",this.token);
        return true;
    }

    /**
     * 测试连通性
     * @return
     */
    public boolean isConnect(){
        try {
            Http.get(url + "/ping",null,null);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
