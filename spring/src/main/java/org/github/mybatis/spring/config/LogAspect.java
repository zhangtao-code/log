package org.github.mybatis.spring.config;

import mybatis.log.model.OperationTrunkLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.github.mybatis.spring.service.ITrunkService;
import org.github.mybatis.spring.util.PrimaryLocal;
import org.github.mybatis.spring.util.TraceLocal;
import org.github.mybatis.spring.util.UserLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

@Component
@Aspect
public  class LogAspect {
    private static Logger logger = LoggerFactory.getLogger(LogAspect.class);
    @Autowired
    private ITrunkService trunkService;

    @Pointcut("@annotation(org.github.mybatis.spring.config.DataLog)")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void before(JoinPoint joinPoint) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        TraceLocal.set(traceId);
    }

    @After("pointCut()")
    public void insertTrunk(JoinPoint joinPoint) {
        try {
            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            Method method = methodSignature.getMethod();
            Long primaryId = PrimaryLocal.get();
            if (primaryId == null) {
                logger.error("get primaryId fail name=" + method.getName());
                throw new NullPointerException();
            }
            DataLog dataLog = method.getAnnotation(DataLog.class);
            int type = dataLog.type();
            String value = dataLog.value();
            String userId = UserLocal.get();
            OperationTrunkLog trunk = new OperationTrunkLog();
            trunk.setPrimaryId(primaryId);
            trunk.setOperationType(type);
            trunk.setOperatorId(userId);
            trunk.setTrunk(value);
            trunk.setTagId(TraceLocal.get());
            trunkService.addTrunk(trunk);
        } finally {
            PrimaryLocal.remove();
            TraceLocal.remove();
        }
    }
}
