package com.podcast.Servlet;

import com.podcast.update.UpdateInit;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.*;
import java.util.List;

/**
 * 插件上传
 */
@WebServlet("/uploadServlet")
@MultipartConfig
public class UploadServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger("UploadServlet");
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            //上传插件
        String pluginsPath = UpdateInit.WEBAPP_PATH+"plugin"+File.separator;
        System.out.println(pluginsPath);
        upload(request,response,pluginsPath);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }


    /**
     * 文件上传
     * @param request
     * @param response
     * @param savePath
     */
    public  static void upload(HttpServletRequest request, HttpServletResponse response,String savePath) throws IOException {
        // 检查请求是否是上传文件
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            // 返回错误信息
            return;
        }

        // 创建文件上传工厂和上传组件
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        String fileName = null;
        try {
            // 解析请求
            List<FileItem> items = upload.parseRequest(request);

            // 处理上传文件
            for (FileItem item : items) {
                if (!item.isFormField()) {
                   fileName = item.getName();
                    String contentType = item.getContentType();
                    long fileSize = item.getSize();

                    File uploadedFile = new File(savePath + fileName);
                    item.write(uploadedFile);

                    // 处理上传结果
                    LOGGER.info(fileName+"上传成功！");
                    response.setContentType("text/*; charset=utf-8");
                    response.getWriter().write("uploadok");
                }
            }
        } catch (Exception e) {
            // 处理异常情况
            LOGGER.error(fileName+"上传失败！");
            response.setContentType("text/*; charset=utf-8");
            response.getWriter().write("uploadno");
        }
    }
}
