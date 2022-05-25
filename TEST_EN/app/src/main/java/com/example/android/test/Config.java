package com.example.android.test;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Config{
    public static String conPath ="/sdcard/voltz";
    public static boolean isGetID;
    public static String mqttUrl;
    public static String mqttPort;
    public static String account;
    public static String password;
    public static String devId;
    public static String mac;
    public static Context context;
    public static int logPeriod;
    public static int infoPeriod;
    public static int alertPeriod;
    public static String ftpUrl;
    public static String ftpUser;
    public static String ftpPassWord;
    public static int ftpPort;
    public static String ftpRemoteDir;
    public static boolean autoUpdateApk;
    public static boolean autoUpdateSub;
    public static String urlCurrent;
    public static String apkFname;
    public static String mainFname;
    public static String subFname;
    public static String fPath;
    public static String apkFpath;
    public static String lastApkFname;
    public static String mainFpath;
    public static String subFpath;
    public static String mHwVersion;
    public static String mSwVersion;
    public static String mProVersion;
    public static int zldConfigPeriod;
    public static boolean isDownloading;
    public static boolean enableGzip;
    public static int resvision;
    public static int mainVersion;
    public static int subVersion;
    public static String wifiName;
    public static String wifiPass;
    public static String startPwd;
    public static void readConfig(){
        try{
            File file = new File(Config.conPath + "/config.ini");
            if(!file.exists()) {
                CommonClass.CreateFile(Config.conPath, "config.ini");
                FileOutputStream outputStream = new FileOutputStream(file);
                InputStream inputStream = Config.context.getAssets().open("config.ini");
                byte[] bd = new byte[inputStream.available()];
                inputStream.read(bd);
                outputStream.write(bd);
            }
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            Config.mqttUrl=properties.getProperty("mqttUrl","wooden-coral-pronghorn.rmq2.cloudamqp.com");
            Config.mqttPort=properties.getProperty("mqttPort","1883");
            Config.account=properties.getProperty("account","mda:mda");
            Config.password=properties.getProperty("password","#2mVdTyVnFmcyUi7N@@Y");
            Config.devId=properties.getProperty("devId","MDR20213400001");
            Config.infoPeriod=Integer.valueOf(properties.getProperty("infoPeriod","30"));
            Config.logPeriod=Integer.valueOf(properties.getProperty("logPeriod","2"));
            Config.ftpUrl=properties.getProperty("ftpUrl","192.168.0.109");
            Config.ftpUser=properties.getProperty("ftpUser","logs");
            Config.ftpPassWord=properties.getProperty("ftpPassWord","imCbe2ZHmpAEJGeh");
            Config.ftpPort=Integer.valueOf(properties.getProperty("ftpPort","21"));
            Config.ftpRemoteDir=properties.getProperty("ftpRemoteDir","/");
            Config.autoUpdateApk=Boolean.valueOf(properties.getProperty("autoUpdateApk","true"));
            Config.autoUpdateSub=Boolean.valueOf(properties.getProperty("autoUpdateSub","true"));
            Config.urlCurrent=properties.getProperty("urlCurrent","http://bra.smart2charge.com:30003");
            Config.mHwVersion=properties.getProperty("mHwVersion","200");
            Config.mProVersion=properties.getProperty("mProVersion","101");
            Config.mSwVersion=properties.getProperty("mSwVersion","101");
            Config.zldConfigPeriod=Integer.valueOf(properties.getProperty("zldConfigPeriod","5"));
            Config.apkFname=properties.getProperty("apkFname","");
            Config.lastApkFname=properties.getProperty("lastApkFname","");
            Config.mac=properties.getProperty("mac","");
            Config.enableGzip=Boolean.valueOf(properties.getProperty("enableGzip","false"));
            Config.alertPeriod=Integer.valueOf(properties.getProperty("alertPeriod","30"));
            Config.wifiName=properties.getProperty("wifiName","ZLD_TEST");
            Config.wifiPass=properties.getProperty("wifiPass","admin123");
            Config.startPwd=properties.getProperty("startPwd","voltz_zld_123");
            setConfig_map(properties);
        }catch(Exception e){
            Log.e("error",e.toString());
        }
    }

    private static void setConfig_map(Properties properties) {
        map_config = new HashMap<String, String>((Map) properties);
    }

    private static Map map_config;
    public static Map getConfig_map() {
        return map_config;
    }
    /**
     * 设置配置文件
     *
     * @param map 属性map
     * @param fn
     * @throws IOException
     */
    public static void Set_Config(Map<String, String> map, Object... fn) throws IOException {
        File file;
        if (fn.length > 0) {
            file = (File) fn[0];
        } else {
            file = new File(Config.conPath + "/config.ini");
        }
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            properties.setProperty(mapKey, mapValue);
        }
        properties.store(new FileOutputStream(file), null);

        if (fn.length <= 0) {
            int icout = 0;

            String[] sync = {"sync"};
            int result = execCommand(sync, "sh");
            while (result == -1) {
                if (icout > 5)
                    break;
                else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                result = execCommand(sync, "sh");
                icout++;
            }
        }
    }

    /**
     *
     * @param commands 命令字符串
     * @param commondModel  是"su"还是"sh"， "su"需要root权限
     * @return  返回-1的话说明命令执行错误了
     */
    public static int execCommand(String[] commands,String commondModel) {

        int result = -1;

        if (commands == null || commands.length == 0) {
            Log.v("TEST","请求命令错误");
            return -1;
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(commondModel);
            os = new DataOutputStream(process.getOutputStream());
            StringBuilder commandStr = new StringBuilder("");
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                commandStr.append(command);
                commandStr.append("\n");
            }

            Log.i("TEST","execCommand: 请求执行的命令：" + commandStr);
            os.write(commandStr.toString().getBytes());
            os.flush();
            os.writeBytes("exit\n");
            os.flush();

            result = process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }

}
