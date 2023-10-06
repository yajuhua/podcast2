package com.podcast.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 对资源访问进行管理，xml目录下不限制
 */
@WebFilter("/*")
public class FilterManage implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger("FilterManage");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        //转换成HttpServletRequest已获取资源URI
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse Response = (HttpServletResponse) servletResponse;

        //获取资源的URI
        String requestedResource = request.getRequestURI();
        LOGGER.debug("URI:"+requestedResource);

        //除了xml其他都拦截
        if (requestedResource.contains("xml")){
            LOGGER.debug("放行，xml资源不受限");
            filterChain.doFilter(request,Response);
        }else if (request.getSession().getAttribute("user")!=null || requestedResource.contains("loginServlet")){
            //已登录状态 或 请求登录
            LOGGER.debug("放行，已登录状态 或 请求登录");
            filterChain.doFilter(request,Response);
        }else {
            //未登录，转发到login.html页面
            LOGGER.debug("未登录，转发到登录页面");
            request.getRequestDispatcher("login.html").forward(request,Response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }


    @Override
    public void destroy() {

    }
}
