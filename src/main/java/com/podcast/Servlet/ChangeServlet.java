package com.podcast.Servlet;

import com.podcast.service.PodcastUserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 修改用户名和密码
 */
@WebServlet("/changeServlet")
public class ChangeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PodcastUserService service = new PodcastUserService();
        /*        Map<String, String[]> parameterMap = request.getParameterMap();
        PodcastUserService service = new PodcastUserService();
        String username = null;
        String password = null;
        if (parameterMap.size()==0){
            System.out.println("不修改");
            response.getWriter().write("null");
        }else if (parameterMap.size() == 1){
            for (String s : parameterMap.keySet()) {
                if(s.equals("username")){
                    System.out.println("修改用户名");
                   username = String.valueOf(parameterMap.get("username"));
                    service.changeUsername(username);
                    response.getWriter().write("ok");
                }else {
                    System.out.println("修改密码");
                    password = String.valueOf(parameterMap.get("password"));
                    service.changePassword(password);
                    response.getWriter().write("ok");
                }
            }
        }else {
            System.out.println("修改全部");
            username = String.valueOf(parameterMap.get("username"));
            password = String.valueOf(parameterMap.get("password"));
            service.changeAll(username,password);
            response.getWriter().write("ok");
        }*/

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        service.changeAll(username,password);
/*        System.out.println(username);
        System.out.println(password);*/
        HttpSession session = request.getSession();
        //销毁session
        session.invalidate();
        response.getWriter().write("ok");




    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
