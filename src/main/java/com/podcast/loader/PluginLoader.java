package com.podcast.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * 插件加载器
 */
public class PluginLoader {


    /**
     * 插件的配置文件名称
     */
    private final static String PROPERTIES_NAME = "plugin.properties";
    /**
     * 插件的程序入口
     */
    private final static String MAIN_CLASS = "mainClass";

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
    private static final ClassLoader getClassLoader(String jarFilePath) throws MalformedURLException {
        File jarFile = new File(jarFilePath);
        if (!jarFile.exists()) {
            return null;
        }
        URL url = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url}, null);
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
    private static Properties getProperties(ClassLoader classLoader, String propertiesName) throws IOException {
        InputStream propertiesStream = classLoader.getResourceAsStream(propertiesName);
        Properties properties = new Properties();
        properties.load(propertiesStream);
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

}
