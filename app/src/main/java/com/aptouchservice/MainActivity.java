package com.aptouchservice;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView statusText;
    private Button startServiceBtn;
    private Button accessibilityBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        startServiceBtn = findViewById(R.id.btn_start_service);
        accessibilityBtn = findViewById(R.id.btn_accessibility);

        updateStatus();

        startServiceBtn.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, TcpServerService.class);
            startForegroundService(serviceIntent);
            Toast.makeText(this, "TCP Server Started", Toast.LENGTH_SHORT).show();
            updateStatus();
        });

        accessibilityBtn.setOnClickListener(v -> {
            // 打开无障碍服务设置
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
    }

    private void updateStatus() {
        boolean accessibilityEnabled = isAccessibilityServiceEnabled();
        boolean serverRunning = TcpServerService.isRunning();

        StringBuilder sb = new StringBuilder();
        sb.append("APK Touch Service\n\n");
        sb.append("TCP Server: ").append(serverRunning ? "Running" : "Stopped").append("\n");
        sb.append("Accessibility: ").append(accessibilityEnabled ? "Enabled" : "Disabled").append("\n\n");

        if (!accessibilityEnabled) {
            sb.append("Please enable Accessibility Service for touch injection!");
        } else if (!serverRunning) {
            sb.append("Tap 'Start Service' to begin.");
        } else {
            sb.append("Ready! Connect from PC using:\n");
            sb.append("Protocol: TCP\n");
            sb.append("Port: 5555\n\n");
            sb.append("Commands:\n");
            sb.append("  TAP|x|y     - Tap position\n");
            sb.append("  SWIPE|sx|sy|ex|ey|ms - Swipe\n");
            sb.append("  TOUCH|x|y|p - Touch down/move/up");
        }

        statusText.setText(sb.toString());
    }

    private boolean isAccessibilityServiceEnabled() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED
            );
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
            if (settingValue != null) {
                return settingValue.toLowerCase().contains(
                    getPackageName().toLowerCase() + "/" + TouchService.class.getName()
                );
            }
        }

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
}
