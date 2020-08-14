package mybatis.log.diff;

import mybatis.log.log.ItemLog;

import java.util.List;

public interface IDiffService {
    List<ItemLog> getLog(String moduleName, long primaryId, int pageNo, int pageSize);
}
