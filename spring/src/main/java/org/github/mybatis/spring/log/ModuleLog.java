package org.github.mybatis.spring.log;

import org.github.mybatis.spring.util.OperationType;

import java.util.List;

public class ModuleLog {
    private String name;
    private OperationType type;
    private String value;
    private List<CommonLog> list;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public List<CommonLog> getList() {
        return list;
    }

    public void setList(List<CommonLog> list) {
        this.list = list;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
