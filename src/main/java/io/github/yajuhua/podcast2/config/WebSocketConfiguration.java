package io.github.yajuhua.podcast2.config;

import io.github.yajuhua.podcast2.handler.ws.DownloadWebSocketHandler;
import io.github.yajuhua.podcast2.handler.ws.LogsWebSocketHandler;
import io.github.yajuhua.podcast2.handler.ws.SystemInfoWebSocketHandler;
import io.github.yajuhua.podcast2.interceptor.WSTokenInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类，用于注册WebSocket的Bean
 */
@Configuration
@EnableWebSocket
@Slf4j
public class WebSocketConfiguration  implements WebSocketConfigurer {

    @Autowired
    private WSTokenInterceptor wsTokenInterceptor;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("开始注册Websocket拦截器...");
        registry.addHandler(new LogsWebSocketHandler(), "/ws/logs/*")
                .addHandler(new DownloadWebSocketHandler(), "/ws/download/*")
                .addHandler(new SystemInfoWebSocketHandler(), "/ws/system/*")
                .setAllowedOrigins("*")
                .addInterceptors(wsTokenInterceptor);
    }
}