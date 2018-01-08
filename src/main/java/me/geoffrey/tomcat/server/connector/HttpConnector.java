package me.geoffrey.tomcat.server.connector;

import me.geoffrey.tomcat.server.http.process.HttpProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/25 22:38
 * @description Http服务类
 */
public class HttpConnector implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnector.class);

    /**
     * 用户自定义web项目的相对路径
     */
    public static final String WEB_PROJECT_ROOT;

    /**
     * 是否关闭服务器标识
     */
    private transient boolean shutdowned;

    static{
        //initialization relative Directory
        URL webrootURL = HttpConnector.class.getClassLoader().getResource("webroot");
        WEB_PROJECT_ROOT = Optional.ofNullable(webrootURL)
                .orElseThrow(() -> new IllegalStateException("can't not find user web root file."))
                .getFile().substring(1);
    }

    @Override
    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(8080, 1, InetAddress.getByName("127.0.0.1"));
            LOGGER.info("Server is starting ... listener port {}",8080);
        } catch (IOException e) {
            LOGGER.error("Server shutdown!",e);
            throw new RuntimeException(e);
        }

        while (!shutdowned) {
            try (Socket accept = serverSocket.accept()) {
                HttpProcess process = new HttpProcess(this);
                process.process(accept);
            } catch (IOException e) {
                LOGGER.warn("Catch from user process.",e);
            }
        }
        //关闭服务器
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.error("Shutdown server is fail!", e);
        }
    }

    public void start(){
        new Thread(this).start();
    }
}
