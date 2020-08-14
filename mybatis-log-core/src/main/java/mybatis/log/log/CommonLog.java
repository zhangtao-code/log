package mybatis.log.log;


public class CommonLog {
    private String name;
    private OperationType type;
    private String source;
    private String dest;

    public CommonLog() {
    }

    public CommonLog(String name, OperationType type) {

        this.name = name;
        this.type = type;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
