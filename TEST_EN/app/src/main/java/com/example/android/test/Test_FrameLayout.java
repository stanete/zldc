package com.example.android.test;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Test_FrameLayout extends FrameLayout {
    Context context;
    String boxnumber;

    public Test_FrameLayout(@NonNull Context context) {
        super(context);
        Init(context);
    }

    public Test_FrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Init(context);
    }

    public Test_FrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Init(context);
    }

    private void Init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.layout_box_test, this);
        boxnumber = getTag().toString();
        ((TextView) findViewById(R.id.tex_boxid)).setText(boxnumber);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示单仓的分控、电池、充电器信息
                Log.e("click_frame", boxnumber + "仓点击");
                if (TestActivity.isOpenSlot) {
                    TestActivity.seriaPort.Command_OpenBox(Integer.valueOf(boxnumber), 1);
                    Log.e("click_frame", boxnumber + "仓打开");
                } else {
                    Log.e("click_frame", boxnumber + "仓信息查询");
                    Intent detail = new Intent(context, BatteryStateActivity.class);
                    detail.putExtra("Boxnumber", boxnumber);
                    context.startActivity(detail);
                }
            }
        });
    }
}
