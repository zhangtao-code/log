package org.github.mybatis.spring.config;

import mybatis.log.config.DataLogInterceptor;
import mybatis.log.model.OperationBranchLog;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.github.mybatis.spring.service.IBranchService;
import org.github.mybatis.spring.util.PrimaryLocal;
import org.github.mybatis.spring.util.TraceLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Intercepts(
        {
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
        }
)
@Component
public class SpringInterceptor extends DataLogInterceptor {
    @Autowired
    private IBranchService branchService;

    @Override
    protected void insertLog(String data, String branchName, Configuration configuration, Executor executor, long primary, String parentId) throws Exception {
        OperationBranchLog branch = new OperationBranchLog();
        branch.setBranch(branchName);
        branch.setContent(data);
        branch.setParentId(parentId);
        branchService.addBranch(branch);
        PrimaryLocal.set(primary);
    }

    @Override
    protected String getTraceId() {
        return TraceLocal.get();
    }


}
