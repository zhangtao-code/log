package org.github.mybatis.spring.mapper;

import org.apache.ibatis.annotations.Param;
import org.github.mybatis.spring.model.OperationBranchLog;
import org.github.mybatis.spring.model.OperationTrunkLog;

import java.util.List;
import java.util.Set;

public interface OperationLogMapper {

    void addTrunk(OperationTrunkLog model);

    void addBranch(@Param("list") List<OperationBranchLog> list);

    List<OperationTrunkLog> getTrunk(String trunk, long primaryId);

    List<OperationBranchLog> getBranch(@Param("set") Set<String> set);
}
