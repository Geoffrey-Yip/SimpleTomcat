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
 * @description Http连接器，用于等待客户端请求并将连接转交执行器执行
 * @see HttpProcess HTTP执行器
 */
public class HttpConnector implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnector.class);

    /**
     * 用户自定义web项目的相对路径
     */
    public static final String WEB_PROJECT_ROOT;

    /**
     * 是否关闭服务器标识(后期会实现更优雅关闭方式)
     */
    private transient boolean shutdowned;

    static{
        //初始化用户的相对目录
        URL webrootURL = HttpConnector.class.getClassLoader().getResource("webroot");
        WEB_PROJECT_ROOT = Optional.ofNullable(webrootURL)
                .orElseThrow(() -> new IllegalStateException("can't not find user web root file."))
                .getFile().substring(1);
    }

    /**
     * 开启线程和Socket服务端，等待并处理连接
     */
    @Override
    public void run() {
        //开启SocketServer服务等待连接
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(8080, 1, InetAddress.getByName("127.0.0.1"));
            LOGGER.info("Server is starting ... listener port {}",8080);
        } catch (IOException e) {
            LOGGER.error("Server shutdown!",e);
            throw new RuntimeException(e);
        }

        while (!shutdowned) {
            //阻塞等待连接
            try (Socket accept = serverSocket.accept()) {
                //处理连接
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

    /**
     * 开启线程
     */
    public void start(){
        new Thread(this).start();
    }
}
