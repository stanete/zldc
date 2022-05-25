package com.example.android.test;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class StartPwsDialog extends Dialog {

    /**
     * 上下文对象 *
     */
    Activity context;

    private Button btn_enter,btn_quit;

    public EditText pwd;

    private View.OnClickListener mClickListener;

    public StartPwsDialog(Activity context) {
        super(context);
        this.context = context;
    }

    public StartPwsDialog(Activity context, int theme, View.OnClickListener clickListener) {
        super(context, theme);
        this.context = context;
        this.mClickListener = clickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 指定布局
        this.setContentView(R.layout.activity_password);

        pwd = (EditText) findViewById(R.id.pwd);

        /*
         * 获取圣诞框的窗口对象及参数对象以修改对话框的布局设置, 可以直接调用getWindow(),表示获得这个Activity的Window
         * 对象,这样这可以以同样的方式改变这个Activity的属性.
         */
        Window dialogWindow = this.getWindow();

        WindowManager m = context.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
//        p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
        p.width = (int) (d.getWidth() * 0.6); // 宽度设置为屏幕的0.6
        dialogWindow.setAttributes(p);

        // 根据id在布局中找到控件对象
        btn_enter = (Button) findViewById(R.id.enter);
        btn_quit = (Button) findViewById(R.id.quit);
        // 为按钮绑定点击事件监听器
        btn_enter.setOnClickListener(mClickListener);
        btn_quit.setOnClickListener(mClickListener);
        //back键退出
        this.setCancelable(false);
        //设置外界点击可推出
        this.setCanceledOnTouchOutside(false);
        this.setTitle("enter_start_password");
    }
}

