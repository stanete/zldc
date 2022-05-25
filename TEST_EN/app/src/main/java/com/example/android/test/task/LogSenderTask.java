package com.example.android.test.task;


import com.example.android.test.CommonClass;
import com.example.android.test.Config;
import com.example.android.test.MyLog;
import com.example.android.test.sers.MyService;
import com.example.android.test.util.FtpUtil;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

/**
 * 日志定时发送器
 */
public class LogSenderTask extends TimerTask {
    private MyLog myLog= MyLog.getInstance();
    private String logPath;
    private String mac;
    private SimpleDateFormat df_date = new SimpleDateFormat("yyyyMMdd");
    @Override
    public void run() {
        if(MyService.nwct.getNetWorkType()==-1){
            myLog.Write_Log(MyLog.LOG_INFO,"日志定时发送：没有网络");
            return;
        }
        String date = df_date.format(new Date());
        try{
            FtpUtil.FtpConfig ftpConfig=new FtpUtil.FtpConfig().setIp(Config.ftpUrl)
                    .setUserName(Config.ftpUser).setPassword(Config.ftpPassWord).setPort(Config.ftpPort);
            FtpUtil.makeDirectory(ftpConfig,Config.ftpRemoteDir,mac);
            String fileName = date + ".log";
            File logfile = CommonClass.CreateFile(logPath, fileName);
            boolean isSuccess = FtpUtil.upload(ftpConfig,"/"+mac,new FileInputStream(logfile),fileName);
            if(isSuccess){
                myLog.Write_Log(MyLog.LOG_INFO,"日志定时发送："+fileName+"\t文件大小："+logfile.length()/1024+"Kb");
            }else{
                myLog.Write_Log(MyLog.LOG_INFO,"日志定时发送："+fileName+"失败！");
            }
        }catch(Exception e){
            myLog.Write_Log(MyLog.LOG_INFO,"日志定时发送：异常"+e.toString());
        }
    }
    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
