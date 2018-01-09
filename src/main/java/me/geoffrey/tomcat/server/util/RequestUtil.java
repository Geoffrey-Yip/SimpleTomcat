/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/util/RequestUtil.java,v 1.19 2002/02/21 22:51:55 remm Exp $
 * $Revision: 1.19 $
 * $Date: 2002/02/21 22:51:55 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */


package me.geoffrey.tomcat.server.util;

import java.util.Map;
import java.util.stream.Stream;

/**
 * 请求工具类
 */
public final class RequestUtil {

    /**
     * 解析请求参数
     *
     * @param map      Request对象中的参数map
     * @param params   解析前的参数
     * @param encoding 编码
     */
    public static void parseParameters(ParameterMap map, String params, String encoding) {
        if (StringUtil.isBlank(params)) {
            return;
        }
        String[] paramArray = params.split("&");
        if (ArrayUtil.isEmpty(paramArray)) {
            return;
        }
        Stream.of(paramArray).forEach(param -> {
            String[] splitParam = param.split("=");
            String name = splitParam[0];
            String value = splitParam[1];
            putMapEntry(map, StringUtil.urlDecode(name, encoding), StringUtil.urlDecode(value, encoding));
        });
    }

    /**
     * 将key和value添加进map中
     *
     * @param map   Map
     * @param name  key
     * @param value value
     */
    private static void putMapEntry(Map<String, String[]> map, String name, String value) {
        String[] newValues;
        String[] oldValues = map.get(name);
        if (oldValues == null) {
            newValues = new String[1];
            newValues[0] = value;
        } else {
            newValues = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }
        map.put(name, newValues);
    }




    /**
     * 解析请求头的字符编码
     * @param contentType 请求头字符编码
     */
    public static String parseCharacterEncoding(String contentType) {
        if (contentType == null) {
            return null;
        }
        int start = contentType.indexOf("charset=");
        if (start < 0) {
            return null;
        }
        String encoding = contentType.substring(start + 8);
        int end = encoding.indexOf(';');
        if (end >= 0) {
            encoding = encoding.substring(0, end);
        }
        encoding = encoding.trim();
        if ((encoding.length() > 2) && (encoding.startsWith("\""))
                && (encoding.endsWith("\""))) {
            encoding = encoding.substring(1, encoding.length() - 1);
        }
        return encoding.trim();

    }
    /**
     * 规范化URI/检测非法URI
     *
     * @param path URI
     * @return 规范化后的URI （null 规范化失败）
     */
    public static String normalize(String path) {
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

