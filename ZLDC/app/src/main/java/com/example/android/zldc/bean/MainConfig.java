package com.example.android.zldc.bean;

import android.util.Log;

import com.alibaba.fastjson.annotation.JSONField;
import com.example.android.zldc.Config;
import com.example.android.zldc.MyLog;

import java.util.Map;

/**
 * 配置下发
 */
public class MainConfig {
    /**
     * 日志
     */
//    private MyLog myLog = MyLog.getInstance();



    /**
     * 协议号
     */
    private int proVer=101;
    /**
     * 软件版本号
     */
    private int softVer=101;
    /**
     * 充电器过温阈值
     */
    @JSONField(name="charge_over_temp_period")
    private int chargeOverTempPeriod;
    /**
     * 电池过温阈值
     */
    @JSONField(name="bat_over_temp_period")
    private int batOverTempPeriod;
    /**
     * soc阈值
     */
    @JSONField(name="soc_period")
    private int socPeriod;
    /**
     * 充电时长阈值
     */
    @JSONField(name="charge_time_period")
    private long chargeTimePeriod;
    /**系统设置参数开始**/
    /**
     * 柜子信息上报周期（s）
     */
    @JSONField(name="info_up_period")
    private int infoPeriod;
    /**
     * 日志上报ftp周期(m)
     */
    @JSONField(name="log_up_period")
    private int logPeriod;
    /**
     * 日志上传ftp服务器地址
     */
    @JSONField(name="ftp_url")
    private String ftpUrl;
    /**
     * ftp用户名
     */
    @JSONField(name="ftp_user")
    private String ftpUser;
    /**
     * ftp密码
     */
    @JSONField(name="ftp_password")
    private String ftpPassWord;
    /**
     * ftp服务器端口
     */
    @JSONField(name="ftp_port")
    private int ftpPort;
    /**
     * ftp远程根目录
     */
    @JSONField(name="ftp_remote_dir")
    private String ftpRemoteDir;
    /**
     * 是否启用gzip压缩消息体
     */
    @JSONField(name="enable_gzip")
    private boolean enableGzip;
    /**
     * 日志缓存天数
     */
    @JSONField(name="log_date")
    private int logDate;
    /**系统设置参数结束**/
    public MainConfig() {
    }

    public MainConfig(int proVer, int softVer, int chargeOverTempPeriod, int batOverTempPeriod, int socPeriod, long chargeTimePeriod, int infoPeriod, int logPeriod, String ftpUrl, String ftpUser, String ftpPassWord, int ftpPort, String ftpRemoteDir, boolean enableGzip, int logDate) {
        this.proVer = proVer;
        this.softVer = softVer;
        this.chargeOverTempPeriod = chargeOverTempPeriod;
        this.batOverTempPeriod = batOverTempPeriod;
        this.socPeriod = socPeriod;
        this.chargeTimePeriod = chargeTimePeriod;
        this.infoPeriod = infoPeriod;
        this.logPeriod = logPeriod;
        this.ftpUrl = ftpUrl;
        this.ftpUser = ftpUser;
        this.ftpPassWord = ftpPassWord;
        this.ftpPort = ftpPort;
        this.ftpRemoteDir = ftpRemoteDir;
        this.enableGzip = enableGzip;
        this.logDate = logDate;
    }

    public int getProVer() {
        return proVer;
    }

    public void setProVer(int proVer) {
        this.proVer = proVer;
    }

    public int getSoftVer() {
        return softVer;
    }

    public void setSoftVer(int softVer) {
        this.softVer = softVer;
    }

    public int getChargeOverTempPeriod() {
        return chargeOverTempPeriod;
    }

    public void setChargeOverTempPeriod(int chargeOverTempPeriod) {
        this.chargeOverTempPeriod = chargeOverTempPeriod;
    }

    public int getBatOverTempPeriod() {
        return batOverTempPeriod;
    }

    public void setBatOverTempPeriod(int batOverTempPeriod) {
        this.batOverTempPeriod = batOverTempPeriod;
    }

    public int getSocPeriod() {
        return socPeriod;
    }

    public void setSocPeriod(int socPeriod) {
        this.socPeriod = socPeriod;
    }

    public long getChargeTimePeriod() {
        return chargeTimePeriod;
    }

    public void setChargeTimePeriod(long chargeTimePeriod) {
        this.chargeTimePeriod = chargeTimePeriod;
    }

    public int getInfoPeriod() {
        return infoPeriod;
    }

    public void setInfoPeriod(int infoPeriod) {
        this.infoPeriod = infoPeriod;
    }

    public int getLogPeriod() {
        return logPeriod;
    }

    public void setLogPeriod(int logPeriod) {
        this.logPeriod = logPeriod;
    }

    public String getFtpUrl() {
        return ftpUrl;
    }

    public void setFtpUrl(String ftpUrl) {
        this.ftpUrl = ftpUrl;
    }

    public String getFtpUser() {
        return ftpUser;
    }

    public void setFtpUser(String ftpUser) {
        this.ftpUser = ftpUser;
    }

    public String getFtpPassWord() {
        return ftpPassWord;
    }

    public void setFtpPassWord(String ftpPassWord) {
        this.ftpPassWord = ftpPassWord;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    public void setFtpPort(int ftpPort) {
        this.ftpPort = ftpPort;
    }

    public String getFtpRemoteDir() {
        return ftpRemoteDir;
    }

    public void setFtpRemoteDir(String ftpRemoteDir) {
        this.ftpRemoteDir = ftpRemoteDir;
    }

    public boolean isEnableGzip() {
        return enableGzip;
    }

    public void setEnableGzip(boolean enableGzip) {
        this.enableGzip = enableGzip;
    }

    public int getLogDate() {
        return logDate;
    }

    public void setLogDate(int logDate) {
        this.logDate = logDate;
    }

    /**
     * 设置系统参数
     * @param mc
     */
    public static void setConfig(MainConfig mc){
        Map map= Config.getConfig_map();
        Config.infoPeriod=mc.getInfoPeriod();
        Config.logPeriod=mc.getLogPeriod();
        Config.ftpUrl=mc.getFtpUrl();
        Config.ftpUser=mc.getFtpUser();
        Config.ftpPassWord=mc.getFtpPassWord();
        Config.ftpPort=mc.getFtpPort();
        Config.ftpRemoteDir=mc.getFtpRemoteDir();
        Config.enableGzip=mc.isEnableGzip();
        Config.logDate=mc.getLogDate();
        map.put("infoPeriod",mc.getInfoPeriod()+"");
        map.put("logUpPeriod",mc.getLogPeriod()+"");
        map.put("ftpUrl",mc.getFtpUrl());
        map.put("ftpUser",mc.getFtpUser());
        map.put("ftpPassWord",mc.getFtpPassWord());
        map.put("ftpPort",mc.getFtpPort()+"");
        map.put("ftpRemoteDir",mc.getFtpRemoteDir());
        map.put("enableGzip",mc.isEnableGzip()+"");
        map.put("logDate",mc.getLogDate()+"");
        try{
            Config.Set_Config(map);
        }catch(Exception e){
            Log.e("setConfig",e.toString());
        }
    }
}
