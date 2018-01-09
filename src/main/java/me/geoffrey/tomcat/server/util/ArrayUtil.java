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

    /**
     * 返回数组是否为空
     * @param array 被检测数组
     * @param <E> 泛型
     * @return 检测结果
     */
    public static <E> boolean isEmpty(E[] array) {
        return array == null || array.length == 0;
    }

}
