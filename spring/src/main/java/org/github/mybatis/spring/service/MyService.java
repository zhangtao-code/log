package org.github.mybatis.spring.service;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mybatis.log.IModel;
import mybatis.log.anno.Exclude;
import mybatis.log.anno.Name;
import mybatis.log.anno.Relation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.github.mybatis.spring.config.DataLog;
import org.github.mybatis.spring.log.CommonLog;
import org.github.mybatis.spring.log.ModuleLog;
import org.github.mybatis.spring.log.ItemLog;
import org.github.mybatis.spring.mapper.MyMapper;
import org.github.mybatis.spring.mapper.OperationLogMapper;
import org.github.mybatis.spring.model.MyModel;
import org.github.mybatis.spring.model.OperationBranchLogModel;
import org.github.mybatis.spring.model.OperationTrunkLogModel;
import org.github.mybatis.spring.util.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MyService implements IService {
    private static Logger logger = LoggerFactory.getLogger(MyService.class);
    @Autowired
    private MyMapper mapper;
    @Autowired
    private OperationLogMapper logMapper;
    @Autowired
    private SqlSessionFactory factory;

    @Override
    @DataLog(value = "model")
    @Transactional
    public void demo() {
        MyModel model = new MyModel();
        model.setName("name22");
        model.setExclude("exclude22");
        model.setId(RandomUtils.nextLong(100, 1000000000000000L));
        mapper.add(model);
        System.err.println(logMapper.getTransactionalId());
    }

    @Override
    public List<ItemLog> getLog(String moduleName, long primaryId) {
        List<OperationTrunkLogModel> list = logMapper.getTrunk(moduleName, primaryId);
        Set<Long> set = list.stream().map(OperationTrunkLogModel::getTagId).collect(Collectors.toSet());
        List<OperationBranchLogModel> branch = logMapper.getBranch(set);
        Map<Long, Map<String, OperationBranchLogModel>> branchMap = branch
                .stream()
                .collect(Collectors.groupingBy(OperationBranchLogModel::getParentId, Collectors.toMap(OperationBranchLogModel::getBranch, Function.identity())));
        List<ItemLog> logs = new ArrayList<>();
        OperationTrunkLogModel item = null;
        Map<String, OperationBranchLogModel> map = null;
        for (OperationTrunkLogModel trunk : list) {
            long tagId = trunk.getTagId();
            if (item != null) {
                try {
                    ItemLog myLog = diff(item, branchMap.get(tagId), map);
                    if (myLog != null) {
                        logs.add(myLog);
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
            item = trunk;
            map = branchMap.get(tagId);
        }
        return logs;
    }

    protected ItemLog diff(OperationTrunkLogModel trunk, Map<String, OperationBranchLogModel> srcMap, Map<String, OperationBranchLogModel> destMap) throws Exception {
        //初始化创建
        if (MapUtils.isEmpty(srcMap)) {
            //初始化创建,无日志
            return null;
        }
        List<ModuleLog> result = new ArrayList<>();
        Set<String> srcModuleName = srcMap.keySet();
        for (String module : destMap.keySet()) {
            //拿到类别
            Class clazz = factory.getConfiguration().getTypeAliasRegistry().getTypeAliases().get(module);
            if (clazz == null) {
                continue;
            }
            Gson gson = new Gson();
            OperationBranchLogModel destModel = destMap.get(module);
            JsonElement jsonElement = new JsonParser().parse(destModel.getContent());
            //判断是否是数组
            if (!jsonElement.isJsonArray()) {
                IModel dest = (IModel) gson.fromJson(destModel.getContent(), clazz);
                String moduleName = dest.getClass().getDeclaredAnnotation(Name.class).value();
                OperationBranchLogModel srcModel = srcMap.get(module);
                if (srcModel == null) {
                    ModuleLog moduleLog = new ModuleLog();
                    moduleLog.setType(OperationType.ADD);
                    moduleLog.setValue(dest.showName());
                    moduleLog.setName(moduleName);
                    result.add(moduleLog);
                    continue;
                }
                srcModuleName.remove(module);
                IModel src = (IModel) gson.fromJson(srcModel.getContent(), clazz);
                ModuleLog moduleLog = new ModuleLog();
                moduleLog.setType(OperationType.UPDATE);
                moduleLog.setName(moduleName);
                List<CommonLog> diff = diff(dest, src, clazz);
                if (CollectionUtils.isEmpty(diff)) {
                    continue;
                }
                moduleLog.setList(diff);
                result.add(moduleLog);
            } else {
                List<CommonLog> allLogs = new ArrayList<>();
                OperationBranchLogModel srcModel = srcMap.get(module);
                List<Object> dest = gson.fromJson(destModel.getContent(), getListType(clazz));
                List<Object> src = gson.fromJson(srcModel.getContent(), getListType(clazz));
                if (CollectionUtils.isEmpty(src)) {
                    List<CommonLog> logs = batchLogs(clazz, srcModel, OperationType.ADD);
                    ModuleLog moduleLog = new ModuleLog();
                    moduleLog.setType(OperationType.ADD);
                    moduleLog.setList(logs);
                    result.add(moduleLog);
                    continue;
                }
                Field field = getRelationField(clazz);
                Map<Long, String> nameMap = null;//todo
                Map<Long, Object> oldMap2 = dest.stream().collect(Collectors.toMap(o -> getRelationId(o, field), Function.identity()));
                Map<Long, Object> currentMap2 = src.stream().collect(Collectors.toMap(o -> getRelationId(o, field), Function.identity()));
                Set<Long> remove = oldMap2.keySet();
                for (Long relationId : currentMap2.keySet()) {
                    IModel model1 = (IModel) currentMap2.get(relationId);
                    if (model1 == null) {
                        CommonLog commonLog = new CommonLog();
                        commonLog.setType(OperationType.ADD);
                        commonLog.setName(nameMap.get(relationId));
                        allLogs.add(commonLog);
                    }
                }
                for (Long removeItem : remove) {
                    CommonLog commonLog = new CommonLog();
                    commonLog.setType(OperationType.DELETE);
                    commonLog.setName(nameMap.get(removeItem));
                    allLogs.add(commonLog);
                }

            }
        }
        for (String name : srcModuleName) {
            ModuleLog moduleLog = new ModuleLog();
            moduleLog.setType(OperationType.DELETE);
            Class clazz = factory.getConfiguration().getTypeAliasRegistry().getTypeAliases().get(name);
            if (clazz == null) {
                continue;
            }
            Gson gson = new Gson();
            OperationBranchLogModel srcModel = destMap.get(name);
            JsonElement jsonElement = new JsonParser().parse(srcModel.getContent());
            if (!jsonElement.isJsonArray()) {
                IModel model = (IModel) gson.fromJson(srcModel.getContent(), clazz);
                moduleLog.setName(model.getClass().getDeclaredAnnotation(Name.class).value());
                moduleLog.setValue(model.showName());
            } else {
                List<CommonLog> logs = batchLogs(clazz, srcModel, OperationType.DELETE);
                moduleLog.setList(logs);
            }
            result.add(moduleLog);
        }
        ItemLog itemLog = new ItemLog();
        itemLog.setDate(trunk.getOperatingTime());
        itemLog.setUserId(trunk.getOperatorId());
        itemLog.setLogs(result);
        return itemLog;
    }


    private List<CommonLog> batchLogs(Class clazz, OperationBranchLogModel myModel, OperationType type) {
        Gson gson = new Gson();
        Field field = getRelationField(clazz);
        List<Object> list = gson.fromJson(myModel.getContent(), getListType(clazz));
        Set<Long> ids = list.stream().map(o -> getRelationId(o, field)).filter(i -> i > 0).collect(Collectors.toSet());
        List<IModel> models = null;//todo
        List<CommonLog> logs = models.stream().map(model -> new CommonLog(model.showName(), type)).collect(Collectors.toList());
        return logs;
    }


    private long getRelationId(Object object, Field field) {
        try {
            return (Long) field.get(object);
        } catch (Exception e) {

        }
        return 0L;
    }

    private <T> Type getListType(Class<T> tClass) {
        List<T> list = new ArrayList<>(1);
        return list.getClass().getGenericSuperclass();
    }


    private Field getRelationField(Class clazz) {
        for (Field field : clazz.getFields()) {
            Relation relation = field.getDeclaredAnnotation(Relation.class);
            if (relation != null) {
                return field;
            }
        }
        return null;
    }

    protected List<CommonLog> diff(IModel currentObject, IModel oldObject, Class clazz) throws Exception {
        List<CommonLog> list = new ArrayList<>(clazz.getDeclaredFields().length);
        for (Field field : clazz.getDeclaredFields()) {
            Name name = (Name) clazz.getDeclaredAnnotation(Exclude.class);
            Exclude exclude = (Exclude) clazz.getDeclaredAnnotation(Exclude.class);
            if (exclude != null) {
                continue;
            }
            Object src = field.get(oldObject);
            Object dest = field.get(currentObject);
            boolean compare = Objects.equals(src, dest);
            if (compare) {
                continue;
            }
            Relation relation = field.getDeclaredAnnotation(Relation.class);
            CommonLog commonLog = new CommonLog();
            commonLog.setName(name.value());
            commonLog.setType(OperationType.UPDATE);
            if (relation != null) {
                //todo
            } else {
                commonLog.setSource(String.valueOf(src));
                commonLog.setDest(String.valueOf(dest));
            }
            list.add(commonLog);
        }
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list;
    }
}
