# Raycome RBP-7000 Flutter Plugin

[![Flutter](https://img.shields.io/badge/Flutter-Plugin-blue.svg)](https://flutter.dev)

A comprehensive and easy-to-use Flutter plugin for integrating the **Raycome RBP-7000 Blood Pressure Monitor**. This plugin handles the complexities of Bluetooth Low Energy (BLE) communication, state management, and data parsing, providing a simple, reactive API for your application.

## 🚀 Features

-   **One-Touch Operation**: A single `start()` method handles scanning, connection, and measurement initiation.
-   **Reactive State**: Exposes a real-time stream of `RaycomeState` events for seamless UI updates.
-   **Smart Disconnect**: 
    -   Automatically disconnects 10 seconds after a successful measurement to conserve battery.
    -   Supports manual `stop()` to immediately cancel and reset the device state.
-   **Live Data**: Provides real-time cuff pressure updates during inflation/deflation.
-   **Persistent Results**: Measurement results remain available until explicitly cleared or a new measurement starts.
-   **Robust Error Handling**: built-in recovery for connection failures and protocol errors.

## 📦 Installation

Add the following to your `pubspec.yaml`:

```yaml
dependencies:
  sm_raycome:
    git:
      url: https://github.com/SmartMindSYSCoder/sm_raycome.git
```

## ⚙️ Setup

### Android

Add the necessary permissions to your `android/app/src/main/AndroidManifest.xml`. 
*Note: For Android 12+ (API 31+), granular Bluetooth permissions are required.*

```xml
<manifest ...>
    <!-- Legacy Bluetooth Permissions -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    
    <!-- Android 12+ Bluetooth Permissions -->
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

    <!-- Location Permissions (Required for BLE scanning on older Android versions) -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
</manifest>
```

## 📖 Usage

### 1. Initialization

Initialize the plugin and request permissions early in your app lifecycle.

```dart
import 'package:sm_raycome/sm_raycome.dart';

final raycome = SmRaycome();

// Initialize the plugin
await raycome.init();

// Request runtime permissions (Critical on Android)
if (!await raycome.getPermissions()) {
  await raycome.openSettings();
}
```

### 2. State Management (UI)

The plugin provides a centralized `events` stream. Use a `StreamBuilder` to listen to state changes and update your UI.

```dart
StreamBuilder<RaycomeState>(
  stream: raycome.events,
  initialData: RaycomeState.initial(),
  builder: (context, snapshot) {
    final state = snapshot.data!;

    // 1. Show Busy / Loading State
    if (state.isBusy) {
        return Column(
            children: [
                CircularProgressIndicator(),
                Text(state.displayStatus), // "Scanning...", "Connecting...", "Measuring"
                if (state.measurementStatus == RaycomeMeasurementStatus.measuring)
                    Text("${state.runningPressure} mmHg", style: TextStyle(fontSize: 48)),
            ],
        );
    }

    // 2. Show Result
    if (state.lastResult != null) {
      return ResultCard(
        result: state.lastResult!,
        onClear: () => raycome.clearResult(), // Manually clear result
      );
    }

    // 3. Status Display
    return Text(state.displayStatus);
  },
)
```

### 3. Logic Integration (No StreamBuilder)

For using the plugin in your business logic (e.g., BLoC, Provider, Controller), simply listen to the stream manually.

```dart
class BloodPressureController {
  final raycome = SmRaycome();
  StreamSubscription? _subscription;

  void onInit() {
    _subscription = raycome.events.listen((state) {
      // Handle Success
      if (state.lastResult != null && state.measurementStatus == RaycomeMeasurementStatus.success) {
        // e.g., Save to Database, Analytics, etc.
        print("Measured: ${state.lastResult?.systolic}/${state.lastResult?.diastolic}");
      }
      
      // Handle Errors
      if (state.measurementStatus == RaycomeMeasurementStatus.error) {
         print("Error: ${state.errorMessage}");
      }
    });
  }

  void dispose() {
    _subscription?.cancel();
  }
}
```

### 4. Controlling the Device

#### Start Measurement
This single call initiates the entire workflow: **Scan -> Connect -> Measure**.
```dart
await raycome.start();
```

#### Stop Measurement
Cancels any active operation, disconnects the device, and **immediately resets** the UI state.
```dart
await raycome.stop();
```

#### Clear Results
Manually clear the last measurement result from the state.
```dart
raycome.clearResult();
```

### 5. Simplified Logic Helpers

The `RaycomeState` object includes a helper method to simplify your UI logic:

-   **`state.isBusy`**: Returns `true` if the device is currently Scanning, Connecting, or Measuring. It excludes the "Success" state, allowing you to show a "Start" button immediately after a measurement finishes.

```dart
// Example Button Logic
ElevatedButton(
  onPressed: state.isBusy ? () => raycome.stop() : () => raycome.start(),
  style: state.isBusy ? stopStyle : startStyle,
  child: Text(state.isBusy ? "STOP" : "START"),
)
```

## 📊 Data Models

### `RaycomeState`
| Property | Type | Description |
| :--- | :--- | :--- |
| `connectionState` | `enum` | `connected`, `disconnected`, `unknown` |
| `measurementStatus` | `enum` | `idle`, `scanning`, `measuring`, `success`, `error` |
| `runningPressure` | `int` | Real-time cuff pressure during measurement. |
| `lastResult` | `BloodPressureResult?` | The most recent successful measurement. |
| `errorMessage` | `String?` | Description of the last error, if any. |

### `BloodPressureResult`
| Property | Type | Description |
| :--- | :--- | :--- |
| `systolic` | `int` | Systolic pressure (mmHg). |
| `diastolic` | `int` | Diastolic pressure (mmHg). |
| `heartRate` | `int` | Heart rate (BPM). |
| `createTime` | `String` | Timestamp of the measurement. |

---

## 🛠 Troubleshooting

### ❓ Q: The scan is not finding any devices.
**Ans:** Ensure your device has **Location** (GPS) enabled and you have granted the Location permission. 
On Android 12+, ensure the `BLUETOOTH_SCAN` permission is granted.

### ❓ Q: The device disconnects after measurement.
**Ans:** This is intended behavior. The plugin auto-disconnects after 10 seconds of inactivity to save battery and reset the device state for the next user.

### ❓ Q: How do I reset the UI?
**Ans:** Calling `stop()` will immediately force a disconnected state and clear progress. 
Calling `start()` will also clear previous results before beginning a new scan.
