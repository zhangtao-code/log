package mybatis.example;

import com.mybatis.log.anno.Log;
import com.mybatis.log.anno.LogId;
import com.mybatis.log.anno.LogType;
import com.mybatis.log.anno.Relation;

@Log(LogType.UPDATE)
public class RelationModel {
    private long id;
    @LogId("")
    private long masterId;
    @Relation(tableName = "subTable", column = "sub_id")
    private long subId;
}
