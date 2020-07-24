package org.github.mybatis.spring.mapper;

import mybatis.log.LogMapper;
import mybatis.log.anno.Log;
import org.github.mybatis.spring.model.MyModel;

public interface MyMapper extends LogMapper<MyModel> {

    @Log(auto = true)
    void add(MyModel model);

    @Log
    void update(MyModel model);
}
