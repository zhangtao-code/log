package mybatis.log.diff;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import mybatis.log.IHandle;
import mybatis.log.IModel;
import mybatis.log.anno.Exclude;
import mybatis.log.anno.Name;
import mybatis.log.anno.Relation;
import mybatis.log.log.CommonLog;
import mybatis.log.log.ItemLog;
import mybatis.log.log.ModuleLog;
import mybatis.log.log.OperationType;
import mybatis.log.model.OperationBranchLog;
import mybatis.log.model.OperationTrunkLog;
import mybatis.log.util.CollectionStream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class DiffService implements IDiffService {
    private static Logger logger = LoggerFactory.getLogger(DiffService.class);
    private SqlSessionFactory factory;


    public List<ItemLog> getLog(String moduleName, long primaryId, int pageNo, int pageSize) {
        List<OperationTrunkLog> list = getTrunkByTimeDesc(moduleName, primaryId, pageNo, pageSize);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        Set<String> set = CollectionStream.mapping2Set(list, OperationTrunkLog::getTagId);
        List<OperationBranchLog> branch = getBranch(set);
        Map<String, Map<String, OperationBranchLog>> branchMap = CollectionStream.group2Map(branch, OperationBranchLog::getParentId, OperationBranchLog::getBranch);
        if (MapUtils.isEmpty(branchMap)) {
            return Collections.EMPTY_LIST;
        }
        List<ItemLog> logs = new ArrayList<>();
        if (list.size() == 1) {
            try {
                OperationTrunkLog item = list.get(0);
                Map<String, OperationBranchLog> map = branchMap.get(item.getTagId());
                ItemLog myLog = handleInitData(item, map);
                return Collections.singletonList(myLog);
            } catch (Exception e) {
                logger.error("", e);
            }
            return Collections.EMPTY_LIST;
        }
        OperationTrunkLog item = null;
        Map<String, OperationBranchLog> map = null;
        for (OperationTrunkLog trunk : list) {
            String tagId = trunk.getTagId();
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

    /**
     * 处理数据只有一条的情况,通常情况为数据初始化
     *
     * @return
     */
    protected ItemLog handleInitData(OperationTrunkLog trunk, Map<String, OperationBranchLog> map) {
        List<ModuleLog> list = CollectionStream.map2List(map, (name, model) -> moduleBatch(name, model, OperationType.ADD));
        ItemLog itemLog = new ItemLog();
        itemLog.setDate(trunk.getOperatingTime());
        itemLog.setUserId(trunk.getOperatorId());
        itemLog.setLogs(list);
        return itemLog;
    }


    protected ItemLog diff(OperationTrunkLog trunk, Map<String, OperationBranchLog> srcMap, Map<String, OperationBranchLog> destMap) throws Exception {
        //初始化创建
        if (MapUtils.isEmpty(srcMap)) {
            return handleInitData(trunk, destMap);
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
            OperationBranchLog destModel = destMap.get(module);
            JsonElement jsonElement = new JsonParser().parse(destModel.getContent());
            //判断是否是数组
            if (!jsonElement.isJsonArray()) {
                IModel dest = (IModel) gson.fromJson(destModel.getContent(), clazz);
                String moduleName = dest.getClass().getDeclaredAnnotation(Name.class).value();
                OperationBranchLog srcModel = srcMap.get(module);
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
                ModuleLog moduleLog = new ModuleLog();
                List<CommonLog> allLogs = new ArrayList<>();
                OperationBranchLog srcModel = srcMap.get(module);
                if (srcModel == null) {
                    continue;
                }
                srcMap.remove(module);
                Type type1 = TypeToken.getParameterized(ArrayList.class, clazz).getType();
                Type type2 = TypeToken.getParameterized(ArrayList.class, clazz).getType();

                List<Object> dest = gson.fromJson(destModel.getContent(), type1);
                List<Object> src = gson.fromJson(srcModel.getContent(), type2);
                if (CollectionUtils.isEmpty(src)) {
                    List<CommonLog> logs = batchLogs(clazz, srcModel, OperationType.ADD);
                    moduleLog.setType(OperationType.ADD);
                    moduleLog.setList(logs);
                    result.add(moduleLog);
                    continue;
                }
                moduleLog.setType(OperationType.UPDATE);
                Name name = (Name) clazz.getDeclaredAnnotation(Name.class);
                moduleLog.setName(name.value());
                Field field = getRelationField(clazz);
                Relation relation = field.getDeclaredAnnotation(Relation.class);
                Set<Long> ids = new HashSet<>();
                ids.addAll(dest.stream().map(o -> getRelationId(o, field)).collect(Collectors.toList()));
                ids.addAll(src.stream().map(o -> getRelationId(o, field)).collect(Collectors.toList()));
                Map<Long, String> nameMap = getHandle(clazz).handle(relation.value(), ids);
                Map<Long, Object> oldMap2 = dest.stream().collect(Collectors.toMap(o -> getRelationId(o, field), Function.identity()));
                Map<Long, Object> currentMap2 = src.stream().collect(Collectors.toMap(o -> getRelationId(o, field), Function.identity()));
                Set<Long> remove = oldMap2.keySet();
                for (Long relationId : currentMap2.keySet()) {
                    IModel model1 = (IModel) oldMap2.get(relationId);
                    if (model1 == null) {
                        CommonLog commonLog = new CommonLog();
                        commonLog.setType(OperationType.ADD);
                        commonLog.setName(nameMap.get(relationId));
                        allLogs.add(commonLog);
                    } else {

                        List<CommonLog> update = diff((IModel) currentMap2.get(relationId), (IModel) oldMap2.get(relationId), clazz);
                        if (CollectionUtils.isNotEmpty(update)) {
                            IHandle handle = getHandle(clazz);
                            allLogs.addAll(update);
                            Map<Long, String> valueMap = handle.handle(relation.value(), Collections.singleton(relationId));
                            moduleLog.setValue(valueMap.get(relationId));
                        }
                    }
                }
                for (Long removeItem : remove) {
                    CommonLog commonLog = new CommonLog();
                    commonLog.setType(OperationType.DELETE);
                    commonLog.setName(nameMap.get(removeItem));
                    allLogs.add(commonLog);
                }
                moduleLog.setList(allLogs);
                result.add(moduleLog);

            }
        }
        for (String name : srcModuleName) {
            ModuleLog moduleLog = moduleBatch(name, srcMap.get(name), OperationType.DELETE);
            if (moduleLog != null) {
                result.add(moduleLog);
            }
        }
        ItemLog itemLog = new ItemLog();
        itemLog.setDate(trunk.getOperatingTime());
        itemLog.setUserId(trunk.getOperatorId());
        itemLog.setLogs(result);
        return itemLog;
    }

    private ModuleLog moduleBatch(String name, OperationBranchLog myModel, OperationType type) {
        ModuleLog moduleLog = new ModuleLog();
        moduleLog.setType(type);
        Class clazz = factory.getConfiguration().getTypeAliasRegistry().getTypeAliases().get(name);
        if (clazz == null) {
            return null;
        }
        Gson gson = new Gson();

        JsonElement jsonElement = new JsonParser().parse(myModel.getContent());
        if (!jsonElement.isJsonArray()) {
            IModel model = (IModel) gson.fromJson(myModel.getContent(), clazz);
            moduleLog.setName(model.getClass().getDeclaredAnnotation(Name.class).value());
            moduleLog.setValue(model.showName());
        } else {
            List<CommonLog> logs = batchLogs(clazz, myModel, type);
            moduleLog.setList(logs);
            Name annotation = (Name) clazz.getDeclaredAnnotation(Name.class);
            moduleLog.setName(annotation.value());
        }
        return moduleLog;
    }


    private List<CommonLog> batchLogs(Class clazz, OperationBranchLog myModel, OperationType type) {
        Gson gson = new Gson();
        Field field = getRelationField(clazz);
        Relation relation = field.getDeclaredAnnotation(Relation.class);
        Type genType = TypeToken.getParameterized(ArrayList.class, clazz).getType();
        List<Object> list = gson.fromJson(myModel.getContent(), genType);
        Set<Long> ids = list.stream().map(o -> getRelationId(o, field)).filter(i -> i > 0).collect(Collectors.toSet());
        Map<Long, String> map = getHandle(clazz).handle(relation.value(), ids);
        List<CommonLog> logs = map.values()
                .stream()
                .map(name -> new CommonLog(name, type))
                .collect(Collectors.toList());
        return logs;
    }


    private long getRelationId(Object object, Field field) {
        try {
            field.setAccessible(true);
            return (Long) field.get(object);
        } catch (Exception e) {
            logger.error("", e);
        }
        return 0L;
    }


    private Field getRelationField(Class clazz) {
        for (Field field : clazz.getDeclaredFields()) {
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
            Name name = field.getDeclaredAnnotation(Name.class);
            Exclude exclude = field.getDeclaredAnnotation(Exclude.class);
            if (exclude != null) {
                continue;
            }
            field.setAccessible(true);
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
                IHandle handle = getHandle(clazz);
                Set<Long> id = new TreeSet<>();
                id.add((Long) src);
                id.add((Long) dest);
                Map<Long, String> map = handle.handle(relation.value(), id);
                commonLog.setSource(map.get(src));
                commonLog.setDest(map.get(dest));
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

    public abstract IHandle getHandle(Class clazz);

    public abstract List<OperationTrunkLog> getTrunkByTimeDesc(String name, long id, int pageNo, int pageSize);

    public abstract List<OperationBranchLog> getBranch(Set<String> set);

    public SqlSessionFactory getFactory() {
        return factory;
    }

    public void setFactory(SqlSessionFactory factory) {
        this.factory = factory;
    }
}
