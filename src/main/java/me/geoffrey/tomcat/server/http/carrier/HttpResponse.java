package me.geoffrey.tomcat.server.http.carrier;

import me.geoffrey.tomcat.server.constant.HttpVersionConstant;
import me.geoffrey.tomcat.server.container.SimpleContainer;
import me.geoffrey.tomcat.server.enums.HttpStatusEnum;
import me.geoffrey.tomcat.server.util.ArrayUtil;
import org.apache.catalina.Connector;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Collection;
import java.util.Locale;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/25 22:57
 * @description http响应载体
 */
public class HttpResponse implements HttpServletResponse,Response {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpResponse.class);

    private OutputStream outputStream;
    private HttpRequest request;
    private PrintWriter writer;

    public void accessStaticResources() throws IOException {
        //根据请求URI找到用户对应请求的资源文件
        File staticResource = new File(SimpleContainer.WEB_PROJECT_ROOT + request.getRequestURI());
        //资源存在
        if (staticResource.exists() && staticResource.isFile()) {
            outputStream.write(responseToByte(HttpStatusEnum.OK));
            writeFile(staticResource);
            //资源不存在
        } else {
            staticResource = new File(SimpleContainer.WEB_PROJECT_ROOT + "/404.html");
            outputStream.write(responseToByte(HttpStatusEnum.NOT_FOUND));
            writeFile(staticResource);
        }
    }

    /**
     * 将读取到的资源文件输出
     *
     * @param file 读取到的文件
     * @throws IOException IOException
     */
    private void writeFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] cache = ArrayUtil.generatorCache();
            int read;
            while ((read = fis.read(cache, 0, ArrayUtil.BUFFER_SIZE)) != -1) {
                outputStream.write(cache, 0, read);
            }
        }
    }

    /**
     * 将请求行 请求头转换为byte数组
     *
     * @param status 响应http状态
     * @return 响应头byte数组
     */
    public byte[] responseToByte(HttpStatusEnum status) {
        return new StringBuilder().append(HttpVersionConstant.HTTP_1_1).append(" ")
                .append(status.getStatus()).append(" ")
                .append(status.getDesc()).append("\r\n\r\n")
                .toString().getBytes();
    }

    public HttpResponse() {
    }

    public HttpResponse(OutputStream outputStream, HttpRequest request) {
        this.outputStream = outputStream;
        this.request = request;
    }

    @Override
    public String getCharacterEncoding() {
        return "ISO-8859-1";
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public PrintWriter getReporter() {
        return null;
    }

    @Override
    public void recycle() {

    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer != null) {
            return writer;
        }
        return (writer = new PrintWriter(outputStream));
    }

    @Override
    public void setCharacterEncoding(String charset) {

    }

    @Override
    public void setContentLength(int len) {

    }

    @Override
    public void setContentType(String type) {

    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public void sendAcknowledgement() throws IOException {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Connector getConnector() {
        return null;
    }

    @Override
    public void setConnector(Connector connector) {

    }

    @Override
    public int getContentCount() {
        return 0;
    }

    @Override
    public void setAppCommitted(boolean appCommitted) {

    }

    @Override
    public boolean isAppCommitted() {
        return false;
    }

    @Override
    public boolean getIncluded() {
        return false;
    }

    @Override
    public void setIncluded(boolean included) {

    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }

    @Override
    public void setRequest(Request request) {

    }

    @Override
    public ServletResponse getResponse() {
        return null;
    }

    @Override
    public OutputStream getStream() {
        return null;
    }

    @Override
    public void setStream(OutputStream stream) {

    }

    @Override
    public void setSuspended(boolean suspended) {

    }

    @Override
    public boolean isSuspended() {
        return false;
    }

    @Override
    public void setError() {

    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public ServletOutputStream createOutputStream() throws IOException {
        return null;
    }

    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public String encodeURL(String url) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return null;
    }

    @Override
    public String encodeUrl(String url) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return null;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {

    }

    @Override
    public void sendError(int sc) throws IOException {

    }

    @Override
    public void sendRedirect(String location) throws IOException {

    }

    @Override
    public void setDateHeader(String name, long date) {

    }

    @Override
    public void addDateHeader(String name, long date) {

    }

    @Override
    public void setHeader(String name, String value) {

    }

    @Override
    public void addHeader(String name, String value) {

    }

    @Override
    public void setIntHeader(String name, int value) {

    }

    @Override
    public void addIntHeader(String name, int value) {

    }

    @Override
    public void setStatus(int sc) {

    }

    @Override
    public void setStatus(int sc, String sm) {

    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    @Override
    public void finishResponse() {
        // sendHeaders();
        // Flush and close the appropriate output mechanism
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }

    @Override
    public int getContentLength() {
        return 0;
    }
}
