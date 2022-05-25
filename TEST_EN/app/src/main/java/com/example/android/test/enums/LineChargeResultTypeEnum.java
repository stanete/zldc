package com.example.android.test.enums;


/**
 * 线充结果类型枚举
 *
 * @Date 2021年3月29日11:38:47
 */
public enum LineChargeResultTypeEnum {
    /**
     * 开启充电口，且设备充电中
     */
    START_CHARGING(0, "开启充电口，且设备充电中"),
    /**
     * 开启充电口,未检测到充电电池
     */
    START_NO_BAT(1, "开启充电口,未检测到充电电池"),
    /**
     * 充电口已经被禁用
     */
    PORT_IS_USING(3, "充电口已经被禁用"),
    /**
     * 充电口被占用
     */
    IS_USING(4, "充电口被占用"),
    /**
     * 指令超时
     */
    COMMAND_TIMEOUT(5, "指令超时"),
    /**
     * 结束充电
     */
    END_CHARGING(6, "结束充电"),
    /**
     * 设备繁忙，稍后在试
     */
    BUSY_TRY_AGAIN(7, "设备繁忙，稍后在试"),
    /**
     * 结束充电订单异常，无此订单号
     */
    END_CHARGING_EXCE_NO_ORDER(8, "结束充电订单异常，无此订单号"),
    /**
     * 充电中充电口禁用，结束充电
     */
    END_CHARGING_DISABLE_IN_CHARGING(9, "充电中充电口禁用，结束充电"),
    /**
     * 电流过大
     */
    CURRENT_OVER(10, "电流过大");
    /**
     * 状态吗
     */
    Integer code;
    /**
     * 消息
     */
    String msg;

    /**
     * @param code 状态码
     * @param msg  消息
     */
    LineChargeResultTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 通过状态码获取对应得到消息
     *
     * @param code 状态码
     * @return
     */
    public static String getMsgByCode(Integer code) {
        for (LineChargeResultTypeEnum lcrt : LineChargeResultTypeEnum.values()) {
            if (lcrt.getCode().equals(code)) {
                return lcrt.getMsg();
            }
        }
        return "";
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
