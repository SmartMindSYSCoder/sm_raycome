package com.sm.sm_raycome

import android.content.Context
import androidx.annotation.NonNull
import com.sm.sm_raycome.iknetbluetoothlibrary.BluetoothManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.EventChannel
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import android.app.Activity

/** SmRaycomePlugin */
class SmRaycomePlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private var raycomeHealthPlugin: RaycomeHealthPlugin? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        raycomeHealthPlugin = RaycomeHealthPlugin(flutterPluginBinding.applicationContext)
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "sm_raycome")
        channel.setMethodCallHandler(this)

        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "sm_raycome_events")
        eventChannel.setStreamHandler(raycomeHealthPlugin?.getStreamHandler())
    }

    override fun onDetachedFromActivity() {
        raycomeHealthPlugin?.setActivity(null)
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        raycomeHealthPlugin?.setActivity(binding.activity)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        raycomeHealthPlugin?.setActivity(binding.activity)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        raycomeHealthPlugin?.setActivity(null)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        raycomeHealthPlugin?.onMethodCall(call, result)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
        raycomeHealthPlugin?.onDetachedFromEngine()
    }
}

