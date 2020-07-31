package org.github.mybatis.spring.config;

import mybatis.log.TraceLocal;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.github.mybatis.spring.mapper.OperationLogMapper;
import org.github.mybatis.spring.model.OperationTrunkLog;
import org.github.mybatis.spring.util.PrimaryLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Aspect
public class LogAspect {
    @Autowired
    private OperationLogMapper mapper;

    @Pointcut("@annotation(org.github.mybatis.spring.config.DataLog)")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void before(JoinPoint joinPoint) {

    }

    @After("pointCut()")
    public void insertTrunk(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        DataLog dataLog = method.getAnnotation(DataLog.class);
        int type = dataLog.type();
        String value = dataLog.value();
        String userId = "1234";
        OperationTrunkLog trunk = new OperationTrunkLog();
        trunk.setPrimaryId(PrimaryLocal.get());
        trunk.setOperationType(type);
        trunk.setOperatorId(userId);
        trunk.setTrunk(value);
        trunk.setTagId(TraceLocal.get());
        mapper.addTrunk(trunk);
        TraceLocal.remove();
        PrimaryLocal.remove();
    }


}
