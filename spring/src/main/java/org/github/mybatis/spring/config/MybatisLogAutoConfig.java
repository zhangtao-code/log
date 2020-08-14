package org.github.mybatis.spring.config;

import mybatis.log.IHandle;
import mybatis.log.diff.IDiffService;
import org.apache.ibatis.plugin.Interceptor;
import org.github.mybatis.spring.service.IBranchService;
import org.github.mybatis.spring.service.ITrunkService;
import org.github.mybatis.spring.service.SpringDiffService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Configuration
@ConditionalOnBean(value = {IBranchService.class, ITrunkService.class})
public class MybatisLogAutoConfig {
    public MybatisLogAutoConfig() {
        System.err.println(123);
    }

    @Bean
    public IDiffService initDiffService() {
        return new SpringDiffService();
    }

    @Bean
    public Interceptor initInterceptor() {
        return new SpringInterceptor();
    }

    @Bean
    public LogAspect initLogAspect() {
        return new LogAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public IHandle missHandler() {
        return new EmptyHandle();
    }

    private class EmptyHandle implements IHandle {
        @Override
        public Map<Long, String> handle(String name, Set set) {
            return Collections.EMPTY_MAP;
        }
    }

}
