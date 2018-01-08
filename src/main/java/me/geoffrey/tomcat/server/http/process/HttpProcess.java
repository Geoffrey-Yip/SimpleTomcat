package me.geoffrey.tomcat.server.http.process;

import me.geoffrey.tomcat.server.connector.HttpConnector;
import me.geoffrey.tomcat.server.constant.HttpConstant;
import me.geoffrey.tomcat.server.http.carrier.HttpRequest;
import me.geoffrey.tomcat.server.http.carrier.HttpResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/31 17:40
 * @description Http处理器
 */
public class HttpProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProcess.class);
    /**
     * Servlet资源请求起始字符串
     **/
    private static final String SERVLET_URI_START_WITH = "/servlet/";

    private HttpConnector httpConnector;
    /**
     * HttpRequest
     **/
    private HttpRequest request;
    /**
     * HttpResponse
     **/
    private HttpResponse response;

    private InputStream input;
    private OutputStream output;

    /**
     * 构造方法
     *
     * @param httpConnector http连接器
     */
    public HttpProcess(HttpConnector httpConnector) {
        this.httpConnector = httpConnector;
    }

    /**
     * 执行用户请求
     *
     * @param socket 请求socket
     */
    public void process(Socket socket) throws IOException {
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            //初始化request以及response
            request = new HttpRequest(input);
            response = new HttpResponse(output, request);
            //解析request请求和请求头
            this.parseRequest();
            this.parseHeaders();
            //调用对应的处理器处理
            if (request.getRequestURI().startsWith(SERVLET_URI_START_WITH)) {
                new ServletProcess().process(request, response);
            } else {
                new StaticResourceProcess().process(request, response);
            }
        } catch (ServletException e) {
            LOGGER.info("Catch ServletException from Socket process :", e);
        } finally {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
        }

    }


    private void parseRequest() throws IOException, ServletException {
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
            return;
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
        String normalizedUri = this.normalize(uri);
        if (normalizedUri == null) {
            throw new ServletException("Invalid URI: " + uri + "'");
        }
        request.setRequestURI(normalizedUri);
    }

    private void parseHeaders() throws IOException, ServletException {
        //存储请求头key、value的链表集合
        LinkedList<String> headers = new LinkedList<>();
        //单次读取字节缓存
        int cache;
        //字符串缓存
        StringBuilder sb = new StringBuilder();
        while ((cache = input.read()) != -1) {
            //遇到\r\n时读取下一个内容
            if (this.isLine(HttpConstant.CARRIAGE_RETURN, HttpConstant.LINE_FEED, headers, sb, cache)) {
                //重置字符串缓存
                sb = new StringBuilder();
                //遇到‘:’‘ ’时也读取下一个内容
            } else if (this.isLine(HttpConstant.COLON, HttpConstant.SPACE, headers, sb, cache)) {
                sb = new StringBuilder();
                //否则就拼接到缓存中
            } else {
                sb.append((char) cache);
            }
        }
        while (headers.size() % 2 == 0 && !headers.isEmpty()) {
            //相邻的两个为一对
            request.addHeader(headers.pollFirst(), headers.pollFirst());
        }

        String contentLength = request.getHeader("content-length");
        if (contentLength != null) {
            request.setContentLength(Integer.parseInt(contentLength));
        }
        request.setContentType(request.getHeader("content-type"));

        Cookie[] cookies = parseCookieHeader(request.getHeader("cookie"));
        Optional.ofNullable(cookies).ifPresent(cookie -> {
            Stream.of(cookie).forEach(c -> request.addCookie(c));
        });
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


    /**
     * 连续读取字节符合条件则返回true，不符合则将读取的字节拼接到字符串缓存中
     *
     * @param beforeByte  前置字节
     * @param afterByte   后置字节
     * @param headers     请求头集合
     * @param sb          字符串缓存
     * @param beforeCache 前置读取字节缓存
     * @return 校验结果
     */
    private boolean isLine(byte beforeByte, byte afterByte, List<String> headers, StringBuilder sb, int beforeCache) throws IOException {
        int afterCache; //读取到前置字节符合条件时才读取第二个字节
        if (beforeByte == beforeCache) {
            afterCache = input.read();
            if (afterByte == afterCache) {
                if (!sb.toString().equals("")) {
                    headers.add(sb.toString());
                }
                return true;
            }
            //如果读取的第二个字节不符合条件，则直接拼接即可
            sb.append((char) beforeCache).append((char) afterCache);
        }
        return false;
    }

    /**
     * 规范化URI
     *
     * @param path URI
     * @return 规范化后的URI （null 规范化失败）
     */
    protected String normalize(String path) {
        if (path == null) {
            return null;
        }
        //拷贝一个副本
        String normalized = path;

        // 把/%7E 或 /%7e 替换成 /~
        if (normalized.startsWith("/%7E") || normalized.startsWith("/%7e")) {
            normalized = "/~" + normalized.substring(4);
        }

        //如果URI包含以下字符串，则停止规范化
        if ((normalized.contains("%25"))
                || (normalized.contains("%2F"))
                || (normalized.contains("%2E"))
                || (normalized.contains("%5C"))
                || (normalized.contains("%2f"))
                || (normalized.contains("%2e"))
                || (normalized.contains("%5c"))) {
            return null;
        }

        if ("/.".equals(normalized)) {
            return "/";
        }

        // 规范化斜杠
        if (normalized.indexOf('\\') >= 0) {
            normalized = normalized.replace('\\', '/');
        }
        //如果URI不是以斜杠开头，则拼接一个斜杠
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        // 将双斜杠替换为单斜杠
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0) {
                break;
            }
            normalized = normalized.substring(0, index) +
                    normalized.substring(index + 1);
        }

        // 将 "/./" 替换为单斜杠
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0) {
                break;
            }
            normalized = normalized.substring(0, index) +
                    normalized.substring(index + 2);
        }

        // 把 "/../" 替换为单斜杠
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0) {
                break;
            }
            // 试图使用URI做路径跳转，判断为非法请求
            if (index == 0) {
                return null;
            }
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
                    normalized.substring(index + 3);
        }

        //"/..." 也判定为非法请求
        if (normalized.contains("/...")) {
            return null;
        }

        return normalized;

    }
}
