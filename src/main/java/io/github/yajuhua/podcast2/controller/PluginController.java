package io.github.yajuhua.podcast2.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.yajuhua.download.commons.utils.CommonUtils;
import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.constant.PluginPropertiesKey;
import io.github.yajuhua.podcast2.common.constant.ReflectionMethodName;
import io.github.yajuhua.podcast2.common.exception.BaseException;
import io.github.yajuhua.podcast2.common.exception.PluginNotFoundException;
import io.github.yajuhua.podcast2.common.exception.PluginOccupancyException;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.properties.InfoProperties;
import io.github.yajuhua.podcast2.common.properties.RepoProperties;
import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.mapper.*;
import io.github.yajuhua.podcast2.plugin.PluginManager;
import io.github.yajuhua.podcast2.pojo.entity.*;
import io.github.yajuhua.podcast2.pojo.vo.PluginDetailVO;
import io.github.yajuhua.podcast2.pojo.vo.PluginVO;
import io.github.yajuhua.podcast2.service.UserService;
import io.github.yajuhua.podcast2.task.Task;
import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.Podcast2;
import io.github.yajuhua.podcast2API.setting.Setting;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


//TODO
@RestController
@Slf4j
@Api(tags = "插件相关接口")
@RequestMapping("/plugin")
public class PluginController {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private DataPathProperties dataPathProperties;
    @Autowired
    private RepoProperties repoProperties;
    @Autowired
    private PluginMapper pluginMapper;
    @Autowired
    private SubMapper subMapper;
    @Autowired
    private ExtendMapper extendMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SettingsMapper settingsMapper;
    @Autowired
    private Gson gson;
    @Autowired
    private UserService userService;
    @Autowired
    private InfoProperties infoProperties;

    /**
     * 获取插件列表(安装与未安装的)
     * @return
     */
    @ApiOperation("获取插件列表")
    @GetMapping("/list")
    public Result<List<PluginVO>> list() throws Exception{
        //1.获取本地插件列表
        List<Plugin> localPluginList = pluginMapper.list();
        //2.获取远程仓库插件列表
        List<PluginInfo> remotePluginInfoList = new ArrayList<>();
        //获取自定义插件仓库
        String pluginUrl = userService.getExtendInfo().getPluginUrl();
        if (pluginUrl != null && !pluginUrl.isEmpty()){
            pluginUrl = pluginUrl.trim();
            try {
                remotePluginInfoList = pluginManager.getRemotePluginRepoData(pluginUrl);
                //TODO无法获取插件信息
                if (remotePluginInfoList == null){
                    remotePluginInfoList = pluginManager.getRemotePluginRepoData();
                }
            } catch (Exception e) {
                remotePluginInfoList = pluginManager.getRemotePluginRepoData();
            }
        }else {
            remotePluginInfoList = pluginManager.getRemotePluginRepoData();
        }

        //取最新
        Map map = new HashMap();
        for (PluginInfo info : remotePluginInfoList) {
            String name = info.getName();
            String version = info.getVersion();
            if (!map.containsKey(name) || CommonUtils.compareVersion(version,map.get(name).toString()) == 1){
                map.put(name,version);
            }
        }
        remotePluginInfoList = remotePluginInfoList.stream().filter(new Predicate<PluginInfo>() {
            @Override
            public boolean test(PluginInfo pluginInfo) {
                return CommonUtils.compareVersion(map.get(pluginInfo.getName()).toString(),pluginInfo.getVersion()) == 0;
            }
        }).collect(Collectors.toList());

        Set<PluginVO> pluginVOList = new HashSet<>();

        for (PluginInfo remote : remotePluginInfoList) {
            PluginVO pluginVO = null;
            boolean isInstalled = false; // 标记是否已安装
            //遍历本地的
            for (Plugin local : localPluginList) {
                if (local.getName().equals(remote.getName())){
                    //安装的
                    pluginVO = PluginVO.builder()
                            .install(true)
                            .version(local.getVersion())
                            .name(local.getName())
                            .uuid(local.getUuid())
                            .update(local.getUpdateTime())
                            .hasUpdate(CommonUtils.compareVersion(remote.getVersion(),local.getVersion())==1?true:false)
                            .build();
                    isInstalled = true;
                    break;
                }
            }
            if (!isInstalled){
                //未安装的
                pluginVO = PluginVO.builder()
                        .install(false)
                        .version(remote.getVersion())
                        .name(remote.getName())
                        .uuid(remote.getUuid())
                        .update(remote.getUpdateTime())
                        .hasUpdate(false)
                        .build();
            }

            pluginVOList.add(pluginVO);
        }

        for (Plugin plugin : localPluginList) {
            boolean isExist = map.containsKey(plugin.getName());
            if (!isExist){
                pluginVOList.add(PluginVO.builder()
                        .install(true)
                        .version(plugin.getVersion())
                        .name(plugin.getName())
                        .uuid(plugin.getUuid())
                        .update(plugin.getUpdateTime())
                        .hasUpdate(false)
                        .build());
            }
        }

        // 创建一个 Map 用于存储每个插件名称对应的最新版本号
        Map<String, String> latestVersions = new HashMap<>();

        // 遍历插件集合，更新最新版本号
        for (PluginVO pluginVO : pluginVOList) {
            String name = pluginVO.getName();
            String version = pluginVO.getVersion();

            if (!latestVersions.containsKey(name) || CommonUtils.compareVersion(version,latestVersions.get(name)) == 1) {
                latestVersions.put(name, version);
            }
        }

        //根据首字母排序
        List<PluginVO> collect = pluginVOList.stream().filter(pluginVO -> latestVersions.get(pluginVO.getName())
                .equals(pluginVO.getVersion())).sorted(new Comparator<PluginVO>() {
            @Override
            public int compare(PluginVO o1, PluginVO o2) {
                return o1.getName().compareTo(o2.getName());
            }
        }).collect(Collectors.toList());

        /**
         * 获取插件重要信息
         */
        for (PluginVO vo : collect) {
            boolean hasPlugin = pluginManager.hasPlugin(UUID.fromString(vo.getUuid()));
            if (hasPlugin){
                String keyInfo = pluginManager.getPluginKeyInfo(UUID.fromString(vo.getUuid()));
                vo.setKeyInfo(keyInfo);
            }
        }
        return Result.success(collect);
    }


