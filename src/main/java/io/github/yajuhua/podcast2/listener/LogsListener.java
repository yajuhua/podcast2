package io.github.yajuhua.podcast2.listener;

import org.apache.commons.io.input.TailerListenerAdapter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * 继承 TailerListenerAdapter
 * 用于监听文件的改动
 */
public class LogsListener extends TailerListenerAdapter {


    private WebSocketSession session;

    public LogsListener(WebSocketSession session) {
        this.session = session;
    }

    @Override
    public void handle(String line) {
        try {
            if (session.isOpen()){
                session.sendMessage(new TextMessage(new StringBuilder(line)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
