package me.geoffrey.tomcat.server.http.carrier;

import me.geoffrey.tomcat.server.enums.HTTPMethodEnum;
import me.geoffrey.tomcat.server.http.stream.RequestStream;
import me.geoffrey.tomcat.server.util.Enumerator;
import me.geoffrey.tomcat.server.util.ParameterMap;
import me.geoffrey.tomcat.server.util.RequestUtil;
import me.geoffrey.tomcat.server.util.StringUtil;
import org.apache.catalina.Connector;
import org.apache.catalina.Request;
import org.apache.catalina.Response;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/25 22:57
 * @description http请求类
 */
public class HttpRequest implements HttpServletRequest,Request {

    /**
     * 请求内容类型
     **/
    private String contentType;
    /**
     * 请求内容长度
     **/
    private int contentLength;
    /**
     * Internet协议(IP)地址
     **/
    private InetAddress inetAddress;
    /**
     * Socket客户端输入流
     **/
    private InputStream input;
    /**
     * HTTP请求方法
     **/
    private String method;
    /**
     * HTTP请求协议
     **/
    private String protocol;
    /**
     * URI携带的查询参数
     **/
    private String queryString;
    /**
     * POST请求表单参数
     **/
    private String postParams;
    /**
     * HTTP请求URI
     **/
    private String requestURI;
    /**
     * 服务器名称
     **/
    private String serverName;
    /**
     * 服务器端口
     **/
    private int serverPort;
    /**
     * Socket客户端对象
     **/
    private Socket socket;
    /**
     * jsessionid是否从cookie携带
     **/
    private boolean requestedSessionCookie;
    /**
     * jsessionid是否从URL携带
     **/
    private boolean requestedSessionURL;
    /**
     * 携带的jsessionid
     **/
    private String requestedSessionId;


    /**
     * 这个请求的属性Map
     */
    protected HashMap<String, Object> attributes = new HashMap<>();
    /**
     * 这个请求发送的授权凭据
     */
    protected String authorization;
    /**
     * 这个请求的上下文路径
     */
    protected String contextPath = "";
    /**
     * 该请求关联的Cookie列表
     */
    protected ArrayList<Cookie> cookies = new ArrayList<>();
    /**
     * 用于返回空枚举的空集合。请不要在此集合中添加任何元素~
     */
    protected static ArrayList empty = new ArrayList();
    /**
     * getDateHeader()方法中使用到的日期格式
     */
    protected SimpleDateFormat[] formats = {
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
            new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
            new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
    };

    /**
     * 与此请求关联的HTTP请求头，key:请求头名称 value:请求头内容数组
     */
    protected HashMap<String, ArrayList<String>> headers = new HashMap<>();
    /**
     * 该请求的解析参数。只有在通过getParameter()系列方法调用的系列中请求参数信息时，才会填充该信息
     * key是参数名，而value是该参数的的字符串数组。
     * 一旦对特定请求的参数进行解析并存储在这里，它们就不会被修改。因此，对参数的应用程序级别访问不需要同步。
     */
    protected ParameterMap parameters;

    /**
     * 标识本次请求的参数是否解析完毕
     */
    protected boolean parsed = false;
    protected String pathInfo;

    /**
     * Socket的InputStream的字符流版本
     */
    protected BufferedReader reader;

    /**
     * 封装的Socket InputStream (Servlet版本）
     */
    protected ServletInputStream stream;

    /**
     * 构造方法
     **/
    public HttpRequest(InputStream input) {
        this.input = input;
    }

    public HttpRequest() {
    }

    /**
     * 添加请求头
     *
     * @param name  请求头key
     * @param value 请求头内容
     */
    public void addHeader(String name, String value) {
        name = name.toLowerCase();
        ArrayList<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
        values.add(value);
    }

