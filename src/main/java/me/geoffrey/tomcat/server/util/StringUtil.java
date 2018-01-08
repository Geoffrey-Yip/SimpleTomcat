package me.geoffrey.tomcat.server.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author Geoffrey.Yip
 * @time 2017/12/31 19:54
 * @description
 */

public class StringUtil {
    public static final String EMPTY_STRING = "";

    public static final String ISO88591= "ISO-8859-1";

    public static boolean isBlank(String str){
        return str == null || str.trim().equals(EMPTY_STRING);
    }

    public static boolean isNotBlank(String str){
        return !isBlank(str);
    }

    public static String urlDecode(String str,String encoding){
        try {
            return URLDecoder.decode(str, encoding == null ? ISO88591 : encoding);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String urlDecode(String str){
        try {
            return URLDecoder.decode(str, ISO88591);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
