package io.github.yajuhua.podcast2.controller;

import com.google.gson.Gson;
import io.github.yajuhua.download.commons.Context;
import io.github.yajuhua.download.manager.DownloadManager;
import io.github.yajuhua.podcast2.alist.Alist;
import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.constant.ReflectionMethodName;
import io.github.yajuhua.podcast2.common.constant.StatusCode;
import io.github.yajuhua.podcast2.common.exception.BaseException;
import io.github.yajuhua.podcast2.common.exception.SubNotFoundException;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.common.utils.DownloaderUtils;
import io.github.yajuhua.podcast2.common.utils.ExtendListUtil;
import io.github.yajuhua.podcast2.common.utils.Http;
import io.github.yajuhua.podcast2.common.utils.PluginLoader;
import io.github.yajuhua.podcast2.mapper.*;
import io.github.yajuhua.podcast2.pojo.dto.AddSubDTO;
import io.github.yajuhua.podcast2.pojo.dto.GetExtendListDTO;
import io.github.yajuhua.podcast2.pojo.dto.GithubActionWorkflowsDTO;
import io.github.yajuhua.podcast2.pojo.entity.*;
import io.github.yajuhua.podcast2.pojo.vo.EditSubVO;
import io.github.yajuhua.podcast2.pojo.vo.ExtendListVO;
import io.github.yajuhua.podcast2.pojo.vo.SubDetailVO;
import io.github.yajuhua.podcast2.pojo.vo.SubVO;
import io.github.yajuhua.podcast2.service.ExtendService;
import io.github.yajuhua.podcast2.service.ItemsService;
import io.github.yajuhua.podcast2.service.SubService;
import io.github.yajuhua.podcast2.service.UserService;
import io.github.yajuhua.podcast2.task.Task;
import io.github.yajuhua.podcast2API.Channel;
import io.github.yajuhua.podcast2API.Item;
import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.extension.build.ExtendList;
import io.github.yajuhua.podcast2API.extension.build.Input;
import io.github.yajuhua.podcast2API.extension.build.Select;
import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import io.github.yajuhua.podcast2API.setting.Setting;
import io.github.yajuhua.podcast2API.utils.TimeFormat;
import io.github.yajuhua.podcast2API.utils.Xml;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/sub")
@Api(tags = "订阅相关接口")
public class SubController {

    @Autowired
    private Gson gson;
    @Autowired
    private SubMapper subMapper;
    @Autowired
    private SubService subService;
    @Autowired
    private ExtendMapper extendMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ItemsService itemsService;
    @Autowired
    private ItemsMapper itemsMapper;
    @Autowired
    private DataPathProperties dataPathProperties;
    @Autowired
    private SettingsMapper settingsMapper;
    @Autowired
    private ExtendService extendService;
    @Autowired
    private PluginMapper pluginMapper;
    @Autowired
    private Alist alist;
    @Autowired
    private UserService userService;


    /**
     * 获取订阅列表
     * @return
     */
    @ApiOperation("获取订阅列表")
    @GetMapping("/list")
    public Result<List<SubVO>> list(){
        List<Sub> list = subService.list();
        list = list.stream().sorted(new Comparator<Sub>() {
            @Override
            public int compare(Sub o1, Sub o2) {
                return Long.compare(o2.getUpdateTime(),o1.getUpdateTime());
            }
        }).collect(Collectors.toList());

        List<SubVO> subVOList = new ArrayList<>();
        for (Sub channel : list) {
            subVOList.add(new SubVO(TimeFormat.formatDate(channel.getUpdateTime()/1000)
                    ,channel.getTitle(),channel.getUuid()));
        }
        return Result.success(subVOList);
    }

    /**
     * 搜索订阅
     * @param keywords
     * @return
     */
    @ApiOperation("搜索订阅")
    @GetMapping("/search")
    public Result<List<SubVO>> search(String keywords){
        List<Sub> list = subService.list();

        //关键词过滤
        list = list.stream().filter(new Predicate<Sub>() {
            @Override
            public boolean test(Sub sub) {
                return sub.getTitle().contains(keywords) || sub.getDescription().contains(keywords)
                        || sub.getPlugin().contains(keywords) || sub.getLink().contains(keywords)
                        || sub.getUuid().contains(keywords);
            }
        }).sorted(new Comparator<Sub>() {
            @Override
            public int compare(Sub o1, Sub o2) {
                return Long.compare(o2.getUpdateTime(), o1.getUpdateTime());
            }
        }).collect(Collectors.toList());

        //封装
        List<SubVO> subVOList = new ArrayList<>();
        for (Sub channel : list) {
            subVOList.add(new SubVO(TimeFormat.formatDate(channel.getUpdateTime()/1000)
                    ,channel.getTitle(),channel.getUuid()));
        }
        return Result.success(subVOList);
    }