    /**
     * 查看插件安装状态
     * @return
     */
    @ApiOperation("查看插件安装状态")
    @GetMapping("/install/status/{uuid}")
    public Result status(@PathVariable String uuid) throws Exception {
        List<PluginVO> collect = list().getData().stream().filter(new Predicate<PluginVO>() {
            @Override
            public boolean test(PluginVO pluginVO) {
                return pluginVO.getUuid().equals(uuid);
            }
        }).collect(Collectors.toList());
        if (collect.size() == 0 || collect == null){
            throw new PluginNotFoundException(MessageConstant.PLUGIN_NOT_FOUND);
        }
        return Result.success(collect.get(0));
    }

    /**
     * 插件更新状态
     * @return
     */
    @ApiOperation("查看插件更新状态")
    @GetMapping("/update/status/{name}")
    public Result updateStatus(@PathVariable String name) throws Exception {
        List<PluginVO> collect = list().getData().stream().filter(new Predicate<PluginVO>() {
            @Override
            public boolean test(PluginVO pluginVO) {
                return pluginVO.getName().equals(name);
            }
        }).collect(Collectors.toList());
        if (collect.size() == 0 || collect == null){
            throw new PluginNotFoundException(MessageConstant.PLUGIN_NOT_FOUND);
        }
        return Result.success(collect.get(0));
    }

    /**
     * 安装插件
     * @param uuids
     * @return
     */
    @ApiOperation("安装插件")
    @GetMapping("/install")
    @Transactional
    public Result install(@RequestParam List<String> uuids) throws Exception {
        log.info("uuids:{}",uuids);
        for (String uuid : uuids) {
            try {
                pluginManager.add(UUID.fromString(uuid));
                PluginManager.PluginData pluginData = pluginManager.getPluginData(uuid);
                Podcast2 instance = pluginManager.getPluginInstance(pluginData.getName(),new Params());
                //初始化插件设置
                List<Setting> settings = instance.settings();
                pluginManager.initSettings(pluginData.getName(),settings);

            } catch (Exception e) {
                e.printStackTrace();
                log.error("安装插件失败: {}",uuid);
                return Result.error("安装插件失败: " + uuid);
            }
        }
        return Result.success();
    }


