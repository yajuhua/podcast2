package com.podcast.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter("/showlogs.html")
public class Filter04 implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger("Filter03");
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;

        Object user = req.getSession().getAttribute("user");
        LOGGER.debug("登录的状态码"+user);
        if (user!=null){
            chain.doFilter(request, response);
        }else {
            req.getRequestDispatcher("login.html").forward(request,response);
        }


    }

    public void init(FilterConfig config) throws ServletException {
    }

    public void destroy() {
    }

}
