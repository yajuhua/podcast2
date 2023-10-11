package com.podcast.logs;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;


/**
 * 通过websocket向前端发送系统日志信息
 */
@ServerEndpoint("/websocket/logs")
@Component
public class WebSocketServerLog {
    private static Logger LOG = LoggerFactory.getLogger(WebSocketServerLog.class);

    private Tailer tailer;


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        String logsPath = null;
        try {
            //获取日志路径
            InputStream inputStream = WebSocketServerLog.class.getClassLoader().getResourceAsStream("conf.properties");
            Properties mainProperties = new Properties();
            mainProperties.load(inputStream);
            logsPath = mainProperties.getProperty("logsPath");
        } catch (Exception e) {
            LOG.error("读取日志位置时出错！详细："+e.toString());
        }

        //tail listen
        TailerListener listener = new MyTailerListener(session);
        tailer = Tailer.create(new File(logsPath), listener, 1000, true);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        tailer.stop();
        LOG.info("ws已关闭");
    }

    @OnError
    public void onError(Throwable error) {
        LOG.error("ws错误："+ error.getMessage());
    }
}
