package io.appium.uiautomator2.handler;

import android.app.Activity;
import android.util.ArrayMap;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appium.uiautomator2.core.InvocationOperation;
import io.appium.uiautomator2.handler.request.SafeRequestHandler;
import io.appium.uiautomator2.http.AppiumResponse;
import io.appium.uiautomator2.http.IHttpRequest;
import io.appium.uiautomator2.server.WDStatus;
import io.appium.uiautomator2.utils.Logger;

/**
 * Created by rajdeepvarma on 07/11/2017.
 */

public class Backdoor extends SafeRequestHandler {

    public Backdoor(String mappedUri) {
        super(mappedUri);
    }

    @Override
    public AppiumResponse safeHandle(IHttpRequest request) {
        Logger.info("Invoking Backdoor");
        Activity activity = getActivity();

        List<InvocationOperation> ops = getBackdoorOperations(request);

        Object invocationResult = null;

        Object InvokeOn = activity.getApplication();
        for (InvocationOperation op : ops) {
            try {
                invocationResult = op.apply(InvokeOn);
                InvokeOn = invocationResult;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (invocationResult instanceof Map && ((Map) invocationResult).containsKey("error")) {
            InvokeOn = activity;
            for (InvocationOperation op : ops) {
                try {
                    invocationResult = op.apply(InvokeOn);
                    InvokeOn = invocationResult;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return new AppiumResponse(getSessionId(request), WDStatus.SUCCESS, invocationResult);
    }

    @NonNull
    private List<InvocationOperation> getBackdoorOperations(IHttpRequest request) {
        List<InvocationOperation> ops = new ArrayList<>();
        try {
            JSONArray methods = getPayload(request).getJSONArray("methods");
            for (int i = 0; i < methods.length(); i++) {
                String methodName = methods.getJSONObject(i).getString("name");

                List<String> arguments = new ArrayList<String>();
                JSONArray jsonArray = null;
                try {
                    jsonArray = methods.getJSONObject(i).getJSONArray("args");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                if (jsonArray != null) {
                    for (int j = 0; j < jsonArray.length(); j++) {
                        arguments.add(jsonArray.get(j).toString());
                    }
                }
                ops.add(new InvocationOperation(methodName,arguments));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ops;
    }

    @Nullable
    private Activity getActivity() {
        Activity activity = null;
        Class activityThreadClass = null;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            ArrayMap activities = (ArrayMap) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    activity = (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activity;
    }
}
