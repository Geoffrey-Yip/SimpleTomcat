package me.geoffrey.tomcat.server;

import me.geoffrey.tomcat.server.connector.HttpConnector;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/31 17:31
 * @description Tomcat启动类
 */
public final class Bootstrap {

    public static void main(String[] args){
        //启动连接器等待连接
        new HttpConnector().start();
    }
}
