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

    /**
     * 关闭服务器的命令
     */
    private static final String SHUTDOWN_SERVER = "/SHUTDOWN-SERVER";
    /**
     * 是否关闭服务器标识
     */
    private transient boolean shutdowned = false;

    static{
        //initialization relative Directory
        URL webrootURL = HttpServer.class.getClassLoader().getResource("webroot");
        WEB_PROJECT_ROOT = Optional.ofNullable(webrootURL)
                .orElseThrow(() -> new IllegalStateException("can't not find user web root file."))
                .getFile().substring(1);
    }

    public static void main(String[] args) {
        //start
        new HttpServer().await();
    }

    private void await() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(8080, 1, InetAddress.getByName("127.0.0.1"));
            LOGGER.info("Server is starting ... listener port {}",8080);
        } catch (IOException e) {
            LOGGER.error("Server shutdown!",e);
            throw new RuntimeException(e);
        }

        while (!shutdowned) {
            try (Socket accept = serverSocket.accept();
                 InputStream inputStream = accept.getInputStream();
                 OutputStream outputStream = accept.getOutputStream()) {
                //解析用户的请求
                Request request = new Request();
                request.setRequestStream(inputStream);
                request.parseRequest();
                //生成相应的响应
                Response resp = new Response(outputStream, request);
                resp.accessStaticResources();
                //如果本次请求是关闭服务器则修改标识为关闭
                shutdowned = SHUTDOWN_SERVER.equals(request.getUri());
            } catch (IOException e) {
                LOGGER.warn("catch from user request.",e);
            }
        }
        //关闭服务器
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.error("Shutdown server is fail!", e);
        }
    }
}
