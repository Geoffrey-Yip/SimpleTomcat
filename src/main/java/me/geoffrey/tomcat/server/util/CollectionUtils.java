package me.geoffrey.tomcat.server.util;

import java.util.Collection;

/**
 * @author Geoffrey.Yip
 * @time 2018/1/9 20:52
 * @description 集合工具类
 */
public class CollectionUtils {
    /**
     * 返回集合是否为空
     * @param collection 被检测集合
     * @param <E> 泛型
     * @return 校验结果
     */
    public static <E> boolean isEmpty(Collection<E> collection) {
        return collection == null || collection.isEmpty();
    }
}