    /**
     * 删除插件
     * @param uuids
     * @return
     */
    @ApiOperation("删除插件")
    @DeleteMapping
    @Transactional
    public Result delete(@RequestParam List<String> uuids) throws Exception{
        log.info("删除插件uuids:{}",uuids);
        List<Plugin> pluginList = new ArrayList<>();
        for (String uuid : uuids) {
           pluginList.add( pluginMapper.selectByUuid(uuid));
        }

        //订阅使用的插件不删除
         Map map = new HashMap();
        for (Plugin plugin : pluginList) {
            //获取插件域名
            List<String> domainNames = null;
            try {
                domainNames = pluginManager.getPluginDomainNames(UUID.fromString(plugin.getUuid()));
            } catch (Exception e) {
                //删除扩展选项
                extendMapper.deleteByPlugin(plugin.getName());
            }
            if (domainNames != null){
                for (String domainName : domainNames) {
                    map.put("plugin",domainName);
                    List<Sub> sub = subMapper.selectListByMap(map);
                    if (sub.size() != 0){
                        throw new PluginOccupancyException(MessageConstant.PLUGIN_OCCUPANCY_FAILED);
                    }
                }
                //删除扩展选项
                extendMapper.deleteByPlugin(plugin.getName());
            }
        }

        //删除插件
        for (String uuid : uuids) {
            pluginManager.delete(UUID.fromString(uuid));
            //删除插件设置
            Plugin plugin = pluginMapper.selectByUuid(uuid);
            if (plugin != null){
                settingsMapper.deleteByPlugin(plugin.getName());
            }
            pluginMapper.delete(uuid);
        }
        return Result.success();
    }

    /**
     * 获取插件详细信息
     * @return
     */
    @ApiOperation("获取插件详细信息")
    @GetMapping("/info/{uuid}")
    public Result<Map> info(@PathVariable String uuid) throws Exception{
        Plugin plugin = pluginMapper.selectByUuid(uuid);
        if (plugin == null){
            throw new BaseException("找不到该插件: " + uuid);
        }
        Map info = pluginManager.getPluginInstance(UUID.fromString(uuid)).getInfo();
        return Result.success(info);
    }

    /**
     * 搜索插件
     * @return
     */
    @ApiOperation("搜索插件")
    @GetMapping("/search")
    public Result<List<PluginVO>> search(@RequestParam String keyword) throws Exception {
        List<PluginVO> data = list().getData();
        data = data.stream().filter(pluginVO -> pluginVO.getName().contains(keyword)).collect(Collectors.toList());
        return Result.success(data);
    }

