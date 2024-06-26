package io.github.yajuhua.podcast2.common.utils;

import io.github.yajuhua.podcast2.pojo.entity.AddressFilter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 网络工具类
 */
public class NetWorkUtils {
    /**
     * 验证IP地址范围是否有效
     *
     * @param ipRange 例IP范围:192.168.1.1-192.168.2.255
     * @return
     */
    public static boolean isValidIpRange(String ipRange) {
        String[] parts = ipRange.split("-");
        if (parts.length != 2) {
            return false;
        }
        String startIp = parts[0];
        String endIp = parts[1];
        // 验证起始和结束IP地址是否都有效
        if (!isValidIp(startIp) || !isValidIp(endIp)) {
            return false;
        }

        // 转换起始和结束IP地址为整数并比较
        int startIpInt = ipToInt(startIp);
        int endIpInt = ipToInt(endIp);

        // 结束IP地址必须大于或等于起始IP地址
        return endIpInt >= startIpInt;
    }

    /**
     * 根据IP范围获取范围内所有IP地址
     *
     * @param ipRange 例IP范围:192.168.1.1-192.168.2.255
     * @return
     */
    public static List<String> generateRangeIpList(String ipRange) {
        String[] parts = ipRange.split("-");
        String startIp = parts[0];
        String endIp = parts[1];
        int startIpAsInt = ipToInt(startIp);
        int endIpAsInt = ipToInt(endIp);
        List<String> ipList = new ArrayList<>();
        for (int i = startIpAsInt; i <= endIpAsInt; i++) {
            ipList.add(intToIp(i));
        }
        return ipList;
    }

    /**
     * 将IP地址转换为整数（网络字节序）
     *
     * @param ip 192.168.1.1
     * @return
     */
    private static int ipToInt(String ip) {
        String[] parts = ip.split("\\.");
        int result = 0;
        for (int i = 0; i < parts.length; i++) {
            result |= (Integer.parseInt(parts[i]) & 0xFF) << (24 - (i * 8));
        }
        return result;
    }

    /**
     * 将整数转换为IP地址
     *
     * @param ip 192.168.1.1
     * @return
     */
    private static String intToIp(int ip) {
        return (ip >>> 24) + "." +
                ((ip >> 16) & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                (ip & 0xFF);
    }

    /**
     * 使用正则表达式进行匹配 验证IPv4地址是否有效
     *
     * @param ip 例IP地址:192.168.1.1
     * @return
     */
    public static boolean isValidIp(String ip) {
        String pattern = "^((\\d{1,2}|1\\d{2}|2[0-4]\\d|25[0-5])\\.){3}(\\d{1,2}|1\\d{2}|2[0-4]\\d|25[0-5])$";
        return ip.matches(pattern);
    }

    /**
     * 判断IP地址是否被屏蔽
     * @param remoteAddr
     * @param addressFilter
     * @return
     */
    public static boolean isBan(String remoteAddr, AddressFilter addressFilter){
        List<String> whitelist = addressFilter.getWhitelist();
        List<String> blacklist = addressFilter.getBlacklist();
        List<String> banList = new ArrayList<>();

        //黑名单
        for (String b : blacklist) {
            boolean isIp = isValidIp(b);
            if (isIp){
                //单个ip
                if (b.equals(remoteAddr)){
                    banList.add(b);
                }
            }else {
                //IP范围
                List<String> ipList = generateRangeIpList(b);
                for (String s : ipList) {
                    if (s.equals(remoteAddr)){
                        banList.add(s);
                    }
                }
            }
        }

        //白名单
        for (String w : whitelist) {
            boolean isIp = isValidIp(w);
            if (isIp){
                //单个ip
                if (w.equals(remoteAddr)){
                    banList.remove(w);
                }
            }else {
                //IP范围
                List<String> ipList = generateRangeIpList(w);
                for (String s : ipList) {
                    if (s.equals(remoteAddr)){
                        banList.remove(s);
                    }
                }
            }
        }

        //如果banList不等于0。说明被ban了
        if (banList.isEmpty()){
            return false;
        }
        return true;
    }

}
