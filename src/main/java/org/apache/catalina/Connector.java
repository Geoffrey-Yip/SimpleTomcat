package org.apache.catalina;

import org.apache.catalina.net.ServerSocketFactory;

/**
 * 连接器接口
 * Tomcat4精简版
 */
public interface Connector {

    Container getContainer();

    void setContainer(Container container);

    ServerSocketFactory getFactory();

    void setFactory(ServerSocketFactory factory);

    Request createRequest();

    Response createResponse();

    void initialize();
}
