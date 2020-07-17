package mybatis.log.handler;

import com.mybatis.log.anno.Auto;
import com.mybatis.log.anno.Log;
import com.mybatis.log.query.IQuery;

public class InsertHandler extends AbstractHandler {
    private IQuery query;

    @Override
    protected String parse(Object object, Class clazz, Log log) throws Exception {
        Auto auto = (Auto) clazz.getDeclaredAnnotation(Auto.class);
        if (auto == null || (!auto.insert())) {
            long id = getId(object, clazz);
            object = query.query(id);
        }
        return toJson(object);
    }

}
