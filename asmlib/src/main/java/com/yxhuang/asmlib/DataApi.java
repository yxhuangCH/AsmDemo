package com.yxhuang.asmlib;

import android.app.Application;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by yxhuang
 * Date: 2020/9/29
 * Description:
 */
@Keep
public class DataApi {

    private final String TAG = "DataApi";

    public static final String SDK_VERSION = "1.0.0";

    private static final Object mLock = new Object();
    private String mDeviceId;
    private static Map<String, Object> sDeviceInfo;

    private static DataApi sInstance;

    public static DataApi init(Application application){
        synchronized (mLock){
            if (sInstance == null){
                sInstance = new DataApi(application);
            }
            return sInstance;
        }
    }

    public static DataApi getInstance(){
        return sInstance;
    }

    private DataApi(Application application){
        mDeviceId = DataPrivate.getAndroidID(application);
        sDeviceInfo = DataPrivate.getDeviceInfo(application);
    }

    /**
     *  Track 事件
     * @param eventName     事件名称
     * @param properties    事件属性
     */
    @Keep
    public void track(@NonNull final String eventName, @Nullable JSONObject properties){
        try {
            Log.i(TAG, "track eventName " + eventName);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("event", eventName);
            jsonObject.put("device_id", mDeviceId);

            JSONObject sendProperties = new JSONObject(sDeviceInfo);
            if (properties != null){
                DataPrivate.mergeJSONObject(properties, sendProperties);
            }
            jsonObject.put("properties", sendProperties);
            jsonObject.put("time", System.currentTimeMillis());

            Log.i(TAG, DataPrivate.formatJson(jsonObject.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


