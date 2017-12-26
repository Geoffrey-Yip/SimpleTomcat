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

/**
 * @author Geoffrey.Yip
 * @Time 2017/12/25 22:38
 * @Description Http服务类
 */
public class HttpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    public static final String WEB_PROJECT_ROOT = HttpServer.class.getClassLoader().getResource("").getPath().substring(1) + "webroot";
    private static final String SHUTDOWN_SERVER = "SHUTDOWN-SERVER";
    private transient boolean shutdowned = false;

    public static void main(String[] args) throws IOException {
        LOGGER.info("server start...");
        new HttpServer().await();
    }

    private void await() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080, 10, InetAddress.getByName("127.0.0.1"));

        while (!shutdowned) {
            Socket accept = serverSocket.accept();
            InputStream inputStream = accept.getInputStream();
            OutputStream outputStream = accept.getOutputStream();
            Request request = new Request(inputStream);
            if (request.getUri() != null && !"".equals(request.getUri().trim())) {
                Response resp = new Response(outputStream, request);
                resp.accessStaticResources();
            }
            outputStream.close();
            inputStream.close();
            accept.close();
        }
        serverSocket.close();
    }
}
