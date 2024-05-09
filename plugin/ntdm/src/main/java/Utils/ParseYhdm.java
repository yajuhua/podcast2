package Utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 樱花动漫解析工具类
 */
public class ParseYhdm {

    /**
     * 获取在线解析地址
     * @param url 单集URL链接
     * @return
     * @throws Exception
     */
    public static String parseOnline(String url) throws Exception {
        final String API = "https://danmu.yhdmjx.com/m3u8.php?url=";
        //1.jsoup 解析静态网页————>获取在线观看的解析地址
        Document document = Jsoup.connect(url).proxy(Http.proxy()).get();
        //Xpath定位元素
        Elements elements = document.selectXpath("//*[@id=\"ageframediv\"]/script[1]");
        String JSONstr = elements.get(0).data();
        //截取JSONstr
        int index = JSONstr.indexOf('=');
        JSONstr = JSONstr.substring(index+1);
        //解析JSONstr
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(JSONstr, JsonObject.class);
        //获取url
        String parseId = jsonObject.get("url").getAsString();
        return API+parseId;
    }

    /**
     *
     * @param url 单集的URL链接
     * @return 返回视频源地址供下载使用
     * @throws Exception
     */
    public static String parseSourceAddress(String url) throws Exception {

        String httpContent = Http.getHttpContent(parseOnline(url));

        //匹配getVideoInfo
        Pattern compile = Pattern.compile("(?<=getVideoInfo).*?(?=,)");
        String videoInfo = null;
        Matcher matcher = compile.matcher(httpContent);
        if (matcher.find()){
            //去除首尾的（""）字符
            videoInfo = matcher.group().substring(2, matcher.group().length() - 2);
        }

        //var bt_token = 获取向量
        Pattern compile2 = Pattern.compile("(?<=var bt_token =).*?(?=;)");
        Matcher matcher1 = compile2.matcher(httpContent);
        String bt_token = null;
        if (matcher1.find()){
            //去除首尾的（""）字符
            bt_token = matcher1.group().substring(2, matcher1.group().length() - 1);
        }

        //解密
        String decryptURL = AESUtils.Decrypt(videoInfo,bt_token);

        //返回解密后的URL
        return decryptURL;
    }
}
