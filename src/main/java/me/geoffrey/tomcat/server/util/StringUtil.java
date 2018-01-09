package me.geoffrey.tomcat.server.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/31 19:54
 * @description 字符串工具类
 */
public class StringUtil {

    public static final String EMPTY_STRING = "";

    public static final String ISO_8859_1= "ISO-8859-1";

    public static boolean isBlank(String str){
        return str == null || str.trim().equals(EMPTY_STRING);
    }

    public static boolean isNotBlank(String str){
        return !isBlank(str);
    }

    /**
     * URL解码
     * @param str 待解码字符串
     * @param encoding 字符编码
     * @return 解码结果
     */
    public static String urlDecode(String str,String encoding){
        try {
            return URLDecoder.decode(str, encoding);
        } catch (UnsupportedEncodingException e) {
            return urlDecode(str);
        }
    }

    public static String urlDecode(String str){
        try {
            return URLDecoder.decode(str, ISO_8859_1);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
