package io.github.yajuhua.podcast2.plugin;

import com.google.gson.Gson;
import io.github.yajuhua.download.commons.utils.CommonUtils;
import io.github.yajuhua.podcast2.annotation.DatabaseAndPluginFileSync;
import io.github.yajuhua.podcast2.common.utils.Http;
import io.github.yajuhua.podcast2.mapper.PluginMapper;
import io.github.yajuhua.podcast2.pojo.entity.Plugin;
import io.github.yajuhua.podcast2.pojo.entity.PluginInfo;
import io.github.yajuhua.podcast2.pojo.entity.PluginMetadata;
import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.Podcast2;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * 插件管理
 */
@Slf4j
public class PluginManager extends ClassLoader{

    private File pluginDir;

    private static String PROPERTIES_FILE_NAME = "plugin.properties";

    private static Map<Class,URLClassLoader> fileClassLoaderMap = new HashMap<>();
    private Gson gson = new Gson();
    private String remotePluginRepoUrl;
    private PluginMapper pluginMapper;

    public PluginManager(File pluginDir) {
        this.pluginDir = pluginDir;
    }

    public PluginManager(File pluginDir, String remotePluginRepoUrl) {
        this.pluginDir = pluginDir;
        this.remotePluginRepoUrl = remotePluginRepoUrl;
    }

    public PluginManager(File pluginDir, String remotePluginRepoUrl, PluginMapper pluginMapper) {
        this.pluginDir = pluginDir;
        this.remotePluginRepoUrl = remotePluginRepoUrl;
        this.pluginMapper = pluginMapper;
    }

    @DatabaseAndPluginFileSync
    private Class getPlugin(String name){
        //如果有多个，取最新版本
        List<PluginData> collect = getPluginDataList(name).stream().sorted(new Comparator<PluginData>() {
            @Override
            public int compare(PluginData o1, PluginData o2) {
                return CommonUtils.compareVersion(o1.getVersion(), o2.getVersion());
            }
        }).sorted(new Comparator<PluginData>() {
            @Override
            public int compare(PluginData o1, PluginData o2) {
                return o1.getUpdate().compareTo(o2.getUpdate());
            }
        }).collect(Collectors.toList());

        //排除空情况
        if (collect == null || collect.isEmpty()){
            throw new RuntimeException("找不到该插件 : " + name);
        }

        //获取class
        PluginData pluginData = collect.get(collect.size() - 1);
        Class<?> classFromJar = loadClassFromJar(pluginData.getJarFile().getAbsolutePath(), pluginData.getClassPath());

        return classFromJar;
    }

    @DatabaseAndPluginFileSync
    private Class getPlugin(String name, String version){
        List<PluginData> collect = getPluginDataList(name).stream().filter(new Predicate<PluginData>() {
            @Override
            public boolean test(PluginData pluginData) {
                return version.equals(pluginData.getVersion());
            }
        }).sorted(new Comparator<PluginData>() {
            @Override
            public int compare(PluginData o1, PluginData o2) {
                return o1.getUpdate().compareTo(o2.getUpdate());
            }
        }).collect(Collectors.toList());

        //排除空情况
        if (collect == null || collect.isEmpty()){
            throw new RuntimeException("找不到该版本插件 : " + name + "-" + version);
        }

        //获取class
        PluginData pluginData = collect.get(collect.size() - 1);
        Class<?> classFromJar = loadClassFromJar(pluginData.getJarFile().getAbsolutePath(), pluginData.getClassPath());

        return classFromJar;
    }

    @DatabaseAndPluginFileSync
    private Class getPlugin(UUID uuid){
        //获取class
        PluginData pluginData =  getPluginData(uuid.toString());
        Class<?> classFromJar = loadClassFromJar(pluginData.getJarFile().getAbsolutePath(), pluginData.getClassPath());
        return classFromJar;
    }

    public Podcast2 getPluginInstance(String name, String version, Params params)throws Exception{
        Class plugin = getPlugin(name, version);
        boolean hasParamsConstructor = hasParamsConstructor(plugin);
        Podcast2 podcast2;
        if (hasParamsConstructor){
            Constructor constructor = plugin.getConstructor(Params.class);
            podcast2 =  (Podcast2) constructor.newInstance(params);
        }else {
            Constructor constructor = plugin.getConstructor(String.class);
            podcast2 =  (Podcast2) constructor.newInstance(gson.toJson(params));
        }
        return podcast2;
    }

