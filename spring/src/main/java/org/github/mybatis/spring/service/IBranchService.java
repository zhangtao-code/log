package org.github.mybatis.spring.service;

import mybatis.log.model.OperationBranchLog;

import java.util.List;
import java.util.Set;

public interface IBranchService {
    void addBranch(OperationBranchLog branchLog);

    List<OperationBranchLog> getBranch(Set<String> set);
}
