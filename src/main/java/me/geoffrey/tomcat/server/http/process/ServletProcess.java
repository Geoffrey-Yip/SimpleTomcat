package me.geoffrey.tomcat.server.http.process;

import me.geoffrey.tomcat.server.connector.HttpConnector;
import me.geoffrey.tomcat.server.enums.HttpStatusEnum;
import me.geoffrey.tomcat.server.http.carrier.HttpRequest;
import me.geoffrey.tomcat.server.http.carrier.HttpResponse;
import me.geoffrey.tomcat.server.http.carrier.facade.HttpRequestFacade;
import me.geoffrey.tomcat.server.http.carrier.facade.HttpResponseFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/31 0:49
 * @description Servlet处理器
 */
public class ServletProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletProcess.class);

    /**
     * 加载Servlet的URL CLass Loader
     **/
    private static final URLClassLoader URL_CLASS_LOADER;

    static {
        try {
            URL servletClassPath = new File(HttpConnector.WEB_PROJECT_ROOT, "servlet").toURI().toURL();
            URL_CLASS_LOADER = new URLClassLoader(new URL[]{servletClassPath});
        } catch (Exception e) {
            LOGGER.warn("initialized servlet classloader is fail!", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据Request执行相应的Servlet
     *
     * @param request  request对象
     * @param response response对象
     */
    public void process(HttpRequest request, HttpResponse response) throws IOException {
        //根据请求的URI截取Servlet的名字
        String servletName = this.parseServletName(request.getRequestURI());
        //使用URLClassLoader加载这个Servlet
        Class servletClass;
        try {
            servletClass = URL_CLASS_LOADER.loadClass(servletName);
        } catch (ClassNotFoundException e) {
            LOGGER.info("servlet {} not found!", servletName);
            //实例化失败则调用404页面
            response.accessStaticResources();
            return;
        }
        try {
            //实例化这个Servlet
            Servlet servlet = (Servlet) servletClass.newInstance();
            response.getWriter().print(new String(response.responseToByte(HttpStatusEnum.OK)));
            servlet.service(new HttpRequestFacade(request), new HttpResponseFacade(response));
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