    public Podcast2 getPluginInstance(String name, String version)throws Exception{
        Class plugin = getPlugin(name, version);
        Constructor constructor = plugin.getConstructor();
        Podcast2 o = (Podcast2)constructor.newInstance();
        return o;
    }

    @DatabaseAndPluginFileSync
    public Podcast2 getPluginInstance(String name) throws Exception{
        Class plugin = getPlugin(name);
        Constructor constructor = plugin.getConstructor();
        Podcast2 o = (Podcast2)constructor.newInstance();
        return o;
    }

    public Podcast2 getPluginInstance(UUID uuid) throws Exception{
        Class plugin = getPlugin(uuid);
        Constructor constructor = plugin.getConstructor();
        Podcast2 o = (Podcast2)constructor.newInstance();
        return o;
    }

    public Podcast2 getPluginInstance(String name, Params params) throws Exception{
        Class plugin = getPlugin(name);
        Podcast2 podcast2;
        boolean hasParamsConstructor = hasParamsConstructor(plugin);
        if (hasParamsConstructor){
            Constructor constructor = plugin.getConstructor(Params.class);
            podcast2 = (Podcast2) constructor.newInstance(params);
        }else {
            Constructor constructor = plugin.getConstructor(String.class);
            podcast2 = (Podcast2) constructor.newInstance(gson.toJson(params));
        }
        return podcast2;
    }

