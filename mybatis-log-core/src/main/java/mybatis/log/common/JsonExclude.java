package mybatis.log.common;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import mybatis.log.anno.Exclude;


public class JsonExclude implements ExclusionStrategy {

    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        return fieldAttributes.getAnnotation(Exclude.class) != null;
    }

    public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }
}
