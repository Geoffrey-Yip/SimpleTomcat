package org.apache.catalina;


import javax.servlet.ServletException;
import java.io.IOException;

/**
 * 容器接口 Tomcat4精简版
 */
public interface Container {

    void invoke(Request request, Response response) throws IOException, ServletException;
}
