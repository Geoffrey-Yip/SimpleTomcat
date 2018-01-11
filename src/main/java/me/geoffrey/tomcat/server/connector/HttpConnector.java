package me.geoffrey.tomcat.server.connector;

import me.geoffrey.tomcat.server.http.carrier.HttpRequest;
import me.geoffrey.tomcat.server.http.carrier.HttpResponse;
import me.geoffrey.tomcat.server.http.process.HttpProcess;
import org.apache.catalina.Connector;
import org.apache.catalina.Container;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.net.DefaultServerSocketFactory;
import org.apache.catalina.net.ServerSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/25 22:38
 * @description Http连接器，用于等待客户端请求并将连接转交执行器执行
 * @see HttpProcess HTTP执行器
 */
public class HttpConnector implements Runnable, Connector {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnector.class);

    private final int SERVER_PORT = 8080;
    private static final int MAX_ACCEPT_COUNT = 10;

    public static final int DEFAULT_CONNECTION_TIMEOUT = 60000;


    private Container container;

    private ServerSocketFactory factory;

    private ServerSocket serverSocket;

    private BlockingQueue<HttpProcess> processors = new ArrayBlockingQueue<>(maxProcessors);

    /**
     * The minimum number of processors to start at initialization time.
     */
    protected int minProcessors = 5;


    /**
     * The maximum number of processors allowed, or <0 for unlimited.
     */
    private static int maxProcessors = 20;

    private int curProcessors = 0;

    /**
     * 标识连接器是否已被初始化过
     **/
    private boolean initialized;

    private String threadName;

    private Thread thread;

    /**
     * 是否关闭服务器标识(后期会实现更优雅关闭方式)
     */
    private transient boolean shutdowned;


    /**
     * 开启线程和Socket服务端，等待并处理连接
     */
    @Override
    public void run() {
        while (!shutdowned) {
            Socket socket;
            try {
                socket = serverSocket.accept();
                socket.setSoTimeout(DEFAULT_CONNECTION_TIMEOUT);
                socket.setTcpNoDelay(true);
            } catch (IOException ace) {
                continue;
            }
            HttpProcess processor = createProcesser();
            processor.assign(socket);
        }
    }

    /**
     * 开启线程
     */
    public void start() {
        //创建Process队列池
        while (curProcessors < minProcessors) {
            if ((maxProcessors > 0) && (curProcessors >= maxProcessors)) {
                break;
            }
            newProcessor();
        }
        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();
    }

    private HttpProcess newProcessor() {
        HttpProcess processor = new HttpProcess(this, curProcessors++);
        processor.start();
        recycle(processor);
        return processor;
    }

    private HttpProcess createProcesser() {
        if (processors.isEmpty() && curProcessors < maxProcessors) {
           return newProcessor();
        }
        try {
            return processors.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public void recycle(HttpProcess processor) {
        try {
            processors.put(processor);
        } catch (InterruptedException e) {
            LOGGER.warn("HttpProcesser Queue Interrupted!", e);
        }
    }

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public ServerSocketFactory getFactory() {
        return factory == null ? factory = new DefaultServerSocketFactory() : factory;
    }

    @Override
    public void setFactory(ServerSocketFactory factory) {
        this.factory = factory;
    }

    @Override
    public Request createRequest() {
        Request request = new HttpRequest();
        request.setConnector(this);
        return request;
    }

    @Override
    public Response createResponse() {
        Response response = new HttpResponse();
        response.setConnector(this);
        return response;
    }

    @Override
    public void initialize() {
        if (initialized) {
            throw new IllegalStateException("HttpConnector has initialized!");
        }
        initialized = true;

        serverSocket = getFactory().createSocket(SERVER_PORT, MAX_ACCEPT_COUNT);

        threadName = "[" + this.getClass().getName() + "]-" + SERVER_PORT;
    }

    public int getServerPort() {
        return SERVER_PORT;
    }
}
