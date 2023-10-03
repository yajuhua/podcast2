package com.podcast.Servlet;

import com.podcast.Utils.N_m3u8DL_RE;
import com.podcast.loader.PluginLoader;
import com.podcast.pojo.Plugin;
import com.podcast.update.UpdateInit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.podcast.Servlet.XmlFactoryServlet.*;

/**
 * 目前（2023-10-03）还无法实现,资源占用无法删除
 */
@WebServlet("/deletePluginServlet")
public class DeletePluginServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger("DeletePluginServlet");
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pluginName = request.getParameter("name");
        String pluginVersion = request.getParameter("version");
        /**
         * 根据插件名称和版本号进行删除
         * 读取plugin包下的所有插件配置文件，符合的删除
         */
        //配置文件的名称
        String PROPERTIES_NAME = "plugin.properties";

        //所有插件的位置
        String pluginsPath = UpdateInit.WEBAPP_PATH+"plugin";
        File pluginFile = new File(pluginsPath);

        //获取所有的插件文件
        File[] pluginList = pluginFile.listFiles();


        //遍历,将要删除的插件绝对路径存入插件删除列表
        for (File plugin : pluginList) {
            String pluginPath = plugin.getAbsolutePath();
            ClassLoader classLoader = getClassLoader(pluginPath);
            Properties properties = getProperties(classLoader,PROPERTIES_NAME);
            String name = (String) properties.get("name");
            String version = (String) properties.get("version");
            if (pluginName.equals(name) && pluginVersion.equals(version)){
                FileUtils.write(new File(UpdateInit.deletePluginPath),plugin.getAbsolutePath(),"utf-8",true);
            }
        }

        //重启系统
        LOGGER.info("系统重启");
        N_m3u8DL_RE.Cmd("/restartTomat.sh");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
