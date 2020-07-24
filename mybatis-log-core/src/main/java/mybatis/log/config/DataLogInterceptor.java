package mybatis.log.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mybatis.log.IModel;
import mybatis.log.LogCert;
import mybatis.log.anno.Log;
import mybatis.log.common.JsonExclude;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.Alias;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


public abstract class DataLogInterceptor implements Interceptor {
    private Map<String, LogCert> interceptorMap = new ConcurrentHashMap<>();
    private String name = ".query4Log";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();
        Object par = invocation.getArgs()[1];
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        String id = ms.getId();
        LogCert filter = interceptorMap.get(id);
        int index = id.lastIndexOf(".");
        String mapperName = id.substring(0, index);
        if (filter == null) {

            String methodName = id.substring(index + 1);
            Class clazz = Class.forName(mapperName);
            Method method = null;
            for (Method item : clazz.getMethods()) {
                if (item.getName().equals(methodName)) {
                    method = item;
                }
            }

            if (method == null) {
                interceptorMap.put(id, LogCert.DEFAULT);
            }
            Log logAnnotation = method.getDeclaredAnnotation(Log.class);
            if (logAnnotation == null) {
                interceptorMap.put(id, LogCert.DEFAULT);
                return result;
            }
            String tag = getTag(par);
            filter = new LogCert(logAnnotation, tag);
            interceptorMap.put(id, filter);
        } else if (!filter.isCert()) {
            return result;
        }
        Object log = par;
        long primaryId = getId(par);
        Executor executor = (Executor) invocation.getTarget();
        if (!filter.isAuto()) {
            MappedStatement query = ms.getConfiguration().getMappedStatement(mapperName + name);
            if (query == null) {
                return result;
            }

            log = executor.query(query, primaryId, RowBounds.DEFAULT, null);
        }
        insertLog(toJson(log), filter.getTag(), ms.getConfiguration(), executor, primaryId);
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    protected long getId(Object object) {
        if (object instanceof List) {
            return handleList((List) object);
        }
        if( object instanceof Map){
            
        }
        IModel model = (IModel) object;
        return model.logId();
    }

    private long handleList(List list) {
        Object object = list.get(0);
        return getId(object);
    }


    protected abstract void insertLog(String data, String tag, Configuration configuration, Executor executor, long primary) throws Exception;

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


    protected String getTag(Object object) {
        if (object instanceof List) {
            List list = (List) object;
            return getTag(list.get(0));
        }
        if (object instanceof Map) {
            Map map = (Map) object;
            for (Object value : map.values()) {
                String tag = getTag(value);
                if (!StringUtils.isEmpty(tag)) {
                    return tag;
                }
            }
        }
        Alias alias = object.getClass().getDeclaredAnnotation(Alias.class);
        if (alias == null) {
            return null;
        }
        return alias.value();
    }

}
