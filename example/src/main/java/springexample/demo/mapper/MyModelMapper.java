package springexample.demo.mapper;

import mybatis.log.LogMapper;
import mybatis.log.anno.Log;
import springexample.demo.model.MyModel;

import java.util.List;
import java.util.Set;

public interface MyModelMapper extends LogMapper<MyModel> {
    @Log(auto = true)
    void add(MyModel model);

    @Log
    void update(MyModel model);

    List<MyModel> query(Set<Long> set);
}
