package io.github.yajuhua.podcast2.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.constant.PluginPropertiesKey;
import io.github.yajuhua.podcast2.common.constant.ReflectionMethodName;
import io.github.yajuhua.podcast2.common.exception.PluginNotFoundException;
import io.github.yajuhua.podcast2.common.exception.PluginOccupancyException;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.properties.RepoProperties;
import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.common.utils.Http;
import io.github.yajuhua.podcast2.common.utils.PluginLoader;
import io.github.yajuhua.podcast2.mapper.*;
import io.github.yajuhua.podcast2.pojo.entity.*;
import io.github.yajuhua.podcast2.pojo.vo.PluginDetailVO;
import io.github.yajuhua.podcast2.pojo.vo.PluginVO;
import io.github.yajuhua.podcast2.service.SubService;
import io.github.yajuhua.podcast2.task.Task;
import io.github.yajuhua.podcast2API.Params;
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

@RestController
@Slf4j
@Api(tags = "插件相关接口")
@RequestMapping("/plugin")
public class PluginController {

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
        List<PluginInfo> remotePluginInfoList = PluginLoader.remoteRepoPluginList(repoProperties.getPluginUrl());
        //获取自定义插件仓库
        UserMoreInfo moreInfo = gson.fromJson(userMapper.list().get(0).getUuid(), UserMoreInfo.class);
        String pluginUrl = moreInfo.getPluginUrl();
        if (pluginUrl != null){
            try {
                remotePluginInfoList = PluginLoader.remoteRepoPluginList(pluginUrl);
                if (remotePluginInfoList == null){
                    remotePluginInfoList = PluginLoader.remoteRepoPluginList(repoProperties.getPluginUrl());
                }
            } catch (Exception e) {
                remotePluginInfoList = PluginLoader.remoteRepoPluginList(repoProperties.getPluginUrl());
            }
        }

