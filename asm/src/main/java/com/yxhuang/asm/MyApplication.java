package com.yxhuang.asm;

import android.app.Application;

import com.yxhuang.asmlib.DataApi;

/**
 * Created by yxhuang
 * Date: 2020/9/29
 * Description:
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DataApi.init(this);
    }
}
