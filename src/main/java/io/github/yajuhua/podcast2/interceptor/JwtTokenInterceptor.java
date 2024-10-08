package io.github.yajuhua.podcast2.interceptor;

import io.github.yajuhua.podcast2.common.constant.JwtClaimsConstant;
import io.github.yajuhua.podcast2.common.properties.JwtProperties;
import io.github.yajuhua.podcast2.common.utils.JwtUtil;
import io.github.yajuhua.podcast2.common.utils.NetWorkUtils;
import io.github.yajuhua.podcast2.controller.UserController;
import io.github.yajuhua.podcast2.mapper.UserMapper;
import io.github.yajuhua.podcast2.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;


    /**
     * 校验jwt和地址过滤
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //地址过滤
        String remoteAddr = request.getRemoteAddr();
        if (UserController.addressFilterTmp != null){
            boolean ban = NetWorkUtils.isBan(remoteAddr, UserController.addressFilterTmp);
            if (ban){
                log.debug("屏蔽：{}",remoteAddr);
                response.setStatus(403);
                return false;
            }
        }

        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)){
            //当前拦截的不是动态方法，直接放行
            return true;
        }

        try {
            //api调用的
            String apiToken = request.getHeader("apiToken");
            if (apiToken != null){
                boolean equals = userMapper.list().get(0).getApiToken().equals(apiToken);
                if (!equals){
                    throw new Exception("apiToken校验未通过: " + apiToken);
                }
            }else {
                String token = request.getHeader(jwtProperties.getUserTokenName());
                //2.校验令牌
                log.debug("jwt校验:{}",token);
                Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(),token);
                //如果解析不出来就会抛异常
                UUID uuid = UUID.fromString(claims.get(JwtClaimsConstant.UUID).toString());
                log.debug("系统uuid:{}",uuid);
            }
            //3.通过，放行
            return true;

        } catch (Exception e) {
            //4.不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
}
