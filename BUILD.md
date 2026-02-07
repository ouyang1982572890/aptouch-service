# APK Touch Service - Android App

## Build Instructions

### Prerequisites
- Android Studio installed
- JDK 8 or higher
- Android SDK

### Build Steps

1. **Open Android Studio**
   - Click "Open"
   - Select the `android-app` folder
   - Wait for Gradle sync to complete

2. **Build APK**
   - Menu: Build → Build Bundle(s) / APK(s) → Build APK(s)
   - APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

3. **Install on Phone**
   - Transfer APK to your Android phone
   - Enable "Install from unknown sources"
   - Install the APK

## First Run Setup

1. Open APK Touch Service app
2. Tap "Open Accessibility Settings"
3. Find "APK Touch Service" in the list
4. Enable it
5. Return to the app
6. Tap "Start TCP Service"

## Usage with PC

### Option 1: USB Debugging with ADB Port Forwarding

```bash
# Setup port forwarding
adb forward tcp:5555 tcp:5555

# Run SF_DML on PC and select "APK Touch" mode
```

### Option 2: WiFi Connection (same network)

1. Get phone's IP address: Settings → About → Status
2. Modify PC code to use phone IP instead of 127.0.0.1

## Protocol

| Command | Format |
|---------|--------|
| Tap | `TAP\|x\|y` |
| Touch | `TOUCH\|x\|y\|pressed` |
| Swipe | `SWIPE\|sx\|sy\|ex\|ey\|duration` |

## Troubleshooting

- **"Accessibility service not enabled"**: Enable it in Settings
- **"Connection refused"**: Make sure TCP Service is running on phone
- **Touch not working**: Some apps block accessibility touch injection
