package me.geoffrey.tomcat.server.carrier;

import me.geoffrey.tomcat.server.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/25 22:57
 * @description http请求载体
 */
public class Request {

    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);
    /**用户请求输入流**/
    private InputStream requestStream;
    /**解析用户请求后的URI**/
    private String uri;

    /**
     * 解析用户的请求
     */
    public void parseRequest() {
        StringBuilder request = new StringBuilder();
        int i;
        byte[] buffer = ArrayUtil.generatorCache();
        try {
            i = requestStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            i = -1;
        }
        for (int j = 0; j < i; j++) {
            request.append((char) buffer[j]);
        }
        LOGGER.trace("parse request {}", request.toString());
        this.setUri(request.toString());
    }

    /**
     * 将解析的用户请求信息筛选出请求URI
     * @param parsedContent 用户的请求信息
     */
    private void setUri(String parsedContent) {
        //获取第一个空格的索引，是用户请求method后面的空格 例如 GET /index.html ABCD.....
        int oneSpace = parsedContent.indexOf(" ");
        //获取第二个空格的索引，是用户请求URI后面的第一个空格
        int twoSpace = parsedContent.indexOf(" ", oneSpace + 1);
        if (oneSpace == -1 || twoSpace == -1) {
            LOGGER.debug("Parse Request is empty.");
            return;
        }
        //截取获得用户请求URI
        uri = parsedContent.substring(oneSpace + 1, twoSpace);
        LOGGER.info("request URI:{}", uri);
    }

    public InputStream getRequestStream() {
        return requestStream;
    }

    public void setRequestStream(InputStream requestStream) {
        this.requestStream = requestStream;
    }

    public String getUri() {
        return uri;
    }

}
