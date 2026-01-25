package com.sm.sm_raycome

import android.app.Activity
import android.content.Context
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class RaycomeHealthPlugin(private val context: Context) {
    private var activity: Activity? = null
    private val raycomeManager: RaycomeManager = RaycomeManager(context)
    private val raycomeHandler = RaycomeHandler()

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    fun getStreamHandler(): RaycomeHandler {
        return raycomeHandler
    }

    fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {

            "init" -> {
                result.success(true)
            }

            "start" -> {
                raycomeManager.start(activity, raycomeHandler)
                result.success(true)
            }
            "stop" -> {
                raycomeManager.stop()
                result.success(true)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    fun onDetachedFromEngine() {
        raycomeManager.stop()
    }
}
