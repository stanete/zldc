package com.example.android.test.enums;

public enum BatCheckEnum {
    YES(1, "强校验"),
    NO(0, "弱校验");
    int code;
    String msg;

    BatCheckEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
