package io.appium.uiautomator2.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rajdeepvarma on 07/01/2018.
 */

public class BackdoorResultVoid {
    public static final BackdoorResultVoid instance = new BackdoorResultVoid();

    private BackdoorResultVoid() {}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object asMap(String methodName, Object receiver,
                        String errorMessage) {
        Map map = new HashMap();
        map.put("error", errorMessage);
        map.put("methodName", methodName);
        map.put("receiverClass", receiver.getClass().getName());
        map.put("receiverString", receiver.toString());
        return map;
    }
}
