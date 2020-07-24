package org.github.mybatis.spring.mapper;

import org.apache.ibatis.annotations.Param;
import org.github.mybatis.spring.model.OperationBranchLogModel;
import org.github.mybatis.spring.model.OperationTrunkLogModel;

import java.util.List;
import java.util.Set;

public interface OperationLogMapper {

    void addTrunk(OperationTrunkLogModel model);

    void addBranch(@Param("list") List<OperationBranchLogModel> list);

    long getTransactionalId();

    List<OperationTrunkLogModel> getTrunk(String trunk, long primaryId);

    List<OperationBranchLogModel> getBranch(@Param("set") Set<Long> set);
}
