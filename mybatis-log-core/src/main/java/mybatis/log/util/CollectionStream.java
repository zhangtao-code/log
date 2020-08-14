package mybatis.log.util;

import org.apache.commons.collections4.MapUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionStream {
    /**
     * 用于抽取key值
     *
     * @param collection
     * @param key        分类方法
     * @param <E>
     * @param <T>
     * @return
     */
    public static <E, T> Map<T, E> toIdentityMap(Collection<E> collection, Function<E, T> key) {
        if (CollectionUtils.isEmpty(collection) || key == null) {
            return Collections.EMPTY_MAP;
        }
        return collection.stream().collect(Collectors.toMap(key, Function.identity()));
    }

    /**
     * list转map
     *
     * @param collection
     * @param key
     * @param value
     * @param <E>
     * @param <T>
     * @param <U>
     * @return
     */
    public static <E, T, U> Map<T, U> toMap(Collection<E> collection, Function<E, T> key, Function<E, U> value) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.EMPTY_MAP;
        }
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        return collection.stream().collect(Collectors.toMap(key, value));
    }

    /**
     * 分类成list
     *
     * @param collection
     * @param key
     * @param <E>
     * @param <T>
     * @return
     */
    public static <E, T> Map<T, List<E>> groupList(Collection<E> collection, Function<E, T> key) {
        if (CollectionUtils.isEmpty(collection) || key == null) {
            return Collections.EMPTY_MAP;
        }
        return collection
                .stream()
                .collect(Collectors.groupingBy(key, Collectors.toList()));
    }

    /**
     * 分类成list
     *
     * @param collection
     * @param key1
     * @param key2
     * @param <E>
     * @param <T>
     * @return
     */
    public static <E, T, U> Map<T, Map<U, List<E>>> group2List(Collection<E> collection, Function<E, T> key1, Function<E, U> key2) {
        if (CollectionUtils.isEmpty(collection) || key1 == null || key2 == null) {
            return Collections.EMPTY_MAP;
        }
        return collection
                .stream()
                .collect(Collectors.groupingBy(key1, Collectors.groupingBy(key2, Collectors.toList())));
    }

    public static <E, T, U> Map<T, Map<U, E>> group2Map(Collection<E> collection, Function<E, T> key1, Function<E, U> key2) {
        if (CollectionUtils.isEmpty(collection) || key1 == null || key2 == null) {
            return Collections.EMPTY_MAP;
        }
        return collection
                .stream()
                .collect(Collectors.groupingBy(key1, Collectors.toMap(key2, Function.identity())));
    }

    /**
     * 转化
     *
     * @param collection
     * @param function
     * @param <E>
     * @param <T>
     * @return
     */
    public static <E, T> List<T> mapping2List(Collection<E> collection, Function<E, T> function) {
        if (CollectionUtils.isEmpty(collection) || function == null) {
            return Collections.EMPTY_LIST;
        }
        return collection.stream().map(function).collect(Collectors.toList());
    }

    /**
     * 转化
     *
     * @param collection
     * @param function
     * @param <E>
     * @param <T>
     * @return
     */
    public static <E, T> Set<T> mapping2Set(Collection<E> collection, Function<E, T> function) {
        if (CollectionUtils.isEmpty(collection) || function == null) {
            return Collections.EMPTY_SET;
        }
        return collection.stream().map(function).collect(Collectors.toSet());
    }

    public static  <T, U, V> List<T> map2List(Map<U, V> map, BiFunction<U, V, T> function) {
        if (MapUtils.isEmpty(map) || function == null) {
            return Collections.EMPTY_LIST;
        }
        return map.keySet().stream()
                .map(key -> function.apply(key, map.get(key)))
                .collect(Collectors.toList())
                ;
    }

}
