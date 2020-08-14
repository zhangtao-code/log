package org.github.mybatis.spring.util;

public class TraceLocal {
    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static void set(String id) {
        threadLocal.set(id);
    }

    public static String get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }
}
