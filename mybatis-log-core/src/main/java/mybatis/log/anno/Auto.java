package mybatis.log.anno;

public @interface Auto {
    boolean insert() default true;

    boolean update() default false;
}
