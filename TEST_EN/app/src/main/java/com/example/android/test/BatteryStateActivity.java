package com.example.android.test;

import static com.example.android.test.TestActivity.seriaPort;

import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BatteryStateActivity extends AppCompatActivity {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private String boxNumber;              //仓号
    private String type;                   //当前状态
    private int inArrayNumber;          //数组中的标号,仓号减一
    private int timeInterval_ms = 2000; //刷新数据的间隔 ms单位
    private boolean isBack = false;//工作线程是否退出标志

    private String SCF = "Sub-control panel fault";//分控故障
    private String SCC = "Sub-control panel out of contact";//分控失联
    private String SW = "Software version";
    private String HW = "Protocol version";

    private String CYC = "Cycle index";//循环次数
    private String CEF = "Cell failure";//电芯失效
    private String CEI = "Cell imbalance";//电芯失衡
    private String BCF = "Battery communication fault";//电池通讯故障
    private String DLP = "Discharge low-temp. protection";//放电低温保护
    private String CLP = "Charge low-temp. Protection";//充电低温保护
    private String COP_C = "Charge overcurrent protection";//充电过流保护
    private String OTP = "Over-temp. protection";//温差过大保护
    private String TSD = "Temp. Sensor damage";//温度传感器损坏
    private String ICD = "IC damage";//IC损坏
    private String DHP = "Discharge high-temp. Protection";//放电高温保护
    private String DOP = "Discharge overcurrent protection";//放电过流保护
    private String COP_V = "Charge overvoltage protection";//充电过压保护
    private String DMD = "Discharge MOS damage";//放电MOS损坏
    private String CMD = "Charge MOS damage";//充电MOS损坏
    private String CHP = "Charge high-temp. protection";//充电高温保护
    private String DUP = "Discharge undervoltage protection";//放电欠压保护
    private String SCP = "Short-circuit protection";//短路保护


    int fullsoc = 0xFF;
    LocalData.Bms bd;
    LocalData.SubData subData;
    LocalData.ChargeData chargeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_state);

        //隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        boxNumber = getIntent().getStringExtra("Boxnumber");
        inArrayNumber = Integer.valueOf(boxNumber) - 1;
        subData = LocalData.getSubData(Integer.valueOf(boxNumber));
        chargeData = LocalData.getChargeData(boxNumber);
        bd = LocalData.getBatData(boxNumber);
        String datas = "";
        int box = Integer.valueOf(boxNumber);
        switch (box) {
            case 1:
                datas = "First ";
                break;
            case 2:
                datas = "Second ";
                break;
            case 3:
                datas = "Third ";
                break;
            case 4:
                datas = "Fourth ";
                break;
            case 5:
                datas = "Fifth ";
                break;
            case 6:
                datas = "Sixth ";
                break;
            case 7:
                datas = "Seventh ";
                break;
            case 8:
                datas = "Eighth ";
                break;
            default:
                datas = "unknow ";
                break;
        }
        ((TextView) findViewById(R.id.bucket_number_tev)).setText(datas + "box detailed information");
        if (boxNumber != null) {
            showall();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start;
                long end;

                int icout = 0;
                while (!isBack) {
                    try {
                        if (seriaPort != null) {

                            start = System.currentTimeMillis();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(500l);
                                        showall();
                                    } catch (Exception e) {
                                        Log.e("eeeeee", e.toString());
                                    }

                                }
                            });
                            end = System.currentTimeMillis();
                            if (end - start < timeInterval_ms) {
                                Thread.sleep(timeInterval_ms - (end - start));
                            }
                            if (icout < 3) {
                                icout++;
                            } else {
                                icout = 0;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    ArrayList arrayList = new ArrayList();

    private void showall() {

        bd = LocalData.getBatData(boxNumber);

        subData = LocalData.getSubData(Integer.valueOf(boxNumber));

        chargeData = LocalData.getChargeData(boxNumber);

        if (subData == null) {
            findViewById(R.id.no_battery_show_ll).setVisibility(View.VISIBLE);
            return;
        }
        int code = 0;
        String error = "";

        //分控故障 Sub control fault
        int subRunStatus = 0;
        boolean sublost = false;
        if (subData != null) {
            subRunStatus = subData.getBoxRunStatus();
            boolean isOffline = subData.getRuningState()[6];
            sublost = isOffline;
        }
        if (subRunStatus == 3) {
            //电池故障 Battery fault
            if (subData.getRuningState()[1]) {
                error = "Battery fault";
            }
            //充电器故障 Charger fault
            if (subData.getRuningState()[4]) {
                error = "Charger fault";
            }
        }
        if (sublost) {
            error = "Sub-control offline";
        }
        if(subData==null){
            sublost=true;
        }
        boxshow(bd, fullsoc, error, code);
        //0空仓 1 充电中 2 满仓 3 故障
        boolean ishavebat = (bd != null) && (subData != null) && (LocalData.isBatOnline()[Integer.valueOf(boxNumber) - 1]);
        if (sublost) {
            bucketshow(true);
            findViewById(R.id.no_battery_show_ll).setVisibility(View.VISIBLE);
        } else {
            bucketshow(false);
            if (ishavebat && bd != null) {
                //电池信息展示
                batteryshow();
                //充电器信息
                chargerInfo();
                findViewById(R.id.no_battery_show_ll).setVisibility(View.GONE);
            } else {
                findViewById(R.id.no_battery_show_ll).setVisibility(View.VISIBLE);
            }
        }

    }

    private void boxshow(LocalData.Bms bms, int fullsoc, String error, int code) {
        showa(code, bd, error, fullsoc);
    }

    private void showa(int code, LocalData.Bms bms, String error, int fullsoc) {
        boolean isshow = false;
        if (!error.isEmpty()) {
            isshow = true;
        }
        if (isshow) {
            findViewById(R.id.no_battery_show_ll).setVisibility(View.GONE);
            text_soc_size(28);
            text_soc_str(error);
            findViewById(R.id.socbg_batterystate).setBackgroundResource(R.drawable.bucket_border_suber);
        } else {
            showsoc(bd, fullsoc);
        }
    }

    private void showsoc(LocalData.Bms bms, int fullsoc) {
        text_soc_size(40);
        if ((bms != null) && (subData != null) && (LocalData.isBatOnline()[Integer.valueOf(boxNumber) - 1])) {
            double soc = bms.getBmsData().getSoc();
            SpannableString spannableString = new SpannableString(soc + "%");
            RelativeSizeSpan sizeSpan = new RelativeSizeSpan(0.7f);
            spannableString.setSpan(sizeSpan, spannableString.length() - 1, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            text_soc_str(spannableString);
            if (soc >= 80) {
                findViewById(R.id.socbg_batterystate).setBackgroundResource(R.drawable.bucket_border_full);
            } else if (soc >= 60) {
                findViewById(R.id.socbg_batterystate).setBackgroundResource(R.drawable.bucket_charge_80);
            } else if (soc >= 40) {
                findViewById(R.id.socbg_batterystate).setBackgroundResource(R.drawable.bucket_charge_60);
            } else if (soc >= 20) {
                findViewById(R.id.socbg_batterystate).setBackgroundResource(R.drawable.bucket_charge_40);
            } else {
                findViewById(R.id.socbg_batterystate).setBackgroundResource(R.drawable.bucket_charge_20);
            }
            findViewById(R.id.no_battery_show_ll).setVisibility(View.GONE);
        } else {
            findViewById(R.id.no_battery_show_ll).setVisibility(View.VISIBLE);
            text_soc_str("Empty");
            findViewById(R.id.socbg_batterystate).setBackgroundResource(R.drawable.alertdialog_border);
        }
    }

    private void bucketshow(boolean sublost) {
        String message;
        String chargeOffline = "<html><font color=\"#FF7C6B\">Charge Online：NO</font></html>";
        String chargeOnline = "Charge online：YES";
        String chargeFault = "<html><font color=\"#FF7C6B\">Charge fault：YES</font></html>";
        String chargeNorml = "Charge fault：NO";
        String batLockE="<html><font color=\"#FF7C6B\">Bat lock fault：YES</font></html>";
        String batLockF="Bat lock fault：NO";
        if (sublost) {
            message = SCC + ":";
            message = "<html><font color=\"#FF7C6B\">" + SCC + "：YES</font></html>";
            ((TextView) findViewById(R.id.communication_failure)).setText(Html.fromHtml(message));
            ((TextView) findViewById(R.id.hw_version)).setText(HW + "：" + subData.getSubProNum());
            ((TextView) findViewById(R.id.sw_version)).setText(SW + "：" + subData.getSubSoftNum());
            ((TextView) findViewById(R.id.charge_online)).setText(subData.getRuningState()[3] ? chargeOnline : Html.fromHtml(chargeOffline));
            ((TextView) findViewById(R.id.charge_faults)).setText(subData.getRuningState()[4] ? Html.fromHtml(chargeFault) : chargeNorml);
            ((TextView) findViewById(R.id.bat_lock_faults)).setText(subData.getRuningState()[5] ? Html.fromHtml(batLockE) : batLockF);
        } else {
            message = SCC + "：NO";
            ((TextView) findViewById(R.id.communication_failure)).setText(Html.fromHtml(message));
            try {
                ((TextView) findViewById(R.id.hw_version)).setText(HW + "：" + subData.getSubProNum());
                ((TextView) findViewById(R.id.sw_version)).setText(SW + "：" + subData.getSubSoftNum());
                ((TextView) findViewById(R.id.charge_online)).setText(subData.getRuningState()[3] ? chargeOnline : Html.fromHtml(chargeOffline));
                ((TextView) findViewById(R.id.charge_faults)).setText(subData.getRuningState()[4] ? Html.fromHtml(chargeFault) : chargeNorml);
                ((TextView) findViewById(R.id.bat_lock_faults)).setText(subData.getRuningState()[5] ? Html.fromHtml(batLockE) : batLockF);
            } catch (Exception e) {
                ((TextView) findViewById(R.id.hw_version)).setText(HW + "：");
                ((TextView) findViewById(R.id.sw_version)).setText(SW + "：");
                ((TextView) findViewById(R.id.charge_online)).setText(Html.fromHtml(chargeOffline));
                ((TextView) findViewById(R.id.charge_faults)).setText(chargeNorml);
                ((TextView) findViewById(R.id.bat_lock_faults)).setText(batLockF);
            }
        }
    }

    private void batteryshow() {
        LocalData.BmsData b = bd.getBmsData();
        long time = 0;
        ((TextView) findViewById(R.id.battery_voltage)).setText("BatteryVoltage：" + String.format("%.2f", b.getVol()) + "V");
        ((TextView) findViewById(R.id.battery_current)).setText("BatteryCurrent：" + String.format("%.2f", b.getCur()) + "A");
        ((TextView) findViewById(R.id.battery_soc)).setText("SOC：" + b.getSoc() + "%");
        ((TextView) findViewById(R.id.bat_id)).setText("ID：" + b.getId());
        ((TextView) findViewById(R.id.soh)).setText("SOH：" + b.getSoh() + "%");
        ((TextView) findViewById(R.id.max_chg_cur)).setText("MaxChgCur：" + String.format("%.2f", b.getMaxChargCur()) + "A");
        ((TextView) findViewById(R.id.max_chg_vol)).setText("MaxChgVol：" + String.format("%.2f", b.getMaxChargVol()) + "V");
        int contrModel = b.getContrModel();
        ((TextView) findViewById(R.id.contr_model)).setText("CtrWorkMode：" + (contrModel == 1 ? "Heating Mode" : "Charge Mode"));
        boolean conSwt = b.isContrSwitch();
        String conSwtStr = "";
        if(conSwt){
            conSwtStr = "Charger is on";
        }else{
            conSwtStr = "Charger is off";
        }
        ((TextView) findViewById(R.id.ccs)).setText("Charger_CtrSW：" + conSwtStr);
        ((TextView) findViewById(R.id.max_temp)).setText("MaxTemp：" + b.getMaxTemp() + "℃");
        ((TextView) findViewById(R.id.min_temp)).setText("MinTemp：" + b.getMinTemp() + "℃");

        boolean[] commonFaults = b.getCommonFault();

        boolean[] chargInFaults = b.getChargInFault();

        boolean[] chargOutFaults = b.getChargOutFault();

        ((TextView) findViewById(R.id.excessive_temp_diff)).setText(getMessage("ExcessiveTempDiff：",commonFaults[0]));
        ((TextView) findViewById(R.id.cexcessive_vol_diff)).setText(getMessage("CellExcessiveVolDiff：" ,commonFaults[1]));
        ((TextView) findViewById(R.id.afe_error)).setText(getMessage("AFE_Error：", commonFaults[2]));
        ((TextView) findViewById(R.id.mos_over_temp)).setText(getMessage("MosOverTemp：",commonFaults[3]));
        ((TextView) findViewById(R.id.external_eeprom_failure)).setText(getMessage("External_EEPROM_Failure：",commonFaults[4]));
        ((TextView) findViewById(R.id.rtc_failure)).setText(getMessage("RTC_Failure：",commonFaults[5]));
        ((TextView) findViewById(R.id.conflict_id)).setText(getMessage("ID_Conflict：" ,commonFaults[6]));
        ((TextView) findViewById(R.id.can_message_miss)).setText(getMessage("CAN_Message_Miss：" ,commonFaults[7]));
        ((TextView) findViewById(R.id.pexcessive_vol_diff)).setText(getMessage("PackExcessiveVolDiff：",commonFaults[8]));
        ((TextView) findViewById(R.id.cadcc)).setText(getMessage("ChgAndDisChgCurConflict：" ,commonFaults[9]));
        ((TextView) findViewById(R.id.ca)).setText(getMessage("CableAbnormal：", commonFaults[10]));

        ((TextView) findViewById(R.id.over_chg_warn)).setText(getMessage("OverChargeWarning：", chargInFaults[0]));
        ((TextView) findViewById(R.id.over_chg_error)).setText(getMessage("OverChargeError：", chargInFaults[1]));
        ((TextView) findViewById(R.id.chg_over_heat_warn)).setText(getMessage("ChgOverHeatWarning：", chargInFaults[2]));
        ((TextView) findViewById(R.id.chg_over_heat_error)).setText(getMessage("ChgOverHeatError：", chargInFaults[3]));
        ((TextView) findViewById(R.id.chg_low_temp_warn)).setText(getMessage("ChgLowTempWarning：", chargInFaults[4]));
        ((TextView) findViewById(R.id.chg_low_temp_error)).setText(getMessage("ChgLowTempError：", chargInFaults[5]));
        ((TextView) findViewById(R.id.chg_over_cur_warn)).setText(getMessage("ChgOverCurrentWarning：", chargInFaults[6]));
        ((TextView) findViewById(R.id.chg_over_cur_error)).setText(getMessage("ChgOverCurrentError：", chargInFaults[7]));
        ((TextView) findViewById(R.id.over_vol_warn)).setText(getMessage("OverVoltageWarning：", chargInFaults[8]));
        ((TextView) findViewById(R.id.over_vol_error)).setText(getMessage("OverVoltageError：", chargInFaults[9]));
        ((TextView) findViewById(R.id.com_error_chg)).setText(getMessage("CommunicationErrorWithChg：", chargInFaults[10]));
        ((TextView) findViewById(R.id.chg_soft_start_error)).setText(getMessage("ChgSoftStartError：", chargInFaults[11]));
        ((TextView) findViewById(R.id.chg_relay_stuck)).setText(getMessage("ChgRelayStuck：", chargInFaults[12]));

        ((TextView) findViewById(R.id.dischg_under_vol_warn)).setText(getMessage("DisChgUnderVolWarning：", chargOutFaults[0]));
        ((TextView) findViewById(R.id.dischg_under_vol_error)).setText(getMessage("DisChgUnderVolError：", chargOutFaults[1]));
        ((TextView) findViewById(R.id.deep_under_vol)).setText(getMessage("DeepUnderVol：", chargOutFaults[2]));
        ((TextView) findViewById(R.id.dischg_over_heat_warn)).setText(getMessage("DisChgOverHeatWarning：", chargOutFaults[3]));
        ((TextView) findViewById(R.id.dischg_over_heat_error)).setText(getMessage("DisChgOverHeatError：", chargOutFaults[4]));

        ((TextView) findViewById(R.id.dischg_low_temp_warn)).setText(getMessage("DisChgLowTempWarning：", chargOutFaults[5]));
        ((TextView) findViewById(R.id.dischg_low_temp_error)).setText(getMessage("DisChgLowTempError：", chargOutFaults[6]));
        ((TextView) findViewById(R.id.dischg_over_cur_warn)).setText(getMessage("DisChgOverCurWarning：", chargOutFaults[7]));
        ((TextView) findViewById(R.id.dischg_over_cur_error)).setText(getMessage("DisChgOverCurError：", chargOutFaults[8]));
        ((TextView) findViewById(R.id.under_vol_warn)).setText(getMessage("UnderVolWarning：", chargOutFaults[9]));

        ((TextView) findViewById(R.id.under_vol_error)).setText(getMessage("UnderVolError：", chargOutFaults[10]));
        ((TextView) findViewById(R.id.com_error_to_vcu)).setText(getMessage("CommunicationErrorToVCU：", chargOutFaults[11]));
        ((TextView) findViewById(R.id.dischg_soft_start_error)).setText(getMessage("DisChgSoftStartError：", chargOutFaults[12]));
        ((TextView) findViewById(R.id.dischg_relay_stuck)).setText(getMessage("DisChgRelayStuck：", chargOutFaults[13]));
        ((TextView) findViewById(R.id.dischg_short)).setText(getMessage("DisChgShort：", chargOutFaults[14]));

    }

    private String temperror(int temp) {
        if ((temp & 0xFF) == 0xFF || (temp & 0xFF) == 0x8F) {
            return "未定义";
        }

        return temp + "℃";
    }

    private void chargerInfo() {
        if (chargeData == null) {
            return;
        }
        boolean isChargeOnline = subData.getRuningState()[3];
        if (!isChargeOnline) {
            ((TextView) findViewById(R.id.voltage)).setText("Voltage：");
            ((TextView) findViewById(R.id.current)).setText("Current：");
            ((TextView) findViewById(R.id.power_switch)).setText("ChargeSwitch：");
            ((TextView) findViewById(R.id.chg_cur)).setText("ChargeCurrent：");
            ((TextView) findViewById(R.id.bat_vol)).setText("BatteryVoltage：");
            ((TextView) findViewById(R.id.out_vol)).setText("OutputVoltage：");
            ((TextView) findViewById(R.id.charger_abnormal_state)).setText("ChargerState：");
        } else {
            LocalData.ChargeInfo ci = chargeData.getChargeInfo();
            ((TextView) findViewById(R.id.voltage)).setText("Voltage："+String.format("%.2f", ci.getStVol()) + "V");
            ((TextView) findViewById(R.id.current)).setText("Current："+String.format("%.2f", ci.getStCur()) + "A");
            ((TextView) findViewById(R.id.power_switch)).setText("ChargeSwitch："+(ci.isOpenClose()?"on":"off"));
            ((TextView) findViewById(R.id.chg_cur)).setText("ChargeCurrent："+ci.getChargCur()+"A");
            ((TextView) findViewById(R.id.bat_vol)).setText("BatteryVoltage："+ci.getBatVol()+"V");
            ((TextView) findViewById(R.id.out_vol)).setText("OutputVoltage："+ci.getOutVol()+"V");
            String chargerStatusStr = "";
            int chargerStatus = ci.isStatus();
            switch (chargerStatus) {
                case 0:
                    chargerStatusStr = "idle";
                    break;
                case 1:
                    chargerStatusStr = "ready charging";
                    break;
                case 2:
                    chargerStatusStr = "charging";
                    break;
                case 3:
                    chargerStatusStr = "full";
                    break;
            }
            ((TextView) findViewById(R.id.charger_abnormal_state)).setText("ChargerState：" + chargerStatusStr);
        }
    }

    private boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    private void text_soc_size(int size) {
        ((TextView) findViewById(R.id.charge_soc_batstate)).setTextSize(size);
    }

    private void text_soc_str(CharSequence string) {
        TextView text_soc = ((TextView) findViewById(R.id.charge_soc_batstate));
        String str = text_soc.getText().toString();
        if (!str.equals(string.toString()))
            text_soc.setText(string);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isBack = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private CharSequence getMessage(String title, boolean val) {
        String temp = "<html><font color=\"#FF7C6B\">" + title;
        if (val) {
            temp += "Yes</font></html>";
        } else {
            temp = title + "No";
        }
        return Html.fromHtml(temp);
    }
}
