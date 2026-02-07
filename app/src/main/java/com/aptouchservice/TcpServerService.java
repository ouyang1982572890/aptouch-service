package com.aptouchservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.accessibilityservice.AccessibilityService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServerService extends Service {

    private static final String TAG = "TcpServerService";
    private static final int PORT = 5555;
    private static volatile boolean running = false;
    private ServerSocket serverSocket;
    private Thread serverThread;

    public static boolean isRunning() {
        return running;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification());
        startServer();
        return START_STICKY;
    }

    private void startServer() {
        running = true;
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                Log.d(TAG, "TCP Server started on port " + PORT);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    Log.d(TAG, "Client connected: " + clientSocket.getRemoteSocketAddress());

                    // 处理客户端连接
                    handleClient(clientSocket);
                }
            } catch (Exception e) {
                if (running) {
                    Log.e(TAG, "Server error", e);
                }
            }
        });
        serverThread.start();
    }

    private void handleClient(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            // 发送连接确认
            writer.println("CONNECTED|1.0|" + android.os.Build.MODEL + "|" +
                getScreenWidth() + "|" + getScreenHeight());

            String line;
            while (running && (line = reader.readLine()) != null) {
                Log.d(TAG, "Received: " + line);
                String response = processCommand(line);
                writer.println(response);
            }
        } catch (Exception e) {
            Log.e(TAG, "Client error", e);
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                Log.e(TAG, "Close error", e);
            }
        }
    }

    private String processCommand(String command) {
        try {
            String[] parts = command.split("\\|");
            String cmd = parts[0].toUpperCase();

            switch (cmd) {
                case "TAP":
                    if (parts.length >= 3) {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        performTap(x, y);
                        return "OK|TAP|" + x + "|" + y;
                    }
                    return "ERROR|Invalid parameters";

                case "TOUCH":
                    if (parts.length >= 4) {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        int pressed = Integer.parseInt(parts[3]);
                        performTouch(x, y, pressed == 1);
                        return "OK|TOUCH|" + x + "|" + y + "|" + pressed;
                    }
                    return "ERROR|Invalid parameters";

                case "SWIPE":
                    if (parts.length >= 6) {
                        int sx = Integer.parseInt(parts[1]);
                        int sy = Integer.parseInt(parts[2]);
                        int ex = Integer.parseInt(parts[3]);
                        int ey = Integer.parseInt(parts[4]);
                        int duration = Integer.parseInt(parts[5]);
                        performSwipe(sx, sy, ex, ey, duration);
                        return "OK|SWIPE|completed";
                    }
                    return "ERROR|Invalid parameters";

                case "STATUS":
                    return "STATUS|1|1|1";

                case "DISCONNECT":
                    return "BYE";

                default:
                    return "ERROR|Unknown command";
            }
        } catch (Exception e) {
            Log.e(TAG, "Command error", e);
            return "ERROR|" + e.getMessage();
        }
    }

    private void performTap(int x, int y) {
        // 使用AccessibilityService模拟触摸
        long downTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, downTime, 
            MotionEvent.ACTION_DOWN, x, y, 0);
        injectEvent(event);

        MotionEvent eventUp = MotionEvent.obtain(downTime, downTime + 50, 
            MotionEvent.ACTION_UP, x, y, 0);
        injectEvent(eventUp);
    }

    private void performTouch(int x, int y, boolean pressed) {
        long downTime = SystemClock.uptimeMillis();
        int action = pressed ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_UP;
        MotionEvent event = MotionEvent.obtain(downTime, downTime, 
            action, x, y, 0);
        injectEvent(event);
    }

    private void performSwipe(int sx, int sy, int ex, int ey, int duration) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = downTime;
        int steps = 20;
        long stepDelay = duration / steps;

        // 按下
        MotionEvent downEvent = MotionEvent.obtain(downTime, eventTime,
            MotionEvent.ACTION_DOWN, sx, sy, 0);
        injectEvent(downEvent);

        // 滑动
        for (int i = 1; i < steps; i++) {
            eventTime += stepDelay;
            int cx = sx + (ex - sx) * i / steps;
            int cy = sy + (ey - sy) * i / steps;
            MotionEvent moveEvent = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_MOVE, cx, cy, 0);
            injectEvent(moveEvent);
        }

        // 抬起
        long upTime = downTime + duration;
        MotionEvent upEvent = MotionEvent.obtain(downTime, upTime,
            MotionEvent.ACTION_UP, ex, ey, 0);
        injectEvent(upEvent);
    }

    private void injectEvent(MotionEvent event) {
        // 通过AccessibilityService注入事件
        if (TouchService.getInstance() != null) {
            TouchService.getInstance().dispatchGenericMotionEvent(event);
        }
    }

    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    private int getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                "tcp_service",
                "APK Touch Service",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        return new Notification.Builder(this, "tcp_service")
            .setContentTitle("APK Touch Service")
            .setContentText("TCP Server running on port " + PORT)
            .setSmallIcon(R.drawable.ic_service)
            .setContentIntent(pendingIntent)
            .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Destroy error", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
