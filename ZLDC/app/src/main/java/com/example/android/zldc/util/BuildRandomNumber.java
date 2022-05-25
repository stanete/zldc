package com.example.android.zldc.util;

import java.util.Random;

/**
 * Created by xiaomo
 * Date on  2019/4/14
 *
 * @Desc
 */

public class BuildRandomNumber {
    public static String createGUID() {
        String result = "00000000";
        try {
            char[] content = {'0', '1', '2',
                    '3', '4', '5', '6', '7', '8', '9'};
            Random random = new Random();
            char[] charArray = result.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                if (charArray[i] == '0') {
                    charArray[i] = content[random.nextInt(10)];
                }
            }
            result = String.valueOf(charArray);
        } catch (Exception ex) {
        }
        return result;
    }

    /**
     * 时间戳取后多少位
     *
     * @param length 位数(取值范围0~10)
     * @return
     */
    public static String createTimeStampLast(int length) {
        //最大取10位
        int max = 10;
        //最小取0位
        int min = 0;
        long timestamp = System.currentTimeMillis() / 1000;
        if (length >= max || length <= min) {
            return String.valueOf(timestamp);
        }
        return String.valueOf(timestamp).substring(max - length);
    }
}
