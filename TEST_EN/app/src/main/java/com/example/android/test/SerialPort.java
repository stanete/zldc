package com.example.android.test;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SerialPort {

    public static final int R_MainInfo = 0x81;
    public static final int R_SubInfo = 0x82;
    public static final int R_confInfo = 0x83;
    public static final int R_bmsInfo = 0x84;
    public static final int R_openBox = 0x85;
    public static final int R_openBoxResult = 0x86;


    public static final int W_MainInfo = 0x81;
    public static final int W_SubInfo = 0x82;
    public static final int W_confInfo = 0x83;
    public static final int W_bmsInfo = 0x84;
    public static final int W_openBox = 0x85;
    public static final int w_openBoxResult = 0x86;


    public static final int R_LockerInfo = 0x81;
    public static final int R_ExchangeBattery = 0x82;
    public static final int R_RemoteOpenBox = 0x83;
    public static final int R_BatteryInfo = 0x84;
    public static final int R_ExchangeBatteryResult = 0x85;
    public static final int R_BoxOpened = 0x86;
    public static final int R_BoxClosed = 0x87;
    public static final int R_SystemBusy = 0x88;
    public static final int R_DevState = 0x89;

    public static final int W_LockerInfo = 0x1;
    public static final int W_ExchangeBattery = 0x2;
    public static final int W_RemoteOpenBox = 0x3;
    public static final int W_BatteryInfo = 0x4;
    public static final int W_ExchangeBatteryResult = 0x5;
    public static final int W_BoxOpened = 0x6;
    public static final int W_BoxClosed = 0x7;
    public static final int W_SystemBusy = 0x8;
    public static final int W_DevState = 0x9;

    private final boolean[] w_t_r = new boolean[9];

    final int STX = 0x68;
    final int ETX = 0x16;
    final int MID = 3;
    final int SL = 4;
    final int SD = 7;

    private GetSp_Data getSp_data;

    public interface GetSp_Data {
        void getdata(byte[] bd);
    }

    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    private boolean isHavePermission = false;

    Handler handler;
    final int RECV_DATA = 0;
    //分配4个字节的缓存空间
    ByteBuffer byteBuffer = ByteBuffer.allocate(0xFFFF);
    HandlerThread handlerThread = new HandlerThread("handlerThread");

    private final byte[] sp_r_buffer = new byte[255];

    public byte[] getSp_r_buffer() {
        return sp_r_buffer;
    }

    public boolean isHavePermission() {
        return isHavePermission;
    }

    public SerialPort(File device, int baudrate, GetSp_Data getSp_data) throws IOException {
        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            this.isHavePermission = false;
            return;
        }

        isHavePermission = true;

        this.getSp_data = getSp_data;

        open_serial_port(device, baudrate, 0);
    }

    private void open_serial_port(File device, int baudrate, int flags) throws IOException {
        if (!isHavePermission) return;

        mFd = open(device.getAbsolutePath(), baudrate, flags);
        if (mFd == null) {
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);

        //读取串口数据 用于多线程 在485通讯中不适用
        new Thread(new Runnable() {
            @Override
            public void run() {
                handlerThread.start();
                handler = new Handler(handlerThread.getLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case RECV_DATA://在接收区做数据校验，收到完整包时发出去
                                //一包数据可能会接收多次，需要一个接收区域
                                try {
                                    byteBuffer.put((byte[]) msg.obj, 0, msg.arg1);
                                    data_cycle();
                                } catch (Exception e) {
                                    byteBuffer.clear();
                                }
                                break;
                        }
                    }
                };

                if (mFileInputStream == null) return;
                byte[] buffer = new byte[255];
                while (true) {
                    try {
                        int size = mFileInputStream.read(buffer);
                        if (size > 0) {
                            byte[] bd = new byte[size];
                            System.arraycopy(buffer, 0, bd, 0, size);
                            Message message = Message.obtain();
                            message.what = RECV_DATA;
                            message.arg1 = size;
                            message.obj = bd;
                            handler.sendMessage(message);
                            Log.i("test", Others.byte2hex(bd));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 循环从缓冲区读取数据
     */
    private void data_cycle() {
        for (; byteBuffer.position() > 0; ) {
            //开始标记
            if ((byteBuffer.get(0) & 0xFF) == STX) {
                if (byteBuffer.position() >= 3) {
                    //读取消息长度
                    int len = (byteBuffer.get(1) & 0xFF)
                            + ((byteBuffer.get(2) & 0xFF) << 8);
                    if (byteBuffer.position() >= len) {
                        //结束标记
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
                                int message_id = bd[MID] & 0xFF;
                                w_t_r[message_id - 0x81] = true;
                                getSp_data.getdata(bd);
                                Log.i("test", "R :" + new String(bd, StandardCharsets.US_ASCII));
                            } else {
                                //串口校验错误
                            }
                            if (isNextPacket)
                                data_cycle();
                        } else {
                            byteBuffer.flip();
                            byteBuffer.position(3);
                            byteBuffer.compact();
                            //串口ETX错误
                            data_cycle();
                        }
                    } else
                        break;
                } else
                    break;
            } else {
                byteBuffer.flip();
                byteBuffer.get();
                byteBuffer.compact();
            }
        }
    }

    public void LockerInfo(byte[] bd, boolean reply) {
        wd(W_LockerInfo, bd, reply);
    }

    public void ExchangeBattery(byte[] bd, boolean reply) {
        wd(W_ExchangeBattery, bd, reply);
    }

    public void RemoteOpenBox(byte[] bd, boolean reply) {
        wd(W_RemoteOpenBox, bd, reply);
    }

    public void BatteryInfo(byte[] bd, boolean reply) {
        wd(W_BatteryInfo, bd, reply);
    }

    public void ExchangeBatteryResult(byte[] bd, boolean reply) {
        wd(W_ExchangeBatteryResult, bd, reply);
    }

    public void BoxOpened(byte[] bd, boolean reply) {
        wd(W_BoxOpened, bd, reply);
    }

    public void BoxClosed(byte[] bd, boolean reply) {
        wd(W_BoxClosed, bd, reply);
    }

    public void SystemBusy(byte[] bd, boolean reply) {
        wd(W_SystemBusy, bd, reply);
    }

    public void DevState(byte[] bd, boolean reply) {
        wd(W_DevState, bd, reply);
    }

    private void wd(int CMD, byte[] bd, boolean reply) {
        if (mFileOutputStream == null) return;
        try {
            for (int i = 0; i < 3; i++) {
                if (write_data(getPackages(CMD, bd), reply))
                    break;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean write_data(byte[] data, boolean reply) throws IOException, InterruptedException {
        int message_id = data[MID];
        boolean rt = false;

        w_t_r[message_id - 1] = false;
        mFileOutputStream.write(data);
        Log.i("test", "W : " + new String(data, StandardCharsets.US_ASCII));

        if (reply) return true;

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 3000) {
            Thread.sleep(100);
            if (w_t_r[message_id - 1]) {
                rt = true;
                break;
            }
        }
        return rt;
    }


    //用于写串口组包
    private byte[] getPackages(int CMD, byte[] arg) {
        int off = 0;
        int len = (arg != null ? arg.length : 0) + 9;//到校验码，消息体长度为0
        byte[] bd = new byte[len];
        bd[off++] = STX; //开始帧
        bd[off++] = (byte) len;
        bd[off++] = (byte) (len >> 8);//消息长度,占两个
        bd[off++] = (byte) CMD;//消息ID
        bd[off++] = (byte) (arg != null ? arg.length : 0);
        bd[off++] = (byte) (arg != null ? arg.length >> 8 : 0); //消息体长度
        off++;//地址位

        if (arg != null)
            for (byte b : arg)
                bd[off++] = b;
        bd[off++] = GetBbc(bd, off);
        bd[off++] = ETX;//结束帧

        return bd;
    }

    private boolean read_data() throws IOException {
        boolean rd = false;

        int sum = 0;
        int dlen = 0;
        int off = 0;
        int len = 3;
        long start = System.currentTimeMillis();
        while (true) {
            int size = mFileInputStream.read(sp_r_buffer, off, len);
            if (size > 0) {
                off += size;
                sum += size;
                if (sum >= 3) { //数据长度
                    dlen = (sp_r_buffer[1] & 0xFF) + ((sp_r_buffer[2] << 8) & 0xFF00);
                    if (dlen == sum) {
                        if (data_check(sp_r_buffer, dlen)) {
                            rd = true;
//                            byte[] bd = new byte[dlen];
//                            System.arraycopy(sp_r_buffer,0,bd,0,dlen);
                            getSp_data.getdata(sp_r_buffer);
                        }
                        break;
                    }
                    len = dlen - sum;
                } else {
                    len -= size;
                }
            }
            if (System.currentTimeMillis() - start > 5000) {
                rd = false;
                break;
            }
        }
        return rd;
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

    // JNI
    public native String stringFromJNI();

    private native static FileDescriptor open(String path, int baudrate, int flags);

    public native void close();

    public native int crc16(byte[] bd, int len);

    public native static String Base_64(byte[] bd, int len);

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
