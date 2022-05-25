package com.example.android.zldc.enums;
/**
 * 告警信息类型
 *
 * @Date 2021年3月29日11:38:59
 */
public enum AlarmTypeEnum {

    //cabinet_alert 柜子告警

    /**
     * 雷电告警
     */
    Lightning_warning("Lightning warning",10001),

    /**
     * 电池短接
     */
    Bat_Error("bat reverse connection",10002);
    /**
     * 消息
     */
     String msg;

    /**
     * 对应的状态码
     */
     int code;



    AlarmTypeEnum(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}