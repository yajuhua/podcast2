package com.podcast.logs;

import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.io.IOException;

/**
 * 继承 TailerListenerAdapter
 * 用于监听文件的改动
 */
public class MyTailerListener extends TailerListenerAdapter {

    /**
     * 日志
     */
    private static Logger LOG = LoggerFactory.getLogger(MyTailerListener.class);

    private Session session;

    public MyTailerListener(Session session) {
        this.session = session;
    }

    @Override
    public void handle(String line) {
        try {
            LOG.debug(line);
            if (session.isOpen()){
                session.getBasicRemote().sendText(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
