package mybatis.log.log;

import java.util.Date;
import java.util.List;

public class ItemLog {
    private Date date;
    private String userId;
    private List<ModuleLog> logs;


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<ModuleLog> getLogs() {
        return logs;
    }

    public void setLogs(List<ModuleLog> logs) {
        this.logs = logs;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
