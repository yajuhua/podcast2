package io.github.yajuhua.podcast2.listener;

import org.apache.commons.io.input.TailerListenerAdapter;
import javax.websocket.Session;
import java.io.IOException;

/**
 * 继承 TailerListenerAdapter
 * 用于监听文件的改动
 */
public class LogsListener extends TailerListenerAdapter {


    private Session session;

    public LogsListener(Session session) {
        this.session = session;
    }

    @Override
    public void handle(String line) {
        try {
            if (session.isOpen()){
                session.getBasicRemote().sendText(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
