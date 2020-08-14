package springexample.demo.mapper;

import mybatis.log.LogMapper;
import springexample.demo.model.SubModel;

import java.util.List;

public interface SubMapper extends LogMapper<List<SubMapper>> {
    void saveAndDelete(long masterId, List<SubModel> list);
}
