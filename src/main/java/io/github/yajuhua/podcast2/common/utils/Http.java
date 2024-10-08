package io.github.yajuhua.podcast2.common.utils;

import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.exception.DownloadFileException;
import io.github.yajuhua.podcast2.common.exception.GetSecondLevelDomainException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class Http {

    /**
     * get请求
     * @return
     * @throws Exception
     */
    public static String get(String url){
        String Content = null;
        try {
            HttpURLConnection connection =(HttpURLConnection)new URL(url).openConnection();

            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            Content = "";
            while ((line = reader.readLine()) != null) {
                Content = Content + line;
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return  Content;
    }

    /**
     * 构建请求参数
     * @param params
     * @return
     */
    public static String buildParams(HashMap<String,String> params){
        Set<String> keys = params.keySet();
        String paramsString = "?";
        for (String key : keys) {
            paramsString = paramsString + "&" + key + "=" + params.get(key);
        }
        return paramsString;
    }

    /**
     * 文件下载器
     * @param
     * @throws Exception
     */
    public static void downloadFile(String url, String saveDir) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestMethod("GET");

            String disposition = connection.getHeaderField("Content-Disposition");
            String fileName = "none";
            if (disposition != null) {
                // 从Content-Disposition中获取文件名
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 9).replaceAll("\"", "");
                }
            } else {
                // 如果Content-Disposition中未找到文件名，可以尝试从URL中获取
                String urlPath = new URL(url).getPath();
                fileName = urlPath.substring(urlPath.lastIndexOf("/") + 1);
            }

            inputStream = connection.getInputStream();
            outputStream = new FileOutputStream(saveDir + File.separator + fileName);
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new DownloadFileException(MessageConstant.DOWNLOAD_FILE_FAILED + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                // 处理流关闭异常
                e.printStackTrace();
            }
        }
    }

    /*public static void downloadFile(String url,String saveDir){

        try {
            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestMethod("GET");

            String disposition = connection.getHeaderField("content-disposition");
            String fileName = "none";
            if (disposition != null) {
                // 从Content-Disposition中获取文件名
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 9).replaceAll("\"","");
                }
            } else {
                // 如果Content-Disposition中未找到文件名，可以尝试从URL中获取
                String urlPath = new URL(url).getPath();
                fileName = urlPath.substring(urlPath.lastIndexOf("/") + 1);
            }
            IOUtils.copy(connection.getInputStream(),new FileOutputStream(saveDir + File.separator + fileName));
        } catch (IOException e) {
            throw new DownloadFileException(MessageConstant.DOWNLOAD_FILE_FAILED + e.getMessage());
        }
    }*/


    /**
     * 获取二级域名
     * @return
     */
    public static String getSecondLevelDomain(String domain){
        if (!domain.contains(".")){
            throw new GetSecondLevelDomainException(MessageConstant.GET_SECOND_LEVEL_DOMAIN_FAILED);
        }
        String[] split = domain.split("\\.");
        if (split.length < 2){
            throw new GetSecondLevelDomainException(MessageConstant.GET_SECOND_LEVEL_DOMAIN_FAILED);
        }
        return Arrays.stream(split).skip(split.length -2).collect(Collectors.joining("."));
    }

    /**
     * 获取url中的主机名称
     * @param url
     * @return
     * @throws Exception
     */
    public static String getHost(String url){
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
