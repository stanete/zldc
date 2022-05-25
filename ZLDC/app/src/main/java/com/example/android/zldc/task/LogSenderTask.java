package com.example.android.zldc.task;


import com.example.android.zldc.CommonClass;
import com.example.android.zldc.Config;
import com.example.android.zldc.MainActivity;
import com.example.android.zldc.MyLog;
import com.example.android.zldc.sers.MyService;
import com.example.android.zldc.util.DecompressionUtil;
import com.example.android.zldc.util.FtpUtil;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
//            myLog.Write_Log(MyLog.LOG_INFO,"日志定时发送：没有网络");
            return;
        }
        //前一天的日志不再变化
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = (Date) calendar.getTime();
        String yesterday = df_date.format(date);

        try{
            FtpUtil.FtpConfig ftpConfig=new FtpUtil.FtpConfig().setIp(Config.ftpUrl)
                    .setUserName(Config.ftpUser).setPassword(Config.ftpPassWord).setPort(Config.ftpPort);
            FtpUtil.makeDirectory(ftpConfig,Config.ftpRemoteDir,mac);
            String fileName = yesterday + ".log";
            String uploadFileName=fileName+".tar.gz";
            //创建文件，不存在的话
            File logfile = CommonClass.CreateFile(logPath, fileName);   //xxxxx.log
            //创建文件，不存在的话
            File uploadfile = CommonClass.CreateFile(logPath, uploadFileName);   //xxxxx.log.tar.gz
            //压缩文件
            DecompressionUtil.compressToTargz(logfile.getPath());
            if(logfile.length()!=0){
                boolean isSuccess = FtpUtil.upload(ftpConfig,"/"+mac,new FileInputStream(uploadfile),uploadFileName);
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
