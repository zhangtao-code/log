package springexample.demo.mapper;

import mybatis.log.model.OperationBranchLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface BranchMapper {

    void addBranch(OperationBranchLog branchLog);

    List<OperationBranchLog> getBranch(@Param("set") Set<String> set);
}
