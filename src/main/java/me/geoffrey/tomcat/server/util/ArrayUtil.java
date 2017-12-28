package me.geoffrey.tomcat.server.util;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/26 23:44
 * @description 数组工具类
 */
public class ArrayUtil {

    public static final int BUFFER_SIZE = 1024;

    /**
     * 获取一个缓存byte数组
     * @return 获取结果
     */
    public static byte[] generatorCache(){
        return new byte[BUFFER_SIZE];
    }
}
