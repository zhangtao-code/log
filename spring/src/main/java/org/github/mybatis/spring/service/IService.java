package org.github.mybatis.spring.service;

import org.github.mybatis.spring.log.ItemLog;

import java.util.List;

public interface IService {
    public void demo();

    List<ItemLog> getLog(String tag, long id);
}
