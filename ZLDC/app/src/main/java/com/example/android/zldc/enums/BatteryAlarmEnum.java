package com.example.android.zldc.enums;

/**
 * 电池告警信息
 * 0   其它
 * 1	单芯过压
 * 2	单芯压差过大
 * 3	充电过流
 * 4	短路
 * 5	充电高温
 * 6	充电低温
 * 7	充电MOS损坏
 * 8	内部通讯异常
 * 9	总电压过压
 * 10	单芯欠压
 * 11	放电MOS损坏
 * 12   充电异常
 *
 * @Date 2021年3月30日09:59:25
 */
public enum BatteryAlarmEnum implements Cloneable {
    /**
     * 其它
     */
    OTHERS_ALARM(0, "其它"),
    /**
     * 单芯过压
     */
    SINGLE_HIGH_VOL_ALARM(1, "单芯过压"),
    /**
     * 单芯压差过大
     */
    SINGLE_DIFF_BIG_VOL_ALARM(2, "单芯压差过大"),
    /**
     * 充电过流
     */
    CHARGE_OVER_CURRENT_ALARM(3, "充电过流"),
    /**
     * 短路
     */
    SUICIDE_ALARM(4, "短路"),
    /**
     * 充电高温
     */
    CHARGE_HIGH_TEMPERATURE_ALARM(5, "充电高温"),
    /**
     * 充电低温
     */
    CHARGE_LOW_TEMPERATURE_ALARM(6, "充电低温"),
    /**
     * 充电MOS损坏
     */
    CHARGE_MOS_DAMAGE_ALARM(7, "充电MOS损坏"),
    /**
     * 内部通讯异常
     */
    INTERNAL_EXCEPTION_ALARM(8, "内部通讯异常"),
    /**
     * 总电压过压
     */
    TOTAL_VOL_OVER_ALARM(9, "总电压过压"),
    /**
     *
     */
    SINGLE_LOW_VOL_ALARM(10, "单芯欠压"),
    /**
     * 放电MOS损坏
     */
    DISCHARGE_MOS_DAMAGE_ALARM(11, "放电MOS损坏"),
    /**
     * 充电异常
     */
    CHARGE_EXCEPTION_ALARM(12, "充电异常");
    /**
     * 状态码
     */
    Integer code;
    /**
     * 消息
     */
    String msg;

    BatteryAlarmEnum(Integer code, String msg) {
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
        for (BatteryAlarmEnum bae : BatteryAlarmEnum.values()) {
            if (bae.getCode().equals(code)) {
                return bae.getMsg();
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
