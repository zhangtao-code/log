package mybatis.log;

import java.util.Map;
import java.util.Set;

public interface IHandle<T> {

    Map<Long, String> handle(String name, Set<Long> set);
}
