package org.github.mybatis.spring.log;

import org.github.mybatis.spring.util.OperationType;

public class UpdateOperation extends Operation {
    public UpdateOperation(String name, OperationType type, String update) {
        super(name, type);
        this.update = update;
    }

    private String update;

}
