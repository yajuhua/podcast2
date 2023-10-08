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

        //无条件放行的资源
        String[] pass = {"css","js","xml","loginServlet"};

        //转换成HttpServletRequest已获取资源URI
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //获取资源的URI
        String requestedResource = request.getRequestURI();
        LOGGER.debug("URI:"+requestedResource);

        for (int i = 0; i < pass.length; i++) {
            if (requestedResource.contains(pass[i])){
                //放行
                LOGGER.debug("放行，必要加载资源");
                filterChain.doFilter(request,response);
                return;
            }
        }

        //判断是否登录
       if (request.getSession().getAttribute("user")!=null){
            //已登录状态
            LOGGER.debug("放行，已登录状态");
            filterChain.doFilter(request,response);
        }else {
            //未登录，转发到login.html页面
            LOGGER.debug("未登录，转发到登录页面");
            request.getRequestDispatcher("login.html").forward(request,response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }


    @Override
    public void destroy() {

    }
}
