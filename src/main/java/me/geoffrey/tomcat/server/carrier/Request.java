package me.geoffrey.tomcat.server.carrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
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

    public Request(InputStream requestStream) {
        this.requestStream = requestStream;
        parseRequest(requestStream);
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

    private void parseRequest(InputStream in) {
        BufferedInputStream stream = new BufferedInputStream(in);
        byte[] cacheArray = new byte[8096];
        StringBuilder sb = new StringBuilder();
        int cache;
        try {
            while ((cache = stream.read(cacheArray)) != -1) {
                sb.append(new String(cacheArray, 0, cache));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.debug("=================parseRequest=================");
        String parsedContent = sb.toString();
        LOGGER.debug(parsedContent);
        this.setUri(parsedContent);
    }


}
