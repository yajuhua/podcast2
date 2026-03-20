package io.github.yajuhua.podcast2.handler.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.EOFException;
import java.io.IOException;
import java.util.Collection;

import static io.github.yajuhua.podcast2.interceptor.WSTokenInterceptor.sessionMap;

@Slf4j
@Component
public class DownloadWebSocketHandler implements WebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("客户端：{} 建立连接", session.getUri().getPath());
        sessionMap.put(session.getId(), session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if ((exception instanceof EOFException) && exception.getCause() == null) {
            log.warn("客户端异常退出：{}", session.getId());
        } else {
            log.error("socket发生异常：{}", session.getId());
            log.error("异常信息", exception);
        }

        try {
            session.close();
        } catch (IOException e) {
            log.error("关闭socket发生异常", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("客户端: {} 连接断开", session.getUri().getPath());
        sessionMap.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 群发
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        Collection<WebSocketSession> sessions = sessionMap.values();
        for (WebSocketSession session : sessions) {
            try {
                if (session.getUri().getPath().startsWith("/ws/download/")){
                    //服务器向客户端发送消息
                    if (session != null && session.isOpen() && message != null){
                        synchronized (session){
                            session.sendMessage(new TextMessage(new StringBuilder(message)));
                        }
                    }
                }
            } catch (Exception e) {
                log.error("群发错误:{}",e.getMessage());
            }
        }
    }
}
