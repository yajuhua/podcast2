package io.github.yajuhua.podcast2.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 对自定义剧集进行解析
 */
public class Episodes {

    /**
     * 对自定义剧集进行解析
     * @param esStr 支持 1,3-6
     * @return
     */
    public static List<Integer> parse(String esStr){
        List<Integer> es = new ArrayList<>();

        //直接返回空的集合
        if (esStr == null || esStr.isEmpty()){
            return es;
        }

        //分割
        String[] split1 = esStr.split(",");
        for (String s : split1) {
            //移除负数
           if (!s.startsWith("-")){
               //区间
               String[] interval  = s.split("-");
               if (interval.length == 2){
                   int start = Integer.parseInt(interval[0]);
                   int end = Integer.parseInt(interval[1]);
                   es.addAll(IntStream.rangeClosed(start,end).boxed().collect(Collectors.toList()));
               }else if (interval.length == 1){
                   es.add(Integer.parseInt(interval[0]));
               }
           }
        }

        //去重、升序并返回
        return es.stream().distinct().sorted().collect(Collectors.toList());
    }

    /**
     * 对自定义剧集进行解析
     * @param esStr 支持 1,3-6
     * @return 字符串类型
     */
    public static String parseToStr(String esStr){
        if (parse(esStr).isEmpty()){
            return "";
        }
       return parse(esStr).stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
