package me.geoffrey.tomcat.server.http.process;

import me.geoffrey.tomcat.server.http.carrier.HttpRequest;
import me.geoffrey.tomcat.server.http.carrier.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/31 0:49
 * @description 静态资源处理器
 */
public class StaticResourceProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticResourceProcess.class);

    /**
     * 执行静态资源处理
     * @param request  request
     * @param response response
     * @throws IOException IO异常
     */
    public void process(HttpRequest request, HttpResponse response) throws IOException {
        LOGGER.debug("Start Process static resource...");
        response.accessStaticResources();
    }
}