    /**
     * 根据uuid获取订阅xml
     * @param uuid
     * @return
     */
    @ApiOperation("根据uuid获取订阅xml")
    @GetMapping(value = "xml/{uuid}", produces = {MediaType.APPLICATION_XML_VALUE})
    public String xml(@PathVariable String uuid, HttpServletRequest request){
        Sub sub = subService.selectByUuid(uuid);
        if (sub == null){
            throw new SubNotFoundException(MessageConstant.SUB_NOT_FOUND_FAILED + ":" + uuid);
        }
        User user = userMapper.list().get(0);
        String enclosureDomain = user.getHostname()==null || user.getHostname().contains(" ") || user.getHostname().length() == 0?null:user.getHostname();
        enclosureDomain = enclosureDomain==null? request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort():enclosureDomain;
        Channel channel = new Channel();
        BeanUtils.copyProperties(sub,channel);
        List<Items> items = itemsService.selectByChannelUuid(uuid);
        List<Item> itemList = new ArrayList<>();
        Integer status;
        String url;
        AlistInfo alistInfo = userService.getExtendInfo().getAlistInfo();
        for (Items item : items) {
            try {
                status = item.getStatus();
                if (status == Context.COMPLETED || status == Context.ALIST_UPLOAD_SUCCESS || status == Context.ALIST_UPLOAD_OUT_OF_MEMORY){
                    Item item1 = new Item();
                    url = enclosureDomain + "/resources/" + item.getFileName();
                    if (status == Context.ALIST_UPLOAD_SUCCESS && alistInfo.isOpen()){
                        //上传完成的
                        url = enclosureDomain + "/resources/alist/" + item.getFileName();
                    }
                    item1.setEnclosureType(item.getType().toLowerCase() + "/" + item.getFormat());
                    BeanUtils.copyProperties(item,item1);
                    item1.setEnclosure(url);
                    itemList.add(item1);
                }
            } catch (BeansException e) {
                log.error("节目信息异常：{} - 名称：{}",e.getMessage(),item.getTitle());
            }
        }

        //服务是否异常
        List<String> uuids = new ArrayList<>();
        uuids.add(uuid);
        List<Item> items1 = serviceStatus(enclosureDomain, user,uuids);
        if (items1 != null && !items1.isEmpty()){
            itemList.addAll(items1);
        }

        return Xml.build(channel,itemList);
    }

