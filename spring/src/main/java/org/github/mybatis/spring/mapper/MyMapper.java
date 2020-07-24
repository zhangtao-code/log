package org.github.mybatis.spring.mapper;

import mybatis.log.LogMapper;
import mybatis.log.anno.Log;
import org.github.mybatis.spring.model.MyModel;

import java.util.List;
import java.util.Set;

public interface MyMapper extends LogMapper<MyModel> {

    @Log(auto = true)
    void add(MyModel model);

    @Log
    void update(MyModel model);

    List<MyModel> query(Set<Long> set);
}
