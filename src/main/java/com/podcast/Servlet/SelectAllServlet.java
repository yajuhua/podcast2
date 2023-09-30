package com.podcast.Servlet;

import com.alibaba.fastjson.JSON;
import com.podcast.Utils.TimeFormat;
import com.podcast.pojo.ChannelDataShow;
import com.podcast.pojo.ChannelDate;
import com.podcast.service.ChannelService;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/selectAllServlet")
public class SelectAllServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ChannelService service = new ChannelService();
        response.setCharacterEncoding("utf-8");
        List<ChannelDate> channelDates = service.seletAll();

        //存入新的集合
       List<ChannelDataShow> channelDataShows = new ArrayList<>();
        for (int i = 0; i < channelDates.size(); i++) {
            ChannelDataShow channelDataShow = new ChannelDataShow();
            channelDataShow.setChannelTitle(channelDates.get(i).getChannelTitle());
            channelDataShow.setUuid(channelDates.get(i).getUuid());
            channelDataShow.setUpdateTimestamp(TimeFormat.formatDate(channelDates.get(i).getUpdateTimestamp()));
            channelDataShow.setChannelFace(channelDates.get(i).getChannelFace());
            channelDataShows.add(channelDataShow);
        }

        String jsonString = JSON.toJSONString(channelDataShows);
        response.setContentType("text/json;charset=utf-8");
        response.getWriter().write(jsonString);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
