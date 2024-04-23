package io.github.yajuhua.podcast2.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.EOFException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket服务
 */
@Component
@Slf4j
@ServerEndpoint("/ws/download/{sid}")
public class DownloadWebSocketServer {

    //存放会话对象
    private static Map<String, Session> sessionMap = new HashMap();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
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
        sessionMap.remove(sid);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        if ((throwable instanceof EOFException) && throwable.getCause() == null) {
            log.warn("客户端异常退出：{}", session.getId());
        } else {
            log.error("socket发生异常：{}", session.getId());
            log.error("异常信息", throwable);
        }

        try {
            session.close();
        } catch (IOException e) {
            log.error("关闭socket发生异常", e);
        }
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
