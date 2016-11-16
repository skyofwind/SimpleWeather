package com.example.dzj.theweather;

import android.app.Application;

import com.baidu.apistore.sdk.ApiStoreSDK;

/**
 * Created by dzj on 2016/11/1.
 */

public class MyApplication extends Application{
    public void onCreate(){
        ApiStoreSDK.init(this,"0874e407afabb63ee55b4eed56632978");
        super.onCreate();
    }
}