        //取最新
        Map map = new HashMap();
        for (PluginInfo info : remotePluginInfoList) {
            String name = info.getName();
            String version = info.getVersion();
            if (!map.containsKey(name) || PluginLoader.compareVersion(version,map.get(name).toString()) == 1){
                map.put(name,version);
            }
        }
        remotePluginInfoList = remotePluginInfoList.stream().filter(new Predicate<PluginInfo>() {
            @Override
            public boolean test(PluginInfo pluginInfo) {
                return PluginLoader.compareVersion(map.get(pluginInfo.getName()).toString(),pluginInfo.getVersion()) == 0;
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
                            .hasUpdate(PluginLoader.compareVersion(remote.getVersion(),local.getVersion())==1?true:false)
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

        // 创建一个 Map 用于存储每个插件名称对应的最新版本号
        Map<String, String> latestVersions = new HashMap<>();

        // 遍历插件集合，更新最新版本号
        for (PluginVO pluginVO : pluginVOList) {
            String name = pluginVO.getName();
            String version = pluginVO.getVersion();

            if (!latestVersions.containsKey(name) || PluginLoader.compareVersion(version,latestVersions.get(name)) == 1) {
                latestVersions.put(name, version);
            }
        }

        return Result.success(pluginVOList.stream().filter(pluginVO -> latestVersions.get(pluginVO.getName())
                .equals(pluginVO.getVersion())).collect(Collectors.toList()));
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
    @ApiOperation("查看插件安装状态")
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
        //排除已存在的
        List<Properties> localPluginList = new ArrayList<>();
        List<Properties> finalLocalPluginList = localPluginList;

        List<Plugin> list = pluginMapper.list();
        for (Plugin plugin : list) {
            Properties properties = new Properties();
            properties.setProperty(PluginPropertiesKey.NAME,plugin.getName());
            properties.setProperty(PluginPropertiesKey.UUID,plugin.getUuid());
            properties.setProperty(PluginPropertiesKey.VERSION,plugin.getVersion());
            properties.setProperty(PluginPropertiesKey.UPDATE,plugin.getUpdateTime());
            localPluginList.add(properties);
        }

        //需要安装插件的uuid
         List<String> installUuids = uuids;
         if (localPluginList.size() > 0){
             installUuids = uuids.stream().filter(s -> {
                 for (Properties properties : finalLocalPluginList) {
                     return !properties.getProperty(PluginPropertiesKey.UUID).equals(s);
                 }
                 return false;
             }).collect(Collectors.toList());
         }


        log.info("安装插件的uuid:{}",installUuids);
        //1.获取下载链接
        String json = Http.get(repoProperties.getPluginUrl());
        PluginMetadata pluginMetadata = new Gson().fromJson(json, PluginMetadata.class);
        List<String> finalInstallUuids = installUuids;
        List<PluginInfo> collect = pluginMetadata.getPluginList().stream().filter(pluginInfo -> {
            for (String uuid : finalInstallUuids) {
                if (pluginInfo.getUuid().equals(uuid)) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
        log.info(collect.toString());

        //2.下载插件文件
        URL url = new URL(repoProperties.getPluginUrl());
        String p = url.getProtocol() + "://" + url.getHost();
        for (PluginInfo info : collect) {
            String dlUrl = p + info.getUrl();
            log.info("插件下载:{}",dlUrl);
            Http.downloadFile(dlUrl,dataPathProperties.getLocalPluginPath());
            //封装插件信息
            Plugin plugin = Plugin.builder()
                    .uuid(info.getUuid())
                    .name(info.getName())
                    .updateTime(info.getUpdateTime())
                    .version(info.getVersion())
                    .build();
            //3.读取插件信息并存入数据库
            pluginMapper.deleteByName(plugin.getName());
            pluginMapper.insert(plugin);
            //插件设置信息存入数据库
            List<Class> classList = PluginLoader.selectByName(info.getName(), dataPathProperties);
            Gson gson = new Gson();
            Type type = new TypeToken<List<Setting>>() {}.getType();
            if (classList.size() > 0){
                Class aClass = classList.get(0);
                Constructor constructor = aClass.getConstructor(String.class);
                Params params = new Params();
                Object o = constructor.newInstance(gson.toJson(params));
                String json1 = gson.toJson(aClass.getMethod(ReflectionMethodName.SETTINGS).invoke(o));
                List<Setting> settings = gson.fromJson(json1,type);
               //插件设置信息保留
                settingsMapper.deleteByPlugin(info.getName());
                for (Setting setting : settings) {
                    Settings settings1 = new Settings();
                    BeanUtils.copyProperties(setting,settings1);
                    settings1.setPlugin(info.getName());
                    settings1.setUpdateTime(System.currentTimeMillis());
                    settingsMapper.insert(settings1);
                }
            }
        }

        //去重，取最新版的
        localPluginList = PluginLoader.localPluginList(dataPathProperties.getLocalPluginPath());
        List<String> names = new ArrayList<>();
        for (Properties properties : localPluginList) {
            names.add(properties.getProperty(PluginPropertiesKey.NAME));
        }
        Set<String> tmp = new HashSet<>();

        List<String> repetitive = new ArrayList<>();
        for (String name : names) {
            if (!tmp.add(name)){
                //重复的
                repetitive.add(name);
            }
        }
        log.info("重复的插件:{}",repetitive);
        //取最新版本的
        //要删除的
        for (String s : repetitive) {
            List<Class> classList = PluginLoader.selectByName(s, dataPathProperties);
            Map<String,String> map = new HashMap();
            for (int i = 0; i < classList.size(); i++) {
                Properties properties = PluginLoader.getProperties(classList.get(i).getClassLoader(), PluginLoader.PROPERTIES_NAME);
                map.put(properties.getProperty(PluginPropertiesKey.VERSION),properties.getProperty(PluginPropertiesKey.UUID));
                URLClassLoader classLoader = (URLClassLoader) classList.get(i).getClassLoader();
                classLoader.close();
            }
            List<String> c1 = map.keySet().stream().sorted((o1, o2) -> PluginLoader.compareVersion(o2, o1))
                    //如果两个是相同的话，删除一个即可
                    .skip(map.size()==1?0:1).collect(Collectors.toList());
            for (String s1 : c1) {
                PluginLoader.deletePluginByUuid(map.get(s1),dataPathProperties.getLocalPluginPath());
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
    public Result delete(@RequestParam List<String> uuids) throws Exception{
        log.info("删除插件uuids:{}",uuids);
        List<Plugin> pluginList = new ArrayList<>();
        for (String uuid : uuids) {
           pluginList.add( pluginMapper.selectByUuid(uuid));
        }

        //订阅使用的插件不删除
         Map map = new HashMap();
        for (Plugin plugin : pluginList) {
            map.put("plugin",plugin.getName());
            List<Sub> sub = subMapper.selectListByMap(map);
            if (sub.size() != 0){
                throw new PluginOccupancyException(MessageConstant.PLUGIN_OCCUPANCY_FAILED);
            }
            //删除扩展选项
            extendMapper.deleteByPlugin(plugin.getName());
        }

        //删除插件
        for (String uuid : uuids) {
            PluginLoader.deletePluginByUuid(uuid,dataPathProperties.getLocalPluginPath());
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
        Gson gson = new Gson();
        Params params = new Params();
        File file = PluginLoader.selectByPluginUuid(uuid, dataPathProperties.getLocalPluginPath());
        Class aClass = PluginLoader.loadJar(file.getAbsolutePath());
        Constructor constructor = aClass.getConstructor(String.class);
        Object o = constructor.newInstance(gson.toJson(params));
        Map info = (Map) aClass.getMethod(ReflectionMethodName.GET_INFO).invoke(o);
        PluginLoader.close(aClass);
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
        List<PluginInfo> pluginInfos = PluginLoader.remoteRepoPluginList(repoProperties.getPluginUrl());
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
        //1.本地插件，获取插件内部
        File file = PluginLoader.selectByPluginUuid(uuid, dataPathProperties.getLocalPluginPath());
        Class aClass = PluginLoader.loadJar(file.getAbsolutePath());
        Constructor constructor = aClass.getConstructor(String.class);
        Params params = new Params();
        Gson gson = new Gson();
        String json = gson.toJson(params);
        Object o = constructor.newInstance(json);
        Method method = aClass.getMethod(ReflectionMethodName.GET_INFO);
        String json1 = gson.toJson(method.invoke(o));
        Map map = gson.fromJson(json1, Map.class);

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
