package me.geoffrey.tomcat.server;

import me.geoffrey.tomcat.server.carrier.Request;
import me.geoffrey.tomcat.server.carrier.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;

/**
 * @author Geoffrey.Yip
 * @Time 2017/12/25 22:38
 * @Description Http服务类
 */
public class HttpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);


    /**
     * 用户自定义web项目的相对路径
     */
    public static final String WEB_PROJECT_ROOT;

    private static final String SHUTDOWN_SERVER = "/SHUTDOWN-SERVER";

    private transient boolean shutdowned = false;

    static{
        URL webrootURL = HttpServer.class.getClassLoader().getResource("webroot");
        WEB_PROJECT_ROOT = Optional.ofNullable(webrootURL)
                .orElseThrow(() ->
                        new IllegalStateException("项目不存在")
                ).getFile().substring(1);
    }

    public static void main(String[] args) throws IOException {
        LOGGER.info("server start...");
        new HttpServer().await();
    }

    public void await() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080, 1, InetAddress.getByName("127.0.0.1"));

        while (!shutdowned) {
            Socket accept = null;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                accept = serverSocket.accept();
                inputStream = accept.getInputStream();
                outputStream = accept.getOutputStream();
                Request request = new Request();
                request.setRequestStream(inputStream);
                request.parseRequest();
                Response resp = new Response(outputStream, request);
                resp.accessStaticResources();
                shutdowned = SHUTDOWN_SERVER.equals(request.getUri());
            } finally {
                accept.close();
            }
        }
        serverSocket.close();
    }
}
