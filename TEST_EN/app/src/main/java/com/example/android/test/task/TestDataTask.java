package com.example.android.test.task;

import com.example.android.test.LocalData;
import com.example.android.test.test.Test;

import java.util.TimerTask;

/**
 * 测试UI数据同步任务
 */
public class TestDataTask extends TimerTask {
    @Override
    public void run() {
        Test t = LocalData.getTest();
        LocalData.getMainData();
        LocalData.getLocalData().getSubDataList();
        LocalData.setTest(t);
    }
}
