package com.example.android.zldc.toservers;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.example.android.zldc.CommonClass;
import com.example.android.zldc.Config;
import com.example.android.zldc.LocalData;
import com.example.android.zldc.MainActivity;
import com.example.android.zldc.MyLog;
import com.example.android.zldc.UpdateData;
import com.example.android.zldc.sers.MyService;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ToUpdateServer {
    private MyLog myLog= MyLog.getInstance();
    public final static int Mode_Config = 0;
    public final static int Mode_Apk    = 1;
    public final static int Mode_Main   = 2;
    public final static int Mode_Sub    = 3;
    public final static int Mode_Bat    = 4;
    public final static int Mode_Charge = 5;

    private static boolean isLogin = false;

    OkHttpClient okHttpClient   = new OkHttpClient();//获取配置和固件
    OkHttpClient okHttpClient_1 = new OkHttpClient();//鉴权登录
    OkHttpClient okHttpClient_2 = new OkHttpClient();//升级数据上报
    Request.Builder builde    = new Request.Builder();
    Request.Builder builde_1  = new Request.Builder();
    Request.Builder builde_2  = new Request.Builder();

    public interface DataCallBack {
        void getData(Object data, int mode);
    }

    public interface upgradeNum{
        void cancel();
    };

    public interface postupCommon{
        void post();
    }

    private Vector<PostData> postlist = new Vector();
    private Thread postThread;
    private Thread refresh;

    public ToUpdateServer() {
        postThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if(postlist.size() > 0) {
                            PostData data = postlist.get(0);
                            if(postupload(data.getUrl(),data.getJson_str(),data.getMode(),data.getUpgradeNum(),data.getPostupCommon())) {
                                postlist.remove(data);
                            } else {
                                Thread.sleep(5000);
                            }
                            myLog.Write_Log(MyLog.LOG_INFO,"OTA post error list size == " + postlist.size());
                        } else {
                            synchronized (postThread) {
                                postThread.wait();
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });
        postThread.start();
        Log.e("ToUpdateServer Constructor",postThread.getState().toString()+"");
    }

    public static String md5(String input) throws NoSuchAlgorithmException {
        byte[] bytes = MessageDigest.getInstance("MD5").digest(input.getBytes());
        return printHexBinary(bytes);
    }
    private static final char[] hexCode = "0123456789abcdef".toCharArray();
    public static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

    MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
    //当OTA指令被清除或结束限制次数时将数据发送到正常接口
    public void PostUploadCommon(String type,
                                 String filename,
                                 String currentfn,
                                 boolean issuccess,
                                 boolean isUp,
                                 int bucket,
                                 String failed) throws JSONException {
//        if(!DataManage.isNetworking) return;
//        if(DataManage.url_current.isEmpty()) return;

        JSONObject object = new JSONObject();
        String method = "/v1/file/upload/commonreport";

        final String url = Config.urlCurrent + method;
        object.put("devid",Config.devId);
        object.put("fileName",filename);
        object.put("types",type);
        object.put("currentFilename",currentfn);
        object.put("isSuccess",issuccess);
        object.put("isUp",isUp);
        object.put("bucket",bucket);
        object.put("failedReason",failed);

        addPostData(new PostData(url,object.toString(),3,null,null));
    }
    private boolean postupload(
            String url,
            String json_str,
            int mode,
            upgradeNum upgradeNum,
            postupCommon postupCommon) {
        if(!LocalData.isNetworking) return true;

        builde_2.url(url);
        builde_2.header("devId", Config.devId);
        builde_2.post(RequestBody.create(mediaType, json_str));

        Call call = okHttpClient_2.newCall(builde_2.build());

        try {
            Response response = call.execute();
            boolean isS = response.isSuccessful();
            if(isS) {
                getResponse(response,mode,upgradeNum,postupCommon);
            } else {
                myLog.Write_Log(MyLog.LOG_INFO,"ota请求错误: " + "ota请求错误: " + response.code());
            }
            return isS;
        } catch (IOException e) {
            e.printStackTrace();
            myLog.Write_Log(MyLog.LOG_INFO,"ota请求错误: " + e.toString());
        }
        return false;
    }
    private void getResponse(Response response,int mode,upgradeNum upgradeNum,postupCommon postupCommon) {
        try {
            int http_code = response.code();
            if (http_code >= 200 && http_code < 300) {
                String body = response.body().string();
                JSONObject jsonObject = new JSONObject(body);
                int   code = jsonObject.getInt("code");
                String mes = jsonObject.getString("msg");

                switch (mode) {
                    case 1://PreOTA
                        switch (code) {
                            case 0://上报成功:
                                break;
                            case 1000://指令已被清除
                                upgradeNum.cancel();
                                if(postupCommon != null)
                                    postupCommon.post();
                                break;
                            case 1001://结束限制次数
                                upgradeNum.cancel();
                                if(postupCommon != null)
                                    postupCommon.post();
                                break;
                            case 1002://上报完成（已达到指定的次数
                                break;
                        }
                        break;
                    case 2://测试模式
                        switch (code) {
                            case 1002://上报完成（已达到指定的次数,当达到次数后不再发送超时
                                String str = response.request().url().toString();
                                if(str.contains("battestreport"))
//                                    ThreadManage.isBatRecvData = false;

                                if(str.contains("chargetestreport"))
//                                    ThreadManage.isChargerRecvData = false;

                                break;
                        }
                        break;
                    case 3://正常模式
                        switch (code) {
                            case 0://上报成功:
                                break;
                            case 1://不存在的程序类型
                                break;
                        }
                        break;
                    case 4://退出测试模式
                        break;
                    case 5://测试模式超时
                        break;
                }
                myLog.Write_Log(MyLog.LOG_INFO,"OTA服务器正确响应： " + response.request().url() + " <---> " + body);
            }
        } catch (Exception e) {
            String str = "OTA数据解析错误" + e.toString();
            myLog.Write_Log(MyLog.LOG_INFO,str);
        }
    }
    private void addPostData(PostData postData) {
        if(!LocalData.isNetworking) return;
        Log.e("addPostData",postData.toString());
        postlist.add(postData);

        if(postlist.size()>=1000)
            postlist.remove(0);

        synchronized (postThread) {
            postThread.notify();
        }
    }
    class PostData {
        private String url;
        private String json_str;
        private int    mode;
        private upgradeNum   upgradeNum;
        private postupCommon postupCommon;

        public PostData(String url,String json_str,int mode,upgradeNum upgradeNum,postupCommon postupCommon) {
            this.url         = url;
            this.json_str    = json_str;
            this.mode        = mode;
            this.upgradeNum  = upgradeNum;
            this.postupCommon= postupCommon;
        }

        public String getUrl() {
            return url;
        }

        public int getMode() {
            return mode;
        }

        public ToUpdateServer.postupCommon getPostupCommon() {
            return postupCommon;
        }

        public String getJson_str() {
            return json_str;
        }

        public ToUpdateServer.upgradeNum getUpgradeNum() {
            return upgradeNum;
        }
    }

    //获取设备ID
    public void GetDeviceID() throws IOException, JSONException {
        if(!LocalData.isNetworking) return;
        String mac=Config.mac;
        if(StringUtils.isNotBlank(mac)){
            mac=mac.replaceAll("-", ":");
        }
        String url= Config.urlCurrent + "/v1/register?mac="+mac;
        Call  call= okHttpClient.newCall(builde.url(url)
                .get()//默认就是GET请求，可以不写
                .build());
        Response response = call.execute();
        if(response.isSuccessful()) {
            String body = response.body().string();
            JSONObject object = new JSONObject(body);
            int code = object.getInt("code");
            switch (code) {
                case 0://获取成功
                    Config.isGetID     = true;
                    object                 = object.getJSONObject("obj");
                    String id = object.getString("DevId");
                    if(!id.equals(Config.devId)) {//服务器下发新ID
                        Config.devId = id;
                        Map map = Config.getConfig_map();
                        map.put("devId", Config.devId);
                        Config.Set_Config(map);
//                        MyService.rebootDeviceTask.run();
                    }
                    break;
                case 1://暂无该设备相关信息
                    Config.isGetID = false;
                    break;
            }
            myLog.Write_Log(MyLog.LOG_INFO,url + " " + body);
        } else {
            myLog.Write_Log(MyLog.LOG_INFO,url + " " + response.code());
        }
    }

    public void GetConfigData(DataCallBack callBack) {
        Request("config", "dev_id=" + Config.devId, callBack);
    }
    public void GetApk(String fname, DataCallBack callBack) {
        Config.apkFname = fname;
        Request("apk", "dev_id="+ Config.devId + "&fname=" + fname, callBack);
    }
    public void GetMain(String fname, DataCallBack callBack) {
        Config.mainFname = fname;
        Request("main", "dev_id="+ Config.devId + "&fname=" + fname, callBack);
    }
    public void GetSub(String fname, DataCallBack callBack) {
        Config.subFname = fname;
        Request("sub", "dev_id="+ Config.devId + "&fname=" + fname, callBack);
    }

    public void Request(String method, String arg, final DataCallBack callBack) {
        if(!LocalData.isNetworking) return;
        if(!Config.isGetID) return;
        builde.header("devId", Config.devId);
        try {
            Execute(method, arg, callBack);
        } catch (Exception e) {
            myLog.Write_Log(MyLog.LOG_INFO,"OTA同步请求错误：" + e.toString());
        }
    }
    //异步请求
    private void Enqueue(final String method, String arg, final DataCallBack callBack) {
        final String url = Config.urlCurrent + "/v1/file/download/" + method + "?" + arg;
        Call call = okHttpClient.newCall(builde.url(url)
                .get()//默认就是GET请求，可以不写
                .build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                myLog.Write_Log(MyLog.LOG_INFO,e.toString() + "\t\t" + url);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    int http_code = response.code();
                    if(http_code >= 200 && http_code < 300) {
                        if(method.equals("config")) {
                            try {
                                get_String(response, callBack);
                            } catch (Exception e) {
                                myLog.Write_Log(MyLog.LOG_WARN,"Json 数据异常 : \t\t " + e + " \t\t " + url);
                            }
                        } else {
                            download_file(response, method, callBack);
                        }
                        myLog.Write_Log(MyLog.LOG_INFO,"OTA服务器正确响应： " + response.request().url());
                    } else {
                        myLog.Write_Log(MyLog.LOG_INFO,response.request().url() + ", code:" + http_code);
                    }
                } catch (Exception e) {
                    String str = "OTA数据解析错误" + e.toString();
                    myLog.Write_Log(MyLog.LOG_INFO,str);
                }
            }
        });
        myLog.Write_Log(MyLog.LOG_INFO,"智联电接口请求：" + url);
    }
    //同步请求
    private void Execute(String method, String arg, DataCallBack callBack) throws IOException {
        String url= Config.urlCurrent + "/v1/file/download/" + method + "?" + arg;
        Call call = okHttpClient.newCall(builde.url(url)
                .get()//默认就是GET请求，可以不写
                .build());
        Response response = call.execute();
        if(response.isSuccessful()) {
            try {
                int http_code = response.code();
                if(http_code >= 200 && http_code < 300) {
                    if(method.equals("config")) {
                        try {
                            get_String(response, callBack);
                        } catch (Exception e) {
                            myLog.Write_Log(MyLog.LOG_INFO,"OTA Json 数据异常 : \t\t " + e + " \t\t " + url);
                        }
                    } else {
                        download_file(response, method, callBack);
                    }
                    myLog.Write_Log(MyLog.LOG_INFO,"OTA 服务器正确响应： " + url);
                } else {
                    myLog.Write_Log(MyLog.LOG_INFO,url + ", code:" + http_code);
                }
            } catch (Exception e) {
                String str = "OTA 数据解析错误" + e.toString();
                myLog.Write_Log(MyLog.LOG_INFO,str);
            }
        } else {
            myLog.Write_Log(MyLog.LOG_INFO,"OTA 服务器请求：" + response.code() + " " +url);
        }
    }

    private void get_String(Response response, DataCallBack callBack) throws Exception {
        String body = response.body().string();
        JSONObject jsonObject = new JSONObject(body);
        UpdateData updateData = new UpdateData();
        updateData.setApk(JSON.parseObject(jsonObject.getString("apk"), UpdateData.Apk.class));
        updateData.setMain(JSON.parseObject(jsonObject.getString("main"), UpdateData.Main.class));
        updateData.setSub(JSON.parseObject(jsonObject.getString("sub"), UpdateData.Sub.class));
        callBack.getData(updateData, Mode_Config);
    }
    private void download_file(Response response, String method, DataCallBack callBack) throws IOException {
        Long crc = Long.parseLong(response.header("Authorization"),16);
        String fp = "";
        String filename = "";

        switch (method) {
            case "apk":
                fp = Config.apkFpath;
                filename = Config.apkFname;
                break;
            case "main":
                fp = Config.mainFpath;
                filename = Config.mainFname;
                break;
            case "sub":
                fp = Config.subFpath;
                filename = Config.subFname;
                break;
        }

        File file1 = CommonClass.CreateFile(fp, filename);
        FileOutputStream fos = new FileOutputStream(file1);
        InputStream inputStream = response.body().byteStream();
        java.util.zip.CRC32 crc32 = new java.util.zip.CRC32();
        int  readoff   = 0; //读取到数组的偏移位置
        int  readlen   = 0; //读取到数组的长度
        int  returnlen = 0; //返回的读取长度，实际读取长度
        int  copysum   = 0; //总字节的副本，大于数组长度时初始化
        long allsum    = 0; //总字节数，等于返回长度时退出循环
        long rblen= response.body().contentLength();
        int bsize= rblen < 0x200000 ? (int)rblen : 102400;//数组长度
        //当数据长度超过2M时将数据流输出到文件，内存中只保存最后100K，数据小于2M时保存到内存
        byte[] byte_body = new byte[bsize];

        readlen = bsize;
        long start = System.currentTimeMillis();
        try{
            while ((returnlen = inputStream.read(byte_body, readoff, readlen)) != -1) {
                fos.write   (byte_body, readoff, returnlen);
                crc32.update(byte_body, readoff, returnlen);

                allsum  += returnlen;
                copysum += returnlen;
                readoff = copysum >= bsize ? copysum = 0 : copysum;
                readlen  = bsize-readoff;

                if(allsum == rblen) break;
                if((System.currentTimeMillis() - start)/1000 > 1) {
                    start = System.currentTimeMillis();
                    myLog.Write_Log(MyLog.LOG_INFO,"######" + String.format("%.2f", allsum / (rblen * 1.0f)) + "%"
                            + "######" + method);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        fos.close();
        inputStream.close();

        myLog.Write_Log(MyLog.LOG_INFO,"文件下载完成：" + filename +
                " 下载次数: "+ findFileArray(filename));
        if(crc == crc32.getValue()) {//数据校验
            switch (method) {
                case "apk"://"file/download/apk":
                    callBack.getData(null, Mode_Apk);
                    break;
                case "main"://"file/download/main":
                    callBack.getData(null, Mode_Main);
                    break;
                case "sub"://"file/download/sub":
                    callBack.getData(null, Mode_Sub);
                    break;
            }
        } else {
            myLog.Write_Log(MyLog.LOG_INFO,"OTA 数据校验错误" + response.request().url());
        }
    }
    private int findFileArray(String filename) {
        int size = LocalData.fileUpdate_array.size();
        for (int i = 0; i < size; i++) {
            String fn = LocalData.fileUpdate_array.get(i);
            if(fn.contains(filename)) {
                String file = fn.split("-")[0];
                int    cout = Integer.valueOf(fn.split("-")[1]) + 1;
                fn          = file + "-" + cout;
                LocalData.fileUpdate_array.set(i, fn);
                return cout;
            }
        }
        LocalData.fileUpdate_array.add(filename + "-1");
        return  1;
    }


    public static class Login {
        String accessToken = "";
        String accessTokenExpiration = "";
        int    accessTokenExpiresIn;
        String refreshToken;

        public int getAccessTokenExpiresIn() {
            return accessTokenExpiresIn;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getAccessTokenExpiration() {
            return accessTokenExpiration;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public void setAccessTokenExpiration(String accessTokenExpiration) {
            this.accessTokenExpiration = accessTokenExpiration;
        }

        public void setAccessTokenExpiresIn(int accessTokenExpiresIn) {
            this.accessTokenExpiresIn = accessTokenExpiresIn;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
    public static class Refresh {
        String expiration;
        String value;
        int    expiresIn;
        refreshToken refreshToken;

        public int getExpiresIn() {
            return expiresIn;
        }

        public Refresh.refreshToken getRefreshToken() {
            return refreshToken;
        }

        public String getExpiration() {
            return expiration;
        }

        public String getValue() {
            return value;
        }

        public void setRefreshToken(Refresh.refreshToken refreshToken) {
            this.refreshToken = refreshToken;
        }

        public void setExpiration(String expiration) {
            this.expiration = expiration;
        }

        public void setExpiresIn(int expiresIn) {
            this.expiresIn = expiresIn;
        }

        public void setValue(String value) {
            this.value = value;
        }

        class refreshToken {
            String expiration;
            String value;

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public String getExpiration() {
                return expiration;
            }

            public void setExpiration(String expiration) {
                this.expiration = expiration;
            }
        }

    }
    public static class PerOTA {
        String filename;
        int    num;
        boolean status;
        int    type;

        public PerOTA() {

        }

        public PerOTA(String filename, int num, boolean status, int type) {
            this.filename = filename;
            this.num = num;
            this.status = status;
            this.type = type;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public int getNum() {
            return num;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public boolean isStatus() {
            return status;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    // string类型转换为long类型
    // strTime要转换的String类型的时间
    // formatType时间格式
    // strTime的时间格式和formatType的时间格式必须相同
    public static long stringToLong(String strTime, String formatType)
            throws ParseException {
        Date date = stringToDate(strTime, formatType); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateToLong(date); // date类型转成long类型
            return currentTime;
        }
    }
    // string类型转换为date类型
    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }
    // date类型转换为long类型
    // date要转换的date类型的时间
    public static long dateToLong(Date date) {
        return date.getTime();
    }
}
