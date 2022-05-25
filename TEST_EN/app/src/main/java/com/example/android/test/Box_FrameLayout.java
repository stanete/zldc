package com.example.android.test;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Box_FrameLayout extends FrameLayout {
    Context context;
    String boxnumber;

    public Box_FrameLayout(@NonNull Context context) {
        super(context);
        Init(context);
    }

    public Box_FrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Init(context);
    }

    public Box_FrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Init(context);
    }

    private void Init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.layout_box_t, this);
        boxnumber = getTag().toString();
        ((TextView) findViewById(R.id.tex_boxid)).setText(boxnumber);
    }

    AlphaAnimation alphaAnimation;

    public void OpenAnimation() {
//        findViewById(R.id.tex_bg).setAlpha(1);
//        if(alphaAnimation == null) {
//            alphaAnimation = new AlphaAnimation(0, 1);
//            alphaAnimation.setDuration(100);
//            alphaAnimation.setRepeatCount(Animation.INFINITE);
//            alphaAnimation.setRepeatMode(Animation.RESTART);
//        }
//        findViewById(R.id.tex_bg).setAnimation(alphaAnimation);
//        alphaAnimation.start();
    }

    public void CloseAnimation() {
        if (alphaAnimation != null)
            alphaAnimation.cancel();
    }
}
