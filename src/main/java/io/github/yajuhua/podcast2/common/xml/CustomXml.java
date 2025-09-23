package io.github.yajuhua.podcast2.common.xml;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.yajuhua.podcast2API.Channel;
import io.github.yajuhua.podcast2API.Item;
import io.github.yajuhua.podcast2API.utils.TimeFormat;
import io.github.yajuhua.podcast2API.utils.Xml;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Slf4j
public class CustomXml {
    private static List<Item> items;

    /**
     * <code>
     {
     "confList": [
     {
     "name": "folo",
     "conf": {
     "rss": {
     "_attr": {
     "xmlns:atom":"http://www.w3.org/2005/Atom",
     "xmlns:itunes": "http://www.itunes.com/dtds/podcast-1.0.dtd",
     "version": "2.0",
     "encoding": "UTF-8"
     }
     },
     "channel": {
     "title": {
     "_text": "<![CDATA[ {title} ]]>"
     },
     "pubDate": {
     "_text": "{latestPubDate}"
     },
     "link": {
     "_text": "<![CDATA[ {link} ]]>"
     },
     "itunes:image": {
     "_attr": {
     "href":"{image}"
     }
     },
     "description": {
     "_text": "<![CDATA[ {description} ]]>"
     },
     "itunes:author": {
     "_text": "<![CDATA[ {title} ]]>"
     },
     "itunes:category": {
     "_attr": {
     "text": "null"
     }
     }
     },
     "item": {
     "title": {
     "_text": "<![CDATA[ {title} ]]>"
     },
     "pubDate": {
     "_text": "{publicTime}"
     },
     "link": {
     "_text": "<![CDATA[ {link} ]]>"
     },
     "enclosure": {
     "_attr": {
     "url": "{enclosure}",
     "type": "{type}"
     }
     },
     "itunes:duration": {
     "_text": "{duration}"
     },
     "itunes:image": {
     "_attr": {
     "href":"{image}"
     }
     },
     "description": {
     "_text": "<![CDATA[ {description} ]]>"
     }
     }
     }
     }
     ]
     }
     * <code/>
     * @param channel
     * @param items
     * @param jsonStr
     * @param confName
     * @return
     * @throws Exception
     */
    public static String custom(Channel channel, List<Item> items, String jsonStr, String confName){
        if (confName.equalsIgnoreCase("default")){
            return Xml.build(channel, items);
        }
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonStr, JsonObject.class);
            for (JsonElement confEl : jsonObject.get("confList").getAsJsonArray()) {
                JsonObject confObj = confEl.getAsJsonObject();
                if (confObj.get("name").getAsString().equalsIgnoreCase(confName)){
                    return custom(channel, items, gson.toJson(confObj.get("conf")));
                }
            }
        } catch (Exception e) {
            log.error("使用自定义XML配置错误: {}", e.getMessage());
        }
        //默认返回内置的
        return Xml.build(channel, items);

    }

    /**
     * 自定义xml
     /**
     * 支持JSON配置XML
     * <code>
     *    {
     *   "rss": {
     *       "_attr": {
     *         "xmlns:atom":"http://www.w3.org/2005/Atom",
     *         "xmlns:itunes": "http://www.itunes.com/dtds/podcast-1.0.dtd",
     *         "version": "2.0",
     *         "encoding": "UTF-8"
     *       }
     *     },
     *   "channel": {
     *     "title": {
     *       "_text": "<![CDATA[ {title} ]]>"
     *     },
     *     "pubDate": {
     *       "_text": "{pubDate}"
     *     },
     *     "link": {
     *       "_text": "<![CDATA[ {link} ]]>"
     *     },
     *     "itunes:image": {
     *       "_attr": {
     *         "href":"{image}"
     *       }
     *     },
     *     "description": {
     *       "_text": "<![CDATA[ {description} ]]>"
     *     },
     *     "itunes:author": {
     *       "_text": "<![CDATA[ {title} ]]>"
     *     },
     *     "itunes:category": {
     *       "_attr": {
     *         "text": "null"
     *       }
     *     }
     *   },
     *   "item": {
     *     "title": {
     *       "_text": "<![CDATA[ {title} ]]>"
     *     },
     *     "pubDate": {
     *       "_text": "{pubDate}"
     *     },
     *     "link": {
     *       "_text": "<![CDATA[ {link} ]]>"
     *     },
     *     "enclosure": {
     *       "_attr": {
     *         "url": "{enclosure}",
     *         "type": "{type}"
     *       }
     *     },
     *     "itunes:duration": {
     *       "_text": "00:46:30"
     *     },
     *     "itunes:image": {
     *       "_attr": {
     *         "href":"{image}"
     *       }
     *     },
     *     "description": {
     *       "_text": "<![CDATA[ {description} ]]>"
     *     }
     *   }
     * }
     * </code>
     * @param channel
     * @param items
     * @param jsonStr JSON配置文件
     * @return
     * @throws Exception
     */
    private static String custom(Channel channel, List<Item> items, String jsonStr) throws Exception{
        Gson gson = new Gson();
        CustomXml.items = items;
        JsonObject jsonObject = gson.fromJson(jsonStr, JsonObject.class);
        List<String> supportTags = Arrays.asList("rss", "channel", "item");
        for (String tag : supportTags) {
            if (!jsonObject.has(tag)){
                throw new Exception("缺少" + tag + "字段");
            }
        }

        //构建xml文档
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        //rss标签
        Element rssTag = doc.createElement("rss");
        JsonObject rssJsonObj = jsonObject.get("rss").getAsJsonObject();
        if (rssJsonObj.has("_attr")){
            Set<Map.Entry<String, JsonElement>> rssEntries = rssJsonObj.get("_attr").getAsJsonObject().entrySet();
            for (Map.Entry<String, JsonElement> rssEntry : rssEntries) {
                rssTag.setAttribute(rssEntry.getKey(), rssEntry.getValue().getAsString());
            }
        }

        //channel标签
        Element channelTag = doc.createElement("channel");
        JsonObject channelJsonObj = jsonObject.get("channel").getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> channelEntries =
                channelJsonObj.entrySet().stream().filter(new Predicate<Map.Entry<String, JsonElement>>() {
                    @Override
                    public boolean test(Map.Entry<String, JsonElement> stringJsonElementEntry) {
                        return !(stringJsonElementEntry.getKey().equalsIgnoreCase("_text")
                                || stringJsonElementEntry.getKey().equalsIgnoreCase("_attr")
                                || stringJsonElementEntry.getKey().equalsIgnoreCase("item"));
                    }
                }).collect(Collectors.toSet());
        for (Map.Entry<String, JsonElement> entry : channelEntries) {
            JsonObject object = entry.getValue().getAsJsonObject();
            Element channelEl = buildElement(doc, entry.getKey(), object, channel);
            channelTag.appendChild(channelEl);
        }
        rssTag.appendChild(channelTag);

        //item标签
        for (Item item : items) {
            Element itemTag = doc.createElement("item");
            JsonObject itemJsonObj = jsonObject.get("item").getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> itemEntries =
                    itemJsonObj.entrySet().stream().filter(new Predicate<Map.Entry<String, JsonElement>>() {
                        @Override
                        public boolean test(Map.Entry<String, JsonElement> stringJsonElementEntry) {
                            return !(stringJsonElementEntry.getKey().equalsIgnoreCase("_text")
                                    || stringJsonElementEntry.getKey().equalsIgnoreCase("_attr"));
                        }
                    }).collect(Collectors.toSet());
            for (Map.Entry<String, JsonElement> entry : itemEntries) {
                JsonObject object = entry.getValue().getAsJsonObject();
                Element itemEl = buildElement(doc, entry.getKey(), object, item);
                itemTag.appendChild(itemEl);
            }
            channelTag.appendChild(itemTag);
        }
        doc.appendChild(rssTag);

        // 转换成 XML 字符串
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String xmlString = writer.toString();
        return xmlString.replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&amp;", "&")
                        .replace("&quot;", "\"")
                        .replace("&apos;", "'");
    }

    private static Element buildElement(Document doc, String tagName, JsonObject obj, Object o) throws IllegalAccessException {
        Element element = doc.createElement(tagName);
        if (obj.has("_attr")) {
            for (Map.Entry<String, JsonElement> attr : obj.get("_attr").getAsJsonObject().entrySet()) {
                String replaced = replacePlaceholders(o, attr.getValue().getAsString());
                element.setAttribute(attr.getKey(), replaced);
            }
        }
        if (obj.has("_text")) {
            String replaced = replacePlaceholders(o, obj.get("_text").getAsString());
            element.setTextContent(replaced);
        }
        return element;
    }

    /**
     * 替换占位符，例如 {name} 会被对象 o 的成员变量 name 替换
     *
     * @param o 对象，提供成员变量
     * @param s 包含占位符的字符串，例如 "这个是成员变量{name}"
     * @return 替换后的字符串
     */
    private static String replacePlaceholders(Object o, String s) throws IllegalAccessException {
        //channel标签中pubDate标签
        if (s.contains("{latestPubDate}")){
            Item item = null;
            if (items.size()>0){
                item = items.stream().max(Comparator.comparingLong(Item::getCreateTime)).get();//获取最新的
            }
            String change = TimeFormat.change(item == null ? System.currentTimeMillis() : item.getCreateTime());
            return s.replace("{latestPubDate}", change);
        }
        //映射对象的字段
        Class<?> clazz = o.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // 允许访问私有字段
            Object value = field.get(o);
            String name = field.getName();
            if (value != null) {
                //item标签中的时间字段不一样
                if (name.equalsIgnoreCase("publicTime") || name.equalsIgnoreCase("createTime")){
                    long t = (long) value;
                    value = TimeFormat.change(t);
                }
                if (name.equalsIgnoreCase("duration")){
                    int t = (int) value;
                    value = TimeFormat.duration(t);
                }
                s = s.replace("{" + field.getName() + "}", value.toString());
            }
        }
        return s;
    }
}
