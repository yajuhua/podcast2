package io.github.yajuhua.podcast2.handler.ws;

import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.listener.LogsListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import static io.github.yajuhua.podcast2.interceptor.WSTokenInterceptor.sessionMap;

@Slf4j
@Component
public class LogsWebSocketHandler implements WebSocketHandler {

    private static DataPathProperties dataPathProperties;
    private Tailer tailer;

    @Autowired
    public void setDataPathProperties(DataPathProperties dataPathProperties) {
        LogsWebSocketHandler.dataPathProperties = dataPathProperties;
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        LogsListener logsListener = new LogsListener(session);
        tailer = Tailer.create(new File(dataPathProperties.getInfoPath()), logsListener, 1000, true);
        log.info("客户端：{} 建立连接", session.getUri().getPath());
        sessionMap.put(session.getId(), session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if ((exception instanceof EOFException) && exception.getCause() == null) {
            log.warn("客户端异常退出：{}", session.getUri().getPath());
        } else {
            log.error("socket发生异常：{}", session.getUri().getPath());
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
        log.info("客户端：{} 连接断开", session.getUri().getPath());
        tailer.close();
        sessionMap.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