    /**
     * 更新插件
     * @return
     */
    @ApiOperation("更新插件")
    @PostMapping("/update")
    public Result update(@RequestParam List<String> names) throws Exception{
        if(Task.updateStatus){
            //避免订阅更新时更新插件
            throw new PluginOccupancyException(MessageConstant.SUB_UPDATE_ING);
        }
        log.info("更新插件names:{}",names);
/*        List<PluginInfo> pluginInfos = PluginLoader.remoteRepoPluginList(repoProperties.getPluginUrl());
        pluginInfos = pluginInfos.stream().filter(new Predicate<PluginInfo>() {
            @Override
            public boolean test(PluginInfo pluginInfo) {
                return names.contains(pluginInfo.getName());
            }
        }).collect(Collectors.toList());

        List<PluginInfo> pluginInfos1 = new ArrayList<>();

        Map map = new HashMap();
        for (PluginInfo info : pluginInfos) {
            if (!map.containsKey(info.getName()) || PluginLoader.compareVersion(info.getVersion(),map.get(info.getName()).toString()) == 1){
                pluginInfos1.removeIf(new Predicate<PluginInfo>() {
                    @Override
                    public boolean test(PluginInfo pluginInfo) {
                        return pluginInfo.getName().equals(info.getName());
                    }
                });
                map.put(info.getName(),info.getVersion());
                pluginInfos1.add(info);
            }
        }

        //安装插件
        List<String> uuids= new ArrayList<>();
        for (PluginInfo info : pluginInfos1) {
            uuids.add(info.getUuid());
        }
        //先保留之前的
        Map<String,List<Settings>> bak = new HashMap<>();
        for (String name : names) {
            List<Settings> settings = settingsMapper.selectByPluginName(name);
            bak.put(name,settings);
        }
        //安装新的
        install(uuids);

        //清除安装的
        for (String name : names) {
            settingsMapper.deleteByPlugin(name);
        }
        //恢复之前的
        Set<String> keys = bak.keySet();
        for (String key : keys) {
            List<Settings> settings = bak.get(key);
            for (Settings setting : settings) {
                settingsMapper.insert(setting);
            }
        }*/
        for (String name : names) {
            List<Settings> settingsFromDB = settingsMapper.selectByPluginName(name);
            pluginManager.update(name);

            //获取之前设置
            List<Setting> settings = pluginManager.settingsToSetting(settingsFromDB);
            Params params = new Params();
            params.setSettings(settings);
            Podcast2 instance = pluginManager.getPluginInstance(name,params);

            //更新到最新设置
            pluginManager.initSettings(name,instance.settings());

        }
        log.info("插件更新完成");
        return Result.success();
    }

    /**
     * 设置自动更新插件
     * @return
     */
    @ApiOperation("设置自动插件更新")
    @PostMapping("/autoUpdate")
    @Transactional
    public Result setAutoUpdate(@RequestParam Boolean status){
        User user = new User();
        user.setAutoUpdatePlugin(status);
        userMapper.update(user);
       return Result.success();
    }

    /**
     * 获取自动更新插件状态
     * @return
     */
    @ApiOperation("获取自动更新插件状态")
    @GetMapping("autoUpdate")
    public Result getAutoUpdateStatus(){
        return Result.success(userMapper.list().get(0).getAutoUpdatePlugin());
    }

    /**
     * 获取插件详细信息
     * @param uuid
     * @return
     */
    @ApiOperation("获取插件详细信息")
    @GetMapping("/detail/{uuid}")
    public Result pluginDetail(@PathVariable String uuid) throws Exception {
        boolean hasPlugin = pluginManager.hasPlugin(UUID.fromString(uuid));
        Map map;
        if (hasPlugin){
            map = pluginManager.getPluginInstance(UUID.fromString(uuid)).getInfo();
        }else {
            return Result.error("请先安装该插件");
        }

        List<PluginDetailVO> pluginDetailVOList = new ArrayList<>();
        Set keys = map.keySet();
        for (Object key : keys) {
            pluginDetailVOList.add(
            PluginDetailVO.builder()
                    .name(key.toString())
                    .content(map.get(key).toString())
                    .build());
            map.get(key);
        }

        return Result.success(pluginDetailVOList);
    }

    /**
     * 获取插件更新列表
     * @return
     */
    @ApiOperation("获取插件更新列表")
    @GetMapping("/updateList")
    public Result<List<PluginVO>> updateList() throws Exception {
        List<PluginVO> data = list().getData();
        data = data.stream().filter(pluginVO -> pluginVO.getInstall() && pluginVO.getHasUpdate())
                .collect(Collectors.toList());
        return Result.success(data);
    }


    /**
     * 获取插件设置
     * @return
     */
    @ApiOperation("获取插件设置")
    @GetMapping("/settings/{pluginName}")
    public Result<List<Settings>> settings(@PathVariable String pluginName){
        return Result.success(settingsMapper.selectByPluginName(pluginName));
    }


    /**
     * 更新插件设置
     * @param settings
     * @return
     */
    @ApiOperation("更新插件设置")
    @PutMapping("/settings")
    @Transactional
    public Result updateSettings(@RequestBody List<Settings> settings){
        if (settings.size() != 0){
            for (Settings setting : settings) {
                setting.setUpdateTime(System.currentTimeMillis());
                settingsMapper.update(setting);
            }
        }
        return Result.success();
    }
}
