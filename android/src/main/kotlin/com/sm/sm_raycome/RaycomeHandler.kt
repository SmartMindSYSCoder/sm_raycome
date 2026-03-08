package com.sm.sm_raycome

import com.sm.sm_raycome.iknetbluetoothlibrary.BluetoothManager
import com.sm.sm_raycome.iknetbluetoothlibrary.MeasurementResult
import android.bluetooth.BluetoothDevice
import io.flutter.plugin.common.EventChannel
import io.reactivex.subjects.PublishSubject
import io.reactivex.disposables.Disposable
import io.reactivex.android.schedulers.AndroidSchedulers

class RaycomeHandler : EventChannel.StreamHandler, BluetoothManager.OnBTMeasureListener {
    private var eventSink: EventChannel.EventSink? = null
    private val TAG = "RaycomeHandler"

    // Subject to handle measurement events reactively
    private val eventSubject = PublishSubject.create<Map<String, Any?>>()
    private var disposable: Disposable? = null

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        android.util.Log.i(TAG, "onListen called - Flutter is now listening for events")
        eventSink = events

        // Subscribe to the event stream and deliver to Flutter on the main thread
        disposable = eventSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ event ->
                eventSink?.success(event)
            }, { error ->
                android.util.Log.e(TAG, "RxJava stream error: ${error.message}")
                eventSink?.error("RX_ERROR", error.message, null)
            })
    }

    override fun onCancel(arguments: Any?) {
        android.util.Log.i(TAG, "onCancel called - Flutter stopped listening")
        disposable?.dispose()
        disposable = null
        eventSink = null
    }

    private fun sendEvent(type: String, data: Any?) {
        val event = mutableMapOf<String, Any?>()
        event["type"] = type
        event["data"] = data
        
        // Emit event to the reactive subject
        eventSubject.onNext(event)
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
