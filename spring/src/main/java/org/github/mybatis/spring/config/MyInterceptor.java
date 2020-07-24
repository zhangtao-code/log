package org.github.mybatis.spring.config;

import mybatis.log.config.DataLogInterceptor;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.github.mybatis.spring.model.OperationBranchLogModel;
import org.github.mybatis.spring.util.PrimaryLocal;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Intercepts(
        {
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
        }
)
@Component
public class MyInterceptor extends DataLogInterceptor {
    @Override
    protected void insertLog(String data, String tag, Configuration configuration, Executor executor, long tagId) throws Exception {
        MappedStatement branchMethod = configuration.getMappedStatement("org.github.mybatis.spring.mapper.OperationLogMapper.addBranch");
        MappedStatement id = configuration.getMappedStatement("org.github.mybatis.spring.mapper.OperationLogMapper.getTransactionalId");
        List<Long> list = executor.query(id, null, RowBounds.DEFAULT, null);
        OperationBranchLogModel branch = new OperationBranchLogModel();
        branch.setBranch(tag);
        branch.setContent(data);
        branch.setParentId(list.get(0));
        MapperMethod.ParamMap map = new MapperMethod.ParamMap();
        map.put("list", Collections.singletonList(branch));
        executor.update(branchMethod, map);
        PrimaryLocal.set(tagId);
    }
}
