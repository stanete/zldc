package com.example.android.test;

import android.os.Build;
import android.util.Log;

import com.example.android.test.eventbus.C;
import com.example.android.test.eventbus.Event;
import com.example.android.test.eventbus.EventBusUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class MyLog {

    private MyLog() {
    }

    private static MyLog instance;

    public static MyLog getInstance() {
        if (instance == null)
            instance = new MyLog();
        return instance;
    }

    public static final int LOG_DEBUG = 0;
    public static final int LOG_INFO = 1;
    public static final int LOG_WARN = 2;
    public static final int LOG_ERROR = 3;

    private File logfile;
    private String date;
    private String path;
    StringBuilder stringBuilder = new StringBuilder();
    private final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    private final SimpleDateFormat logD = new SimpleDateFormat(("yyyyMMdd HH:mm:ss.SSS"));
    //每天创建一个日志文件
    public void Init(String path) {
        this.path = path;

        date = df.format(new Date()).split(" ")[0];

        deleteTimeoutLog(path, 15);

        try {
            logfile = CommonClass.CreateFile(path, date + ".log");
        } catch (Exception e) {
            EventBusUtil.sendEvent(new Event(C.EventCode.rece_system, "日志文件初始化异常：" + e));
        }
    }

    /**
     * 写入当天的日志文件
     *
     * @param live 日志等级
     * @param str  日志内容
     * @param test
     */
    public void Write_Log(int live, String str, boolean... test) {
        Log.e("Write_Log",str);
        if (logfile != null && logfile.exists()) {
//            if(DataManage.save_log || test.length > 0) {
            if (!date.equals(df.format(new Date()).split(" ")[0])) {
                Init(path);
            }

            stringBuilder.setLength(0);
            switch (live) {
                case LOG_DEBUG:
                    stringBuilder.append("[D ");
                    break;
                case LOG_INFO:
                    stringBuilder.append("[I ");
                    break;
                case LOG_WARN:
                    stringBuilder.append("[W ");
                    break;
                case LOG_ERROR:
                    stringBuilder.append("[E ");
                    break;
            }
            stringBuilder
                    .append(logD.format(new Date()))
                    .append(":")
                    .append(str)
                    .append(" ]")
                    .append("\n");
            write_tofile(stringBuilder.toString());
//            }
        }
    }

    private void write_tofile(String str) {
        try {
            FileOutputStream outStream = new FileOutputStream(logfile, true);
            outStream.write(str.getBytes());
            outStream.close();
        } catch (Exception e) {
            Log.e("Write_Log","日志写入异常："+e.toString());
        }
    }

    //删除time天之前日志文件
    private void deleteTimeoutLog(String path, int time) {
        String[] list = new File(path).list();
        if (list == null) return;

        ArrayList<Long> listdate = new ArrayList();
        for (String str : list) {
            if (str.contains(".log") && str.length() == "yyyyMMdd.log".length())
                listdate.add(Long.valueOf(str.replace(".log", "")));
        }

        int len = listdate.size();
        if (len > time) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                listdate.sort(new Comparator() {
                    @Override
                    public int compare(Object o, Object t1) {
                        long l1 = ((Long) o).longValue();
                        long l2 = ((Long) t1).longValue();

                        if (l1 > l2)
                            return 1;
                        if (l1 < l2)
                            return -1;

                        return 0;
                    }
                });
            } else {
                ArrayList arrayList = new ArrayList();
                for (int i = 0; i < listdate.size(); ) {
                    int cout = -1;
                    long l = Long.MAX_VALUE;
                    for (int j = 0; j < listdate.size(); j++) {
                        long d = listdate.get(j);
                        if (l > d) {
                            l = d;
                            cout = j;
                        }
                    }
                    arrayList.add(l);
                    listdate.remove(cout);
                }
                listdate = arrayList;
            }

            for (int i = 0; i < len - time; i++) {
                File f = new File(path + "/" + listdate.get(0) + ".log");
                if (f.exists())
                    f.delete();
                listdate.remove(0);
            }
        }
    }
}
