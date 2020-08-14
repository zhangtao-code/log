package springexample.demo.service;

import org.github.mybatis.spring.config.DataLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springexample.demo.mapper.MyModelMapper;
import springexample.demo.model.MyModel;
@Service
public class MyService implements IService{
    @Autowired
    private MyModelMapper mapper;
    @Override
    @DataLog(value = "model")
    @Transactional
    public void addDemo(MyModel model) {
        mapper.add(model);
    }

    @DataLog(value = "model")
    public void update(MyModel model) {
        mapper.update(model);
    }
}