    /**
     * 组订阅
     * @param uuids group
     * @return
     */
    @ApiOperation("获取组xml")
    @GetMapping(value = "/xml", produces = {MediaType.APPLICATION_XML_VALUE})
    public String groupXml(@RequestParam("uuids") List<String> uuids, @RequestParam("group") String group, HttpServletRequest request){

        String requestURL = request.getRequestURL() + "?" + request.getQueryString();

        if (uuids != null && !uuids.isEmpty() && group != null){

            List<Sub> subList = subMapper.list();
            List<String> subUuid = new ArrayList<>();

            for (Sub sub : subList) {
                subUuid.add(sub.getUuid());
            }

            //只要有一个单独订阅找不到就抛异常
            boolean isContainAll = subUuid.containsAll(uuids);
            if (!isContainAll){
                throw new SubNotFoundException(MessageConstant.SUB_NOT_FOUND_FAILED + ":" + uuids);
            }

            //获取附件域名
            User user = userMapper.list().get(0);
            String enclosureDomain = user.getHostname()==null || user.getHostname().contains(" ") || user.getHostname().length() == 0?null:user.getHostname();
            enclosureDomain = enclosureDomain==null? request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort():enclosureDomain;

            //生成一个封面https://img.shields.io/badge/-组名-颜色
            // 将字节数组中的每个字节转换为十六进制表示
            StringBuilder hexString = new StringBuilder();
            for (byte b : group.getBytes(StandardCharsets.UTF_8)) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    // 如果转换后的十六进制表示只有一位，则在前面补0
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String color = "";
            if (hexString.length() < 6){
                hexString.reverse();
            }
            while (hexString.length() < 6){
                hexString.append(0);
            }
            if (hexString.length() > 6){
                color = hexString.substring(0,4) + hexString.substring(hexString.length()-2,hexString.length());
            }

            String imageUrl = "https://img.shields.io/badge/-" + group + "-" + color + ".png";
            //组频道信息
            Channel groupChannel = new Channel();
            groupChannel.setTitle(group);
            groupChannel.setDescription(group);
            groupChannel.setLink(requestURL);
            groupChannel.setImage(imageUrl);

            //聚合节目
            List<Items> groupItems = new ArrayList<>();
            for (String uuid : uuids) {
                groupItems.addAll(itemsMapper.selectByChannelUUid(uuid));
            }

            //构建附件URL
            List<Item> itemList = new ArrayList<>();
            Integer status;
            String url;
            AlistInfo alistInfo = userService.getExtendInfo().getAlistInfo();
            for (Items item : groupItems) {
                status = item.getStatus();
                if (status == Context.COMPLETED || status == Context.ALIST_UPLOAD_SUCCESS){
                    Item item1 = new Item();
                    url = enclosureDomain + "/resources/" + item.getFileName();
                    if (status == Context.ALIST_UPLOAD_SUCCESS && alistInfo.isOpen()){
                        //上传完成的
                        url = enclosureDomain + "/resources/alist/" + item.getFileName();
                    }
                    item1.setEnclosureType(item.getType().toLowerCase() + "/" + item.getFormat());
                    BeanUtils.copyProperties(item,item1);
                    item1.setEnclosure(url);
                    itemList.add(item1);
                }
            }

            //服务是否异常
            List<Item> items = serviceStatus(enclosureDomain, user,uuids);
            if (items != null && !items.isEmpty()){
                itemList.addAll(items);
            }

            return Xml.build(groupChannel,itemList);

        }else {
            throw new SubNotFoundException( "请求参数不全:" + requestURL);
        }
    }

