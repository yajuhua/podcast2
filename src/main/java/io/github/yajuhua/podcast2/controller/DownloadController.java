package io.github.yajuhua.podcast2.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.commons.Operation;
import io.github.yajuhua.download.commons.Type;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.download.manager.Request;
import io.github.yajuhua.podcast2.alist.Alist;
import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.constant.Unit;
import io.github.yajuhua.podcast2.common.exception.DeleteException;
import io.github.yajuhua.podcast2.common.exception.ItemNotFoundException;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.common.utils.DownloaderUtils;
import io.github.yajuhua.podcast2.mapper.DownloaderMapper;
import io.github.yajuhua.podcast2.mapper.ItemsMapper;
import io.github.yajuhua.podcast2.mapper.SubMapper;
import io.github.yajuhua.podcast2.pojo.entity.Downloader;
import io.github.yajuhua.podcast2.pojo.entity.Items;
import io.github.yajuhua.podcast2.pojo.entity.Sub;
import io.github.yajuhua.podcast2.pojo.vo.DownloadCompletedVO;
import io.github.yajuhua.podcast2.pojo.vo.DownloadDetailVO;
import io.github.yajuhua.podcast2.pojo.vo.DownloaderInfoVO;
import io.github.yajuhua.podcast2.task.ReDownload;
import io.github.yajuhua.podcast2.task.Task;
import io.github.yajuhua.podcast2API.utils.TimeFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@Slf4j
@Api(tags = "下载相关接口")
@RequestMapping("/download")
public class DownloadController {
    @Autowired
    private DataPathProperties dataPathProperties;
    @Autowired
    private DownloaderMapper downloaderMapper;
    @Autowired
    private ItemsMapper itemsMapper;
    @Autowired
    private SubMapper subMapper;
    @Autowired
    private Gson gson;
    @Autowired
    private Alist alist;
    public static List<Items> reUploadItems = new ArrayList<>();


    /**
     * 获取下载器信息
     * @return
     */
    @ApiOperation("获取下载器信息")
    @GetMapping("/info")
    public Result<List<DownloaderInfoVO>> downloaderInfo(){
        List<DownloaderInfoVO> downloaderInfoVOList = new ArrayList<>();
        for (Downloader downloader : downloaderMapper.list()) {
            DownloaderInfoVO downloaderInfoVO = new DownloaderInfoVO();
            BeanUtils.copyProperties(downloader,downloaderInfoVO);
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(downloader.getUpdateTime()), ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            downloaderInfoVO.setUpdateTime(dateTime.format(formatter));
            downloaderInfoVOList.add(downloaderInfoVO);
        }
        return Result.success(downloaderInfoVOList);
    }

    /**
     * 获取上传&下载完成的信息
     * @return
     */
    public Result<List<DownloadCompletedVO>> downloadCompleted(){
        //1.查询items表
        List<Items> itemsList = itemsMapper.list();
        itemsList = itemsList.stream().filter(new Predicate<Items>() {
            @Override
            public boolean test(Items items) {
                return !items.getStatus().equals(Context.DOWNLOADING);
            }
        }).collect(Collectors.toList());

        List<DownloadCompletedVO> downloadCompletedVOList = new ArrayList<>();
        for (Items item : itemsList) {
            Map map = new HashMap();
            map.put("uuid",item.getChannelUuid());
            Sub sub = subMapper.selectByMap(map);
            if (sub == null){
                //删除没有记录的
                itemsMapper.deleteByChannelUuid(item.getChannelUuid());
                throw new ItemNotFoundException(MessageConstant.ITEMS_NOT_FOUND_FAILED);
            }
            String itemName = item.getTitle();
            //节目标题只展示30个字符
            if (itemName.length() > 30){
               itemName = itemName.substring(0,30) + "...";
            }
            DownloadCompletedVO build = DownloadCompletedVO.builder()
                    .downloadTimeLeft(TimeFormat.duration(item.getDownloadTimeLeft().intValue()))
                    .channelUuid(item.getChannelUuid())
                    .uuid(item.getUuid())
                    .downloadProgress(item.getDownloadProgress())
                    .downloadSpeed(DownloaderUtils.byteToMB(item.getDownloadSpeed()) + Unit.MB_BY_SECOUND)
                    .channelName(sub.getTitle())
                    .status(item.getStatus())
                    .itemName(itemName)
                    .build();
            downloadCompletedVOList.add(build);
        }

        //倒序
        Collections.reverse(downloadCompletedVOList);

        return Result.success(downloadCompletedVOList);
    }

    /**
     * 获取下载和上传完成的信息
     * @return
     */
    @ApiOperation("获取完成下载和上传的信息")
    @GetMapping("/completed")
    public Result<List<DownloadCompletedVO>> downloadDone(){
        return Result.success(downloadCompleted().getData().stream().filter(new Predicate<DownloadCompletedVO>() {
            @Override
            public boolean test(DownloadCompletedVO downloadCompletedVO) {
                return downloadCompletedVO.getStatus().equals(Context.COMPLETED) || downloadCompletedVO.getStatus()
                        .equals(Context.ALIST_UPLOAD_SUCCESS);
            }
        }).collect(Collectors.toList()));
    }

    /**
     * 获取下载和上传错误的信息
     * @return
     */
    @ApiOperation("获取上传和下载错误的信息")
    @GetMapping("/error")
    public Result<List<DownloadCompletedVO>> downloadError(){
     return Result.success(downloadCompleted().getData().stream().filter(new Predicate<DownloadCompletedVO>() {
         @Override
         public boolean test(DownloadCompletedVO downloadCompletedVO) {
             return !downloadCompletedVO.getStatus().equals(Context.COMPLETED) && !downloadCompletedVO.getStatus()
                     .equals(Context.ALIST_UPLOAD_SUCCESS);
         }
     }).collect(Collectors.toList()));
    }

