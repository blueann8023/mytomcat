package com.lm.mytomcat.chap3.connector;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import com.lm.mytomcat.chap2.Constants;
import com.lm.mytomcat.chap3.connector.http.HttpRequest;
import com.lm.mytomcat.chap3.connector.http.HttpResponse;

public class ServletProcessor {

    public void process(HttpRequest request, HttpResponse response) {
        String uri = request.getRequestURI();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        // This class loader is used to load classes and resources from a search path of URLs referring to both JAR files and directories
        URLClassLoader classLoader = null;

        try {
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(Constants.WEB_ROOT);
            String repository = new URL("file", null, classPath.getCanonicalPath() + File.separator).toString();

            // spec 当作url来分析的字符串 context对spec提供补充 handler协议流处理器 其实是建立连接 但这里是空的 只是为了让编译器识别
            // URL(URL context, String spec, URLStreamHandler handler)
            urls[0] = new URL(null, repository, streamHandler);
            classLoader = new URLClassLoader(urls);// 装载类到类装载器
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Class servletClass = null;
        try {
            servletClass = classLoader.loadClass(servletName);// 装载类到类
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Servlet servlet = null; 
        
        try {
            servlet = (Servlet)servletClass.newInstance();
            servlet.service(request, response);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
