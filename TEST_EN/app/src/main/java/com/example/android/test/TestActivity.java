package com.example.android.test;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.test.task.NetWorkCheckTask;
import com.example.android.test.task.NetWorkConnectTask;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestActivity extends AppCompatActivity {
    MyApp myApp;
    public static boolean isOpenSlot;
    private Button wifiButton,openQuery;
    public static SeriaPort seriaPort;
    private NetWorkCheckTask netWorkCheckTask;
    private Timer timer;
    private NetWorkConnectTask netWorkConnectTask;
    private String mac;
    private WifiSettingDialog wifiSettingDialog;
    private StartPwsDialog startPwsDialog;
    View   tview;
    Vector<View> views = new Vector<>();  //单仓集合
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.layout_update);
        super.onCreate(savedInstanceState);
        Config.context = this;
        Config.fPath = getExternalFilesDir(null).getPath();
        Config.readConfig();
        mac = getMac(this);
        netWorkConnectTask=new NetWorkConnectTask();
        netWorkConnectTask.setContext(this);
        wifiSettingDialog = new WifiSettingDialog(this, 0, onClickListener);
        startPwsDialog = new StartPwsDialog(this,0,onClickListener);
        startPwsDialog.show();
        //wifi连接
        try {
            openserialport();
        } catch (Exception e) {

        }
        tview=findViewById(R.id.tview);
        openQuery=(Button)findViewById(R.id.open_query);
        initClickEvent();
        showAllBox();
        //网络状态检测

    }

    @Override
    protected void onDestroy() {
        Log.e("life","onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.e("life","onRestart");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.e("life","onStart");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.e("life","onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.e("life","onResume");
        super.onResume();
    }

    private void initClickEvent() {
        for (int i = 0; i < 8; i++) {
            String str = String.format("%d",i + 1);
            views.add(tview.findViewWithTag(str));
        }
        ((Button) findViewById(R.id.wifiButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiSettingDialog.show();
            }
        });
        ((View) findViewById(R.id.openBoxButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TestActivity.this, "open all slot", Toast.LENGTH_SHORT).show();
                seriaPort.Command_OpenBox(0, 1);
            }
        });
        ((View) findViewById(R.id.lightButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TestActivity.this, "please wait...", Toast.LENGTH_SHORT).show();
                seriaPort.Command_LightChose(!LocalData.getMainData().getRuningState()[3]);
            }
        });
        ((View)findViewById(R.id.quitButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });
        openQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOpenSlot=!isOpenSlot;
                if(isOpenSlot){
                    openQuery.setText("OPEN");
                }else{
                    openQuery.setText("QUERY");
                }
            }
        });
    }

    private void openserialport() throws IOException {
        //232 ttys4
        //485 ttys1
        seriaPort = new SeriaPort(new File("/dev/ttyS4"), 115200, getSp_data);
        if (seriaPort.isHavePermission()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(2 * 1000);
                            seriaPort.Command_GetFileName(1);
                            Thread.sleep(2 * 1000);
                            seriaPort.Command_GetFileName(2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    //改为同步执行，下面这些方法需要改造
    SeriaPort.GetSp_Data getSp_data = new SeriaPort.GetSp_Data() {
        @Override
        public void getdata(byte[] bd) {
            int message_id = bd[3] & 0xFF;
            if ((bd[4] & 0xFF) + ((bd[5] << 8) & 0xFF00) == 0) {//下位机回复
                return;
            }

            byte[] bs = new byte[bd.length - 9];
            System.arraycopy(bd, 7, bs, 0, bs.length);

            String str = new String(bs);
            switch (message_id) {

            }
        }
    };

    /**
     * 显示单仓信息
     * @param view
     * @param subData
     * @param index
     */
    private void showBox(final View  view,final LocalData.SubData subData,final int index){
        view.post(new Runnable() {
            @Override
            public void run() {
                if(subData==null){
                    ((TextView)view.findViewById(R.id.te_soc)).setText("wait use•");
                    ((TextView)view.findViewById(R.id.sub_error)).setText("error");
                    ((TextView)view.findViewById(R.id.sub_status)).setText("offline");
                    ((TextView)view.findViewById(R.id.door_status)).setText("close");
                    return;
                }
                boolean[]  runingState = subData.getRuningState();
                //soc右上角
                if(runingState[0]){
                    LocalData.Bms bms = LocalData.getBatData(String.valueOf(subData.getBoxId()));
                    if(bms!=null){
                        ((TextView)view.findViewById(R.id.te_soc)).setText(bms.getBmsData().getSoc()+"");
                    }else{
                        ((TextView)view.findViewById(R.id.te_soc)).setText("wait use•");
                    }
                }else{
                    ((TextView)view.findViewById(R.id.te_soc)).setText("wait use•");
                }
                //故障   电池    分控  充电器  电池锁
                if(runingState[1]||runingState[2]||runingState[4]||runingState[5]){
                    ((TextView)view.findViewById(R.id.sub_error)).setText("error");
                }else{
                    ((TextView)view.findViewById(R.id.sub_error)).setText("norml");
                }
                //分控在线状态 左下角
                if(runingState[6]){
                    ((TextView)view.findViewById(R.id.sub_status)).setText("offline");
                }else{
                    ((TextView)view.findViewById(R.id.sub_status)).setText("online");
                }
                //仓门状态
                int boxStatus = subData.getBoxStatus();
                if(boxStatus==0){
                    ((TextView)view.findViewById(R.id.door_status)).setText("close");
                }else if(boxStatus==1){
                    ((TextView)view.findViewById(R.id.door_status)).setText("open");
                }else{
                    ((TextView)view.findViewById(R.id.door_status)).setText("fault");
                }
            }
        });
    }

    /**
     * 显示所有仓的信息
     */
    private void showAllBox(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        Thread.sleep(1000l);
                        String mv = LocalData.getVersion().getMainVer();
                        String sv = LocalData.getVersion().getSubVer();
                        String lockTemp = LocalData.getMainData().getLockTemp();
                        if (StringUtils.isEmpty(mv)) {
                            mv = "initing...waiting...";
                        }
                        if (StringUtils.isEmpty(sv)) {
                            sv = "initing...waiting...";
                        }
                        if (StringUtils.isEmpty(lockTemp)) {
                            lockTemp = "initing...waiting...";
                        } else {
                            lockTemp += "°";
                        }
                        ((TextView) findViewById(R.id.main_ver)).setText("main control:" + mv);
                        ((TextView) findViewById(R.id.sub_ver)).setText("sub control:" + sv);
                        ((TextView) findViewById(R.id.main_temp)).setText("temperature:" + lockTemp);
                        boolean[] runState = LocalData.getMainData().getRuningState();
                        //风扇
                        findViewById(R.id.fanButton).setBackgroundResource(runState[1] ? R.drawable.bucket_border_suber : R.drawable.bucket_border_subup);
                        findViewById(R.id.lightButton).setBackgroundResource(runState[3] ? R.drawable.bucket_border_suber : R.drawable.bucket_border_subup);
                        ((TextView) findViewById(R.id.devid)).setText("devID:" + Config.devId);
                        ((TextView) findViewById(R.id.mac)).setText("MAC:" + mac);
                        CopyOnWriteArrayList<LocalData.SubData> subList = LocalData.getLocalData().getSubDataList();
                        for(int i=0;i<8;i++){
                            LocalData.SubData subData=null;
                            View view = views.get(i);
                            try{
                                subData = subList.get(i);
                            }catch(Exception e){
                                showBox(view,null,i);
                                Log.e("testactivity1",e.toString());
                            }
                            if(subData==null){
                                continue;
                            }
                            showBox(view,subData,i);
                        }
                    }catch(Exception e){

                    }
                }
            }
        }).start();
    }

    //分控运行状态
    public static final String[] subRunStatus = new String[]{"bat online", "bat fault", "sub control fault", "charger online", "charger fault", "bat lock err", "sub control offline", "sub control ota upgrading"};
    //分控运行状态故障标记
    public static final boolean[] subRunStatusFlag = new boolean[]{false, true, true, false, true, true, true, false};
    //电池充电故障
    public static final String[] batChargeIn = new String[]{"cell over charge warning","cell over charge error", "pack charge over heat warning", "pack charge over heat error", "pack charge low temperatue warning","pack charge low temperatue error", "pack chare over current warning", "pack chare over current error", "pack over voltage warning", "pack over voltage error", "communication error with charger", "pack charge soft start error", "charging relay stuck"};
    //电池充电故障标记
    public static final boolean[] batChargeInFlag = new boolean[]{true, true, true, true, true, true, true, true, true, true, true, true, true};
    //电池放电故障
    public static final String[] batChargeOut = new String[]{"cell discharge under voltage warning","cell discharge under voltage error","cell deep under voltage","pack discharge over heat warning","pack discharge over heat error","pack discharge low temperature warning","pack discharge low temperature error","pack dischage over current waning","pack dischage over current error","pack under voltage warning","pack under voltage  error","Communication error to VCU","pack discharge soft start error","discharging relay stuck","pack discharge short"};
    //电池放电故障标记
    public static final boolean[] batChargeOutFlag = new boolean[]{true, true, true, true, true, true, true, true, true, true, true, true, true, true, true};
    //电池通用故障
    public static final String[] batChargeComm = new String[]{"pack excessive temperature differentials","cell excessive voltage differentials","AFE Error","MOS over temperature","external EEPROM failure","RTC failure","ID conflict","CAN message miss","pack excessive voltage differentials","charge and discharge current conflict","cable abnormal"};
    //电池通用故障标记
    public static final boolean[] batChargeCommFlag = new boolean[]{true, true, true, true, true, true, true, true, true, true, true};
    //充电器故障信息
    public static final String[] Charge = new String[]{"output voltage high", "average output voltage high", "battery voltage high", "average battery voltage high", "average battery voltage low", "battery reverse connection", "battery voltage abnormal", "output overcurrent", "average output current high", "charger temperature too high"};
    //充电器故障信息标记
    public static final boolean[] ChargeFlag = new boolean[]{true, true, true, true, true, true, true, true, true, true};

    private String getFaultStr(String[] parm, boolean[] stand, boolean[] colors) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stand.length; i++) {
            if (stand[i]) {
                if (colors[i]) {
                    sb.append("<font color=\"#FF0000\">" + parm[i] + "</font>").append(",");
                } else {
                    sb.append(parm[i]).append(",");
                }
            }
        }
        int datalength = sb.length();
        if (datalength > 0) {
            sb.replace(datalength - 1, datalength, "");
        }
        return sb.toString();
    }


    private String getMac(Context context) {
        String mac = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = CommonClass.getMacDefault(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = CommonClass.getMacAddress();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mac = CommonClass.getMacFromHardware();
        }
        return mac;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_save:
                    String wifiName = wifiSettingDialog.wifi_name.getText().toString().trim();
                    String wifiPass = wifiSettingDialog.wifi_pass.getText().toString().trim();
                    Config.wifiName = wifiName;
                    Config.wifiPass = wifiPass;
                    Map cmap = Config.getConfig_map();
                    cmap.put("wifiName", wifiName);
                    cmap.put("wifiPass", wifiPass);
                    try {
                        Config.Set_Config(cmap);
                    } catch (Exception e) {
                    }
                    //销毁当前弹框
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            netWorkConnectTask.run();
                        }
                    }).start();
                    wifiSettingDialog.dismiss();
                    break;
                case R.id.wifi_con:
                    Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                    intent.putExtra("only_access_points", true);
                    intent.putExtra("extra_prefs_show_button_bar", true);
                    intent.putExtra("extra_prefs_set_next_text", "back");
                    intent.putExtra("extra_prefs_set_back_text", "");
                    startActivityForResult(intent, 1);
                    break;
                case R.id.enter:
                    String pwd = startPwsDialog.pwd.getText().toString().trim();
                    Log.e("startpassword",pwd);
                    Log.e("startpassword_",Config.startPwd);
                    if(pwd.equals(Config.startPwd)){
                        startPwsDialog.dismiss();
                    }
                    break;
                case R.id.quit:
                    System.exit(0);
                    break;
            }
        }
    };

}
