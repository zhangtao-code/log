package springexample.demo.model;

import mybatis.log.IModel;
import mybatis.log.anno.Name;
import org.apache.ibatis.type.Alias;

@Alias("model")
@Name("类名称")
public class MyModel implements IModel {
    private long id;
    @Name("名称")
    private String name;
    private String exclude;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    @Override
    public String showName() {
        return name;
    }

    @Override
    public long logId() {
        return id;
    }
}
