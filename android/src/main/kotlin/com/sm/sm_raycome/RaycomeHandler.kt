package com.sm.sm_raycome

import com.sm.sm_raycome.iknetbluetoothlibrary.BluetoothManager
import com.sm.sm_raycome.iknetbluetoothlibrary.MeasurementResult
import android.bluetooth.BluetoothDevice
import io.flutter.plugin.common.EventChannel
import android.os.Handler
import android.os.Looper

class RaycomeHandler : EventChannel.StreamHandler, BluetoothManager.OnBTMeasureListener {
    private var eventSink: EventChannel.EventSink? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val TAG = "RaycomeHandler"

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        android.util.Log.i(TAG, "onListen called - Flutter is now listening for events")
        eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        android.util.Log.i(TAG, "onCancel called - Flutter stopped listening")
        eventSink = null
    }

    private fun sendEvent(type: String, data: Any?) {
        mainHandler.post {
            if (eventSink == null) {
                android.util.Log.w(TAG, "sendEvent FAILED - eventSink is null! Type: $type, Data: $data")
            } else {
                android.util.Log.i(TAG, "sendEvent: type=$type, data=$data")
            }
            val event = mutableMapOf<String, Any?>()
            event["type"] = type
            event["data"] = data
            eventSink?.success(event)
        }
    }

    override fun onFoundFinish(deviceList: List<BluetoothDevice>?) {
        sendEvent("onFoundFinish", deviceList?.size ?: 0)
    }

    override fun onConnected(isConnected: Boolean, device: BluetoothDevice?) {
        val data = mutableMapOf<String, Any?>()
        data["isConnected"] = isConnected
        data["address"] = device?.address
        data["name"] = device?.name
        sendEvent("connectionState", data)
    }

    override fun onPower(power: String?) {
        sendEvent("power", power)
    }

    override fun onRunning(running: String?) {
        android.util.Log.i("RaycomeHandler", "onRunning event received: $running")
        sendEvent("running", running)
    }

    override fun onMeasureError() {
        sendEvent("error", "Measure Error")
    }

    override fun onMeasureResult(result: MeasurementResult?) {
        if (result == null) return
        val data = mutableMapOf<String, Any?>()
        data["systolic"] = result.checkShrink
        data["diastolic"] = result.checkDiastole
        data["heartRate"] = result.checkHeartRate

        data["createTime"] = result.createTime
        sendEvent("measurementResult", data)
    }

    override fun onDisconnected(device: BluetoothDevice?) {
        val data = mutableMapOf<String, Any?>()
        data["address"] = device?.address
        data["name"] = device?.name
        sendEvent("disconnected", data)
    }
}
