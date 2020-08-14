package springexample.demo;

import com.google.gson.Gson;
import mybatis.log.diff.IDiffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import springexample.demo.model.MyModel;
import springexample.demo.service.MyService;

@SpringBootTest
class DemoApplicationTests {
    @Autowired
    private MyService myService;
    @Autowired
    private IDiffService diffService;

    @Test
    void contextLoads() {
        MyModel myModel = new MyModel();
        myModel.setName("springtest");
        myModel.setExclude("exclude");
        myService.addDemo(myModel);
        System.err.println(myModel.getId());
    }

    @Test
    public void getDiff() {
        System.err.println(new Gson().toJson(diffService.getLog("model", 153, 1, 100)));
    }

}
