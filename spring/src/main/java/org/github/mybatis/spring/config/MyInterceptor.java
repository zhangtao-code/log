package org.github.mybatis.spring.config;

import mybatis.log.config.DataLogInterceptor;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.github.mybatis.spring.model.OperationBranchLog;
import org.github.mybatis.spring.util.PrimaryLocal;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Intercepts(
        {
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
        }
)
@Component
public class MyInterceptor extends DataLogInterceptor {
    @Override
    protected void insertLog(String data, String branchName, Configuration configuration, Executor executor, long primary, String parentId) throws Exception {
        MappedStatement branchMethod = configuration.getMappedStatement("org.github.mybatis.spring.mapper.OperationLogMapper.addBranch");
        OperationBranchLog branch = new OperationBranchLog();
        branch.setBranch(branchName);
        branch.setContent(data);
        branch.setParentId(parentId);
        MapperMethod.ParamMap map = new MapperMethod.ParamMap();
        map.put("list", Collections.singletonList(branch));
        executor.update(branchMethod, map);
        PrimaryLocal.set(primary);
    }
}
