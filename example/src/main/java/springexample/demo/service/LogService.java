package springexample.demo.service;

import com.github.pagehelper.PageHelper;
import mybatis.log.model.OperationBranchLog;
import mybatis.log.model.OperationTrunkLog;
import org.github.mybatis.spring.service.IBranchService;
import org.github.mybatis.spring.service.ITrunkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springexample.demo.mapper.BranchMapper;
import springexample.demo.mapper.TrunkMapper;

import java.util.List;
import java.util.Set;
@Service
public class LogService implements IBranchService, ITrunkService {
    @Autowired
    private BranchMapper branchMapper;
    @Autowired
    private TrunkMapper trunkMapper;

    @Override
    public void addBranch(OperationBranchLog branchLog) {
        branchMapper.addBranch(branchLog);
    }

    @Override
    public List<OperationBranchLog> getBranch(Set<String> set) {
        return branchMapper.getBranch(set);
    }

    @Override
    public void addTrunk(OperationTrunkLog trunkLog) {
        trunkMapper.addTrunk(trunkLog);
    }

    @Override
    public List<OperationTrunkLog> getTrunk(String name, long primaryId, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return trunkMapper.getTrunk(name, primaryId);
    }
}
