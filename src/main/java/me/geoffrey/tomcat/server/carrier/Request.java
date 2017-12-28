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
        // Read a set of characters from the socket
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
        System.out.print(request.toString());
        this.setUri(request.toString());
    }


}
