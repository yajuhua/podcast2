package io.github.yajuhua.podcast2.alist.utils;

import io.github.yajuhua.podcast2.alist.Alist;
import io.github.yajuhua.podcast2.alist.dto.fs.PutDTO;
import org.testng.annotations.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Http {
    public static String get(String url, Map params, Map heads){
        String Content = null;
        try {
            //拼接参数
            if (params!= null && !params.isEmpty()){
                StringBuilder paramsStr = new StringBuilder(url);
                Set keySet = params.keySet();
                int count = 0;
                for (Object key : keySet) {
                    if (count == 0){
                        paramsStr.append("?" + key + "=" + params.get(key));
                    }else {
                        paramsStr.append("&" + key + "=" + params.get(key));
                    }
                    count++;
                }
                url = url + paramsStr;
            }
            HttpURLConnection connection =(HttpURLConnection)new URL(url).openConnection();

            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 Edg/92.0.902.67");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
            connection.setRequestProperty("Sec-Fetch-Mode", "navigate");
            connection.setRequestMethod("GET");
            if (heads != null){
                Set set = heads.keySet();
                for (Object key : set) {
                    connection.setRequestProperty((String) key, (String) heads.get(key));
                }
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
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
    public static String post(String url, String params, Map heads){
        String content = null;
        try {
            HttpURLConnection connection =(HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("POST");
            //设置请求头
            if (heads != null && !heads.isEmpty()){
                Set set = heads.keySet();
                for (Object key : set) {
                    connection.setRequestProperty((String) key, (String) heads.get(key));
                }
            }
            if (params != null){
                connection.setDoOutput(true);
                OutputStream os = connection.getOutputStream();
                byte[] bytes = params.getBytes("UTF-8");
                os.write(bytes,0,bytes.length);
                os.flush();
                os.close();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            content = "";
            while ((line = reader.readLine()) != null) {
                content = content + line;
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content;
    }

    public static String put(String url, String filePath, Map heads){
        String content = null;
        try {
            HttpURLConnection connection =(HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            if (heads != null && !heads.isEmpty()){
                Set set = heads.keySet();
                for (Object key : set) {
                    connection.setRequestProperty((String) key, (String) heads.get(key));
                }
            }
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            OutputStream os = connection.getOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
            os.close();
            fileInputStream.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            content = "";
            while ((line = reader.readLine()) != null) {
                content = content + line;
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content;
    }
}
