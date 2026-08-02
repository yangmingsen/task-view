package top.yms.task.msgcd;

public enum BusinessErrorCode implements ErrorCode {
    E_204000(204000, "加密内容禁止进入lucene"),

    ;
    private final int code;
    private final String desc;

    BusinessErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
