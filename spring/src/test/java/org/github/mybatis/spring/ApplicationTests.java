package org.github.mybatis.spring;

import org.github.mybatis.spring.model.MyModel;
import org.github.mybatis.spring.service.IService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {
    @Autowired
    private IService service;

    @Test
    void contextLoads() {
        MyModel model = new MyModel();
        model.setName("name");
        model.setExclude("exclude");
        //mapper.add(model);
    }

    @Test
    void update() {
        service.demo();
        service.getLog("model", 30);
    }
}
