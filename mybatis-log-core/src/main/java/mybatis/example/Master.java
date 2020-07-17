package mybatis.example;

import com.mybatis.log.anno.*;

import java.util.Date;

@Log(LogType.INSERT)
public class Master {
    @LogId("")
    private long id;
    @Name("名字")
    private String name;
    @Exclude
    private Date createDate;
}
