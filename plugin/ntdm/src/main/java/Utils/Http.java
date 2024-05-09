package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Properties;

public class Http {

    /**
     * 读取 plugin.properties配置文件
     * @return
     */
    public static Properties readProperties(){
        Properties pluginProperties = new Properties();
        try {
            // 使用ClassLoader加载Properties文件
            InputStream inputStream = Http.class.getClassLoader().getResourceAsStream("plugin.properties");
            pluginProperties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pluginProperties;
    }

    /**
     * 无代理返回null
     * @return
     */
    public static Proxy proxy(){
        //获取配置文件
        Properties properties = readProperties();
        if("true".equals(properties.get("proxy"))){
            String proxyHost = properties.getProperty("proxyHost");
            int proxyPort = Integer.parseInt((String)properties.get("proxyPort")); // 替换为你的代理端口
            //到服务器的时候直接注释掉即可关闭代理
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            return proxy;
        }else {
            return null;
        }
    }

    /**
     * 获取网页内容
     * @param url
     * @return
     */
    public static String getHttpContent(String url){
        return WebContentP(url);
    }

    /**
     * 代理版获取网页内容
     * @param URL
     * 配置proxyHost和proxyPort即可
     * @return
     * @throws Exception
     */
    public static String WebContentP(String URL){
        String Content = null;
        try {
            java.net.URL url = new URL(URL);
            HttpURLConnection connection =(HttpURLConnection)url.openConnection();
            if (proxy()!=null){
                connection  = (HttpURLConnection)url.openConnection(proxy());
            }
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);//超时设置为5s

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
}
