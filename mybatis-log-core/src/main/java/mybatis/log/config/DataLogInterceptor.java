package mybatis.log.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.Primitives;
import mybatis.log.IModel;
import mybatis.log.LogCert;
import mybatis.log.anno.Log;
import mybatis.log.common.JsonExclude;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public abstract class DataLogInterceptor implements Interceptor {
    private Map<String, LogCert> interceptorMap = new ConcurrentHashMap<>();
    private String name = "query4Log";
    private Map<String, Boolean> hasMany = new HashMap<>();


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();
        String traceId = getTraceId();
        if (StringUtils.isEmpty(traceId)) {
            return result;
        }
        //
        Object par = invocation.getArgs()[1];
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Executor executor = (Executor) invocation.getTarget();
        String classMethodName = ms.getId();
        //
        int index = classMethodName.lastIndexOf(".");
        String mapperName = classMethodName.substring(0, index);
        LogCert logCert = getLogCert(classMethodName, index, mapperName, par);
        if (logCert == null || (!logCert.isCert())) {
            return result;
        }
        Object log = par;
        Long primaryId = parseParameterId(par);
        if (primaryId == null) {
            return result;
        }

        String json = parseLog(log, primaryId, logCert, mapperName, ms.getConfiguration(), executor);
        if (StringUtils.isNotBlank(json)) {
            insertLog(json, logCert.getBranchName(), ms.getConfiguration(), executor, primaryId, traceId);
        }
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    protected Object formatLog(Object object, String clazzName, String method) throws Exception {
        if (object == null) {
            return null;
        }
        if (object instanceof List || object.getClass().isArray()) {
            Boolean has = hasMany.get(clazzName + method);
            if (has != null) {
                if (has) {
                    return object;
                } else {
                    if (object instanceof List) {
                        List list = ((List) object);
                        if (CollectionUtils.isEmpty(list)) {
                            return Collections.EMPTY_LIST;
                        }
                        return list.get(0);
                    }
                    if (object.getClass().isArray()) {
                        Object[] array = (Object[]) object;
                        if (ArrayUtils.isEmpty(array)) {
                            return new Object[0];
                        } else {
                            return array[0];
                        }
                    }
                }
            } else {
                Class clazz = Class.forName(clazzName);
                for (Type type : clazz.getGenericInterfaces()) {
                    Type returnType = ((ParameterizedType) type).getActualTypeArguments()[0];
                    if (returnType instanceof Class) {
                        hasMany.put(clazzName + method, false);
                    } else {
                        hasMany.put(clazzName + method, true);
                    }
                    return formatLog(object, clazzName, method);
                }
            }
        }
        return object;
    }

    protected String parseLog(Object log, long primaryId, LogCert filter, String mapperName, Configuration configuration, Executor executor) throws Exception {
        if (!filter.isAuto()) {
            MappedStatement query = configuration.getMappedStatement(mapperName + "." + name);
            if (query == null) {
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("masterId", primaryId);
            log = executor.query(query, map, RowBounds.DEFAULT, null);
            log = formatLog(log, mapperName, name);
        }
        return toJson(log);
    }

    protected LogCert getLogCert(String id, int index, String mapperName, Object par) throws Exception {
        LogCert logCert = interceptorMap.get(id);
        if (logCert != null) {
            return logCert;
        }
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
            return LogCert.DEFAULT;
        }
        Log logAnnotation = method.getDeclaredAnnotation(Log.class);
        if (logAnnotation == null) {
            interceptorMap.put(id, LogCert.DEFAULT);
            return LogCert.DEFAULT;
        }
        String branchName = parseBranchName(par);
        logCert = new LogCert(logAnnotation, branchName);
        interceptorMap.put(id, logCert);
        return logCert;
    }

    /**
     * 解析出Id
     *
     * @param object
     * @return
     */
    protected Long parseParameterId(Object object) {
        if (object instanceof List) {
            return handleCollection((List) object);
        }
        if (object instanceof Map) {
            Map map = (Map) object;
            return handleCollection(map.values());
        }
        if ((object instanceof String) || Primitives.isPrimitive(object.getClass()) || Primitives.isWrapperType(object.getClass())) {
            return null;
        }
        if (object instanceof IModel) {
            IModel model = (IModel) object;
            return model.logId();
        }
        return null;
    }

    private Long handleCollection(Collection collection) {
        Optional<Long> optional = collection.stream().map(this::parseParameterId).filter(Objects::nonNull).findFirst();
        return optional.get();
    }


    protected abstract void insertLog(String data, String tag, Configuration configuration, Executor executor, long primary, String parentId) throws Exception;

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


    protected String parseBranchName(Object object) {
        if (object instanceof List) {
            List list = (List) object;
            return parseBranchName(list.get(0));
        }
        if (object instanceof Map) {
            Map map = (Map) object;
            for (Object value : map.values()) {
                String tag = parseBranchName(value);
                if (!StringUtils.isEmpty(tag)) {
                    return tag;
                }
            }
            return null;
        }
        Alias alias = object.getClass().getDeclaredAnnotation(Alias.class);
        if (alias == null) {
            return null;
        }
        return alias.value();
    }

    protected abstract String getTraceId();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
