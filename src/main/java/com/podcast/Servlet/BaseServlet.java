package com.podcast.Servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 通过资源路径进行方法分发
 */
public class BaseServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1. 获取请求路径
        String uri = req.getRequestURI(); // 例如路径为：/podcast2/loginServlet
        //2. 获取最后一段路径，方法名
        int index = uri.lastIndexOf('/');
        String methodName = uri.substring(index + 1); //  获取到资源的二级路径  loginServlet

        //执行方法
        Class<? extends BaseServlet> cls = this.getClass();
        //2.2 获取方法 Method对象
        try {
            Method method = cls.getMethod(methodName,HttpServletRequest.class,HttpServletResponse.class);
            //4,调用该方法
            method.invoke(this,req,resp);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
