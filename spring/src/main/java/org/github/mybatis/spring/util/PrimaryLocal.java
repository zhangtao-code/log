package org.github.mybatis.spring.util;

public class PrimaryLocal {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void set(long id) {
        threadLocal.set(id);
    }

    public static long get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }

}
