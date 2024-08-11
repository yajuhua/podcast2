package io.github.yajuhua.podcast2.downloader.aria2;

import io.github.yajuhua.download.commons.utils.BuildCmd;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.*;

@Slf4j
public class Aria2RPC {

    /**
     * 启动RPC
     */
    public static void start(){
        List<String> args = Arrays.asList(BuildCmd.buildArrayArgs(getAria2Conf()));
        List<String> cmd = new ArrayList<>(args);
        cmd.add(0,"aria2c");
        String[] cmdByArr = new String[cmd.size()];
        for (int i = 0; i < cmd.size(); i++) {
            cmdByArr[i] = cmd.get(i);
        }

        //守护进程命令不同系统
        String name = System.getProperty("os.name");

        if (name.contains("Windows")){
            //Windows
            cmd.add(0,"/b");
            cmd.add(0,"start");
            cmd.add(0,"/c");
            cmd.add(0,"cmd.exe");
        }else if(name.contains("Linux")){
            //Linux
            cmd.add("-D");
        }else if (name.contains("Mac OS X") || name.contains("macOS")){
            //macOS
            cmd.add("-D");
        }else {
            cmd.add("-D");
        }

        Thread aria2RPC = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("Aria2RPC启动...");
                    ProcessBuilder processBuilder = new ProcessBuilder(cmd);
                    Process start = processBuilder.start();
                    int waitFor = start.waitFor();
                    if (waitFor != 0){
                        log.error("Aria2RPC启动失败!");
                    }
                } catch (Exception e) {
                    log.error("Aria2RPC启动失败: {}",e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        aria2RPC.start();
    }

    @Test
    public void test(){
    }


    /**
     * 配置引用了部分'Aria2完美配置' https://github.com/P3TERX/aria2.conf
     * @return
     */
    public static Map getAria2Conf(){
        Map map = new HashMap();
        map.put("--disk-cache=32M","");
        map.put("--file-allocation=prealloc","");
        map.put("--continue=true","");
        map.put("--always-resume=true","");
        map.put("--max-file-not-found=5","");
        map.put("--max-tries=5","");
        map.put("--retry-wait=15","");
        map.put("--connect-timeout=30","");
        map.put("--max-connection-per-server=16","");
        map.put("--split=5","");
        map.put("--min-split-size=8M","");
        map.put("--piece-length=1M","");
        map.put("--allow-piece-length-change=true","");
        map.put("--max-overall-download-limit=0","");
        map.put("--max-overall-upload-limit=64K","");
        map.put("--max-download-limit=0","");
        map.put("--max-upload-limit=32K","");
        map.put("--disable-ipv6=false","");
        map.put("--reuse-uri=false","");
        map.put("--allow-overwrite=false","");
        map.put("--auto-file-renaming=true","");
        map.put("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 Edg/126.0.0.0","");
        map.put("--enable-rpc=true","");
        map.put("--rpc-allow-origin-all=true","");
        map.put("--rpc-listen-all=true","");
        map.put("--rpc-listen-port=6800","");
        map.put("--rpc-max-request-size=10M","");
        map.put("--rpc-secret=aria2","");
        map.put("--async-dns=true","");
        map.put("--async-dns-server=119.29.29.29,223.5.5.5,1.1.1.1,8.8.8.8,114.114.114.114","");
        return map;
    }
}