    /**
     * 获取服务状态
     * @return
     */
    public List<Item> serviceStatus(String enclosureDomain,User user,List<String> subUuids) {
        List<Sub> subList = subMapper.list();
        int subErrorSize = subList.stream().filter(new Predicate<Sub>() {
            @Override
            public boolean test(Sub sub) {
                Long cron = sub.getCron() * 1000;
                Integer isUpdate = sub.getIsUpdate();
                Long checkTime = sub.getCheckTime();
                Integer isFirst = sub.getIsFirst();
                //这个应该随订阅数量来定,每个订阅增加10分钟
                long numberTime = TimeUnit.MINUTES.toMillis(10) * subList.size();
                return System.currentTimeMillis() - checkTime > numberTime + cron + TimeUnit.MINUTES.toMillis(60) && isUpdate == 1 && isFirst != 1;
            }
        }).collect(Collectors.toList()).size();

        //下载错误的
        List<Items> itemsDownloadError = itemsMapper.list().stream().filter(new Predicate<Items>() {
            @Override
            public boolean test(Items items) {
                Integer status = items.getStatus();
                return DownloaderUtils.errorStatusCode().contains(status) && subUuids.contains(items.getChannelUuid());
            }
        }).collect(Collectors.toList());

        //上传错误的
        List<Items> itemsUploadError = itemsMapper.list().stream().filter(new Predicate<Items>() {
            @Override
            public boolean test(Items items) {
                Integer status = items.getStatus();
                return DownloaderUtils.aListErrStatusCode().contains(status) && subUuids.contains(items.getChannelUuid());
            }
        }).collect(Collectors.toList());

        List<Item> serveStatusItems = new ArrayList<>();
        Item item;

        if (subErrorSize != 0) {
            item = new Item();
            item.setTitle("服务异常");
            item.setDescription("订阅超过一个小时未检查更新,详细情况请查看日志");
            item.setDuration(10);
            item.setCreateTime(System.currentTimeMillis());
            item.setLink("https://github.com/yajuhua/podcast2");
            item.setImage("https://yajuhua.github.io/images/975x975-logo.png");
            item.setEnclosure("https://yajuhua.github.io/resources/error.mp3");
            item.setEnclosureType("audio/mp3");
            serveStatusItems.add(item);
        }
        if (!itemsDownloadError.isEmpty()) {
            //将节目标题拼接到描述区
            StringBuilder desc = new StringBuilder("节目标题：");
            for (Items items : itemsDownloadError) {
                desc.append(items.getTitle() + "\n");
            }
            item = new Item();
            item.setTitle("节目下载错误，详细情况请查看服务。");
            item.setDescription(desc.toString());
            item.setDuration(10);
            item.setCreateTime(System.currentTimeMillis());
            item.setLink("https://github.com/yajuhua/podcast2");
            item.setImage("https://yajuhua.github.io/images/975x975-logo.png");
            item.setEnclosure("https://yajuhua.github.io/resources/error.mp3");
            item.setEnclosureType("audio/mp3");
            serveStatusItems.add(item);
        }

        if (!itemsUploadError.isEmpty()) {
            //将节目标题拼接到描述区
            StringBuilder desc = new StringBuilder("节目标题：");
            for (Items items : itemsDownloadError) {
                desc.append(items.getTitle() + "\n");
            }
            item = new Item();
            item.setTitle("节目上传错误，详细情况请查看服务。");
            item.setDescription(desc.toString());
            item.setDuration(10);
            item.setCreateTime(System.currentTimeMillis());
            item.setLink("https://github.com/yajuhua/podcast2");
            item.setImage("https://yajuhua.github.io/images/975x975-logo.png");
            item.setEnclosure("https://yajuhua.github.io/resources/error.mp3");
            item.setEnclosureType("audio/mp3");
            serveStatusItems.add(item);
        }

        List<GithubActionWorkflowsDTO> actionWorkflowsDTOList = Task.actionWorkflowsDTOList;
        //获取订阅插件域名
        List<String> pluginDomains = subMapper.list().stream().filter(new Predicate<Sub>() {
            @Override
            public boolean test(Sub sub) {
                return subUuids.contains(sub.getUuid());
            }
        }).map(Sub::getPlugin).collect(Collectors.toList());

        if (!actionWorkflowsDTOList.isEmpty()){
            List<GithubActionWorkflowsDTO> collect = actionWorkflowsDTOList.stream().filter(new Predicate<GithubActionWorkflowsDTO>() {
                @Override
                public boolean test(GithubActionWorkflowsDTO githubActionWorkflowsDTO) {
                    GithubActionWorkflowsDTO.WorkflowRunsDTO workflowRunsDTO = githubActionWorkflowsDTO.getWorkflowRuns().get(0);
                    for (String domain : pluginDomains) {
                        if (domain.contains(workflowRunsDTO.getName())) {
                            return true;
                        }
                    }
                    return false;
                }
            }).collect(Collectors.toList());

            //格式化时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            LocalDateTime localDateTime = null;
            //简单描述
            StringBuilder desc = new StringBuilder("插件状态由Github Action检查。\n");

            int descLength = desc.length();
            for (GithubActionWorkflowsDTO workflowsDTO : collect) {
                GithubActionWorkflowsDTO.WorkflowRunsDTO workflowRunsDTO = workflowsDTO.getWorkflowRuns().get(0);
                String status = workflowRunsDTO.getStatus();
                String conclusion = workflowRunsDTO.getConclusion();
                if ("completed".equals(status) && "failure".equals(conclusion)){
                    localDateTime = LocalDateTime.parse(workflowRunsDTO.getCreatedAt(), formatter).atZone(Clock.systemUTC().getZone()).
                            withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                    desc.append("插件名称：" + workflowRunsDTO.getName() + "\n");
                    desc.append("检查时间：" + localDateTime + "\n");
                    desc.append("详细内容：" + workflowRunsDTO.getUrl() + "\n\n\n");
                }
            }

            //如果描述长度不变，可能是没有获取到状态
            if (desc.length() > descLength) {
                item = new Item();
                item.setTitle("插件状态异常");
                item.setDescription(desc.toString());
                item.setDuration(10);
                item.setCreateTime(System.currentTimeMillis());
                item.setLink("https://github.com/yajuhua/podcast2");
                item.setImage("https://yajuhua.github.io/images/975x975-logo.png");
                item.setEnclosure("https://yajuhua.github.io/resources/error.mp3");
                serveStatusItems.add(item);
            }
        }
        return serveStatusItems;
    }


