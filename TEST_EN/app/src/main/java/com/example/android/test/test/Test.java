package com.example.android.test.test;

import java.util.List;

public class Test {
    /**
     * 风扇
     */
    private boolean fan;
    /**
     * 照明
     */
    private boolean light;
    /**
     * 柜子温蒂
     */
    private Integer temp;
    /**
     * 主控固件名称
     */
    private String mainFname;
    /**
     * 分控固件名称
     */
    private String subFname;
    /**
     * 设备ID
     */
    private String devId;
    /**
     * mac地址
     */
    private String mac;
    /**
     * 仓门信息
     */
    private List<Box> boxs;

    public boolean isFan() {
        return fan;
    }

    public void setFan(boolean fan) {
        this.fan = fan;
    }

    public boolean isLight() {
        return light;
    }

    public void setLight(boolean light) {
        this.light = light;
    }

    public Integer getTemp() {
        return temp;
    }

    public void setTemp(Integer temp) {
        this.temp = temp;
    }

    public String getMainFname() {
        return mainFname;
    }

    public void setMainFname(String mainFname) {
        this.mainFname = mainFname;
    }

    public String getSubFname() {
        return subFname;
    }

    public void setSubFname(String subFname) {
        this.subFname = subFname;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public List<Box> getBoxs() {
        return boxs;
    }

    public void setBoxs(List<Box> boxs) {
        this.boxs = boxs;
    }

    @Override
    public String toString() {
        return "Test{" +
                "fan=" + fan +
                ", light=" + light +
                ", temp=" + temp +
                ", mainFname='" + mainFname + '\'' +
                ", subFname='" + subFname + '\'' +
                ", devId='" + devId + '\'' +
                ", mac='" + mac + '\'' +
                ", boxs=" + boxs +
                '}';
    }

    public class Box{
        /**
         * soc
         */
        private int soc;
        /**
         * 仓门状态
         */
        private boolean open;
        /**
         * 电池是否在线
         */
        private boolean batOnline;

        public int getSoc() {
            return soc;
        }

        public void setSoc(int soc) {
            this.soc = soc;
        }

        public boolean isOpen() {
            return open;
        }

        public void setOpen(boolean open) {
            this.open = open;
        }

        public boolean isBatOnline() {
            return batOnline;
        }

        public void setBatOnline(boolean batOnline) {
            this.batOnline = batOnline;
        }

        @Override
        public String toString() {
            return "Box{" +
                    "soc=" + soc +
                    ", open=" + open +
                    ", batOnline=" + batOnline +
                    '}';
        }
    }
}
