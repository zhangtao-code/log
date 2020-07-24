package org.github.mybatis.spring.handle;

import org.github.mybatis.spring.model.MyModel;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
@Service
public class MyModelHandler implements IHandle<MyModel> {
    @Override
    public Map<Long, String> handle(String name, Set<Long> set) {

        return null;
    }
}
