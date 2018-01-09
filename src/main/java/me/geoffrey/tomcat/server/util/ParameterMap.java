/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/util/ParameterMap.java,v 1.2 2001/07/22 20:25:13 pier Exp $
 * $Revision: 1.2 $
 * $Date: 2001/07/22 20:25:13 $
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


import java.util.HashMap;
import java.util.Map;

/**
 * Request参数Map，一个增强版的HashMap
 * 内置布尔值锁字段，如果锁字段为真，则Map为只读状态
 * 其他功能均与
 * @see HashMap 一致
 * @author undefind
 */
public final class ParameterMap extends HashMap<String,String[]> {

    public ParameterMap() {
        super();
    }

    public ParameterMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ParameterMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }
    public ParameterMap(Map<String,String[]> map) {
        super(map);
    }

    /**
     * 此参数表示Map的锁定状态
     */
    private boolean locked = false;

    /**
     * 返回map的锁定状态
     */
    public boolean isLocked() {

        return (this.locked);

    }
    /**
     * 设置Map的锁定状态
     *
     * @param locked 锁定状态
     */
    public void setLocked(boolean locked) {

        this.locked = locked;

    }
    /**
     * 清除Map中的所有映射
     *
     * @exception IllegalStateException 如果Map为只读时
     */
    @Override
    public void clear() {
        if (locked) {
            throw new IllegalStateException("ParameterMap is locked,clear map is fail!");
        }
        super.clear();
    }


    /**
     * 将参数名/参数内容放入map中，如果map已存在该参数名，则原先参数值会被替换
     * @param key   参数名
     * @param value 参数值数组
     * @return 已添加的参数值
     */
    @Override
    public String[] put(String key, String[] value) {

        if (locked) {
            throw new IllegalStateException("ParameterMap is locked,put map is fail!");
        }
        return (super.put(key, value));

    }


    /**
     * 将参数的内容全部拷贝进map中
     * @param map 被拷贝的参数
     */
    @Override
    public void putAll(Map<? extends String,? extends String[]> map) {

        if (locked) {
            throw new IllegalStateException("ParameterMap is locked,putAll map is fail!");
        }
        super.putAll(map);

    }

    /**
     * 删除参数名和参数内容
     * @param key 参数名
     * @return 删除的参数内容
     */
    @Override
    public String[] remove(Object key) {
        if (locked) {
            throw new IllegalStateException("ParameterMap is locked,remove map is fail!");
        }
        return (super.remove(key));

    }
}
