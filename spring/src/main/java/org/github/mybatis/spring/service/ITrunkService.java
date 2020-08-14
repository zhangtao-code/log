package org.github.mybatis.spring.service;

import mybatis.log.model.OperationTrunkLog;

import java.util.List;

public interface ITrunkService {
    void addTrunk(OperationTrunkLog trunkLog);

    List<OperationTrunkLog> getTrunk(String name, long primaryId, int pageNo, int pageSize);
}
