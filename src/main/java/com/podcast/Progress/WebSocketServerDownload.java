package com.podcast.Progress;
import com.google.gson.Gson;
import com.podcast.pojo.Download;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;


/**
 * 通过websocket向前端发送下载器信息
 */
@ServerEndpoint("/websocket/download")
@Component
public class WebSocketServerDownload {
    private static Logger LOG = LoggerFactory.getLogger(WebSocketServerDownload.class);
    public static boolean DOWNLOAD_STATUS=false; //下载状态
    public static String DOWNLOAD_LOG = null; //下载器输出的日志
    public static Session session; //共享Session，供下载器日志推送
    public static Gson gson = new Gson();


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    public synchronized static void send(Download download) throws Exception {
        if (session!=null && session.isOpen()){
            session.getBasicRemote().sendText(gson.toJson(download));
            Thread.sleep(1000);//等待1s
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        LOG.info("ws已关闭");
    }

    @OnError
    public void onError(Throwable error) {
        LOG.error("ws错误："+ error.getMessage());
    }
}
