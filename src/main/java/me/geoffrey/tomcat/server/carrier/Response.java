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
    private static final String message = "HTTP/1.1 404 File Not Found\r\n"+
            "Content-Type:text/html\r\n"+
            "Content-Length:23\r\n"+
            "\r\n"+"<h1>404 not found!</h1>";
    private static final String htmlMessage = "HTTP/1.1 200 OK\r\n"
            +"Content-Type: text/html; charset=utf-8\r\n\r\n";

    public Response(OutputStream outputStream, Request request) {
        this.outputStream = outputStream;
        this.request = request;
    }

    public void accessStaticResources() throws IOException {
        LOGGER.info("{},{}", HttpServer.WEB_PROJECT_ROOT, request.getUri());
        File staticResource = new File(HttpServer.WEB_PROJECT_ROOT + request.getUri());
        if (!staticResource.exists() || !staticResource.isFile()) {
            LOGGER.info("output====");
            LOGGER.info(message);
            outputStream.write(message.getBytes());
        } else {
            outputStream.write(htmlMessage.getBytes());
            byte[] cache = ArrayUtil.generatorCache();
            FileInputStream fis = new FileInputStream(staticResource);
            int read = fis.read(cache, 0, ArrayUtil.BUFFER_SIZE);
            StringBuilder sb = new StringBuilder();
            while (read != -1) {
                sb.append(new String(cache));
                outputStream.write(cache, 0, read);
                read = fis.read(cache, 0, ArrayUtil.BUFFER_SIZE);
            }
            LOGGER.info("output====");
            LOGGER.info(sb.toString());
            fis.close();
        }
    }

}
