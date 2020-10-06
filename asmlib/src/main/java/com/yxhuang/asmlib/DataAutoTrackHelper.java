package com.yxhuang.asmlib;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Keep;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yxhuang
 * Date: 2020/9/29
 * Description: 用于数据的埋点
 */
@Keep
public class DataAutoTrackHelper {

    @Keep
    public static void trackViewOnClick(View view) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("$element_type", DataPrivate.getElementType(view));
            jsonObject.put("$element_id", DataPrivate.getViewId(view));
            jsonObject.put("$element_content", DataPrivate.getElementContent(view));

            Activity activity = DataPrivate.getActivityFromView(view);
            if (activity != null) {
                jsonObject.put("$activity", activity.getClass().getCanonicalName());
            }
            DataApi.getInstance().track("$AppClick", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