    /**
     * 删除订阅
     * @param uuids
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除订阅")
    public Result delete(@RequestParam List<String> uuids) throws Exception {
        log.info("delete uuids:{}",uuids);
        for (String uuid : uuids) {
            //删除订阅资源线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //结束下载如果有的话
                        for (DownloadManager dm : Task.downloadManagerList) {
                            //dm.killByChannelUuid(uuid);
                            //更新是单线程,在同一时间只有一个订阅在更新下载
                            dm.killAll();
                            Task.getDownloadProgressVOSet().clear();
                        }

                        //删除相关资源
                        List<Items> itemsList = itemsService.selectByChannelUuid(uuid);
                        for (Items items : itemsList) {
                            //本地还是AList
                            if (items.getStatus() == Context.COMPLETED){
                                //本地
                                //删除文件
                                File[] files = new File(dataPathProperties.getResourcesPath()).listFiles();
                                for (File file : files) {
                                    if (file.getName().contains(items.getUuid())){
                                        log.info("删除本地文件:{}",file.getName());
                                        try {
                                            FileUtils.forceDelete(file);
                                        } catch (IOException e) {
                                            log.error("删除本地文件失败:{}",e.getMessage());
                                        }
                                    }
                                }
                            }
                            else if (items.getStatus() == Context.ALIST_UPLOAD_SUCCESS){
                                //删除AList端文件
                                log.info("删除AList端文件：{}",items.getFileName());
                                try {
                                    alist.deleteFile(items.getFileName());
                                } catch (Exception e) {
                                    log.error("删除AList端文件错误：{}",e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("删除订阅资源异常：{}",e.getMessage());
                    }
                }
            }).start();
            //删除sub订阅
            subMapper.deleteByUuid(uuid);
            //删除items
            itemsMapper.deleteByChannelUuid(uuid);
            //删除extend数据库记录
            extendMapper.deleteByUuid(uuid);
        }
        return Result.success();
    }

    /**
     * 添加订阅
     * @return
     */
    @ApiOperation("添加订阅")
    @PostMapping("/add")
    public Result add(@RequestBody AddSubDTO addSubDTO){
        try {
            //构建插件参数
            Params params = new Params();
            params.setUrl(addSubDTO.getUrl());
            List<Settings> settingsFromDB = settingsMapper.selectByPluginName(Http.getSecondLevelDomain(addSubDTO.getPlugin()));
            List<Setting> settings = new ArrayList<>();
            for (Settings dbSetting : settingsFromDB) {
                Setting setting = new Setting();
                BeanUtils.copyProperties(dbSetting,setting);
                settings.add(setting);
            }
            params.setSettings(settings);

            //获取插件Channel数据
            Class aClass = PluginLoader.selectByName(Http.getSecondLevelDomain(addSubDTO.getPlugin()), dataPathProperties).get(0);
            Constructor constructor = aClass.getConstructor(String.class);
            Object o = constructor.newInstance(gson.toJson(params));
            String channelStr = gson.toJson(aClass.getMethod(ReflectionMethodName.CHANNEL).invoke(o));
            Channel channel = gson.fromJson(channelStr, Channel.class);

            log.info("addSubDTO:{}",addSubDTO);
            String descKeywords = String.join(",", addSubDTO.getDescKeywords());
            String titleKeywords = String.join(",", addSubDTO.getTitleKeywords());
            String uuid = UUID.randomUUID().toString();
            Sub sub =  Sub.builder()
                    .description(channel.getDescription())
                    .equal("none")
                    .image(channel.getImage())
                    .title(channel.getTitle())
                    .cron(addSubDTO.getCron())
                    .episodes(addSubDTO.getEpisodes())
                    .customEpisodes(addSubDTO.getCustomEpisodes())
                    .isFilter(addSubDTO.getIsFilter())
                    .link(addSubDTO.getUrl())
                    .isUpdate(addSubDTO.getIsUpdate())
                    .isFirst(StatusCode.YES)
                    .isExtend(addSubDTO.getIsExtend())
                    .plugin(Http.getSecondLevelDomain(addSubDTO.getPlugin()))
                    .maxDuration(addSubDTO.getMaxDuration())
                    .minDuration(addSubDTO.getMinDuration())
                    .createTime(System.currentTimeMillis())
                    .status(addSubDTO.getStatus())
                    .survivalTime(addSubDTO.getSurvivalTime())
                    .type(addSubDTO.getType())
                    .descKeywords(descKeywords)
                    .titleKeywords(titleKeywords)
                    .uuid(uuid)
                    .updateTime(System.currentTimeMillis())
                    .checkTime(0L)
                    .build();

            subService.addSub(sub);
            List<Extend> extendList = extendList(addSubDTO.getInputAndSelectDataList(), Http.getSecondLevelDomain(addSubDTO.getPlugin())
                    , uuid, addSubDTO.getIsExtend(),addSubDTO.getUrl(),addSubDTO.getType());
            //将扩展选项写入数据库
            extendService.batchExtend(extendList);

            PluginLoader.close(aClass);
            //添加插件信息
            return Result.success();
        }catch (InvocationTargetException e){
            //如果是通过反射 API 获取的异常类型
            Throwable targetException = e.getTargetException();
            throw new BaseException(targetException.getMessage());
        }catch (Exception e) {
            throw new BaseException(e.getMessage());
        }finally {
            Task.addSubStatus = false;
        }
    }

