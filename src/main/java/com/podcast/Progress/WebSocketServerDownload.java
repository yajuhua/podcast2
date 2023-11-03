package com.podcast.Progress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


/**
 * 通过websocket向前端发送下载器信息
 */
@ServerEndpoint("/websocket/download")
@Component
public class WebSocketServerDownload {
    private static Logger LOG = LoggerFactory.getLogger(WebSocketServerDownload.class);
    public static boolean DOWNLOAD_STATUS=false; //下载状态
    public static String DOWNLOAD_LOG = null; //下载器输出的日志
    public static Session _session; //共享Session，供下载器日志推送


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        _session = session;
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
