import 'dart:io';
import 'package:permission_handler/permission_handler.dart';

class RaycomePermissions {
  /// Check and request all necessary permissions for Bluetooth.
  static Future<bool> checkAndRequestPermissions() async {
    if (Platform.isAndroid) {
      Map<Permission, PermissionStatus> statuses = await [
        Permission.bluetoothScan,
        Permission.bluetoothConnect,
        Permission.location,
      ].request();

      return statuses[Permission.bluetoothScan]!.isGranted &&
          statuses[Permission.bluetoothConnect]!.isGranted &&
          statuses[Permission.location]!.isGranted;
    }
    // Add iOS logic if needed
    return true;
  }

  /// Specifically check if location is granted (often forgotten but required for BLE scan on Android).
  static Future<bool> isLocationGranted() async {
    return await Permission.location.isGranted;
  }

  /// Open app settings if permissions are permanently denied.
  static Future<void> openSettings() async {
    await openAppSettings();
  }
}