    /**
     * 获取插件扩展
     * @param getExtendListDTO
     * @return
     */
    @GetMapping("/extendList")
    @ApiOperation("获取插件扩展")
    public Result<ExtendListVO> extendList(GetExtendListDTO getExtendListDTO) throws Exception {
        log.info("getExtendListDTO:{}",getExtendListDTO);
        Params params = new Params();
        params.setUrl(getExtendListDTO.getUrl());

        //获取插件信息
        Class aClass = PluginLoader.selectByName(Http.getSecondLevelDomain(getExtendListDTO.getPlugin()), dataPathProperties).get(0);
        Constructor constructor = aClass.getConstructor(String.class);
        Object o = constructor.newInstance(gson.toJson(params));
        String extendListStr = gson.toJson(aClass.getMethod(ReflectionMethodName.GET_EXTENSION).invoke(o));
        ExtendList extendList = gson.fromJson(extendListStr, ExtendList.class);

        PluginLoader.close(aClass);
        return Result.success(ExtendListUtil.buildExtendListVO(extendList));
    }

    /**
     * 提交编辑订阅
     * @return
     */
    @ApiOperation("提交编辑订阅")
    @PutMapping
    public Result editSub(@RequestBody EditSubVO editSubVO) throws Exception{
        log.info("editSubVO:{}",editSubVO);
        //1.更新sub表
        Sub sub = new Sub();
        BeanUtils.copyProperties(editSubVO,sub);
        String titleKeywords = String.join(",", editSubVO.getTitleKeywords());
        String descKeywords = String.join(",", editSubVO.getDescKeywords());
        sub.setTitleKeywords(titleKeywords);
        sub.setDescription(descKeywords);
        log.info("sub:{}",sub);
        subMapper.update(sub);
        //2.更新extend表
        //清空之前的扩展
        extendMapper.deleteByUuid(sub.getUuid());
        //添加新的的扩展选项
        List<InputAndSelectData> inputAndSelectDataList = new ArrayList<>();
        inputAndSelectDataList.addAll(editSubVO.getInputListData());
        inputAndSelectDataList.addAll(editSubVO.getSelectListData());
        //获取sub表的plugin
        Sub beforeSub = subService.selectByUuid(editSubVO.getUuid());
        extendService.batchExtend(extendList(inputAndSelectDataList,beforeSub.getPlugin(),sub.getUuid(),
                sub.getIsExtend(),sub.getLink(),sub.getType()));
        return Result.success();
    }

