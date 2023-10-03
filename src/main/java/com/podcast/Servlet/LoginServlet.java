package com.podcast.Servlet;

import com.podcast.pojo.PodcastUser;
import com.podcast.service.PodcastUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/loginServlet")
public class LoginServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger("LoginServlet");
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        PodcastUserService podcastUserService = new PodcastUserService();
        PodcastUser login = podcastUserService.login(username, password);

        request.setCharacterEncoding("utf-8");
        HttpSession session = request.getSession();

        if (login!=null){
            //提示成功
            response.getWriter().write("ok");

            session.setAttribute("user",login);

            LOGGER.debug("登录状态:"+login);
        }else {
            //提示错误，继续在login页面
            response.getWriter().write("error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
