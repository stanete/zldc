package com.example.android.test;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.android.test.bean.MainConfig;
import com.example.android.test.eventbus.C;
import com.example.android.test.eventbus.Event;
import com.example.android.test.eventbus.EventBusUtil;
import com.example.android.test.util.CrCUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class SeriaPort extends Application {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    //    public static boolean isDataTran = false;
    SimpleDateFormat dfst = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

    // JNI
    public native String stringFromJNI();

    private native static FileDescriptor open(String path, int baudrate, int flags);

    public native void close();

    public native int crc16(byte[] bd, int len);

    public native static String Base_64(byte[] bd, int len);

    // Used to load the 'native-lib' library on application startup.
    public static final int SP_WRITE_MAIN_INFO = 1;
    public static final int SP_WRITE_SUB_INFO = 2;
    public static final int SP_WRITE_CONF_INFO = 3;
    public static final int SP_WRITE_BMS_INFO = 4;
    public static final int SP_WRITE_OPEN_BOX = 5;
    public static final int SP_WRITE_OPEN_BOX_RESULT = 6;
    public static final int SP_WRITE_UPDATE_PRO = 0xE;
    public static final int SP_WRITE_CONFIG = 0x0F;


    public static final int SP_READ_MAIN_INFO = 0x81;
    public static final int SP_READ_SUB_INFO = 0x82;
    public static final int SP_READ_CONF_INFO = 0x83;
    public static final int SP_READ_BMS_INFO = 0x84;
    public static final int SP_READ_OPEN_BOX = 0x85;
    public static final int SP_READ_OPEN_BOX_RESULT = 0x86;
    public static final int SP_READ_CHARGING=0x87;
    public static final int SP_READ_UPDATE_PRO = 0x8E;
    public static final int SP_READ_CONFIG = 0x8F;

    public static final int DevcieErrore = -1;
    public static final int BucketsError = -2;
    public static final int BatteryError = -3;
    public static int SP_WRITE_READ_ERROE = -1;
    public static int SP_WRITE_READ_OK = 0;
    private final int SP_READ_WAIT_MS = 2000;
    private final int SP_READ_WAIT_RS_MS = 500;
    private final int SP_READ_WAIT_R_MS = 10;
    private final int FI_VALUE_ZERO = 0;
    private final int FI_VALUE_R_COUT = 5;
    private int cout;//??????????????????

    final int STX = 0x68;
    final int ETX = 0x16;
    final int MID = 3;
    final int SL = 4;
    final int SD = 7;

    private int FLAG = 0x68, ADDR = 0, FrameID = 0;
    private String filename;

    public void setFilename(String filename) {
        this.filename = filename;
    }

    private UpdateData hostUpdateData;

    public void SetHostConfig(UpdateData hostUpdateData) {
        this.hostUpdateData = hostUpdateData;
    }

    private final boolean isHavePermission = true;

    public boolean isHavePermission() {
        return isHavePermission;
    }

    //??????????????????????????????????????????????????????????????????????????????
    //???????????????????????????????????????????????????????????????????????????????????????????????????
    private AutoData_interface autoData_interface;

    public interface AutoData_interface {//?????????????????????????????????????????????

        void Exception(int mode, int... number);

        void DeviceState(int mode, int boxnumber, boolean ist);
    }

    private Update_interface update_interface;

    public interface Update_interface {//??????????????????

        void dataTransmission(int type);//??????????????????

        void getData(boolean isok, int boxid);//????????????
    }

    private final SeriaPort.GetSp_Data getSp_data;

    public interface GetSp_Data {
        void getdata(byte[] bd);
    }

    public static boolean is_up_Available;

    public boolean is_up_Available(int time_out) throws InterruptedException {
        long start = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - start > time_out)
                break;

            if (is_up_Available)
                break;

            Thread.sleep(50);
        }
        return is_up_Available;
    }

    EventBusUtil.EventData eventData_w = new EventBusUtil.EventData();
    EventBusUtil.EventData eventData_devstate = new EventBusUtil.EventData();

    private final FileDescriptor mFd;
    private final FileInputStream mFileInputStream;
    private final FileOutputStream mFileOutputStream;

    Handler handler;
    final int RECV_DATA = 0;
    ByteBuffer byteBuffer = ByteBuffer.allocate(0xFFFF);
    HandlerThread handlerThread = new HandlerThread("handlerThread");

    private GetBms_interface bms_interface;

    public interface GetBms_interface {
        void getData(int bucket_id);
    }

    private StateManual_interface stateManual_interface;

    public interface StateManual_interface {
        void getData();
    }

    private Commands_interface commands_interface;

    public interface Commands_interface {
        void getData(int mode);
    }

    private UpdateSendOTA_interface sendOTA_interface;

    public interface UpdateSendOTA_interface {
//        void setData(int boxnumber,String type,String ufn,String fn,String bid,int yy,ToUpdateServer.upgradeNum upgradeNum);
    }

    private Update_interface update_main;
    private Update_interface update_sub;

    public SeriaPort(File device, int baudrate,
                     SeriaPort.GetSp_Data getSp_data) throws IOException {
        this.getSp_data = getSp_data;
        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            EventBusUtil.EventData eventData = new
                    EventBusUtil.EventData(EventBusUtil.EventData.EVENT_WARNING,
                    "??????????????????" + device.getPath() + " " + baudrate);
            EventBusUtil.sendEvent(new Event(C.EventCode.rece_system, eventData));
        }
        mFd = open(device.getAbsolutePath(), baudrate, 0);
        if (mFd == null) throw new IOException();
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
        thread_recv(device, baudrate);
    }

    Thread readThread;

    //????????????????????????
    private void thread_recv(final File device, final int baudrate) {
        readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                EventBusUtil.EventData eventData = new
                        EventBusUtil.EventData(EventBusUtil.EventData.EVENT_OK,
                        "??????????????????: " + device.getPath() + " " + baudrate);
                EventBusUtil.sendEvent(new Event(C.EventCode.rece_system, eventData));
                handlerThread.start();
                handler = new Handler(handlerThread.getLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case RECV_DATA://?????????????????????????????????????????????????????????
                                //????????????????????????????????????????????????????????????
                                try {
                                    byteBuffer.put((byte[]) msg.obj, 0, msg.arg1);
//                                    if (SeriaPort.isDataTran) {
//                                        if (byteBuffer.position() == 10) {
//                                            byte[] bd = new byte[10];
//                                            byteBuffer.flip();
//                                            byteBuffer.get(bd, 0, 10);
//                                            byteBuffer.compact();
//                                            update_parsing_low(bd, 10);
//                                        } else if (byteBuffer.position() > 10) {
//                                            byteBuffer.clear();
//                                            Log.i("TEST", "?????????????????? : " +
//                                                    CommonClass.bytesToHexString((byte[]) msg.obj, 0, msg.arg1));
//                                        }
//                                    } else {
                                    data_cycle();
//                                    }
                                } catch (Exception e) {
                                    byteBuffer.clear();
                                    EventBusUtil.EventData eventData = new EventBusUtil.EventData();
                                    eventData.setCode(EventBusUtil.EventData.EVENT_ERROR);
                                    eventData.setStr_message("??????????????????" + e);
                                    send_event_str(eventData);
                                }
                                break;
                        }
                    }
                };
