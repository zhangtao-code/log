package mybatis.log.handler;


import com.mybatis.log.anno.Log;
import com.mybatis.log.anno.LogId;
import com.mybatis.log.query.IQuery;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class UpdateHandler extends AbstractHandler {
    private IQuery query;

    @Override
    protected String parse(Object object, Class clazz, Log log) throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        Optional<Field> option = Arrays.stream(fields).filter(f -> f.getDeclaredAnnotation(LogId.class) != null).findFirst();
        if (!option.isPresent()) {
            throw new NullPointerException();
        }
        Field field = option.get();
        long id = field.getLong(object);
        Object result = query.query(id);
        return toJson(result);
    }


}