    /**
     * 获取下载详细信息
     * @return
     */
    @ApiOperation("获取上传和下载详细信息")
    @GetMapping("/detail/{uuid}")
    public Result<DownloadDetailVO> detail(@PathVariable String uuid, HttpServletRequest request){
        Map map = new HashMap<>();
        map.put("uuid",uuid);
        Items items = itemsMapper.selectByUuid(uuid);
        if (items != null){
            Sub sub = subMapper.selectByUuid(items.getChannelUuid());
            Downloader downloader = downloaderMapper.selectByName(items.getDownloader());
            LocalDateTime createTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(items.getCreateTime()), ZoneId.systemDefault());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + "/sub/xml/" + sub.getUuid();
            DownloadDetailVO detailVO = DownloadDetailVO.builder()
                    .downloaderName(items.getDownloader())
                    .downloaderVersion(downloader == null?null:downloader.getVersion())
                    .fileName(items.getFileName())
                    .channelName(sub.getTitle())
                    .status(DownloaderUtils.downloadStatusDescription(items.getStatus()))
                    .itemTitle(items.getTitle())
                    .duration(TimeFormat.duration(items.getDuration()))
                    .itemLink(items.getLink())
                    .uuid(items.getUuid())
                    .channelUuid(items.getChannelUuid())
                    .subLink(url)
                    .createTime(createTime.format(dateTimeFormatter))
                    .build();
            return Result.success(detailVO);
        }else {
            throw new ItemNotFoundException(MessageConstant.ITEMS_NOT_FOUND_FAILED);
        }
    }

    /**
     * 重新下载和上传
     * @param uuid
     * @return
     */
    @ApiOperation("重新下载和上传")
    @GetMapping("/reDownload/{uuid}")
    public Result reDownload(@PathVariable String uuid){
        log.info("重新下载:{}",uuid);
        Items items = itemsMapper.selectByUuid(uuid);
        if (items == null){
            throw new ItemNotFoundException(MessageConstant.ITEMS_NOT_FOUND_FAILED);
        }
        //重新上传
        if (DownloaderUtils.aListErrStatusCode().contains(items.getStatus())){
            //加入上传列表任务
            reUploadItems.add(items);
            log.info("加入重新上传列表：{}",items.getFileName());
            return Result.success();
        }
        //重新下载
        else {
            //先把之前的删除掉，不然在格式转换时出现错误
            List<File> before = Arrays.stream(new File(dataPathProperties.getResourcesPath()).listFiles())
                    .filter(file -> file.getName().contains(items.getUuid())).collect(Collectors.toList());
            try {
                for (File file : before) {
                    log.info("删除文件:{}",file.getName());
                    FileUtils.forceDelete(file);
                }
            } catch (Exception e) {
                throw new ItemNotFoundException(MessageConstant.ITEMS_RESOURCE_DELETE_FAILED);

            }
            Map args = gson.fromJson(items.getArgs(), Map.class);
            List<String> links = gson.fromJson(items.getLinks(),new TypeToken<List<String>>() {}.getType());

            //解析枚举
            Type type = Type.valueOf(items.getType());
            DownloadManager.Downloader downloader = DownloadManager.Downloader.valueOf(items.getDownloader());
            Operation operation = Operation.valueOf(items.getOperation());

            //构建下载请求
            Request build = Request.builder()
                    .links(links)
                    .type(type)
                    .operation(operation)
                    .downloader(downloader)
                    .args(args)
                    .dir(new File(dataPathProperties.getResourcesPath()))
                    .channelUuid(items.getChannelUuid())
                    .uuid(items.getUuid())
                    .build();
            Thread thread = new Thread(new ReDownload(build,itemsMapper,subMapper));
            thread.start();
        }
        return Result.success();
    }

    /**
     * 删除下载节目
     * @param uuids
     * @return
     */
    @ApiOperation("删除下载节目")
    @DeleteMapping
    public Result delete(@RequestParam List<String> uuids){
        log.info("删除下载节目:{}",uuids);
        for (String uuid : uuids) {
            Items items = itemsMapper.selectByUuid(uuid);
            if (items != null && DownloaderUtils.endStatusCode().contains(items.getStatus())){
                if (DownloaderUtils.aListStatusCode().contains(items.getStatus())){
                    //删除AList中文件
                    try {
                        alist.deleteFile(items.getFileName());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }else {
                    //删除本地文件，如果有的话
                    List<File> deleteFiles = Arrays.stream(new File(dataPathProperties.getResourcesPath()).listFiles())
                            .filter(file -> file.getName().contains(items.getUuid())).collect(Collectors.toList());
                    for (File file : deleteFiles) {
                        try {
                            FileUtils.forceDelete(file);
                        } catch (Exception e) {
                            throw  new DeleteException(MessageConstant.ITEMS_RESOURCE_DELETE_FAILED);
                        }
                    }
                }
                //删除数据库记录
                itemsMapper.deleteByUuid(uuid);
            }
        }
        return Result.success();
    }

    /**
     * 移除正在下载的
     * @param uuids
     * @return
     */
    @ApiOperation("移除正在下载的")
    @DeleteMapping("/downloading")
    public Result removeDownload(@RequestParam List<String> uuids){
        for (String uuid : uuids) {
            try {
                for (DownloadManager dm : Task.downloadManagerList) {
                    dm.killByUuid(uuid);
                }
            } catch (Exception e) {
                throw new RuntimeException(uuid+ "移除下载失败:" + e.getMessage());
            }
        }
        return Result.success();
    }
}
