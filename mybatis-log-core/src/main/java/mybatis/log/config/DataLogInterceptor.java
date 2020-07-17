package mybatis.log.config;


import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Intercepts(
        {
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
        }
)
@Component
public class DataLogInterceptor implements Interceptor {
    private Map<String, Boolean> interceptorMap = new ConcurrentHashMap<>();

    private String name;
    private Mapper mapper;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();
        String userId = getUserId();
        if (StringUtils.isEmpty(userId)) {
            return result;
        }
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        String id = ms.getId();
        Boolean filter = interceptorMap.get(id);
        if (filter == null) {
            int index = id.lastIndexOf(".");
            String mapperName = id.substring(0, index);
            String methodName = id.substring(index);
            Class clazz = Class.forName(mapperName);
            Method method = clazz.getDeclaredMethod(methodName);
            if (method == null) {
                interceptorMap.put(id, false);
            }
            Annotation annotation = method.getDeclaredAnnotation(Log.class);
            if (annotation == null) {
                interceptorMap.put(id, false);
                return result;
            }
            interceptorMap.put(id, true);
        } else if (!filter) {
            return result;
        }
        Map<String, Object> parameter = (Map<String, Object>) args[1];
        Executor executor = (Executor) invocation.getTarget();
        MappedStatement query = ms.getConfiguration().getMappedStatement(name);
        if (query == null) {
            return result;
        }
        Object log = executor.query(query, parameter, RowBounds.DEFAULT, null);
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    protected String getUserId() {
        return null;
    }

    protected Long getId(Object object) {
        return null;
    }

}
