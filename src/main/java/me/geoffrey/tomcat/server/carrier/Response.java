package me.geoffrey.tomcat.server.carrier;

import me.geoffrey.tomcat.server.HttpServer;
import me.geoffrey.tomcat.server.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/25 22:57
 * @description http响应载体
 */
public class Response {
    private static final Logger LOGGER = LoggerFactory.getLogger(Response.class);

    private OutputStream outputStream;
    private Request request;

    public Response(OutputStream outputStream, Request request) {
        this.outputStream = outputStream;
        this.request = request;
    }

    public void accessStaticResources() throws IOException {
        LOGGER.info("{},{}", HttpServer.WEB_PROJECT_ROOT, request.getUri());
        File staticResource = new File(HttpServer.WEB_PROJECT_ROOT + request.getUri());
        if (!staticResource.exists() || !staticResource.isFile()) {
            return;
        }
        byte[] cache = ArrayUtil.generatorCache();
        FileInputStream fis = new FileInputStream(staticResource);
        int read = fis.read(cache, 0, ArrayUtil.BUFFER_SIE);
        StringBuilder sb = new StringBuilder();
        while (read != -1) {
            sb.append(new String(cache));
            outputStream.write(cache, 0, read);
            read = fis.read(cache, 0, ArrayUtil.BUFFER_SIE);
        }
        LOGGER.info("output====");
        LOGGER.info(sb.toString());
        fis.close();
    }

}
