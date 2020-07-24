package org.github.mybatis.spring.model;

import mybatis.log.IModel;
import mybatis.log.anno.Exclude;
import mybatis.log.anno.Name;
import org.apache.ibatis.type.Alias;

@Alias("model")
public class MyModel implements IModel {
    private long id;
    @Name("名称")
    private String name;
    @Exclude
    private String exclude;

    public MyModel() {
    }

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

    @Override
    public String showName() {
        return null;
    }

    @Override
    public long logId() {
        return id;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }
}
