package org.github.mybatis.spring.service;


import mybatis.log.IHandle;
import mybatis.log.diff.DiffService;
import mybatis.log.model.OperationBranchLog;
import mybatis.log.model.OperationTrunkLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;


public class SpringDiffService extends DiffService implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(SpringDiffService.class);


    public SpringDiffService() {
        super();
    }

    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private List<IHandle> handles;
    @Autowired
    private ITrunkService trunkService;
    @Autowired
    private IBranchService branchService;
    private Map<Class, IHandle> handleMap;


    @Override
    public IHandle getHandle(Class clazz) {
        IHandle iHandle = handleMap.get(clazz);
        if (iHandle == null) {
            throw new NullPointerException();
        }
        return iHandle;
    }

    @Override
    public List<OperationTrunkLog> getTrunkByTimeDesc(String name, long id, int pageNo, int pageSize) {
        return trunkService.getTrunk(name, id, pageNo, pageSize);
    }

    @Override
    public List<OperationBranchLog> getBranch(Set<String> set) {
        return branchService.getBranch(set);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setFactory(sqlSessionFactory);
        if (CollectionUtils.isEmpty(handles)) {
            handleMap = Collections.EMPTY_MAP;
            return;
        }
        handleMap = new HashMap<>();
        for (IHandle handle : handles) {
            Type[] type = handle.getClass().getGenericInterfaces();
            if(type[0] instanceof ParameterizedType){
                ParameterizedType parameterizedType = (ParameterizedType) type[0];
                Type genericType = parameterizedType.getActualTypeArguments()[0];
                handleMap.put((Class) genericType, handle);
            }
        }

    }
}
