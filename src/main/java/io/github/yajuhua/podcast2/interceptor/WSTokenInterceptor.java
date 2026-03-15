package io.github.yajuhua.podcast2.interceptor;

import io.github.yajuhua.podcast2.common.constant.JwtClaimsConstant;
import io.github.yajuhua.podcast2.common.utils.JwtUtil;
import io.github.yajuhua.podcast2.mapper.UserMapper;
import io.github.yajuhua.podcast2.pojo.entity.User;
import io.github.yajuhua.podcast2.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class WSTokenInterceptor implements HandshakeInterceptor {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            String query = request.getURI().getQuery();
            Map<String, String> params = new HashMap<>();

            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                params.put(pair[0], pair.length > 1 ? pair[1] : "");
            }
                //2.校验令牌
                String token = params.get("token");
                log.debug("jwt校验:{}", token);
                User user = userMapper.list().get(0);
                String secretKey = DigestUtils
                        .md5DigestAsHex((user.getUsername() + user.getPassword() + userService.getExtendInfo().getUuid())
                                .getBytes(StandardCharsets.UTF_8));
                Claims claims = JwtUtil.parseJWT(secretKey,token);
                //如果解析不出来就会抛异常
                UUID uuid = UUID.fromString(claims.get(JwtClaimsConstant.UUID).toString());
                log.debug("系统uuid:{}",uuid);
            //3.通过，放行
            return true;

        } catch (Exception e) {
            //4.不通过，响应401状态码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

    }
}
