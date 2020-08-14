package springexample.demo.mapper;

import mybatis.log.model.OperationTrunkLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrunkMapper {
    void addTrunk(OperationTrunkLog trunkLog);

    List<OperationTrunkLog> getTrunk(@Param("trunk") String name, long primaryId);
}
