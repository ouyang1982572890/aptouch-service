package com.aptouchservice;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

public class TouchService extends AccessibilityService {

    private static TouchService instance;

    public static TouchService getInstance() {
        return instance;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d("TouchService", "Service connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 不需要处理无障碍事件
    }

    @Override
    public void onInterrupt() {
        Log.d("TouchService", "Service interrupted");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d("TouchService", "Service destroyed");
    }
}