    /**
     * 获取编辑订阅信息
     * @return
     */
    @ApiOperation("获取编辑订阅信息")
    @GetMapping("/edit/{uuid}")
    public Result<EditSubVO> getEditSubInfo(@PathVariable String uuid)throws Exception{
        //1.获取sub
        Sub sub = subService.selectByUuid(uuid);
        EditSubVO editSubVO = new EditSubVO();
        BeanUtils.copyProperties(sub,editSubVO);

        //2.将titleKeywords和descKeywords字符串转换成List集合
        List<String> titleKeywords = new ArrayList<>();
        if (sub.getTitleKeywords() != null){
            titleKeywords = Arrays.asList(sub.getTitleKeywords().split(","));
        }
        List<String> descKeywords = new ArrayList<>();
        if (sub.getDescKeywords() != null){
           titleKeywords = Arrays.asList(sub.getDescKeywords().split(","));
        }

        //3.获取插件的扩展选项
        GetExtendListDTO getExtendListDTO = new GetExtendListDTO();
        BeanUtils.copyProperties(sub,getExtendListDTO);
        getExtendListDTO.setUrl(sub.getLink());
        ExtendListVO extendListVO = extendList(getExtendListDTO).getData();
        editSubVO.setExtendList(extendListVO.getExtendList());
        editSubVO.setInputListData(extendListVO.getInputListData());
        editSubVO.setSelectListData(extendListVO.getSelectListData());

        //4.获取用户插件扩展选项数据
        List<Extend> anExtends = extendMapper.selectByUuid(uuid);
        List<InputAndSelectData> inputListData = new ArrayList<>();
        List<InputAndSelectData> selectListData = new ArrayList<>();

        //4.将selectList和inputList封装成VO
        for (Select select : extendListVO.getExtendList().getSelectList()) {
            for (Extend extend : anExtends) {
                if (extend.getName().equals(select.getName())){
                    selectListData.add(new InputAndSelectData(extend.getName(),extend.getContent()));
                    break;
                }
            }
        }

        //避免传入null
        if (extendListVO.getExtendList().getInputList() == null){
            List<Input> inputList = new ArrayList<>();
            extendListVO.getExtendList().setInputList(inputList);
        }

        if (extendListVO.getExtendList().getSelectList() == null){
            List<Select> selectList = new ArrayList<>();
            extendListVO.getExtendList().setSelectList(selectList);
        }

        for (Input input : extendListVO.getExtendList().getInputList()) {
            for (Extend extend : anExtends) {
                if (extend.getName().equals(input.getName())){
                    inputListData.add(new InputAndSelectData(extend.getName(),extend.getContent()));
                    break;
                }
            }
        }

        //设置属性
        editSubVO.setTitleKeywords(titleKeywords);
        editSubVO.setDescKeywords(descKeywords);
        editSubVO.setSelectListData(selectListData);
        editSubVO.setInputListData(inputListData);
        editSubVO.setUuid(uuid);

        //返回VO
        return Result.success(editSubVO);
    }

    /**
     * 用于将扩展数据写入数据库
     * @param inputAndSelectDataList
     * @param plugin
     * @param uuid
     * @param isExtend
     * @return
     */
    private List<Extend> extendList(List<InputAndSelectData> inputAndSelectDataList,String plugin,
                                    String uuid,Integer isExtend,String url,String type) throws Exception{
        List<Extend> extendList = new ArrayList<>();
        GetExtendListDTO getExtendListDTO = new GetExtendListDTO();
        getExtendListDTO.setUrl(url);
        getExtendListDTO.setPlugin(plugin);
        getExtendListDTO.setType(type);

        //如果数据长度为0，或扩展关闭
        if (inputAndSelectDataList.size() == 0 || isExtend == 0){
            ExtendList extendList1 = extendList(getExtendListDTO).getData().getExtendList();
            for (Input input : extendList1.getInputList()) {
                Extend extend = new Extend();
                extend.setName(input.getName());
                extend.setChannelUuid(uuid);
                extend.setPlugin(plugin);
                extendList.add(extend);
            }

            for (Select select : extendList1.getSelectList()) {
                Extend extend = new Extend();
                extend.setName(select.getName());
                extend.setPlugin(plugin);
                extend.setChannelUuid(uuid);
                extendList.add(extend);
            }
        }else {
            for (InputAndSelectData inputAndSelectData : inputAndSelectDataList) {
                Extend extend = new Extend();
                BeanUtils.copyProperties(inputAndSelectData,extend);
                extend.setPlugin(plugin);
                extend.setChannelUuid(uuid);
                extendList.add(extend);
            }
        }

        return extendList;

    }

    /**
     * 获取订阅详细信息
     * @param uuid
     * @return
     */
    @ApiOperation("获取订阅详细信息")
    @GetMapping("/detail/{uuid}")
    public Result<SubDetailVO> subDetail(@PathVariable String uuid){
        Sub sub = subMapper.selectByUuid(uuid);
        SubDetailVO subDetailVO = new SubDetailVO();
        if (sub != null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String updateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(sub.getUpdateTime()), ZoneId.systemDefault()).format(formatter);
            String checkTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(sub.getCheckTime()), ZoneId.systemDefault()).format(formatter);
            String createTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(sub.getCreateTime()), ZoneId.systemDefault()).format(formatter);
            BeanUtils.copyProperties(sub,subDetailVO);
            subDetailVO.setUpdateTime(updateTime);
            subDetailVO.setCheckTime(checkTime);
            subDetailVO.setCreateTime(createTime);

            return Result.success(subDetailVO);
        }
        return Result.error("找不到该订阅");
    }
}
