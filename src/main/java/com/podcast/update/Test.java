package com.podcast.update;

import org.podcast2.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Test {
    public static void main(String[] args) {

    }

    public static boolean update(String uuid, List<Item> items){
        /**
         * 常量
         */
        //根据uuid读取：type、plugin
        //通过plugin名称去遍历插件库
        //读取数据库的IP地址
        //获取插件的plugin.properties文件，读取downloader、audioMode、videoMode


        for (int i = 0; i < items.size(); i++) {
            /**
             * 变量
             */
            //生成资源的uuid
            //Mode mode = new Mode(getItemEnclosure,webappPath,downLoderClass,resourceUuid,type,IP);,返回转换后资源的URL
            //writeItem(methodResult,type,enclosureLink,xmlPath,latestCount);,将内容写入xml文件
        }


        return true;
    }

    public static boolean update(String uuid, Class plugin, Properties pluginProperties, Properties mainProperties, String webappPath, List<Item> items){
        /**
         * 常量
         */
        //根据uuid读取：type、plugin
        //通过plugin名称去遍历插件库
        //读取数据库的IP地址
        //获取插件的plugin.properties文件，读取downloader、audioMode、videoMode


        for (int i = 0; i < items.size(); i++) {
            /**
             * 变量
             */
            //生成资源的uuid
            //Mode mode = new Mode(getItemEnclosure,webappPath,downLoderClass,resourceUuid,type,IP);,返回转换后资源的URL
            //writeItem(methodResult,type,enclosureLink,xmlPath,latestCount);,将内容写入xml文件
        }


        return true;
    }

    @org.testng.annotations.Test
    public void t2(){
        List<Item> items = new ArrayList<>();
        System.out.println(items);
        if (items.size()==0){
            items = null;
        }
        System.out.println(items);
    }
}
