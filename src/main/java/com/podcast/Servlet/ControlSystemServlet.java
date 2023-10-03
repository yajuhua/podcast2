package com.podcast.Servlet;

import com.podcast.Utils.Mode;
import com.podcast.Utils.N_m3u8DL_RE;
import com.podcast.Utils.Yt_dlp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

/**
 * 控制tomcat服务器，比如重启
 */
@WebServlet("/controlSystemServlet")
public class ControlSystemServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger("ControlSystemServlet");
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /**
         * 控制码:controlCode
         *       0:关机
         *       1:重启
         */
        String controlCode = request.getParameter("controlCode");
        if (controlCode.equals("0")){
            //关机
        }else if (controlCode.equals("1")){
            //重启
            LOGGER.info("系统重启");
            N_m3u8DL_RE.Cmd("/restartTomat.sh");
        }


    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
