package com.sm.sm_raycome

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.widget.Toast
import com.sm.sm_raycome.iknetbluetoothlibrary.BluetoothManager
import com.sm.sm_raycome.iknetbluetoothlibrary.util.PermissionUtil
import java.util.List

/**
 * Expert-level manager for Raycome SDK interactions.
 * Implements SOLID principles by separating transport (Plugin) from logic (Manager).
 */
class RaycomeManager(private val context: Context) {
    private val bluetoothManager: BluetoothManager = BluetoothManager.getInstance(context)
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    /**
     * Checks if Bluetooth is enabled. if not, shows a toast.
     */
    fun isBluetoothEnabled(): Boolean {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            showToast("Please enable Bluetooth")
            return false
        }
        return true
    }

    /**
     * Checks and requests required Bluetooth/Location permissions across API levels.
     */
    fun checkPermissions(activity: Activity?): Boolean {
        if (activity == null) return false

        val sdk = android.os.Build.VERSION.SDK_INT
        return if (sdk >= android.os.Build.VERSION_CODES.S) {
            val needScan = activity.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) != android.content.pm.PackageManager.PERMISSION_GRANTED
            val needConnect = activity.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED
            if (needScan || needConnect) {
                activity.requestPermissions(arrayOf(
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                ), 1001)
                false
            } else true
        } else {
            if (!PermissionUtil.checkLocationPermission(activity)) {
                PermissionUtil.requestLocationPerm(activity)
                false
            } else true
        }
    }

    fun start(activity: Activity?, handler: RaycomeHandler) {
        if (!isBluetoothEnabled()) return
        if (!checkPermissions(activity)) return

        // Register receivers and set the listener before scanning, like the native Activity does
        bluetoothManager.startBTAffair(handler)
        // Redundant call removed: startBTAffair already triggers searchBluetooth() internally
        // bluetoothManager.searchBluetooth()
    }

    fun stop() {
        bluetoothManager.stopBTAffair()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
