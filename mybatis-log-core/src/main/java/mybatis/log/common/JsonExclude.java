package mybatis.log.common;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.mybatis.log.anno.Exclude;

public class JsonExclude implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        return fieldAttributes.getAnnotation(Exclude.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }
}
