package mybatis.log;

import mybatis.log.anno.Log;


public class LogCert {
    public static LogCert DEFAULT = new LogCert();
    private boolean cert;

    private boolean auto;

    private String branchName;

    private LogCert() {
    }

    public LogCert(Log log, String tag) {
        this.cert = true;
        this.auto = log.auto();
        this.branchName = tag;
    }

    public boolean isCert() {
        return cert;
    }

    public void setCert(boolean cert) {
        this.cert = cert;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
