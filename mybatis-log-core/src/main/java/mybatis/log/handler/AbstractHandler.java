package mybatis.log.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mybatis.log.anno.Log;
import com.mybatis.log.anno.LogId;
import com.mybatis.log.common.JsonExclude;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class AbstractHandler implements IHandler {
    @Override
    public String handle(Object object) throws Exception {
        Class clazz = object.getClass();
        if (object instanceof List) {
            Type type = clazz.getGenericSuperclass();
            boolean cast = type instanceof ParameterizedType;
            if (!cast) {
                return null;
            }
            ParameterizedType parameterizedType = (ParameterizedType) type;
            clazz = (Class) parameterizedType.getActualTypeArguments()[0];
        }
        Log log = (Log) clazz.getDeclaredAnnotation(Log.class);
        if (log == null) {
            return null;
        }
        return parse(object, clazz, log);
    }

    protected abstract String parse(Object object, Class clazz, Log log) throws Exception;

    protected String toJson(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof List) {
            if (CollectionUtils.isEmpty((List) object)) {
                return null;
            }
        }
        Gson gson = new GsonBuilder().setExclusionStrategies(new JsonExclude()).create();
        return gson.toJson(object);
    }

    protected Long getId(Object object, Class clazz) throws Exception {
        if (object instanceof List) {
            List list = (List) object;
            object = list.get(0);
        }
        Field[] fields = clazz.getDeclaredFields();
        Optional<Field> optional = Arrays.stream(fields).filter(f -> f.getDeclaredAnnotation(LogId.class) != null).findFirst();
        if (!optional.isPresent()) {
            return null;
        }
        return (Long) optional.get().get(object);
    }
}
