package io.github.yajuhua.podcast2.common.utils;


import ch.qos.logback.core.util.FileUtil;
import com.google.gson.Gson;
import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.constant.PluginPropertiesKey;
import io.github.yajuhua.podcast2.common.exception.PluginNotFoundException;
import io.github.yajuhua.podcast2.common.exception.PluginVersionException;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.pojo.entity.Plugin;
import io.github.yajuhua.podcast2.pojo.entity.PluginInfo;
import io.github.yajuhua.podcast2.pojo.entity.PluginMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 插件加载器
 */
@Slf4j
public class PluginLoader {

    /**
     * 插件的配置文件名称
     */
    public final static String PROPERTIES_NAME = "plugin.properties";
    /**
     * 插件的程序入口
     */
    private final static String MAIN_CLASS = "mainClass";
    public static List<URLClassLoader> classLoaderList = new ArrayList<>();

    /**
     * 加载jar文件
     *
     * @param jarFilePath jar文件路径
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Class loadJar(String jarFilePath) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = getClassLoader(jarFilePath);
        Properties properties = getProperties(classLoader, PROPERTIES_NAME);
        String mainClass = properties.getProperty(MAIN_CLASS);
        return loadClass(classLoader, mainClass);
    }

    /**
     * 获得ClassLoader
     *
     * @param jarFilePath jar文件路径
     * @return
     * @throws MalformedURLException
     */
    private static ClassLoader getClassLoader(String jarFilePath) throws MalformedURLException {
        File jarFile = new File(jarFilePath);
        if (!jarFile.exists()) {
            return null;
        }
        URL url = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url}, null);
        classLoaderList.add(classLoader);
        return classLoader;
    }

    /**
     * 获得jar中的properties
     *
     * @param classLoader    classLoader
     * @param propertiesName 文件名称
     * @return
     * @throws IOException
     */
    public static Properties getProperties(ClassLoader classLoader, String propertiesName) throws IOException {
        InputStream propertiesStream = classLoader.getResourceAsStream(propertiesName);
        Properties properties = new Properties();
        properties.load(propertiesStream);
        propertiesStream.close();
        return properties;
    }


    /**
     * 获取配置文件
     * @param jarFilePath
     * @return
     * @throws Exception
     */
    public static Properties getProperties(String jarFilePath) throws Exception {
        ClassLoader classLoader = getClassLoader(jarFilePath);
        return getProperties(classLoader, PROPERTIES_NAME);
    }

    /**
     * 加载类
     *
     * @param classLoader classLoader
     * @param className   全类名
     * @return
     * @throws ClassNotFoundException
     */
    private static Class loadClass(ClassLoader classLoader, String className) throws ClassNotFoundException {
        Class<?> clazz = classLoader.loadClass(className);
        return clazz;
    }

    /**
     * 根据插件名称查找
     * @param pluginName
     * @return
     * @throws Exception
     */
    public static List<Class> selectByName(String pluginName, DataPathProperties dataPathProperties) throws Exception{
        List<Class> classList =  new ArrayList<>();
        File file = new File(dataPathProperties.getLocalPluginPath());
        for (File f : file.listFiles()) {
            if (pluginName.contains(PluginLoader.getProperties(f.getAbsolutePath()).get(PluginPropertiesKey.NAME).toString())){
                classList.add(loadJar(f.getAbsolutePath()));
            }
        }
        if (classList.size() == 0){
            throw new PluginNotFoundException(MessageConstant.PLUGIN_NOT_FOUND);
        }
        return classList;
    }

    /**
     * 根据插件名称获取插件文件
     * @param pluginName
     * @param dataPathProperties
     * @return
     */
    public static List<File> selectPluginFileByName(String pluginName,DataPathProperties dataPathProperties) throws Exception {
        String localPluginPath = dataPathProperties.getLocalPluginPath();
        File file = new File(localPluginPath);
        List<File> files = new ArrayList<>();
        for (File f : file.listFiles()) {
            String name = getProperties(f.getAbsolutePath()).getProperty(PluginPropertiesKey.NAME);
            if (pluginName.equals(name)){
                files.add(f);
            }
        }
        return files;
    }

    /**
     * 根据uuid查询插件File
     * @param uuid
     * @param localPluginPath
     * @return
     * @throws Exception
     */
    public static File selectByPluginUuid(String uuid,String localPluginPath){
        File file = new File(localPluginPath);
        List<File> files = Arrays.stream(file.listFiles()).filter(file1 -> {
            Boolean b;
            try {
                b = file1.getName().substring(file1.getName()
                        .lastIndexOf(".") + 1).equals("jar") && getProperties(file1.getAbsolutePath())
                        .getProperty(PluginPropertiesKey.UUID).equals(uuid);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return b;
        }).collect(Collectors.toList());
        if (files.size() == 0){
             throw  new  PluginNotFoundException(MessageConstant.PLUGIN_UUID_NOT_FOUND);
        }
        return files.get(0);
    }

    /**
     * 获取本地插件属性文件信息
     * @param pluginPath
     * @return
     */
    public static List<Properties> localPluginList(String pluginPath) throws Exception{
        File file = new File(pluginPath);
        List<Properties> properties = new ArrayList<>();
        List<File> files = Arrays.stream(file.listFiles()).filter(file1 -> file1.getName()
                .substring(file1.getName().lastIndexOf(".") + 1)
                .equals("jar")).collect(Collectors.toList());
        for (File file1 : files) {
            properties.add(getProperties(file1.getAbsolutePath()));
        }
        return properties;
    }

    /**
     * 获取远程仓库插件信息
     * @param pluginUrl
     * @return
     */
    public static List<PluginInfo> remoteRepoPluginList(String pluginUrl){
        String json = Http.get(pluginUrl);
        PluginMetadata pluginMetadata = new Gson().fromJson(json, PluginMetadata.class);
        return pluginMetadata.getPluginList();
    }

    /**
     * 版本比较
     * @param v1
     * @param v2
     * @return 如果v1 > v2 返回 1;v1<v2 返回 -1;相同返回0
     */
    public static int compareVersion(String v1,String v2){
        String[] v1Sp = v1.split("\\.");
        String[] v2Sp = v2.split("\\.");
        if (v1Sp.length != v2Sp.length){
            throw new PluginVersionException(MessageConstant.PLUGIN_VERSION_FAILED);
        }
        String[] parts1 = v1Sp;
        String[] parts2 = v2Sp;

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int part1 = (i < parts1.length) ? Integer.parseInt(parts1[i]) : 0;
            int part2 = (i < parts2.length) ? Integer.parseInt(parts2[i]) : 0;

            if (part1 < part2) {
                return -1;
            } else if (part1 > part2) {
                return 1;
            }
        }

        return 0; // 版本号相同
    }

    /**
     * 根据uuid删除插件
     * @param uuid
     * @param localPluginPath
     * @throws Exception
     */
    public static void deletePluginByUuid(String uuid,String localPluginPath) throws Exception {
        File file = selectByPluginUuid(uuid, localPluginPath);
        for (URLClassLoader loader : classLoaderList) {
            loader.close();
        }
        FileUtils.forceDelete(file);
    }

    /**
     * 关闭classLoader引用
     * @param c
     */
    public static void close (Class c) throws Exception{
        classLoaderList.remove(c);
        URLClassLoader classLoader = (URLClassLoader) c.getClassLoader();
        classLoader.close();
    }

    /**
     * 关闭所有classLoader
     */
    public static void closeAll(){
        try {
            for (URLClassLoader loader : classLoaderList) {
                loader.close();
            }
            classLoaderList.clear();
        } catch (IOException e) {
            log.error("异常信息:{}",e.getMessage());
        }
    }
}
