package me.geoffrey.tomcat.server;

import me.geoffrey.tomcat.server.connector.HttpConnector;
import me.geoffrey.tomcat.server.container.SimpleContainer;
import org.apache.catalina.Container;

import java.io.IOException;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/31 17:31
 * @description Tomcat启动类
 */
public final class Bootstrap {

    public static void main(String[] args) throws IOException {
        HttpConnector connector = new HttpConnector();
        Container container = new SimpleContainer();
        connector.setContainer(container);
        connector.initialize();
        connector.start();
        /*应用程序等待按下任意键结束*/
        System.in.read();
    }
}
