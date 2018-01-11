package me.geoffrey.tomcat.server.http.process;

import me.geoffrey.tomcat.server.connector.HttpConnector;
import me.geoffrey.tomcat.server.constant.HttpConstant;
import me.geoffrey.tomcat.server.enums.HTTPHeaderEnum;
import me.geoffrey.tomcat.server.http.carrier.HttpRequest;
import me.geoffrey.tomcat.server.http.carrier.HttpResponse;
import me.geoffrey.tomcat.server.util.RequestUtil;
import me.geoffrey.tomcat.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/31 17:40
 * @description Http处理器
 */
public class HttpProcess implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProcess.class);
    /**
     * Servlet资源请求起始字符串
     **/
    private static final String SERVLET_URI_START_WITH = "/servlet/";

    private int id;

    private HttpConnector httpConnector;

    private boolean available;

    /**
     * The background thread.
     */
    private Thread thread;
    /**
     * HttpRequest
     **/
    private HttpRequest request;
    /**
     * HttpResponse
     **/
    private HttpResponse response;

    private String threadName;

    private Socket socket;

    private boolean stopped;


    public HttpProcess(HttpConnector httpConnector, int id) {
        this.httpConnector = httpConnector;
        this.id = id;
        request = (HttpRequest) httpConnector.createRequest();
        response = (HttpResponse) httpConnector.createResponse();
        threadName = "HttpProcessor[" + httpConnector.getServerPort() + "][" + id + "]";
    }

    /**
     * 执行用户请求
     *
     * @param socket 请求socket
     */
    public void process(Socket socket) {

        try (InputStream input = socket.getInputStream();
             OutputStream output = socket.getOutputStream()) {
            //初始化request以及response
            request = new HttpRequest(input);
            response = new HttpResponse(output, request);
            //解析request请求和请求头
            this.parseRequest(input);
            this.parseHeaders(input);
            //调用对应的处理器处理
            if (request.getRequestURI().startsWith(SERVLET_URI_START_WITH)) {
                httpConnector.getContainer().invoke(request, response);
            } else {
                new StaticResourceProcess().process(request, response);
            }
        } catch (ServletException e) {
            LOGGER.info("Catch ServletException from Socket process :", e);
        } catch (IOException e){

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 解析请求行和校验URI安全性
     *
     * @param input socket输入流
     * @throws IOException      流错误
     * @throws ServletException 读取到的请求行有误
     */
    private void parseRequest(InputStream input) throws IOException, ServletException {
        StringBuilder temp = new StringBuilder();
        int cache;
        while ((cache = input.read()) != -1) {
            //请求行读取完毕
            if (HttpConstant.CARRIAGE_RETURN == cache && HttpConstant.LINE_FEED == input.read()) {
                break;
            }
            temp.append((char) cache);
        }
        String[] requestLineArray = temp.toString().split(" ");
        if (requestLineArray.length < 3) {
            throw new ServletException("HTTP request line is not standard！");
        }
        // 填充request的URI和方法信息
        request.setMethod(requestLineArray[0]);
        request.setProtocol(requestLineArray[2]);
        String uri = requestLineArray[1];
        int question = uri.indexOf("?");
        if (question >= 0) {
            request.setQueryString(uri.substring(question + 1, uri.length()));
            uri = uri.substring(0, question);
        }

        // 如果URI是绝对路径则替换成相对路径
        if (!uri.startsWith("/")) {
            //获取 http:// 中://的索引
            int pos = uri.indexOf("://");
            if (pos != -1) {
                //获取相对路径的第一个/索引
                pos = uri.indexOf('/', pos + 3);
                if (pos == -1) {
                    uri = "";
                } else {
                    //直接根据索引截取到URI
                    uri = uri.substring(pos);
                }
            }
        }

        // 解析查询字符串是否携带jsessionid，如果有则设置sessionid信息
        String match = ";jsessionid=";
        int semicolon = uri.indexOf(match);
        if (semicolon >= 0) {
            String rest = uri.substring(semicolon + match.length());
            int semicolon2 = rest.indexOf(';');
            if (semicolon2 >= 0) {
                request.setRequestedSessionId(rest.substring(0, semicolon2));
                rest = rest.substring(semicolon2);
            } else {
                request.setRequestedSessionId(rest);
                rest = "";
            }
            request.setRequestedSessionURL(true);
            uri = uri.substring(0, semicolon) + rest;
        } else {
            request.setRequestedSessionId(null);
            request.setRequestedSessionURL(false);
        }

        //校验URI有没有不符合规范或者不正常的地方，修正
        String normalizedUri = RequestUtil.normalize(uri);
        if (normalizedUri == null) {
            throw new ServletException("Invalid URI: " + uri + "'");
        }
        request.setRequestURI(normalizedUri);
    }

    /**
     * 解析HTTP请求头
     *
     * @param input socket输入流
     * @throws IOException 读取出错
     */
    private void parseHeaders(InputStream input) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cache;
        while (input.available() > 0 && (cache = input.read()) > -1) {
            sb.append((char) cache);
        }
        //使用\r\n分割请求头
        Queue<String> headers = Stream.of(sb.toString().split("\r\n")).collect(toCollection(LinkedList::new));
        //获取一个请求头
        while (!headers.isEmpty()) {
            String headerString = headers.poll();
            //读取到空行则说明请求头已读取完毕
            if (StringUtil.isBlank(headerString)) {
                break;
            }
            //分割请求头的key和value
            String[] headerKeyValue = headerString.split(": ");
            request.addHeader(headerKeyValue[0], headerKeyValue[1]);
        }

        //如果在读取到空行后还有数据，说明是POST请求的表单参数
        if (!headers.isEmpty()) {
            request.setPostParams(headers.poll());
        }

        //设置请求参数
        String contentLength = request.getHeader(HTTPHeaderEnum.CONTENT_LENGTH.getDesc());
        if (contentLength != null) {
            request.setContentLength(Integer.parseInt(contentLength));
        }
        request.setContentType(request.getHeader(HTTPHeaderEnum.CONTENT_TYPE.getDesc()));
        request.setCharacterEncoding(RequestUtil.parseCharacterEncoding(request.getHeader(request.getContentType())));

        Cookie[] cookies = parseCookieHeader(request.getHeader(HTTPHeaderEnum.COOKIE.getDesc()));
        Optional.ofNullable(cookies).ifPresent(cookie ->
                Stream.of(cookie).forEach(c -> request.addCookie(c))
        );
        //如果sessionid不是从cookie中获取的，则优先使用cookie中的sessionid
        if (!request.isRequestedSessionIdFromCookie() && cookies != null) {
            Stream.of(cookies)
                    .filter(cookie -> "jsessionid".equals(cookie.getName()))
                    .findFirst().
                    ifPresent(cookie -> {
                        //设置cookie的值
                        request.setRequestedSessionId(cookie.getValue());
                        request.setRequestedSessionCookie(true);
                        request.setRequestedSessionURL(false);
                    });
        }
    }


    /**
     * 将cookie字符串解析成cookie数组
     *
     * @param cookieListString 请求头中的cookie字符串
     * @return cookie数组
     */
    private Cookie[] parseCookieHeader(String cookieListString) {
        if (StringUtil.isBlank(cookieListString)) {
            return null;
        }
        return Stream.of(cookieListString.split("; "))
                .map(cookieStr -> {
                    String[] cookieArray = cookieStr.split("=");
                    return new Cookie(StringUtil.urlDecode(cookieArray[0]), StringUtil.urlDecode(cookieArray[1]));
                }).toArray(Cookie[]::new);
    }


    @Override
    public void run() {
        while (!stopped) {
            Socket socket = await();
            process(socket);
            httpConnector.recycle(this);
        }
    }

    private Socket await(){
        while(!available){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        available = false;
        Socket socket = this.socket;
        notifyAll();
        return socket;
    }

    public void start() {
        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();
    }

    public void assign(Socket socket) {
        while (available) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.socket = socket;
        available = true;
        notifyAll();
    }
}
