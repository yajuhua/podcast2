package com.podcast.Servlet;

import com.podcast.service.ChannelService;
import com.podcast.service.PodcastUserService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebServlet("/deleteServlet")
public class DeleteServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger("DeleteServlet");
    private PodcastUserService service = new PodcastUserService();
    private ChannelService channelService = new ChannelService();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String uuid = request.getParameter("uuid");


        //也要删除xml文件
        String webappPath = service.getWebappPath();
        String xmlSavePath = webappPath+ "xml"+ File.separator+uuid+".xml";
        File file = new File(xmlSavePath);
        if (FileUtils.deleteQuietly(file)){
            channelService.deleteByUuid(uuid);
            LOGGER.info(xmlSavePath+"删除成功！");
        }else {
            LOGGER.info(xmlSavePath+"删除失败！");
        }

        //删除该频道下的资源
        String videoResourceSavePath = webappPath+ "video"+ File.separator;
        String audioResourceSavePath = webappPath+ "audio"+ File.separator;
        List<String> resourceUuidByXmlUuid = channelService.getResourceUuidByXmlUuid(uuid);
        File videoresourceFile = new File(videoResourceSavePath);
        List<File> vides = Arrays.stream(videoresourceFile.listFiles()).toList();
        File audioresourceFile = new File(audioResourceSavePath);
        List<File> audios = Arrays.stream(audioresourceFile.listFiles()).toList();

        //获取所有资源的文件
       List<File> resources = new ArrayList<>();
       resources.addAll(vides);
       resources.addAll(audios);
       //遍历所有的资源文件
        for (File resource : resources) {
            for (String rUuid : resourceUuidByXmlUuid) {
                if (resource.getName().contains(rUuid)){
                    //删除特定uuid的资源
                    FileUtils.deleteQuietly(resource);
                    //删除数据库
                    channelService.deleteByResourceUuid(rUuid);
                }
            }
        }



        request.getRequestDispatcher("/index.html").forward(request,response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
