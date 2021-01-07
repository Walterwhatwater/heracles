package io.heracles.util;

import java.util.Collection;
import java.util.Map;

/**
 * 集合工具类
 *
 * @author walter
 * @date 2021/01/07 23:38
 **/
public class CollectionUtils {
    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }
}
