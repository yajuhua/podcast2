package io.github.yajuhua.podcast2.websocket;

import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.listener.LogsListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket服务
 * 解决Autowired为null:https://blog.csdn.net/qq_38182820/article/details/83898433
 */
@Component
@Slf4j
@ServerEndpoint("/ws/logs/{sid}")
public class LogWebSocketServer {


    private static DataPathProperties dataPathProperties;
    private Tailer tailer;

    //存放会话对象
    private static Map<String, Session> sessionMap = new HashMap();

    public LogWebSocketServer() {
    }

    @Autowired
    public void setDataPathProperties(DataPathProperties dataPathProperties) {
        LogWebSocketServer.dataPathProperties = dataPathProperties;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        LogsListener logsListener = new LogsListener(session);
        tailer = Tailer.create(new File(dataPathProperties.getInfoPath()), logsListener, 1000, true);
        log.info("客户端：" + sid + "建立连接");
        sessionMap.put(sid, session);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.info("收到来自客户端：" + sid + "的信息:" + message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        log.info("连接断开:" + sid);
        tailer.close();
        sessionMap.remove(sid);
    }

    /**
     * 群发
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息
                if (session != null && session.isOpen() && message != null){
                    synchronized (session){
                        session.getBasicRemote().sendText(message);
                    }
                }
            } catch (Exception e) {
                log.error("群发错误:{}",e.getMessage());
            }
        }
    }

}
