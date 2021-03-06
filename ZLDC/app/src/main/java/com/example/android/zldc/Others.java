package com.example.android.zldc;

import android.os.Build;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

public class Others {

    /*
     * Convert byte[] to hex string.
     * @param src byte[] data
     * @return hex string
     */
    public String bytesToHexString(byte[] src, int offset, int len) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = offset; i < len; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv + " ");
        }
        return stringBuilder.toString();
    }

    public byte[] convert2HexArray(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.replace(" ", "");
        hexString = hexString.toUpperCase();
        int len = hexString.length() / 2;
        char[] chars = hexString.toCharArray();
        String[] hexes = new String[len];
        byte[] bytes = new byte[len];
        for (int i = 0, j = 0; j < len; i = i + 2, j++) {
            hexes[j] = "" + chars[i] + chars[i + 1];
            if (hexes[j].equals("\r\n")) {
                bytes[j] = hexes[j].getBytes()[0];
                continue;
            }
            bytes[j] = (byte) Integer.parseInt(hexes[j], 16);
        }

        return bytes;
    }

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }

        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * ??????????????????16???????????????
     *
     * @param s
     * @return
     */
    public String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }


    /**
     * 16?????????????????????????????????
     *
     * @param s
     * @return
     */
    public String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "gbk");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    static StringBuilder stringBuilder = new StringBuilder();

    // ???????????????base64?????????
    public static StringBuilder GetImageStr(String imgFile) {// ???????????????????????????????????????????????????????????????Base64????????????
        InputStream in = null;
        byte[] data = null;
        // ????????????????????????
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stringBuilder.setLength(0);
        stringBuilder.append(SeriaPort.Base_64(data, data.length));
        return stringBuilder;
        //return Base64.encodeBase64String(data);
        //Base64.Encoder encoder = Base64.getEncoder();
        //return encoder.encodeToString(data);
    }

    // base64????????????????????????
    public static boolean GenerateImage(String imgStr, String imgFilePath) throws Exception {
        if (imgStr == null) // ??????????????????
            return false;
        Base64.Decoder decoder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            decoder = Base64.getDecoder();
        }

        // Base64??????,??????????????????????????????Base64?????????????????????
        byte[] b = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            b = decoder.decode(imgStr);
        }
        for (int i = 0; i < b.length; ++i) {
            if (b[i] < 0) {// ??????????????????
                b[i] += 256;
            }
        }
        // ??????jpeg??????
        // String imgFilePath = "c://temp_kjbl_001_ab_010.jpg";//??????????????????
        OutputStream out = new FileOutputStream(imgFilePath);
        out.write(b);
        out.flush();
        out.close();
        return true;
    }

    /**
     * ??????????????????ASCII????????????
     *
     * @return ASCII?????????
     */
    public static String StringToAsciiString(String content) {
        String result = "";
        int max = content.length();
        for (int i = 0; i < max; i++) {
            char c = content.charAt(i);
            String b = Integer.toHexString(c);
            result = result + b;
        }
        return result;
    }

    /**
     * ????????????????????????
     *
     * @param hexString  ?????????????????????
     * @param encodeType ????????????4???Unicode???2???????????????
     * @return ?????????
     */
    public static String hexStringToString(String hexString, int encodeType) {
        String result = "";
        int max = hexString.length() / encodeType;
        for (int i = 0; i < max; i++) {
            char c = (char) hexStringToAlgorism(hexString.substring(i * encodeType, (i + 1) * encodeType));
            result += c;
        }
        return result;
    }

    /**
     * ?????????????????????????????????
     *
     * @param hex ?????????????????????
     * @return ???????????????
     */
    public static int hexStringToAlgorism(String hex) {
        hex = hex.toUpperCase();
        int max = hex.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = hex.charAt(i - 1);
            int algorism = 0;
            if (c >= '0' && c <= '9') {
                algorism = c - '0';
            } else {
                algorism = c - 55;
            }
            result += Math.pow(16, max - i) * algorism;
        }
        return result;
    }

    /**
     * ??????????????????
     *
     * @param hex ?????????????????????
     * @return ??????????????????
     */
    public static String hexStringToBinary(String hex) {
        hex = hex.toUpperCase();
        String result = "";
        int max = hex.length();
        for (int i = 0; i < max; i++) {
            char c = hex.charAt(i);
            switch (c) {
                case '0':
                    result += "0000";
                    break;
                case '1':
                    result += "0001";
                    break;
                case '2':
                    result += "0010";
                    break;
                case '3':
                    result += "0011";
                    break;
                case '4':
                    result += "0100";
                    break;
                case '5':
                    result += "0101";
                    break;
                case '6':
                    result += "0110";
                    break;
                case '7':
                    result += "0111";
                    break;
                case '8':
                    result += "1000";
                    break;
                case '9':
                    result += "1001";
                    break;
                case 'A':
                    result += "1010";
                    break;
                case 'B':
                    result += "1011";
                    break;
                case 'C':
                    result += "1100";
                    break;
                case 'D':
                    result += "1101";
                    break;
                case 'E':
                    result += "1110";
                    break;
                case 'F':
                    result += "1111";
                    break;
            }
        }
        return result;
    }

    /**
     * ASCII??????????????????????????????
     */
    public static String AsciiStringToString(String content) {
        String result = "";
        int length = content.length() / 2;
        for (int i = 0; i < length; i++) {
            String c = content.substring(i * 2, i * 2 + 2);
            int a = hexStringToAlgorism(c);
            char b = (char) a;
            String d = String.valueOf(b);
            result += d;
        }
        return result;
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param algorism  int ???????????????
     * @param maxLength int ???????????????????????????????????????
     * @return String ?????????????????????????????????
     */
    public static String algorismToHEXString(int algorism, int maxLength) {
        String result = "";
        result = Integer.toHexString(algorism);
        if (result.length() % 2 == 1) {
            result = "0" + result;
        }
        return patchHexString(result.toUpperCase(), maxLength);
    }

    /**
     * ????????????????????????????????????ASCII??????????????????
     *
     * @param bytearray byte[]
     * @return String
     */
    public static String bytetoString(byte[] bytearray) {
        String result = "";
        char temp;
        int length = bytearray.length;
        for (int i = 0; i < length; i++) {
            temp = (char) bytearray[i];
            result += temp;
        }
        return result;
    }

    /**
     * ??????????????????????????????
     *
     * @param binary ??????????????????
     * @return ???????????????
     */
    public static int binaryToAlgorism(String binary) {
        int max = binary.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = binary.charAt(i - 1);
            int algorism = c - '0';
            result += Math.pow(2, max - i) * algorism;
        }
        return result;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param algorism int ??????????????????
     * @return String ??????????????????????????????
     */
    public static String algorismToHEXString(int algorism) {
        String result = "";
        result = Integer.toHexString(algorism);
        if (result.length() % 2 == 1) {
            result = "0" + result;
        }
        result = result.toUpperCase();
        return result;
    }

    /**
     * HEX???????????????0????????????????????????????????????
     *
     * @param str       String ??????????????????????????????????????????
     * @param maxLength int ???????????????????????????????????????
     * @return ????????????
     */
    static public String patchHexString(String str, int maxLength) {
        String temp = "";
        for (int i = 0; i < maxLength - str.length(); i++) {
            temp = "0" + temp;
        }
        str = (temp + str).substring(0, maxLength);
        return str;
    }

    /**
     * ???????????????????????????int
     *
     * @param s          String ?????????????????????
     * @param defaultInt int ??????????????????,?????????????????????
     * @param radix      int ???????????????????????????????????????,???16 8 10.
     * @return int ??????????????????
     */
    public static int parseToInt(String s, int defaultInt, int radix) {
        int i = 0;
        try {
            i = Integer.parseInt(s, radix);
        } catch (NumberFormatException ex) {
            i = defaultInt;
        }
        return i;
    }

    /**
     * ???????????????????????????????????????????????????int
     *
     * @param s          String ?????????????????????
     * @param defaultInt int ??????????????????,?????????????????????
     * @return int ??????????????????
     */
    public static int parseToInt(String s, int defaultInt) {
        int i = 0;
        try {
            i = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            i = defaultInt;
        }
        return i;
    }

    /**
     * ???????????????????????????Byte??????,???????????????????????????????????????Byte
     *
     * @param hex ?????????????????????
     * @return byte ????????????
     */
    public static byte[] hexStringToByte(String hex) {
        int max = hex.length() / 2;
        byte[] bytes = new byte[max];
        String binarys = hexStringToBinary(hex);
        for (int i = 0; i < max; i++) {
            bytes[i] = (byte) binaryToAlgorism(binarys.substring(i * 8 + 1, (i + 1) * 8));
            if (binarys.charAt(8 * i) == '1') {
                bytes[i] = (byte) (0 - bytes[i]);
            }
        }
        return bytes;
    }

    /**
     * ????????????????????????byte??????
     *
     * @return the array of byte
     */
    public static final byte[] hex2byte(String hex) throws IllegalArgumentException {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = new Integer(byteint).byteValue();
        }
        return b;
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param b byte[] ???????????????????????????
     * @return String ?????????????????????
     */
    public static final String byte2hex(byte[] b) {
        if (b == null) {
            throw new IllegalArgumentException("Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
            hs += " ";
        }
        return hs.toUpperCase();
    }

    /**
     * BCD?????????10?????????(???????????????)
     *
     * @param bytes bcd???
     * @return
     */
    public static String bcd2Str(byte[] bytes) {
        //??????????????????2000????????????????????????????????????????????????????????????
        StringBuffer temp = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
            temp.append((byte) (bytes[i] & 0x0f));
        }
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp
                .toString().substring(1) : temp.toString();
    }

    /**
     * 10???????????????BCD???
     *
     * @param asc 10???????????????
     * @return
     */
    public static byte[] str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;
        //???????????????????????????0
        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }
        byte[] abt = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }
        byte[] bbt = new byte[len];
        abt = asc.getBytes();
        int j, k;
        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }
            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    /**
     * bcd?????????????????????
     *
     * @param bytes bcd???
     * @param split ?????????
     * @return
     */
    public static String bcd2TimeStr(byte[] bytes, String split) {
        String pre = "20";
        StringBuffer finalStr = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            StringBuffer temp = new StringBuffer(2);
            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
            temp.append((byte) (bytes[i] & 0x0f));
            if (i == 0) {
                finalStr.append(pre);
            }
            finalStr.append(temp);
            if (i != bytes.length - 1) {
                finalStr.append(split);
            }
        }
        return finalStr.toString();
    }

    /**
     * bcd???????????????(??????)
     *
     * @param bytes bcd???
     * @param split ?????????
     * @return
     */
    public static long bcd2TimeStamp(byte[] bytes, String split) {
        String str = bcd2TimeStr(bytes, split);
        String[] strs = str.split(split);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.valueOf(strs[0]));
        cal.set(Calendar.MONTH, Integer.valueOf(strs[1]) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(strs[2]));
        cal.set(Calendar.HOUR, Integer.valueOf(strs[3]));
        cal.set(Calendar.MINUTE, Integer.valueOf(strs[4]));
        cal.set(Calendar.SECOND, Integer.valueOf(strs[5]));
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        return date.getTime();
    }

    /**
     * ??????cron?????????
     *
     * @param hhmm ??????
     * @return
     */
    public static String getCronParment(String hhmm) {
        String[] arr = hhmm.split(":");
        String parten = "mm hh * * *";
        parten = parten.replace("mm", Integer.valueOf(arr[1]).toString());
        parten = parten.replace("hh", Integer.valueOf(arr[0]).toString());
        return parten;
    }
}
