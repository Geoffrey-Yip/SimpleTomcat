package me.geoffrey.tomcat.client;


import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/26 20:32
 * @description
 */

public class ClientTest {
    public static void main(String[] args)throws Exception{
        Socket socket = new Socket("127.0.0.1",8080);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write("GET /servlet/RegisterServlet HTTP/1.1".getBytes());
        socket.shutdownOutput();
        InputStream inputStream = socket.getInputStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        byte[] bytes = new byte[8096];
        StringBuilder sb = new StringBuilder();
        while (bufferedInputStream.read(bytes) != -1) {
            sb.append(new String(bytes));
        }
        System.out.println(sb.toString());
        socket.shutdownInput();
        socket.close();
    }
}