//                MyService.updateThread.start();

                byte[] buffer = new byte[255];

                //30?????????????????????????????????App
                ss_time = System.currentTimeMillis();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (System.currentTimeMillis() - ss_time >= 5 * 60 * 1000) {
//                                System.exit(0);
                            }
                            try {
                                Thread.sleep(1 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                while (true) {
                    try {
                        if (mFileInputStream == null) return;
                        ss_time = System.currentTimeMillis();
                        int size = mFileInputStream.read(buffer);
                        if (size > 0) {
                            byte[] bd = new byte[size];
                            System.arraycopy(buffer, 0, bd, 0, size);
                            Message message = Message.obtain();
                            message.what = RECV_DATA;
                            message.arg1 = size;
                            message.obj = bd;
                            handler.sendMessage(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        readThread.start();
        wr = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        int size = wiret_array.size();
                        if (size > 0) {
                            write_data(wiret_array.get(0));
                            wiret_array.remove(0);
                            Log.i("TEST", "++++++wiret_array:" + wiret_array.size() + "+++++++++++");
                        } else {
                            synchronized (wr) {
                                wr.wait();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        wr.start();
    }

    long ss_time = 0;

    //??????????????????????????????????????????????????????????????????
    private void data_cycle() {
        for (; byteBuffer.position() > 0; ) {
            if ((byteBuffer.get(0) & 0xFF) == STX) {
                if (byteBuffer.position() >= 3) {
                    int len = (byteBuffer.get(1) & 0xFF)
                            + ((byteBuffer.get(2) & 0xFF) << 8);
                    if (byteBuffer.position() >= len) {
                        if (byteBuffer.get(len - 1) == ETX) {
                            boolean isNextPacket = false;
                            if (byteBuffer.position() > len) {
                                isNextPacket = true;
                            }
                            byte[] bd = new byte[len];
                            byteBuffer.flip();
                            byteBuffer.get(bd, 0, len);
                            byteBuffer.compact();
                            if (data_check(bd, len)) {
                                packet_parsing(bd, len);
                            } else {
                                EventBusUtil.EventData eventData = new EventBusUtil.EventData();
                                eventData.setCode(EventBusUtil.EventData.EVENT_ERROR);
                                eventData.setStr_message("??????????????????");
                                send_event_str(eventData);
                            }

                            if (isNextPacket)
                                data_cycle();
                        } else {
                            byteBuffer.flip();
                            byteBuffer.position(3);
                            byteBuffer.compact();

                            EventBusUtil.EventData eventData = new EventBusUtil.EventData();
                            eventData.setCode(EventBusUtil.EventData.EVENT_ERROR);
                            eventData.setStr_message("??????ETX??????");
                            send_event_str(eventData);

                            data_cycle();
                        }
                    } else {
                        if (len > 255 * 2)
                            byteBuffer.clear();
                        break;
                    }
                } else
                    break;
            } else {
                byteBuffer.flip();
                byteBuffer.get();
                byteBuffer.compact();
            }
        }
    }


    /**
     * ?????????????????????????????????????????????????????????????????????
     *
     * @param isreply  true:??????     false:??????
     * @param isonline true:???????????? false:????????????
     */
    public void Dev_State_Change_Auto(boolean isreply, boolean isonline, int... subr) throws IOException {
        if (subr.length > 0) {
//            write_in_read(getPackages(SP_WRITE_DEV_STATE_A, new byte[]{(byte) 0xFE, ((byte) (isonline ? 1 : 2))}));
        } else {
//            write_in_read(getPackages(SP_WRITE_DEV_STATE_A, new byte[]{((byte) (isreply ? 1 : 2)), ((byte) (isonline ? 1 : 2))}));
        }
    }

    /**
     * ??????????????????
     *
     * @param value ??????(min)
     */
    public void Command_Charging_Time_Value(float value, Commands_interface commands_interface) throws IOException {
        this.commands_interface = commands_interface;
        int iv = (int) (value);
        //word?????????byte
//        write_data(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0x30, new byte[]{(byte) iv, (byte) (iv >> 8)})));
    }

    /**
     * ?????????????????????
     *
     * @param value 0.1??
     */
    public void Command_Charger_Temperature_Value(float value, Commands_interface commands_interface) throws IOException {
        this.commands_interface = commands_interface;
        int iv = (int) (value * 10);
//        write_data(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0x40, new byte[]{(byte) iv, (byte) (iv >> 8)})));
    }

    /**
     * ????????????????????????
     *
     * @param value 0.1??
     */
    public void Command_Charging_Chamber_Temperature_Value(float value, Commands_interface commands_interface) throws IOException {
        this.commands_interface = commands_interface;
        int iv = (int) (value * 10);
//        write_data(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0x50, new byte[]{(byte) iv, (byte) (iv >> 8)})));
    }

    /**
     * ???????????????????????????
     *
     * @param type       1:?????????????????????????????? 2:??????????????????/??????????????? 3:??????????????????/???????????????
     * @param fiducial   ??? type ???3?????????   ????????????
     * @param difference ??? type ???3?????????   ???????????????
     */
    public void Command_Dev_Temperature(byte type, float fiducial, float difference, Commands_interface commands_interface) throws IOException {
        this.commands_interface = commands_interface;
        int ifid = (int) (fiducial * 10);
        int idif = (int) (difference * 10);
        byte[] data;
        if (type == 3) {
            data = new byte[5];
            data[0] = type;
            data[1] = (byte) ifid;
            data[2] = (byte) (ifid >> 8);
            data[3] = (byte) idif;
            data[4] = (byte) (idif >> 8);
        } else {
            data = new byte[]{type};
        }
//        write_data(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0x60, data)));
    }

    /**
     * ???????????????
     *
     * @param type       1:????????? 2:????????? 3:????????? 4:????????????
     * @param hw_version ??? type ???3????????? ????????????
     * @param bid        ??? type ???3????????? ??????ID
     */
    public void Command_GetVerion(byte type, int hw_version, String bid, Commands_interface commands_interface) {
        this.commands_interface = commands_interface;
        byte[] data = new byte[19];
        data[0] = type;
        if (type == 3 || type == 4) {
            int off = 1;
            data[off++] = (byte) hw_version;
            data[off++] = (byte) (hw_version >> 8);
            if (bid == null) {
                //??????ID????????????
                return;
            } else {
                byte[] id = bid.getBytes();
                for (byte b : id)
                    data[off++] = b;
            }
        }

//        write_in_read(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0x80, data)));
    }

    /**
     * ???????????????????????????
     */
    public void Command_GetOperationArea(Commands_interface commands_interface) throws IOException {
        this.commands_interface = commands_interface;
//        write_data(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0xA0, new byte[]{1})));
    }

    /**
     * ?????????????????????????????????
     *
     * @param item ??????????????????
     */
    public void Command_GetBattery(byte item, Commands_interface commands_interface, boolean... asynch) throws IOException {
        this.commands_interface = commands_interface;
        if (asynch.length > 0) {
//            write_in_read(getPackages(SP_WRITE_DEV_COMMAND,
//                    command_package(0x90, new byte[]{item, 0})));
        } else {
//            write_data(getPackages(SP_WRITE_DEV_COMMAND,
//                    command_package(0x90, new byte[]{item, 0})));
        }
    }

    /**
     * ?????????????????????
     */
    public void Command_GetChargerTemp(boolean asynch, Commands_interface commands_interface) throws IOException {
        this.commands_interface = commands_interface;
//        byte[] bd = getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0xB0, new byte[]{1}));
        if (asynch) {
//            write_in_read(bd);
        } else {
//            write_data(bd);
        }
    }

    /**
     * ??????WiFi?????? ????????????????????????
     */
    public void Command_GetWifiState(Commands_interface commands_interface) throws IOException {
        this.commands_interface = commands_interface;
//        write_data(getPackages(SP_WRITE_DEV_COMMAND, command_package(0xC0, new byte[]{1})));
    }

    /**
     * ??????????????????BMS???????????????????????????
     */
    public void Command_GetBmsState(boolean asynch, Commands_interface commands_interface) throws IOException {
        this.commands_interface = commands_interface;
//        byte[] bd = getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0xD0, new byte[]{1}));
        if (asynch) {
//            write_in_read(bd);
        } else {
//            write_data(bd);
        }
    }

    /**
     * ??????SOC
     */
    public void Command_GetSOC(Commands_interface commands_interface) throws IOException {
        this.commands_interface = commands_interface;
//        write_data(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0xE0, new byte[]{1})));

        myLog.Write_Log(MyLog.LOG_INFO, "??????SOC", true);
    }

    /**
     * ?????????????????????
     * ???????????????????????????????????? 0x0000:????????????????????????
     * 0x0001:????????????????????????????????????????????????
     */
    public void Command_GetSubVersion() {
//        write_in_read(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0xF0, new byte[]{0, 0})));
    }

    /**
     * ?????????????????????????????????
     *
     * @param type       0x03:??????????????? 0x04:??????????????????
     * @param hw_version ????????????
     * @param bid        ID
     * @param Num        ????????????
     */
    public void Command_SetupgradeNum(byte type, int hw_version, String bid, int Num) {
        byte[] data = new byte[21];
        int off = 0;
        data[off++] = type;
        data[off++] = (byte) hw_version;
        data[off++] = (byte) (hw_version >> 8);
        byte[] id = bid.getBytes();
        for (byte b : id)
            data[off++] = b;
        data[19] = (byte) Num;
        data[20] = (byte) (Num >> 8);

//        write_in_read(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0x100, data)));
    }

    /**
     * ?????????????????????
     *
     * @param type       0x03:??????????????? 0x04:??????????????????
     * @param hw_version ????????????
     * @param bid        ID
     */
    public void Command_ClearFirmware(byte type, int hw_version, String bid) {
//        command_need_id_hv_type(type, hw_version, bid, 0x110);
    }

    /**
     * ????????????????????????????????????
     *
     * @param type        0x03:??????????????? 0x04:??????????????????
     * @param door_number ??????
     * @param clear       true:???????????????????????? false:????????????????????????
     */
    public void Command_GetupgradeResult(byte type, byte door_number, boolean clear) {
        byte[] data = new byte[3];
        data[0] = type;
        data[1] = door_number;
        data[2] = (byte) (clear ? 2 : 1);
//        write_in_read(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0x120, data)));
    }

    /**
     * ????????????
     *
     * @param slotId   ????????????
     * @param slotInfo ????????????
     */
    public void Command_OpenBox(int slotId, int slotInfo) {
//        LocalData.Bms bms = LocalData.getBatData(String.valueOf(slotId));
//        Api.BoxOpenComm boxComm = Api.getBoxOpenComm();
//        String orderNum = boxComm.getOrderNumber();
//        try{
//            if(LocalData.getIsUpdating()){
//                //???????????????????????????
//                sendNotifications(slotId,orderNum,"ota_upgrading",bms.getBmsData().getId(),bms.getBmsData().getSoc());
//                return;
//            }
//            if(slotInfo==1){
//                //??????
//                if(bms!=null){
//                    //???????????????
//                    sendNotifications(slotId, orderNum, "open_empty_slot_bat_online", bms.getBmsData().getId(), bms.getBmsData().getSoc());
//                    return;
//                }
//            }else if(slotInfo==2){
//                //??????
//                if(bms==null){
//                    //???????????????
//                    sendNotifications(slotId, orderNum, "open_bat_slot_bat_offline", "", 0);
//                    return;
//                }
//            }else{
//                throw new RuntimeException("?????????????????????"+slotInfo);
//            }
//        }catch(Exception e){
//            myLog.Write_Log(MyLog.LOG_INFO, "?????????????????????" + String.valueOf(slotId) + "???\t???????????????"+e.toString());
//            return;
//        }
        //????????????????????????????????????????????????????????????????????????????????????
        myLog.Write_Log(MyLog.LOG_INFO, "?????????????????????" + String.valueOf(slotId) + "???");
        write_in_read(getPackages(SP_WRITE_OPEN_BOX, new byte[]{(byte) (slotId - 1)}));
    }

    /**
     * ??????????????????????????????????????????
     */
    public void Command_ReplyNetWork(boolean isNetWork,boolean isConnected){
        boolean[] result=new boolean[16];
        result[0]=isNetWork;
        result[1]=isConnected;
        byte[] data = toBytes(result);
        write_in_read(getPackages(SP_WRITE_MAIN_INFO, data));
    }

    /**
     * boolean ??? byte
     * @param arrs
     * @return
     */
    public byte[] toBytes(boolean[] arrs){
        int f = 0;byte b = 0;
        byte[] ret = new byte[arrs.length/7 + (arrs.length%7==0?0:1)];
        for(int i=0; i<arrs.length; i+=8){
            for(int j=0; j<8; j++){
                if(i+j<arrs.length && arrs[i+j]){
                    b+= (1<<(7-j));
                }
                if(j==7){
                    ret[f++] = b;
                    b = 0;
                }
            }
        }
        return ret;
    }

    /**
     * ??????????????????
     *
     * @param slotId
     */
    public void Command_OpenBoxResult(int slotId) {
        myLog.Write_Log(MyLog.LOG_INFO, "???????????????????????????" + String.valueOf(slotId) + "???");
        write_in_read(getPackages(SP_WRITE_OPEN_BOX_RESULT, new byte[]{(byte) (slotId - 1)}));
    }

    /**
     * ????????????????????????
     */
    public void Command_GetMainInfo() {
        write_in_read(getPackages(SP_WRITE_MAIN_INFO, new byte[]{}));
    }

    /**
     * ????????????????????????
     */
    public void Command_GetSubInfo() {
        write_in_read(getPackages(SP_WRITE_SUB_INFO, new byte[]{0, 0}));
    }

    /**
     * ??????bms??????
     */
    public void Command_GetBmsInfo() {
        write_in_read(getPackages(SP_READ_BMS_INFO, new byte[]{0, 0}));
    }

    /**
     * ???????????????????????????????????????
     */
    public void Command_ClearHistoryRecord() {
//        write_in_read(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0x6667, new byte[]{1})));
    }

    /**
     * ???????????????
     *
     * @param doorNum ??????
     */
    public void Command_RemoteOpenDoor(byte doorNum) {
//        write_in_read(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0x6668, new byte[]{doorNum})));
    }

    /**
     * ???????????????????????????
     *
     * @param type       0x03:???????????? 0x04:???????????????
     * @param hw_version ????????????
     * @param bid        ID
     */
    public void Command_NumberUpgrades(byte type, int hw_version, String bid) {
//        command_need_id_hv_type(type, hw_version, bid, 0x140);
    }

    /**
     * ???????????????????????????
     *
     * @param type       0x03:???????????? 0x04:???????????????
     * @param hw_version ????????????
     * @param bid        ID
     */
    public void Command_TESTNumberUpgrades(byte type, int hw_version, String bid) {
//        command_need_id_hv_type(type, hw_version, bid, 0x150);
    }

    public void command_need_id_hv_type(byte type, int hw_version, String bid, int cmd) {
        byte[] data = new byte[19];
        int off = 0;
        data[off++] = type;
        data[off++] = (byte) hw_version;
        data[off++] = (byte) (hw_version >> 8);
        byte[] id = bid.getBytes();
        for (byte b : id)
            data[off++] = b;

//        write_in_read(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(cmd, data)));
    }

    /**
     * ???????????????????????????
     */
    public void Command_ChargingoverCurrent_Value(int data) throws IOException {
//        write_in_read(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0x160, new byte[]{(byte) data})));
    }

    /**
     * ??????????????????
     */
    public void Command_BatteryThermal(int data) throws IOException {
//        write_in_read(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0x170, new byte[]{(byte) data})));
    }

    /**
     * ????????????????????????
     */
    public void Command_NDLowTemperature(int data) throws IOException {
//        write_in_read(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0x180, new byte[]{(byte) data})));
    }

    /**
     * ??????????????????
     */
    public void Command_LowTemperature(int data) throws IOException {
//        write_in_read(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0x190, new byte[]{(byte) data})));
    }

    /**
     * ????????????????????????SOC
     */
    public void Command_GetInSoc() {
//        write_in_read(getPackages(SP_WRITE_DEV_COMMAND,
//                command_package(0x1A0, new byte[]{(byte) 1})));
    }

    //??????????????????

    /**
     * ?????????????????????BMS
     *
     * @param door_number ?????????
     * @param asynch      ??????????????????
     */
    public void GetBms(byte door_number, boolean asynch) throws IOException {
//        byte[] bd = getPackages(SP_WRITE_BMS, new byte[]{door_number});
        if (asynch) {
//            write_in_read(bd);
        } else {
//            write_data(bd);
        }
    }

    /**
     * ???????????????????????????????????????
     */
    public void Reply_Dns() throws IOException {
//        write_data(getPackages(SP_WRITE_RECE_DNS, new byte[]{1}));
    }

    //????????????????????????
    private byte[] command_package(int itemid, byte[] value) {
        byte[] data = new byte[value.length + 3];
        int off = 0;
        data[off++] = (byte) itemid;
        data[off++] = (byte) (itemid >> 8);//????????????
        data[off++] = (byte) value.length;

        for (byte b : value)
            data[off++] = b;

        return data;
    }

    //?????????????????????
    private byte[] getPackages(int CMD, byte[] arg) {
        int off = 0;
        int len = (arg != null ? arg.length : 0) + 9;
        byte[] bd = new byte[len];
        bd[off++] = STX;
        bd[off++] = (byte) len;
        bd[off++] = (byte) (len >> 8);
        bd[off++] = (byte) CMD;
        bd[off++] = (byte) (arg != null ? arg.length : 0);
        bd[off++] = (byte) (arg != null ? arg.length >> 8 : 0);
        off++;//?????????
        if (arg != null)
            for (byte b : arg)
                bd[off++] = b;

        bd[off++] = GetBbc(bd, off);
        bd[off++] = ETX;
        Log.e("local_main", "##################################????????????##################################");
        myLog.Write_Log(MyLog.LOG_INFO, "CMD:" + CMD + "\t" + CommonClass.bytesToHexString(bd, 0, bd.length));
        Log.e("local_main", "##################################????????????##################################");
        return bd;
    }

    byte[] bdata = new byte[255];

    //    Thread dev_lost_thread;
    byte cmd;
    volatile boolean isWrite = true;
    Vector<byte[]> wiret_array = new Vector();
    //????????????????????????????????????????????????????????????????????????
    //????????????5???????????????????????????????????????
    //3????????????????????????????????????


    //????????????????????????????????????????????????
    private void packet_parsing(byte[] data, int len) {
        send_event_byte(data, len);
        if ((data[MID] & 0xFF) == ((cmd + 0x80) & 0xFF)) {
            isWrite = true;
        }
        Log.e("messageID", "" + CommonClass.bytesToHexString(new byte[]{(byte) (data[MID] & 0xff)}, 0, 1));
        switch (data[MID]) {
            case (byte) SP_READ_MAIN_INFO:
                Log.e("main_parse_data", CommonClass.bytesToHexString(data, 0, data.length));
                myLog.Write_Log(MyLog.LOG_INFO, "?????????????????????" + CommonClass.bytesToHexString(data, 0, data.length));
                main_info_parsing(data, SD);
                myLog.Write_Log(MyLog.LOG_INFO, "???????????????" + LocalData.getLocalData().getMainData().toString());
                break;
            case (byte) SP_READ_SUB_INFO:
                Log.e("sub_parse_data", CommonClass.bytesToHexString(data, 0, data.length));
                myLog.Write_Log(MyLog.LOG_INFO, "?????????????????????" + CommonClass.bytesToHexString(data, 0, data.length));
                sub_info_parsing(data);
                myLog.Write_Log(MyLog.LOG_INFO, "???????????????" + LocalData.getLocalData().getSubDataList().toString());
                break;
            case (byte) SP_READ_CONF_INFO:
                Log.e("conf_parse_data", CommonClass.bytesToHexString(data, 0, data.length));
                myLog.Write_Log(MyLog.LOG_INFO, "?????????????????????" + CommonClass.bytesToHexString(data, 0, data.length));
                conf_info_parsing(data);
                myLog.Write_Log(MyLog.LOG_INFO, "?????????????????????"+Arrays.toString(LocalData.getConfigSuccess()));
                try{
                    sendThresholdsResponse();
                }catch(Exception e){
                    myLog.Write_Log(MyLog.LOG_INFO, "???????????????????????????"+e.toString());
                }
                break;
            case (byte) SP_READ_BMS_INFO:
                Log.e("bms_parse_data", CommonClass.bytesToHexString(data, 0, data.length));
                myLog.Write_Log(MyLog.LOG_INFO, "BMS?????????" + CommonClass.bytesToHexString(data, 0, data.length));
                bms_parsing(data);
                myLog.Write_Log(MyLog.LOG_INFO, "BMS???" + LocalData.getLocalData().getBmsList().toString());
                break;
            case (byte) SP_READ_OPEN_BOX:
                Log.e("open_parse_data", CommonClass.bytesToHexString(data, 0, data.length));
                myLog.Write_Log(MyLog.LOG_INFO, "???????????????" + CommonClass.bytesToHexString(data, 0, data.length));
                open_box_parsing(data);
                break;
            case (byte) SP_READ_OPEN_BOX_RESULT:
                Log.e("openr_parse_data", CommonClass.bytesToHexString(data, 0, data.length));
                myLog.Write_Log(MyLog.LOG_INFO, "???????????????" + CommonClass.bytesToHexString(data, 0, data.length));
                open_box_result_parsing(data);
                break;
            case (byte)SP_READ_CHARGING:
                Log.e("charge_parse_data",CommonClass.bytesToHexString(data,0,data.length));
                myLog.Write_Log(MyLog.LOG_INFO, "??????????????????" + CommonClass.bytesToHexString(data, 0, data.length));
                charge_parsing(data);
                break;
            case (byte) SP_READ_CONFIG:
                Log.e("SP_READ_CONFIG", CommonClass.bytesToHexString(data, 0, data.length));
                sp_read_config(data);
                break;
            case (byte) SP_READ_UPDATE_PRO:
                Log.e("SP_READ_UPDATE_PRO", CommonClass.bytesToHexString(data, 0, data.length));
                sp_read_update_pro(data);
        }
    }

    /**
     * ??????????????????
     *
     * @param data
     */
    private void open_box_result_parsing(byte[] data) {
        Log.e("open_box_result_parsing", CommonClass.bytesToHexString(data, 0, data.length));
        LocalData.OpenBoxResult obp = LocalData.getLocalData().getOpenBoxResult().setData(data, SD);
        myLog.Write_Log(MyLog.LOG_INFO, "?????????????????????" + obp.toString());
        Log.e("open_box_result_parsing", obp.toString());
        LocalData.getLocalData().setOpenBoxResult(obp);
        Api.BoxOpenComm boxComm = Api.getBoxOpenComm();
        String orderNum = boxComm.getOrderNumber();
        //???????????????????????????????????????????????????
        int slotId = obp.getBoxNum();
        //????????????2???????????????????????????
        int boxState = obp.getBoxState();
        //????????????
        int boxResult = obp.getBoxResult();
        //??????ID
        String batSn = obp.getBatSn();
        int soc = LocalData.getBatSocInPreAndAfter().getPer();
        try {
            if (StringUtils.isBlank(orderNum)) {
                sendNotifications(slotId, "", "", batSn, 0);
            }
            if (boxState == 2) {
                if (boxResult == 0) {
                    //????????????
                    sendNotifications(slotId, orderNum, "open_slot_success", batSn, soc);
                } else if (boxResult == 1) {
                    //?????????????????????
                    sendNotifications(slotId, orderNum, "open_slot_sub_no_execution", batSn, soc);
                } else if (boxResult == 2) {
                    //????????????
                    sendNotifications(slotId, orderNum, "door_err", batSn, soc);
                } else if (boxResult == 3) {
                    //??????????????????????????????
                    sendNotifications(slotId, orderNum, "sub_execution_timeout", batSn, soc);
                } else if(boxResult == 4){
                    //?????????????????????
                    sendNotifications(slotId, orderNum, "ota_sub_upgrading", batSn, soc);
                }else if(boxResult == 5){
                    //???????????????
                    sendNotifications(slotId,orderNum,"bat_lock_fault",batSn,soc);
                } else{
                    //??????????????????
                    Log.e("open_box_", "no exe result!");
                }
            }
        } catch (Exception e) {

        }
        //????????????
        if (obp.getBoxNum() != 0) {
            Command_OpenBoxResult(slotId);
        }
    }

    private void sp_read_config(byte[] data) {
        int off = SL;
        int len = (data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00);
        off++;//?????????
        int item = (data[off++] & 0xFF) + ((data[off++] & 0xFF) << 8);
        int item_len = data[off++];//???????????????
        boolean issucceed = false;
        switch (item) {
            case 0x0001:
                break;
            case 0x0002:
                break;
            case 0x0003:
                break;
            case 0x0004:
                break;
            case 0x0005:
                break;
            case 0x0006:
                break;
            case 0x1000:
                update_parsing(data, off);
                break;
            case 0x1001:
                LocalData.Version version = LocalData.getVersion();
                int type = data[off++];//1,mian 2,sub
                byte op = data[off++];
                issucceed = op == 1 ? true : false;

                String string = "";
                switch (type) {
                    case 1:
                        string = "?????????";
                        break;
                    case 2:
                        string = "?????????";
                        break;
                }
                if (issucceed) {
                    if (item_len > 0x0E) {
                        int vl = item_len - 2;
                        String fn = new String(data, off, vl);
                        switch (type) {
                            case 1://CDG_MD_B_B1_200_332.bin
                                String[] strings = fn.split("\\.")[0].split("_");
                                version.setMainVer(fn);
//                                hostUpdateData.setMain(new UpdateData.Main(fn, 0, 0, 0));
//                                mkDeviceState.setHw_ver(strings[4]);
//                                mkDeviceState.setSw_ver(strings[5]);
//                                mkDeviceState.setMainfilename(fn);
                                break;
                            case 2://CDG_MD_A_B1_200_522.bin
                                strings = fn.split("\\.")[0].split("_");
                                version.setSubVer(fn);
//                                hostUpdateData.setSub(new UpdateData.Sub(fn, 0, 0, 0));
//                                mkDeviceState.setSub_hw_ver(strings[4]);
//                                mkDeviceState.setSub_sw_ber(strings[5]);
//                                mkDeviceState.setSubfilename(fn);
                                break;
                        }
                        LocalData.setVersion(version);
                        myLog.Write_Log(MyLog.LOG_INFO, string + " ????????????" + fn);
                        Log.e("version",string + " ????????????" + fn);
                    } else
                        myLog.Write_Log(MyLog.LOG_INFO, string + " ??????????????????????????????");
                } else {
                    myLog.Write_Log(MyLog.LOG_INFO, string + " ????????????????????????");
                }
                break;
        }
    }

    /**
     * ??????????????????
     *
     * @param data
     */
    private void open_box_parsing(byte[] data) {
        Log.e("open_box_parsing", CommonClass.bytesToHexString(data, 0, data.length));
//        LocalData.OpenBoxComnd obc = LocalData.getLocalData().getOpenBoxCommond().setData(data,SD);
        int soc = LocalData.getBatSocInPreAndAfter().getPer();
        LocalData.OpenBoxComnd obc = new LocalData().new OpenBoxComnd();
        //[104, 12, 0, -123, 3, 0, 0, 0, 2, 0, -32, 22]
        int off = SD;
        int bn, br, snlen;
        String batSn = "";
        bn = data[off++] & 0xff;//??????
        br = data[off++] & 0xff;//????????????
        Api.BoxOpenComm boxComm = Api.getBoxOpenComm();
        int slotId = boxComm.getSlotId();
        boolean batIsOnline = (data[off++] & 0xff) == 1; //??????????????????
        if (batIsOnline) {
            snlen = data[off++] & 0xff;
            //???????????????????????????sn?????????
            byte[] sb = new byte[snlen];
            System.arraycopy(data, off, sb, 0, snlen);
            batSn = new String(sb);
        }
        try {
            //??????
            LocalData.isExchanging=true;
            //??????????????????????????????app????????????????????????????????????
            if(LocalData.getUpdating().isUpdating()){
                sendNotifications(slotId, boxComm.getOrderNumber(), "ota_upgrading", batSn, soc);
                return;
            }
            //??????????????? ????????????||????????????
            if (br == 4||br == 8) {
                //TODO ???????????????
                sendNotifications(slotId, boxComm.getOrderNumber(), "ota_upgrading", batSn, soc);
                return;
            }
            //????????????????????????
            if (br == 2) {
                //???????????????,????????????????????????
                sendNotifications(slotId, boxComm.getOrderNumber(), "open_slot_busy", batSn, soc);
                return;
            } else if (br == 1) {
                //??????????????????
                sendNotifications(slotId, boxComm.getOrderNumber(), "open_slot", batSn, soc);
            } else {
                //??????????????????????????????????????????????????????????????????????????????
                Log.e("open_box_", "no box result???");
                sendNotifications(slotId, boxComm.getOrderNumber(), "open_slot_failed", batSn, soc);
                return;
            }
            Log.e("open_box_success", boxComm.toString());
        } catch (Exception e) {

        }
    }

    /**
     * ??????????????????
     */
    public void sendNotifications(int slotId, String orderNum, String event, String batId, int soc) throws JSONException {
        myLog.Write_Log(MyLog.LOG_INFO, "??????????????????" + String.valueOf(slotId) + "???:\t????????????" + orderNum + "\t??????:" + event + "\t??????ID???" + batId + "\tSOC:" + soc + "???");
        JSONObject jsonobject = new JSONObject();
        jsonobject.put("slot_id", slotId);
        jsonobject.put("order_number", orderNum);
        jsonobject.put("event", event);
        jsonobject.put("battery_id", batId);
        jsonobject.put("soc", soc);
        jsonobject.put("timestamp", dfst.format(new Date()));
        MqttManager.getInstance().sendMsg(Api.Topic_W_Notification, jsonobject.toString());
    }

    /**
     * ??????????????????
     */
    public void sendThresholdsResponse() throws JSONException{
        boolean[] configsuccess = LocalData.getConfigSuccess();
        JSONObject jsonobject = new JSONObject();
        jsonobject.put("charge_over_temp_period",!configsuccess[0]);
        jsonobject.put("bat_over_temp_period",!configsuccess[1]);
        jsonobject.put("soc_period",!configsuccess[2]);
        jsonobject.put("charge_time_period",!configsuccess[3]);
        jsonobject.put("info_up_period",true);
        jsonobject.put("log_up_period",true);
        jsonobject.put("ftp_url",true);
        jsonobject.put("ftp_user",true);
        jsonobject.put("ftp_password",true);
        jsonobject.put("ftp_port",true);
        jsonobject.put("ftp_remote_dir",true);
        jsonobject.put("enable_gzip",true);
        myLog.Write_Log(MyLog.LOG_INFO, "??????????????????" + jsonobject.toString() + "???");
        MqttManager.getInstance().sendMsg(Api.Topic_W_ThresholdsResponse,jsonobject.toString());
    }

    /**
     * ??????????????????
     *
     * @param data
     */
    private void conf_info_parsing(byte[] data) {
        int off=SD;
        byte[] temp=new byte[2];
        temp[0]=data[off++];
        temp[1]=data[off++];
        LocalData.getLocalData().setConfigSuccess(temp);
    }

    /**
     * ????????????????????????
     *
     * @param data
     */
    private void sub_info_parsing(byte[] data) {
        int off = SD;
        int tag = (data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00);
        int offset = data[off++] & 0xff;
        int subnum = data[off++] & 0xff;
        //????????????????????????
        LocalData.getLocalData().setSubOffset(offset);
        //???????????????????????????
        LocalData.getLocalData().setSubNum(subnum);
        //????????????????????????
//        LocalData.getLocalData().setSubListData(data,offset,off, subnum);
        CopyOnWriteArrayList<LocalData.SubData> list = LocalData.getLocalData().getSubDataList();
        int copyOffset = off;
        //??????????????????????????????
        int len = 8;
        //????????????????????????????????????????????????????????????
        if (subnum < list.size()) {
            LocalData.SubData last = list.get(offset);
            byte[] dd = new byte[len];
            System.arraycopy(data, copyOffset, dd, 0, len);
            LocalData.SubData subData = new LocalData().new SubData().setData(dd);
            subData.setBoxId(offset + 1);
            list.remove(last);
            list.add(offset, subData);
            sendChange(last, subData);
        } else {
            List<LocalData.SubData> lastSubList = LocalData.getLocalData().getSubDataList();
            for (int i = 0; i < subnum; i++) {
                byte[] dd = new byte[len];
                System.arraycopy(data, copyOffset, dd, 0, len);
                LocalData.SubData subData = new LocalData().new SubData().setData(dd);
                LocalData.SubData last = lastSubList.get(i);
                subData.setBoxId(offset + i + 1);
                list.remove(i);
                list.add(i, subData);
                sendChange(last, subData);
                copyOffset += len;
            }
        }
        LocalData.setSubDataList(list);
    }


    /**
     * ??????????????????????????????bms??????
     *
     * @param boxId       ????????????
     * @param isBatOnline ??????????????????
     */
    public static void removeBms(int boxId) {
        List<LocalData.Bms> bmsList = LocalData.getLocalData().getBmsList();
        for (int i = 0; i < bmsList.size(); i++) {
            LocalData.Bms bms = bmsList.get(i);
            if (bms.getBoxNum() == boxId) {
                bmsList.remove(i);
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param last    ????????????????????????
     * @param subData ?????????????????????
     */
    private void sendChange(LocalData.SubData last, LocalData.SubData subData) {
        int lBoxStatus = last.getBoxStatus();
        int cBoxStatus = subData.getBoxStatus();

        int lBoxRunStatus = last.getBoxRunStatus();
        int cBoxRunStatus = subData.getBoxRunStatus();
        boolean[] lruns = last.getRuningState();
        boolean[] cruns = subData.getRuningState();
        int boxId = subData.getBoxId();
        boolean clear = false;
        //???????????????????????????????????????????????????
        if (!cruns[0]) {
            clear = true;
            removeBms(boxId);
        }
        //????????????
        Api.BoxOpenComm boxOpenComm = Api.getBoxOpenComm();
        //????????????
        LocalData.OpenBoxResult obr = LocalData.getOpenBoxResult();
        try {
            //??????
            if (lBoxStatus == 1 && cBoxStatus == 0) {
                String batId = "";
                int soc = 0;
                if (boxOpenComm != null) {
                    int slotInfo = boxOpenComm.getSlotInfo();
                    LocalData.Bms bms = null;
                    try {
                        if (clear) {
                            Thread.sleep(2000l);
                        }
                        bms = LocalData.getBatData(String.valueOf(boxId));
                    } catch (Exception e) {

                    }
                    if (bms != null) {
                        batId = bms.getBmsData().getId();
                        soc = bms.getBmsData().getSoc();
                        LocalData.getBatSocInPreAndAfter().setAfter(soc);
                    }
                    //??????
                    LocalData.isExchanging=false;
//                    sendNotifications(boxId, boxOpenComm.getOrderNumber(), "close_slot", batId, soc);
                }
            }
            //
        } catch (Exception e) {

        } finally {
            clear = false;
        }
    }

    /**
     * ????????????????????????
     *
     * @param data
     */
    private void main_info_parsing(byte[] data, int off) {
        LocalData.getLocalData().getMainData().setData(data, off);
    }

    //??????????????????????????????
    StringBuilder stringBuilder = new StringBuilder();

    //BMS????????????
    private void bms_parsing(byte[] data) {
        int off = SD;
        int tag = (data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00);
        int boxId = data[off++] & 0xff;
        List<LocalData.Bms> bmss = LocalData.getLocalData().getBmsList();
        LocalData.Bms b = new LocalData().new Bms();
        byte[] copyData = new byte[data.length - 2 - off];
        System.arraycopy(data, off, copyData, 0, data.length - 2 - off);
        LocalData.BmsData bd = new LocalData().new BmsData();
        b.setTag(tag);
        b.setBoxNum(boxId + 1);
        b.setBmsData(bd.setData(copyData));
        LocalData.getLocalData().addBms(b);
    }

    /**
     * ?????????????????????BMS
     *
     * @param door_number ?????????
     * @param asynch      ??????????????????
     */
    public void GetBms(byte door_number, boolean asynch, GetBms_interface bms_interface) throws IOException {
        if (bms_interface != null)
            this.bms_interface = bms_interface;
//        byte[] bd = getPackages(SP_WRITE_BMS, new byte[]{door_number});
        if (asynch) {
//            write_in_read(bd);
        } else {
//            write_data(bd);
        }
    }

    long ShortCircuit_start = 0;
    public static boolean powerswitch = false;
    private boolean reboot = false;

    public boolean isReboot() {
        return reboot;
    }

    private subreste subreste;

    public void setSubrest(subreste subrest) {
        this.subreste = subrest;
    }

    public interface subreste {
        void reset(boolean isr);
    }

    //?????????????????????????????????
    //TODO log test
    MyLog myLog = MyLog.getInstance();
    //BMS????????????
    byte[] bms = new byte[0xFF];

    int bms_read_cout = 0;
    //BMS??????????????????
    //???????????????

    //???????????????????????????
    private void temp_parsing(byte[] data, int off, EventBusUtil.EventData eventData) {
        switch (data[off++]) {
            case 1:
                float temp = ((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00)) * 0.1f;
                eventData.setCode(EventBusUtil.EventData.EVENT_INFO);
                eventData.setStr_message("????????????????????????:" + temp);
                break;
            case 2:
                temp = ((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00)) * 0.1f;
                float difference = ((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00)) * 0.1f;
                eventData.setCode(EventBusUtil.EventData.EVENT_INFO);
                eventData.setStr_message("????????????????????????:" + temp + " ?????????????????????:" + difference);
                break;
            case 3:
                boolean issucceed = data[off++] == 1;
                temp = (data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00);
                difference = (data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00);
                if (issucceed) {
                    eventData.setCode(EventBusUtil.EventData.EVENT_OK);
                } else {
                    eventData.setCode(EventBusUtil.EventData.EVENT_ERROR);
                }
                eventData.setStr_message("????????????????????????:" + temp + " ?????????????????????:" + difference);
                break;
        }
    }

    int update_type;
    //?????????????????????????????????
    DataInputStream inputStream;
    //??????????????????????????????

    //    MyDialog myDialog;
    volatile boolean isUpdate = false;
    public byte[] bupdate = new byte[41];//?????????????????????????????????????????????????????????

    //??????????????????????????????????????????
    //???????????????????????????
    private int update_read_binfile(DataInputStream inputStream, int slen) throws IOException {
        int i = 0;
        int ilen = 0;
        int irfs = 5;
        int crc;
        int isize = 0;
        int idatalen = 1024;
        byte[] bd = new byte[idatalen + 5];
        bd[i++] = (byte) ADDR;
        bd[i++] = (byte) FrameID;
        bd[i++] = (byte) (FrameID >> 8);
        i++;
        i++;
        Log.e("flag_index_sed", FrameID + "");
        FrameID++;
        if (slen >= 1024) {
            while (ilen != idatalen) {
                //?????????????????????????????????
                int rl = inputStream.read(bd, irfs, idatalen - ilen);
                irfs += rl;
                ilen += rl;
            }
            isize = idatalen;
            bd[3] = (byte) isize;
            bd[4] = (byte) (isize >> 8);
            write_in_read(getPackages(SP_WRITE_UPDATE_PRO, bd));
        } else {
            while (ilen != slen) {
                int rl = inputStream.read(bd, irfs, slen - ilen);
                irfs += rl;
                ilen += rl;
            }
            isize = slen;
            byte[] act = new byte[isize + 5];
            System.arraycopy(bd, 0, act, 0, isize + 5);
            act[3] = (byte) isize;
            act[4] = (byte) (isize >> 8);
            write_in_read(getPackages(SP_WRITE_UPDATE_PRO, act));
        }
        return isize;
    }

    EventBusUtil.EventData eventData_r = new EventBusUtil.EventData();

    //????????????????????????
    private void send_event_byte(byte[] bdata, int bdlen) {
        eventData_r.setByte_message(bdata, bdlen);
        EventBusUtil.sendEvent(new Event(C.EventCode.rece_sp, eventData_r));
    }

    //????????????????????????
    private void send_event_str(EventBusUtil.EventData eventData) {
        EventBusUtil.sendEvent(new Event(C.EventCode.rece_system, eventData));
    }

    //??????????????????
    private void send_event_command(float data, boolean issucceed, String mode, EventBusUtil.EventData eventData) {
        eventData.setCode(issucceed ?
                EventBusUtil.EventData.EVENT_OK :
                EventBusUtil.EventData.EVENT_ERROR);
        eventData.setStr_message(issucceed ? mode + data + "????????????" : mode + data + "????????????");
    }

    //?????????????????????????????????????????????
    private byte[] byte_copy(int len, byte[] data, int off) {
        byte[] bd = new byte[len];
        for (int i = 0; i < len; i++)
            bd[i] = data[SD + off++];
        return bd;
    }

    private boolean data_check(byte[] data, int len) {
        return data[len - 2] == GetBbc(data, len - 2);
    }

    public byte GetBbc(byte[] datas, int len) {
        byte temp = datas[0];
        for (int i = 1; i < len; i++) {
            temp ^= datas[i];
        }
        return temp;
    }


    //?????????crc??????
    static int[] auchCRCHi = {
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81,
            0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
            0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01,
            0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81,
            0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
            0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01,
            0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81,
            0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
            0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01,
            0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81,
            0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
            0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01,
            0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81,
            0x40
    };
    static int[] auchCRCLo = {
            0x00, 0xC0, 0xC1, 0x01, 0xC3, 0x03, 0x02, 0xC2, 0xC6, 0x06, 0x07, 0xC7, 0x05, 0xC5, 0xC4,
            0x04, 0xCC, 0x0C, 0x0D, 0xCD, 0x0F, 0xCF, 0xCE, 0x0E, 0x0A, 0xCA, 0xCB, 0x0B, 0xC9, 0x09,
            0x08, 0xC8, 0xD8, 0x18, 0x19, 0xD9, 0x1B, 0xDB, 0xDA, 0x1A, 0x1E, 0xDE, 0xDF, 0x1F, 0xDD,
            0x1D, 0x1C, 0xDC, 0x14, 0xD4, 0xD5, 0x15, 0xD7, 0x17, 0x16, 0xD6, 0xD2, 0x12, 0x13, 0xD3,
            0x11, 0xD1, 0xD0, 0x10, 0xF0, 0x30, 0x31, 0xF1, 0x33, 0xF3, 0xF2, 0x32, 0x36, 0xF6, 0xF7,
            0x37, 0xF5, 0x35, 0x34, 0xF4, 0x3C, 0xFC, 0xFD, 0x3D, 0xFF, 0x3F, 0x3E, 0xFE, 0xFA, 0x3A,
            0x3B, 0xFB, 0x39, 0xF9, 0xF8, 0x38, 0x28, 0xE8, 0xE9, 0x29, 0xEB, 0x2B, 0x2A, 0xEA, 0xEE,
            0x2E, 0x2F, 0xEF, 0x2D, 0xED, 0xEC, 0x2C, 0xE4, 0x24, 0x25, 0xE5, 0x27, 0xE7, 0xE6, 0x26,
            0x22, 0xE2, 0xE3, 0x23, 0xE1, 0x21, 0x20, 0xE0, 0xA0, 0x60, 0x61, 0xA1, 0x63, 0xA3, 0xA2,
            0x62, 0x66, 0xA6, 0xA7, 0x67, 0xA5, 0x65, 0x64, 0xA4, 0x6C, 0xAC, 0xAD, 0x6D, 0xAF, 0x6F,
            0x6E, 0xAE, 0xAA, 0x6A, 0x6B, 0xAB, 0x69, 0xA9, 0xA8, 0x68, 0x78, 0xB8, 0xB9, 0x79, 0xBB,
            0x7B, 0x7A, 0xBA, 0xBE, 0x7E, 0x7F, 0xBF, 0x7D, 0xBD, 0xBC, 0x7C, 0xB4, 0x74, 0x75, 0xB5,
            0x77, 0xB7, 0xB6, 0x76, 0x72, 0xB2, 0xB3, 0x73, 0xB1, 0x71, 0x70, 0xB0, 0x50, 0x90, 0x91,
            0x51, 0x93, 0x53, 0x52, 0x92, 0x96, 0x56, 0x57, 0x97, 0x55, 0x95, 0x94, 0x54, 0x9C, 0x5C,
            0x5D, 0x9D, 0x5F, 0x9F, 0x9E, 0x5E, 0x5A, 0x9A, 0x9B, 0x5B, 0x99, 0x59, 0x58, 0x98, 0x88,
            0x48, 0x49, 0x89, 0x4B, 0x8B, 0x8A, 0x4A, 0x4E, 0x8E, 0x8F, 0x4F, 0x8D, 0x4D, 0x4C, 0x8C,
            0x44, 0x84, 0x85, 0x45, 0x87, 0x47, 0x46, 0x86, 0x82, 0x42, 0x43, 0x83, 0x41, 0x81, 0x80,
            0x40
    };

    public static int Crc16(byte[] bd, int len) {
        byte uchCRCHi = (byte) 0xFF;
        byte uchCRCLo = (byte) 0xFF;
        int uIndex;
        for (int i = 0; i < len; i++) {
            uIndex = (uchCRCHi ^ bd[i]) & 0xFF;
            uchCRCHi = (byte) (uchCRCLo ^ (auchCRCHi[uIndex] & 0xFF));
            uchCRCLo = (byte) (auchCRCLo[uIndex] & 0xFF);
        }

        return ((uchCRCHi << 8) & 0xFF00 | uchCRCLo & 0xFF);
    }


    //??????????????????????????????????????????
    private void update_start(int type, String filename) throws Exception {
        myLog.Write_Log(MyLog.LOG_INFO, "????????????...");
        isUpdate = false;
        FrameID = 0;
        String file = "";
        switch (type) {
            case 1://?????????
                file = Config.mainFpath + "/" + filename;
                ADDR = 0;
                break;
            case 2://?????????
                file = Config.subFpath + "/" + filename;
                ADDR = 0xcc;
                break;
        }
        int i = 0;
        bupdate = new byte[41];
        bupdate[i++] = (byte) ADDR;
        bupdate[i++] = (byte) FrameID;
        bupdate[i++] = (byte) (FrameID >> 8);
        //2???????????????????????????,??????0??????????????????
        i += 2;
        // ???????????????

        //??????????????????
        bupdate[i++] = 2;
        //???????????????
        inputStream = new DataInputStream(new FileInputStream(file));
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        bupdate[i++] = (byte) bytes.length;
        bupdate[i++] = (byte) (bytes.length >> 8);
        bupdate[i++] = (byte) (bytes.length >> 16);
        bupdate[i++] = (byte) (bytes.length >> 24);
        //crc16
        long crc = Crc16(bytes, bytes.length);
        int lc = CrCUtils.calcCrc16(bytes);
        bupdate[i++] = (byte) (crc);
        bupdate[i++] = (byte) (crc >> 8);
        int k = i;
        //????????????????????????
        byte[] bytes1 = filename.getBytes("US-ASCII");
        for (int j = 0; j < bytes1.length; j++, k++)
            bupdate[i++] = bytes1[j];
        bupdate[3] = (byte) (i - 5);
        bupdate[4] = (byte) ((i - 5) >> 8);
        myLog.Write_Log(MyLog.LOG_INFO, "???????????????" + (FrameID + 1) + "??????\n???" + CommonClass.bytesToHexString(bupdate, 0, bupdate.length) + "???\n");
        FrameID++;
        //???????????????????????????????????????????????????
        inputStream = new DataInputStream(new FileInputStream(file));
        write_in_read(getPackages(SP_WRITE_UPDATE_PRO, bupdate));
    }

    private void end_datatran(int type) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        switch (update_type) {
            case 1:
                if (update_main != null)
                    update_main.dataTransmission(type);
                break;
            case 2:
                if (update_sub != null)
                    update_sub.dataTransmission(type);
                break;
        }

//        synchronized (MainActivity.Update_Thread) {//???????????????Update_Thread.wait();???????????????????????????
//            MainActivity.Update_Thread.notify();
//        }
    }

    //????????????????????????????????????????????????????????????????????????
    //????????????5???????????????????????????????????????
    //3????????????????????????????????????
    private void write_data(final byte[] data) throws IOException {
        if (mFileOutputStream != null) {
//            if (SeriaPort.isDataTran) {
//                return;
//            }
            //??????ID
            cmd = data[MID];
            isWrite = cmd == 1 || cmd == 2 || cmd == 7;
            if (cmd == 3 && data[SD] == 1)
                isWrite = true;
            mFileOutputStream.write(data);
            myLog.Write_Log(MyLog.LOG_INFO, "data:" + Arrays.toString(data) + "length:" + data.length);
        }
    }

    Thread wr;//?????????????????????????????????????????????

//    private void write_in_read(byte[] bd) {
//        if (SeriaPort.isDataTran) {
//            return;
//        }
//        wiret_array.add(bd);
//        synchronized (wr) {
//            wr.notify();
//        }
//    }


    // ???????????????????????????????????????????????????????????????????????????
    // ????????? SP_READ_WAIT_MS ???????????????????????????????????????
    // ???????????? FI_VALUE_R_COUT ??????
    // ???????????? SP_READ_WAIT_RS_MS ?????????
    private synchronized int write_in_read(final byte[] data) {
        if (mFileOutputStream == null) return SP_WRITE_READ_ERROE;

        try {
            cmd = data[MID];
            mFileOutputStream.write(data);
            isWrite = false;

//            boolean isUpdate = cmd == SP_WRITE_CONFIG && data[SD] == 0x0 && data[SD + 1] == 0x10;

//            long start = System.currentTimeMillis();
//            while (!isWrite) {
//                if (System.currentTimeMillis() - start > SP_READ_WAIT_MS + (isUpdate ? SP_READ_WAIT_MS : 0))
//                    break;
//                Thread.sleep(SP_READ_WAIT_R_MS);
//            }
//
//            while (!isWrite) {
//                if (cout >= FI_VALUE_R_COUT) {
//                    cout = FI_VALUE_ZERO;
//                    return SP_WRITE_READ_ERROE;
//                } else
//                    cout++;
//
//                Thread.sleep(SP_READ_WAIT_RS_MS);
//                if (isWrite) break;
//
//                if (isUpdate) {
//                    EventBusUtil.sendEvent(new Event(C.EventCode.rece_system, "E>>>>> : ??????????????????????????????????????????????????????"));
//                }
//                Log.i("TESTTT", "RS " + CommonClass.bytesToHexString(data, 0, data.length));
////                return write_in_read(data);
//            }

//            if (isUpdate) //??????????????????
//            {
//                Log.e("update_command", CommonClass.bytesToHexString(data, 0, data.length));
//            }
            cout = FI_VALUE_ZERO;
            return SP_WRITE_READ_OK;
        } catch (Exception e) {
            e.printStackTrace();

            cout = FI_VALUE_ZERO;
            return SP_WRITE_READ_ERROE;
        }
    }


    /**
     * ????????????
     *
     * @param type        1:????????? 2:?????????
     * @param hw_version  ??? type      ????????????
     * @param bid         ??? type       ?????????ID?????????0
     * @param iscompel    true:???????????? false:????????????
     * @param up_version  ???????????????
     * @param pro_version ????????????
     */
    public int Command_Update(byte type, int hw_version, String bid, boolean iscompel, int up_version, int pro_version, Update_interface update_interface) {
        switch (type) {
            case 1:
                update_main = update_interface;
                break;
            case 2:
                update_sub = update_interface;
                break;
        }
        byte[] data = new byte[12];
        data[0] = type;

        int off = 1;
        data[off++] = (byte) hw_version;
        data[off++] = (byte) (hw_version >> 8);
        byte[] id;
        if (StringUtils.isBlank(bid)) {
            id = new byte[6];
        } else {
            id = bid.getBytes();
        }
        for (byte b : id)
            data[off++] = b;

        data[9] = (byte) (iscompel ? 1 : 0);
        data[10] = (byte) pro_version;
        data[11] = (byte) up_version;

        return write_in_read(getPackages(SP_WRITE_CONFIG,
                command_package(0x1000, data)));
    }

    private void update_parsing(byte[] data, int off) {
        final int up_type = data[off++]; //????????????
        int version = (data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00);//????????????

        int mv = version / 100;
        int sv = version % 100;
        byte[] id = new byte[6];//??????ID
        for (int i = 0; i < id.length; i++)
            id[i] = data[off++];
        int up_mode = data[off++];//???????????? 0:?????? 1?????????
        byte up_return = data[off++]; //??????????????????
        int proVer = data[off++];
        int softVer = data[off++];
        StringBuilder stringBuilder = new StringBuilder();

        update_type = up_type;

        stringBuilder.append("???????????????");
        switch (up_type) {
            case 1://?????????
                stringBuilder.append("?????????");
                break;
            case 2://?????????
                stringBuilder.append("?????????");
                break;
        }
        stringBuilder.append("\n")
                .append("???????????????")
                .append("V" + mv + "." + (sv > 10 ? sv : "0" + sv));
        stringBuilder.append("\n")
                .append("ID???")
                .append(id);
        stringBuilder.append("\n")
                .append("???????????????")
                .append(up_mode == 1 ? "????????????" : "????????????");
        stringBuilder.append("\n")
                .append("?????????????????????");
        switch (up_return) {
            case 1:
                stringBuilder.append("????????????");
                //??????????????????
                try {
                    Thread.sleep(2000);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                update_start(up_type, filename);
                            } catch (Exception e) {
                                e.printStackTrace();
                                end_datatran(-1);
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    end_datatran(-1);
                }
                break;
            case 2:
                end_datatran(1);
                stringBuilder.append("????????????(???????????????)");
                break;
            case 4:
                end_datatran(2);
                stringBuilder.append("?????????????????????????????????");
                break;
            case 8:
                end_datatran(2);
                stringBuilder.append("???????????????????????????????????????,?????????");
                break;
            case 0xC:
                end_datatran(2);
                stringBuilder.append("????????????,?????????????????????????????????????????????????????????????????????,?????????");
                break;
            case 0x10:
                end_datatran(2);
                stringBuilder.append("??????????????????,?????????");
                break;
            case 0x20:
                end_datatran(-1);
                stringBuilder.append("????????????????????????");
                break;
        }
        stringBuilder.append("\n")
                .append("??????????????????" + proVer);
        stringBuilder.append("\n")
                .append("??????????????????" + softVer);
        myLog.Write_Log(MyLog.LOG_INFO, stringBuilder.toString());
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param data
     */
    private void sp_read_update_pro(byte[] data) {
        int len = data.length;
        //68 10 00 8e 07 00 00 cc 00 00 07 00 06 02 3e 16
        Log.e("update_pro", CommonClass.bytesToHexString(data, 0, data.length));
        try {
            if (len >= 10) {
                long crc = Crc16(data, len - 2);
                int off = SD;
                int addr = data[off++] & 0xFF;
                int frameid = (data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00);
                int framelen = (data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00);
                int ack = data[off++] & 0xFF;
                int dflag = data[off++] & 0xFF;
                Log.e("flag_index_rec", frameid + "");
                String string = "";
                switch (dflag) {
                    case 1:
                        int slen = inputStream.available();
                        string = "???????????????...";
                        if (slen > 0) {
                            update_read_binfile(inputStream, slen);
                        } else {
                            //??????????????????
                            bupdate[5] = 3;
                            //??????
                            write_in_read(getPackages(SP_WRITE_UPDATE_PRO, bupdate));
                        }
                        break;
                    case 2:
                        slen = inputStream.available();
                        update_read_binfile(inputStream, slen);
                        string = "??????????????????...";
                        break;
                    case 3:
                        string = "??????????????????...";
                        end_datatran(0);
                        switch (update_type) {
                            case 1:
                                if (update_main != null) {
                                    update_main.getData(true, 0);
                                }
                                break;
                            case 2:
                                if (update_sub != null) {
                                    update_sub.getData(true, 0);
                                }
                                break;
                        }
                        break;
                    case 0xf5:
                        string = "??? bin ??????";
                        end_datatran(-1);
                        break;
                    case 0xf6:
                        string = "??? APP ?????????";
                        end_datatran(-1);
                        break;
                    case 0xf7:
                        string = "?????? APP ??????";
                        end_datatran(-1);
                        break;
                    case 0xf8:
                        string = "?????????????????????";
                        end_datatran(-1);
                        break;
                    case 0xf9:
                        string = "??? Frame ID ?????????";
                        end_datatran(3);
                        break;
                    case 0xfa:
                        string = "?????? Frame ID ???";
                        end_datatran(4);
                        break;
                    case 0xfb:
                        string = "????????? Flash ??????";
                        end_datatran(-1);
                        break;
                    case 0xfc:
                        string = "????????????????????????";
                        end_datatran(-1);
                        break;
                    case 0xfe:
                        string = "?????????????????????";
                        end_datatran(-1);
                        break;
                }
                Log.i("TEST", "fid:" + FrameID);
                myLog.Write_Log(MyLog.LOG_INFO, string);
            } else {
                myLog.Write_Log(MyLog.LOG_INFO, "????????????");
            }
        } catch (Exception e) {
            myLog.Write_Log(MyLog.LOG_INFO, "????????????" + e.toString());
        }
    }

    /**
     * ???????????????
     * @param isOpen ????????????
     */
    public void Command_LightChose(boolean isOpen){
        write_in_read(getPackages(SP_WRITE_CONFIG, command_package(0x0006, new byte[]{(byte)(isOpen?1:0)})));
    }

    /**
     * type1:??????
     * type2:??????
     * ???????????????
     */
    public void Command_GetFileName(int type) {
        int off = 0;
        byte[] data = new byte[8];
        int chVer = LocalData.getMainData().getChVer();
        int shVer = LocalData.getMainData().getShVer();
        data[off++] = (byte) type;
        if (type == 1) {
            data[off++] = (byte) chVer;
            data[off++] = (byte) (chVer >> 8);
        }
        if (type == 2) {
            data[off++] = (byte) shVer;
            data[off++] = (byte) (shVer >> 8);
        }
        //id??????
        write_in_read(getPackages(SP_WRITE_CONFIG, command_package(0x1001, data)));
    }

    /**
     *
     * @param mainConfig
     */
    public void writeConfig(MainConfig mainConfig){
        int off=0;
        byte[] data=new byte[9];
        data[off++]=(byte)mainConfig.getProVer();
        data[off++]=(byte)mainConfig.getSoftVer();
        data[off++]=(byte)mainConfig.getChargeOverTempPeriod();
        data[off++]=(byte)mainConfig.getBatOverTempPeriod();
        data[off++]=(byte)mainConfig.getSocPeriod();
        data[off++]=(byte)(mainConfig.getChargeTimePeriod());
        data[off++]=(byte)(mainConfig.getChargeTimePeriod()>> 8);
        data[off++]=(byte)(mainConfig.getChargeTimePeriod()>> 16);
        data[off++]=(byte)(mainConfig.getChargeTimePeriod()>> 24);
        write_in_read(getPackages(SP_WRITE_CONF_INFO, data));
    }

    /**
     * ?????????????????????
     * @param data ???????????????
     */
    private void charge_parsing(byte[] data){
        int off=SD;
        int tag = (data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00);
        int boxId=data[off++]&0xff;
        List<LocalData.ChargeData> charges = LocalData.getLocalData().getChargeList();
        LocalData.ChargeData cd=new LocalData().new ChargeData();
        byte[] copyData=new byte[data.length-2-off];
        System.arraycopy(data,off,copyData,0,data.length-2-off);
        LocalData.ChargeInfo ci=new LocalData().new ChargeInfo();
        cd.setTag(tag);
        cd.setBoxNum(boxId+1);
        cd.setChargeInfo(ci.setData(copyData));
        LocalData.getLocalData().addCharge(cd);
    }

}

//?????????????????????????????????????????????????????????????????????
