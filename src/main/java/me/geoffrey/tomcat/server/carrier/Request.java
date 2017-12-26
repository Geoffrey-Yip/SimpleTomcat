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

    private InputStream requestStream;
    private String uri;

    public InputStream getRequestStream() {
        return requestStream;
    }

    public void setRequestStream(InputStream requestStream) {
        this.requestStream = requestStream;
    }

    public String getUri() {
        return uri;
    }

    private void setUri(String parsedContent) {
        int oneSpace = parsedContent.indexOf(" ");
        int twoSpace = parsedContent.indexOf(" ", oneSpace + 1);
        if (oneSpace == -1 || twoSpace == -1) {
            return;
        }
        uri = parsedContent.substring(oneSpace + 1, twoSpace);
        LOGGER.info(uri);
    }

    public void parseRequest() {
        byte[] bytes = ArrayUtil.generatorCache();
        StringBuilder sb = new StringBuilder();
        try{
            while (requestStream.read(bytes) != -1) {
                sb.append(new String(bytes));
            }
        }catch (IOException e){
            LOGGER.warn("read request byte is fail!",e);
        }
        LOGGER.info("read request info:");
        String request = sb.toString();
        LOGGER.info(request);
        this.setUri(request);
    }


}