    /**
     * 如果本次请求参数并未解析，如果在URI和POST表单中都存在参数，
     * 则将它们合并，最后放入ParameterMap中
     */
    protected void parseParameters() {
        if (parsed) {
            return;
        }
        ParameterMap results = parameters;
        if (results == null) {
            results = new ParameterMap();
        }
        results.setLocked(false);
        String encoding = getCharacterEncoding();
        if (encoding == null) {
            encoding = StringUtil.ISO_8859_1;
        }
        // 解析URI携带的请求参数
        String queryString = getQueryString();
        RequestUtil.parseParameters(results, queryString, encoding);

        // 初始化Content-Type的值
        String contentType = getContentType();
        if (contentType == null) {
            contentType = "";
        }
        int semicolon = contentType.indexOf(';');
        if (semicolon >= 0) {
            contentType = contentType.substring(0, semicolon).trim();
        } else {
            contentType = contentType.trim();
        }
        //解析POST请求的表单参数
        if (HTTPMethodEnum.POST.name().equals(getMethod()) && getContentLength() > 0
                && "application/x-www-form-urlencoded".equals(contentType)) {
            RequestUtil.parseParameters(results, getPostParams(), encoding);
        }

        //解析完毕就锁定
        results.setLocked(true);
        parsed = true;
        parameters = results;
    }

    /**
     * 添加Cookie
     *
     * @param cookie 被添加的Cookie
     */
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    /**
     * 创建一个输入流，是一个RequestStream包装的Socket  InputStream
     *
     * @throws IOException if an input/output error occurs
     */
    @Override
    public ServletInputStream createInputStream() throws IOException {
        return (new RequestStream(this));
    }

    @Override
    public void finishRequest() throws IOException {

    }

    @Override
    public Object getNote(String name) {
        return null;
    }

    @Override
    public Iterator getNoteNames() {
        return null;
    }

    @Override
    public void recycle() {

    }

    @Override
    public void removeNote(String name) {

    }
    @Override
    public InputStream getStream() {
        return input;
    }

    @Override
    public void setStream(InputStream stream) {

    }
    @Override
    public void setContentLength(int length) {
        this.contentLength = length;
    }
    @Override
    public void setContentType(String type) {
        this.contentType = type;
    }

    @Override
    public void setNote(String name, Object value) {

    }

