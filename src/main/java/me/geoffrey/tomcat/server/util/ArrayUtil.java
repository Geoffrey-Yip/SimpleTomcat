package me.geoffrey.tomcat.server.util;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/26 23:44
 * @description
 */

public class ArrayUtil {

    public static final int BUFFER_SIE = 1024;

    public static byte[] generatorCache(){
        return new byte[BUFFER_SIE];
    }
}
