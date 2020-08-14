package mybatis.log.log;

public enum OperationType {
    ADD(1),
    UPDATE(2),
    DELETE(3),
    ;
    private int id;

    OperationType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