    /**
     * 通过jar获取Class类
     * @param jarPath
     * @param className
     * @return
     */
    private Class<?> loadClassFromJar(String jarPath, String className) {
        Class<?> clazz = null;
        try {
            URL jarUrl = new File(jarPath).toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[] { jarUrl });
            clazz = classLoader.loadClass(className);
            fileClassLoaderMap.put(clazz,classLoader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return clazz;
    }

    /**
     * 获取插件属性
     * @param jarPath
     * @return
     */
    private Properties getPluginProperties(String jarPath,String propertiesFileName){
        try (JarFile jarFile = new JarFile(jarPath)){
            JarEntry jarEntry = jarFile.getJarEntry(propertiesFileName);
            InputStream inputStream = jarFile.getInputStream(jarEntry);
            Properties properties = new Properties();
            properties.load(inputStream);
            inputStream.close();
            return properties;
        } catch (Exception e) {
            throw new RuntimeException("获取插件属性失败 : " + e.getMessage(),e);
        }
    }

    /**
     * 根据插件名称获取插件信息集合
     * @param name
     * @return
     */
    private List<PluginData> getPluginDataList(String name){
        List<PluginData> pluginDataList = new ArrayList<>();
        File[] files = pluginDir.listFiles();
        String jarPath;
        boolean isJar;
        Properties properties;
        for (File file : files) {
             jarPath = file.getAbsolutePath();
             isJar = "jar".equals(jarPath.substring(jarPath.lastIndexOf(".") + 1));
             if (isJar){

                  properties = getPluginProperties(jarPath, PROPERTIES_FILE_NAME);
                  if (name.equals(properties.get("name"))){
                      String mainClass = properties.get("mainClass").toString();
                      String version = properties.get("version").toString();
                      String update = properties.get("update").toString();
                      String uuid = properties.get("uuid").toString();
                      LocalDate updateDate = LocalDate.parse(update,DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                      PluginData pluginData = new PluginData();
                      pluginData.setClassPath(mainClass);
                      pluginData.setJarFile(file);
                      pluginData.setName(name);
                      pluginData.setUuid(uuid);
                      pluginData.setUpdate(updateDate);
                      pluginData.setVersion(version);

                      pluginDataList.add(pluginData);
                  }
             }
        }
        return pluginDataList;
    }

    /**
     * 根据插件uuid获取插件信息
     * @param uuid
     * @return
     */
    private PluginData getPluginData(String uuid){
        File[] files = pluginDir.listFiles();
        String jarPath;
        boolean isJar;
        Properties properties;
        for (File file : files) {
            jarPath = file.getAbsolutePath();
            isJar = "jar".equals(jarPath.substring(jarPath.lastIndexOf(".") + 1));
            if (isJar){

                properties = getPluginProperties(jarPath, PROPERTIES_FILE_NAME);
                if (uuid.equals(properties.get("uuid"))){
                    String mainClass = properties.get("mainClass").toString();
                    String version = properties.get("version").toString();
                    String update = properties.get("update").toString();
                    String name = properties.get("name").toString();
                    LocalDate updateDate = LocalDate.parse(update,DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    PluginData pluginData = new PluginData();
                    pluginData.setClassPath(mainClass);
                    pluginData.setJarFile(file);
                    pluginData.setName(name);
                    pluginData.setUuid(uuid);
                    pluginData.setUpdate(updateDate);
                    pluginData.setVersion(version);

                    return pluginData;
                }
            }
        }
        throw new RuntimeException("找不到该插件 : " + uuid);
    }

    /**
     * 判断是否有params参数构造器
     * @param clazz
     * @return
     */
    private static boolean hasParamsConstructor(Class clazz) {
        try {
            // 尝试获取指定参数类型的构造器
            clazz.getConstructor(Params.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * 关闭类加载器
     * @param plugin
     * @return
     */
    private boolean closeUrlClassLoader(Class plugin){
        try {
            if (fileClassLoaderMap.containsKey(plugin)){
                fileClassLoaderMap.get(plugin).close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 关闭所有类加载器
     */
    public static void closeAllClassLoader(){
        for (Class aClass : fileClassLoaderMap.keySet()) {
            try {
                fileClassLoaderMap.get(aClass).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fileClassLoaderMap.clear();
    }

    /**
     * 删除插件
     * @param name 插件名称
     * @return
     */
    @DatabaseAndPluginFileSync
    public boolean delete(String name){
       //删除文件
        try {
            for (PluginData data : getPluginDataList(name)) {
                File jarFile = data.getJarFile();
                if(jarFile.exists()){
                    jarFile.delete();
                }
            }
        } catch (Exception e) {
            return false;
        }finally {
            log.info("删除插件: {}",name);
            //删除记录
            pluginMapper.deleteByName(name);
        }
        return true;
    }

    /**
     * 插件插件
     * @param uuid 插件UUID
     * @return
     */
    @DatabaseAndPluginFileSync
    public boolean delete(UUID uuid){
        //删除文件
        try {
            File jarFile = getPluginData(uuid.toString()).getJarFile();
            if (jarFile.exists()){
                jarFile.delete();
            }
        } catch (Exception e) {
            return false;
        }finally {
            log.info("删除插件: {}",uuid.toString());
            //删除数据库记录
            pluginMapper.delete(uuid.toString());

        }
        return true;
    }

    /**
     * 删除重复的插件,保留最新的
     * @return
     */
    public boolean deleteDuplicates(){
        //获取所有插件信息
        List<PluginData> pluginDatas = allPluginData();
        
        //取出名称
        Set<String> pluginNames = new HashSet<>();
        for (PluginData data : pluginDatas) {
            pluginNames.add(data.getName());
        }
        
        //如果名称数量 == 插件数量 ? 没有重复 : 继续移除重复的
        if (pluginNames.size() == pluginDatas.size()){
            return true;
        }else {
            for (PluginData data : pluginDatas) {
                //相同插件按更新时间升序
                List<PluginData> collect = pluginDatas.stream().filter(new Predicate<PluginData>() {
                    @Override
                    public boolean test(PluginData pluginData) {
                        return data.getName().equals(pluginData.getName());
                    }
                }).sorted(new Comparator<PluginData>() {
                    @Override
                    public int compare(PluginData o1, PluginData o2) {
                        return o1.getUpdate().compareTo(o2.getUpdate());
                    }
                }).collect(Collectors.toList());
                if (collect != null && collect.size() > 1){
                    File jarFile = null;
                    for (int i = 0; i < collect.size()-1; i++) {
                        try {
                            jarFile = collect.get(i).getJarFile();
                            if (jarFile.exists()){
                                boolean delete = jarFile.delete();
                                if (!delete){
                                    throw new RuntimeException("删除插件失败 : " + jarFile);
                                }
                            }
                            pluginMapper.delete(collect.get(i).getUuid());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 删除重复的插件,但排除传参的
     * @return
     */
    public boolean deleteDuplicatesEx(UUID uuid){
        //获取所有插件信息
        List<PluginData> pluginDatas = allPluginData();

        //取出名称
        Set<String> pluginNames = new HashSet<>();
        for (PluginData data : pluginDatas) {
            pluginNames.add(data.getName());
        }

        //如果名称数量 == 插件数量 ? 没有重复 : 继续移除重复的
        if (pluginNames.size() == pluginDatas.size()){
            return true;
        }else {
            for (PluginData data : pluginDatas) {
                //相同插件按更新时间升序
                List<PluginData> collect = pluginDatas.stream().filter(new Predicate<PluginData>() {
                    @Override
                    public boolean test(PluginData pluginData) {
                        return data.getName().equals(pluginData.getName());
                    }
                }).sorted(new Comparator<PluginData>() {
                    @Override
                    public int compare(PluginData o1, PluginData o2) {
                        return o1.getUpdate().compareTo(o2.getUpdate());
                    }
                }).collect(Collectors.toList());

                if (collect != null && collect.size() > 1){
                    File jarFile = null;
                    for (int i = 0; i < collect.size(); i++) {
                        try {
                            if (!uuid.toString().equals(collect.get(i).getUuid())){
                                jarFile = collect.get(i).getJarFile();
                                if (jarFile.exists()){
                                    boolean delete = jarFile.delete();
                                    if (!delete){
                                        throw new RuntimeException("删除插件失败 : " + jarFile);
                                    }
                                }
                                //删除数据库记录
                                pluginMapper.delete(collect.get(i).getUuid());
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 添加插件
     * @param name 插件名称
     * @return
     */
    @DatabaseAndPluginFileSync
    public boolean add(String name) throws Exception {
        if (hasPlugin(name)){
            //去重
            deleteDuplicates();
            log.info("该插件已存在: {}",name);
            return true;
        }else {
            List<PluginInfo> repoDataList = getRemotePluginRepoData();
            List<PluginInfo> collect = repoDataList.stream().filter(new Predicate<PluginInfo>() {
                @Override
                public boolean test(PluginInfo pluginInfo) {
                    return name.equals(pluginInfo.getName());
                }
            }).sorted(new Comparator<PluginInfo>() {
                @Override
                public int compare(PluginInfo o1, PluginInfo o2) {
                    return CommonUtils.compareVersion(o1.getVersion(), o2.getVersion());
                }
            }).collect(Collectors.toList());
            if (collect.isEmpty()){
                log.error("仓库还没有这个插件: {}",name);
                return false;
            }

            //下载和校验插件文件
            PluginInfo pluginInfo = collect.get(collect.size() - 1);
            boolean verifyMD5 = downloadAndVerifyMD5(pluginInfo);

            //插入数据库
            if (verifyMD5){
                Plugin plugin = pluginInfoToPlugin(pluginInfo);
                pluginMapper.insert(plugin);
                return true;
            }
            return false;
        }
    }

    @DatabaseAndPluginFileSync
    public boolean add(String name,String version) throws Exception{
        if (hasPlugin(name,version)){
            //去重
            List<PluginData> collect = getPluginDataList(name).stream().filter(new Predicate<PluginData>() {
                @Override
                public boolean test(PluginData pluginData) {
                    return name.equals(pluginData.getName()) && version.equals(pluginData.getVersion());
                }
            }).collect(Collectors.toList());
            if (!collect.isEmpty()){
                String uuid = collect.get(0).getUuid();
                deleteDuplicatesEx(UUID.fromString(uuid));
            }
            log.info("该插件已存在: {}",name);
            return true;
        }else {
            List<PluginInfo> repoDataList = getRemotePluginRepoData();
            List<PluginInfo> collect = repoDataList.stream().filter(new Predicate<PluginInfo>() {
                @Override
                public boolean test(PluginInfo pluginInfo) {
                    return name.equals(pluginInfo.getName());
                }
            }).filter(new Predicate<PluginInfo>() {
                @Override
                public boolean test(PluginInfo pluginInfo) {
                    return version.equals(pluginInfo.getVersion());
                }
            }).collect(Collectors.toList());
            if (collect.isEmpty()){
                log.error("仓库还没有这个插件: {}-{}",name,version);
                return false;
            }

            //下载和校验插件文件
            PluginInfo pluginInfo = collect.get(collect.size() - 1);
            boolean verifyMD5 = downloadAndVerifyMD5(pluginInfo);

            //插入数据库
            if (verifyMD5){
                Plugin plugin = pluginInfoToPlugin(pluginInfo);
                pluginMapper.insert(plugin);
                return true;
            }
            return false;
        }
    }

    @DatabaseAndPluginFileSync
    public boolean add(UUID uuid) throws Exception{
        if (hasPlugin(uuid)){
            log.info("该插件已存在: {}",uuid.toString());
            //去重
            deleteDuplicatesEx(uuid);
            return true;
        }else {
            List<PluginInfo> repoData = getRemotePluginRepoData();
            List<PluginInfo> collect = repoData.stream().filter(new Predicate<PluginInfo>() {
                @Override
                public boolean test(PluginInfo pluginInfo) {
                    return uuid.toString().equals(pluginInfo.getUuid());
                }
            }).collect(Collectors.toList());
            if (collect.isEmpty()){
                log.error("仓库还没有这个插件: {}",uuid.toString());
                return false;
            }
            PluginInfo pluginInfo = collect.get(0);
            boolean verifyMD5 = downloadAndVerifyMD5(pluginInfo);
            //插入数据库
            if (verifyMD5){
                Plugin plugin = pluginInfoToPlugin(pluginInfo);
                pluginMapper.insert(plugin);
                return true;
            }
            return false;
        }
    }

    @DatabaseAndPluginFileSync
    public boolean add(InputStream inputStream, String fileName) throws Exception{
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!"jar".equals(ext)){
            log.error("{}不是jar包扩展名",ext);
            return false;
        }

        List<PluginData> pluginData = allPluginData();
        List<PluginData> collect = pluginData.stream().filter(new Predicate<PluginData>() {
            @Override
            public boolean test(PluginData data) {
                return fileName.equals(data.getName());
            }
        }).collect(Collectors.toList());

        //删除同名的文件
        if (!collect.isEmpty()){
            for (PluginData data : collect) {
                File jarFile = data.getJarFile();
                if (jarFile.exists()){
                    jarFile.delete();
                }
            }
        }

        //拷贝
        File fileSave = new File(pluginDir, fileName);
        FileOutputStream fou = new FileOutputStream(fileSave);
        IOUtils.copyLarge(inputStream,fou);

        //关闭流
        fou.close();
        inputStream.close();

        //获取插件属性
        UUID uuid;
        try {
            Properties properties = getPluginProperties(fileSave.getAbsolutePath(), PROPERTIES_FILE_NAME);
            uuid = UUID.fromString(properties.get("uuid").toString());
            //去重
            boolean duplicatesEx = deleteDuplicatesEx(uuid);
            if (!duplicatesEx){
                log.error("去重失败");
                return false;
            }

            //封装数据
            String name = properties.get("name").toString();
            String version = properties.get("version").toString();
            String uuidStr = uuid.toString();
            String update = properties.get("update").toString();

            Plugin plugin = Plugin.builder()
                    .uuid(uuidStr)
                    .version(version)
                    .updateTime(update)
                    .name(name)
                    .build();

            //删除之前的记录
            pluginMapper.deleteByName(name);
            //插入数据库
            pluginMapper.insert(plugin);
            return true;
        } catch (Exception e) {
            log.error("无法获取该插件属性: {}",e.getMessage());
            if (fileSave.exists()){
                fileSave.delete();
            }
            return false;
        }
    }

    /**
     * 以jar文件上传插件
     * @param file
     * @return
     */
    @DatabaseAndPluginFileSync
    public boolean add(File file){
        try{
            File out = new File(pluginDir.getAbsolutePath(),file.getName());
            FileInputStream ins = new FileInputStream(file);
            FileOutputStream outs = new FileOutputStream(out);
            //判断是否有同名的 ? 删除后复制 : 直接复制
            List<File> collect = Arrays.stream(pluginDir.listFiles()).filter(new Predicate<File>() {
                @Override
                public boolean test(File o) {
                    return file.getName().equals(o.getName());
                }
            }).collect(Collectors.toList());
            if (!collect.isEmpty()){
                //删除重复的
                for (File fd : collect) {
                    if (fd.exists()){
                        fd.delete();
                    }
                }
            }

            //开始复制
            IOUtils.copyLarge(ins,outs);
            //关闭流
            outs.close();
            ins.close();

            //获取插件信息
            Properties properties = getPluginProperties(out.getAbsolutePath(), PROPERTIES_FILE_NAME);
            String name = properties.get("name").toString();
            String version = properties.get("version").toString();
            String uuid = properties.get("uuid").toString();
            String update = properties.get("update").toString();

            Plugin plugin = Plugin.builder()
                    .name(name)
                    .version(version)
                    .uuid(uuid)
                    .updateTime(update)
                    .build();

            //删除之前记录
            pluginMapper.deleteByName(name);

            //插入新的数据
            pluginMapper.insert(plugin);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 下载和校验插件文件
     * @param pluginInfo 插件信息
     * @return
     * @throws Exception
     */
    private boolean downloadAndVerifyMD5(PluginInfo pluginInfo)throws Exception{
        //下载该插件文件
        URL url = new URL(remotePluginRepoUrl);
        String dlUrl = url.getProtocol() + "://" + url.getHost() + pluginInfo.getUrl();
        log.info("下载插件: {}",dlUrl);
        Http.downloadFile(dlUrl,pluginDir.getAbsolutePath());

        //校验
        String fileName = pluginInfo.getUrl().substring(pluginInfo.getUrl().lastIndexOf('/'));
        File saveFile = new File(pluginDir,fileName);
        if (!getFileMD5(saveFile).equals(pluginInfo.getMd5())){
            //删除下载文件
            if (saveFile.exists()){
                saveFile.delete();
                log.error("MD5校验失败");
                return false;
            }
        }
        //去重
        boolean duplicatesEx = deleteDuplicatesEx(UUID.fromString(pluginInfo.getUuid()));
        if (!duplicatesEx){
            log.error("插件去重失败");
        }
        return true;
    }


    /**
     * 获取远程插件仓库数据
     * @return
     */
    private List<PluginInfo> getRemotePluginRepoData(){
        String json = Http.get(remotePluginRepoUrl);
        PluginMetadata pluginMetadata = new Gson().fromJson(json, PluginMetadata.class);
        return pluginMetadata.getPluginList();
    }

    /**
     * 判断本地是否有该插件
     * @param uuid
     * @return
     */
    public boolean hasPlugin(UUID uuid){
        List<PluginData> collect = allPluginData().stream().filter(new Predicate<PluginData>() {
            @Override
            public boolean test(PluginData pluginData) {
                return pluginData.getUuid().equals(uuid.toString());
            }
        }).collect(Collectors.toList());
        return !collect.isEmpty();
    }

    /**
     * 根据名称名称判断是否存在
     * @param name 插件名称
     * @return
     */
    public boolean hasPlugin(String name){
        List<PluginData> collect = allPluginData().stream().filter(new Predicate<PluginData>() {
            @Override
            public boolean test(PluginData pluginData) {
                return pluginData.getName().equals(name);
            }
        }).collect(Collectors.toList());
        return !collect.isEmpty();
    }

    /**
     * 根据名称名称和版本号判断是否存在
     * @param name 插件名称
     * @param version 插件版本
     * @return
     */
    public boolean hasPlugin(String name, String version){
        List<PluginData> collect = allPluginData().stream().filter(new Predicate<PluginData>() {
            @Override
            public boolean test(PluginData pluginData) {
                return pluginData.getName().equals(name) && pluginData.getVersion().equals(version);
            }
        }).collect(Collectors.toList());
        return !collect.isEmpty();
    }

    /**
     * 获取文件MD5值
     * @param file
     * @return
     */
    public static String getFileMD5(File file){
        try (FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.md5DigestAsHex(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新插件
     * @param name
     * @return
     */
    public boolean update(String name){
        List<PluginInfo> collect = getRemotePluginRepoData().stream().filter(new Predicate<PluginInfo>() {
            @Override
            public boolean test(PluginInfo pluginInfo) {
                return name.equals(pluginInfo.getName());
            }
        }).sorted(new Comparator<PluginInfo>() {
            @Override
            public int compare(PluginInfo o1, PluginInfo o2) {
                return CommonUtils.compareVersion(o1.getVersion(), o2.getVersion());
            }
        }).collect(Collectors.toList());
        if (collect.isEmpty()){
            throw new RuntimeException("找不到该插件: " + name);
        }

        //比较版本
        PluginInfo pluginInfo = collect.get(collect.size() - 1);
        List<PluginData> pluginDataList = getPluginDataList(name);
        if (pluginDataList.isEmpty()){
            log.error("请先安装插件: {}",name);
            return false;
        }
        String nowVersion = pluginDataList.get(0).getVersion();
        String latestVersion = pluginInfo.getVersion();
        int compareVersion = CommonUtils.compareVersion(nowVersion,latestVersion);
        if (compareVersion == 1 || compareVersion == 0){
            log.info("{}-{}目前已经是最新版",name,nowVersion);
            return true;
        }else {
            log.info("{}正在更新至{}最新版",name,latestVersion);
            try {
                add(name,latestVersion);
                log.info("{}已更新至{}",name,latestVersion);
                return true;
            } catch (Exception e) {
                log.error("更新错误: {}",e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }


    /**
     * 获取所有插件信息
     * @return
     */
    public List<PluginData> allPluginData(){
        List<PluginData> pluginDataList = new ArrayList<>();
        File[] files = pluginDir.listFiles();
        String jarPath;
        boolean isJar;
        Properties properties;
        for (File file : files) {
            jarPath = file.getAbsolutePath();
            isJar = "jar".equals(jarPath.substring(jarPath.lastIndexOf(".") + 1));
            if (isJar){

                properties = getPluginProperties(jarPath, PROPERTIES_FILE_NAME);
                String mainClass = properties.get("mainClass").toString();
                String version = properties.get("version").toString();
                String update = properties.get("update").toString();
                String uuid = properties.get("uuid").toString();
                String name = properties.get("name").toString();
                LocalDate updateDate = LocalDate.parse(update,DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                PluginData pluginData = new PluginData();
                pluginData.setClassPath(mainClass);
                pluginData.setJarFile(file);
                pluginData.setName(name);
                pluginData.setUuid(uuid);
                pluginData.setUpdate(updateDate);
                pluginData.setVersion(version);

                pluginDataList.add(pluginData);
            }
        }
        return pluginDataList;
    }

    /**
     * 封装插件数据
     */
    @Data
    @ToString
    public class PluginData{
        private File jarFile;
        private String name;
        private String version;
        private LocalDate update;
        private String classPath;
        private String uuid;
    }

    /**
     * 确保数据库记录与插件文件相对应,以少的一方为准
     */
    public void databaseAndPluginFileSync(){
        log.info("数据库记录与插件文件同步...");
        //取数据库记录
        List<Plugin> pluginsFromDB = pluginMapper.list();
        //取本地插件信息
        List<Plugin> pluginsFromLocalFile = new ArrayList<>();
        List<PluginData> pluginData = allPluginData();
        for (PluginData data : pluginData) {
            Plugin build = Plugin.builder()
                    .name(data.getName())
                    .version(data.getVersion())
                    .uuid(data.uuid)
                    .build();
            pluginsFromLocalFile.add(build);
        }
        
        //删除数据库没记录而有插件的文件
        List<Plugin> missingFiles = new ArrayList<>(pluginsFromDB);
        missingFiles.removeAll(pluginsFromLocalFile);
        if (!missingFiles.isEmpty()){
            for (Plugin plugin : missingFiles) {
                log.warn("删除未记录的插件 : {}",plugin);
                delete(UUID.fromString(plugin.getUuid()));
            }
        }

        //数据库有记录而没有插件文件，删除数据库记录
        List<Plugin> missingDB = new ArrayList<>(pluginsFromLocalFile);
        missingDB.removeAll(pluginsFromDB);
        if (!missingDB.isEmpty()){
            for (Plugin plugin : missingDB) {
                log.warn("删除{}数据数据库记录,找不到插件文件",plugin);
                delete(UUID.fromString(plugin.getUuid()));
            }
        }
    }

    /**
     * 将pluginInfo转换成 plugin
     * @param pluginInfo
     * @return
     */
    private Plugin pluginInfoToPlugin(PluginInfo pluginInfo){
        return Plugin.builder()
                .name(pluginInfo.getName())
                .updateTime(pluginInfo.getUpdateTime())
                .version(pluginInfo.getVersion())
                .uuid(pluginInfo.getUuid())
                .build();
    }

    /**
     * 测试远程仓库是否可用
     * @param repoUrl
     * @return
     */
    public static boolean remoteRepoIsOK(String repoUrl){
        try {
            Gson gson = new Gson();
            String json = Http.get(repoUrl);
            PluginMetadata pluginMetadata = gson.fromJson(json, PluginMetadata.class);
            return true;
        } catch (Exception e) {
            log.error("无法获取仓库信息: {}",repoUrl);
            return false;
        }
    }
}

