package me.geoffrey.tomcat.server.container;

import me.geoffrey.tomcat.server.enums.HttpStatusEnum;
import me.geoffrey.tomcat.server.http.carrier.HttpRequest;
import me.geoffrey.tomcat.server.http.carrier.HttpResponse;
import me.geoffrey.tomcat.server.http.carrier.facade.HttpRequestFacade;
import me.geoffrey.tomcat.server.http.carrier.facade.HttpResponseFacade;
import org.apache.catalina.Container;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;

/**
 * @author Geoffrey.Yip
 * @time 2018/1/11 22:08
 * @description
 */

public class SimpleContainer implements Container {
    /**
     * 用户自定义web项目的相对路径
     */
    public static final String WEB_PROJECT_ROOT;
    /**
     * 加载Servlet的URL CLass Loader
     **/
    private static final URLClassLoader URL_CLASS_LOADER;

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleContainer.class);

    static {
        //初始化用户的相对目录
        URL webrootURL = SimpleContainer.class.getClassLoader().getResource("webroot");
        WEB_PROJECT_ROOT = Optional.ofNullable(webrootURL)
                .orElseThrow(() -> new IllegalStateException("can't not find user web root file."))
                .getFile().substring(1);
        try {
            URL servletClassPath = new File(SimpleContainer.WEB_PROJECT_ROOT, "servlet").toURI().toURL();
            URL_CLASS_LOADER = new URLClassLoader(new URL[]{servletClassPath});
        } catch (Exception e) {
            LOGGER.warn("initialized servlet classloader is fail!", e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        HttpRequest httpServletRequest = (HttpRequest) request;
        HttpResponse httpServletResponse = (HttpResponse) response;
        //根据请求的URI截取Servlet的名字
        String servletName = this.parseServletName(httpServletRequest.getRequestURI());
        //使用URLClassLoader加载这个Servlet
        Class servletClass;
        try {
            servletClass = URL_CLASS_LOADER.loadClass(servletName);
        } catch (ClassNotFoundException e) {
            LOGGER.info("servlet {} not found!", servletName);
            //实例化失败则调用404页面
            httpServletResponse.accessStaticResources();
            return;
        }
        try {
            //实例化这个Servlet
            Servlet servlet = (Servlet) servletClass.newInstance();
            httpServletResponse.getWriter().print(new String(httpServletResponse.responseToByte(HttpStatusEnum.OK)));
            servlet.service(new HttpRequestFacade(httpServletRequest), new HttpResponseFacade(httpServletResponse));
            response.finishResponse();
        } catch (Exception e) {
            LOGGER.info(String.format("Invoke Servlet %s is fail!", servletName), e);
        }
    }

    /**
     * 解析到用户请求的Servlet类名
     *
     * @param uri 请求URI
     * @return Servlet类名
     */
    private String parseServletName(String uri) {
        return uri.substring(uri.lastIndexOf("/") + 1);
    }
}
