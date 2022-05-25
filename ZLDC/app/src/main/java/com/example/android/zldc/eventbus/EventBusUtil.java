package com.example.android.zldc.eventbus;


import com.example.android.zldc.CommonClass;

import org.greenrobot.eventbus.EventBus;

/**
 * @describe 参考：https://www.jianshu.com/p/e00297348f17
 */
public class EventBusUtil {

    public static void register(Object subscriber) {
        EventBus.getDefault().register(subscriber);
    }

    public static void unregister(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }

    public static void sendEvent(Event event) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(event);
    }

    public static class EventData {
        public static final int EVENT_OK = 0;
        public static final int EVENT_ERROR = -1;
        public static final int EVENT_INFO = 2;
        public static final int EVENT_WARNING = 1;
        public static final int EVENT_SP_R_DATA = 3;
        public static final int EVENT_SP_W_DATA = 4;
        private int code;
        private int byte_len;
        private final StringBuilder str_message = new StringBuilder();
        private byte[] byte_message;

        public EventData() {

        }

        public EventData(int code, String str_message) {
            this.code = code;
            this.str_message.append(str_message);
        }

        public EventData(int code, byte[] byte_message, int byte_len) {
            this.code = code;
            this.byte_len = byte_len;
            this.byte_message = byte_message;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public void setByte_message(byte[] byte_message, int byte_len) {
            this.byte_len = byte_len;
            this.byte_message = byte_message;
        }

        public void setStr_message(String str_message) {
            this.str_message.setLength(0);
            this.str_message.append(str_message);
        }

        public int getCode() {
            return code;
        }

        public byte[] getByte_message() {
            return byte_message;
        }

        public String byteToString() {
            this.str_message.setLength(0);
            str_message.append(CommonClass.bytesToHexString(byte_message, 0, byte_len));
            return str_message.toString();
        }

        public String AppendString(String str) {
            str_message.append(str);
            return str_message.toString();
        }

        public String getStr_message() {
            return str_message.toString();
        }

        public int getByte_len() {
            return byte_len;
        }
    }
}