    public void setInet(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public void setContextPath(String path) {
        if (path == null) {
            this.contextPath = "";
        } else {
            this.contextPath = path;
        }
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPathInfo(String path) {
        this.pathInfo = path;
    }

    @Override
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public void setRemoteAddr(String remote) {

    }

    @Override
    public void setScheme(String scheme) {

    }

    @Override
    public void setSecure(boolean secure) {

    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    /**
     * 设置服务器的名称(虚拟主机)来处理这个请求。
     *
     * @param name 虚拟主机名
     */
    @Override
    public void setServerName(String name) {
        this.serverName = name;
    }

    /**
     * 设置服务器的端口号来处理这个请求。
     *
     * @param port 服务器端口号
     */
    @Override
    public void setServerPort(int port) {
        this.serverPort = port;
    }

    @Override
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * 设置本次请求的jsessionid是否为cookie携带传入
     *
     * @param flag jsessionid是否为cookie携带传入
     */
    public void setRequestedSessionCookie(boolean flag) {
        this.requestedSessionCookie = flag;
    }

    /**
     * 设置本次请求的jsessionid
     *
     * @param requestedSessionId jsessionid
     */
    public void setRequestedSessionId(String requestedSessionId) {
        this.requestedSessionId = requestedSessionId;
    }

    /**
     * 设置本次请求的jsessionid是否为URL携带传入
     *
     * @param flag jsessionid是否为URL携带传入
     */
    public void setRequestedSessionURL(boolean flag) {
        requestedSessionURL = flag;
    }

    /* 下面是实现HttpServletRequest的方法*/
    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return new Enumerator<>(attributes.keySet());
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public Cookie[] getCookies() {
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    @Override
    public long getDateHeader(String name) {
        String value = getHeader(name);
        if (value == null) {
            return (-1L);
        }

        // Work around a bug in SimpleDateFormat in pre-JDK1.2b4
        // (Bug Parade bug #4106807)
        value += " ";

        // Attempt to convert the date header in a variety of formats
        for (SimpleDateFormat format : formats) {
            try {
                Date date = format.parse(value);
                return (date.getTime());
            } catch (ParseException e) {
                ;
            }
        }
        throw new IllegalArgumentException(value);
    }

    /**
     * 获取请求头值
     *
     * @param name 请求头名字
     * @return 请求头值
     */
    @Override
    public synchronized String getHeader(String name) {
        if (name != null) {
            name = name.toLowerCase();
        }
        ArrayList<String> values = headers.get(name);
        if (values != null) {
            return values.get(0);
        } else {
            return null;
        }
    }

    /**
     * 获取所有请求头的名字集合
     *
     * @return 请求头名字集合
     */
    @Override
    public Enumeration getHeaderNames() {
        return new Enumerator<>(headers.keySet());
    }

    /**
     * 获取该请求头所有的值
     *
     * @param name 请求头名
     * @return 请求头值集合
     */
    @Override
    public Enumeration<String> getHeaders(String name) {
        name = name.toLowerCase();
        ArrayList<String> values = headers.get(name);
        if (values != null) {
            return new Enumerator<>(values);
        } else {
            return new Enumerator<>(empty);
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (reader != null) {
            throw new IllegalStateException("getInputStream has been called");
        }

        if (stream == null) {
            stream = createInputStream();
        }
        return (stream);
    }

    @Override
    public int getIntHeader(String name) {
        String value = getHeader(name);
        if (value == null) {
            return (-1);
        } else {
            return (Integer.parseInt(value));
        }
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration getLocales() {
        return null;
    }

    @Override
    public String getMethod() {
        return method;
    }

    /**
     * 获取请求参数
     *
     * @param name 参数名
     * @return 参数内容
     */
    @Override
    public String getParameter(String name) {
        parseParameters();
        String[] values = parameters.get(name);
        return Optional.ofNullable(values).map(arr -> arr[0]).orElse(null);
    }

    /**
     * 获取储存请求参数的Map
     *
     * @return 请求参数的Map
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        parseParameters();
        return this.parameters;
    }

    /**
     * 获取请求参数名集合
     *
     * @return 请求参数名集合
     */
    @Override
    public Enumeration<String> getParameterNames() {
        parseParameters();
        return (new Enumerator<>(parameters.keySet()));
    }

    @Override
    public String[] getParameterValues(String name) {
        parseParameters();
        return parameters.get(name);
    }

    @Override
    public String getPathInfo() {
        return pathInfo;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (stream != null) {
            throw new IllegalStateException("getInputStream has been called.");
        }
        if (reader == null) {
            String encoding = getCharacterEncoding();
            if (encoding == null) {
                encoding = "ISO-8859-1";
            }
            InputStreamReader isr =
                    new InputStreamReader(createInputStream(), encoding);
            reader = new BufferedReader(isr);
        }
        return (reader);
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return requestedSessionCookie;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return isRequestedSessionIdFromURL();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return requestedSessionURL;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public void removeAttribute(String attribute) {
    }

    @Override
    public void setAttribute(String key, Object value) {
    }

    @Override
    public String getAuthorization() {
        return null;
    }

    @Override
    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    @Override
    public Connector getConnector() {
        return null;
    }

    @Override
    public void setConnector(Connector connector) {

    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public ServletRequest getRequest() {
        return null;
    }

    @Override
    public Response getResponse() {
        return null;
    }

    @Override
    public void setResponse(Response response) {

    }

    @Override
    public Socket getSocket() {
        return socket;
    }

    @Override
    public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
    }

    public String getPostParams() {
        return postParams;
    }

    public void setPostParams(String postParams) {
        this.postParams = postParams;
    }
}
