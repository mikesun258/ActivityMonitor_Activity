package com.mikesun258.activitymonitor.activity

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ActivityMonitor : IXposedHookLoadPackage {
    private val TAG = "ActivityMonitor"

    private val BROADCAST_ACTIVITY_CREATE = "com.mikesun258.activitymonitor.ACTIVITY_CREATE"
    private val BROADCAST_ACTIVITY_START  = "com.mikesun258.activitymonitor.ACTIVITY_START"
    private val BROADCAST_ACTIVITY_RESUME = "com.mikesun258.activitymonitor.ACTIVITY_RESUME"
    private val BROADCAST_ACTIVITY_PAUSE  = "com.mikesun258.activitymonitor.ACTIVITY_PAUSE"
    private val BROADCAST_ACTIVITY_STOP   = "com.mikesun258.activitymonitor.ACTIVITY_STOP"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        hookActivityLifecycle(lpparam)
    }

    private fun hookActivityLifecycle(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val instrumentation = lpparam.classLoader.loadClass("android.app.Instrumentation")

            XposedBridge.hookAllMethods(instrumentation, "callActivityOnCreate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendBroadcast(act, BROADCAST_ACTIVITY_CREATE, "onCreate")
                }
            })

            XposedBridge.hookAllMethods(instrumentation, "callActivityOnStart", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendBroadcast(act, BROADCAST_ACTIVITY_START, "onStart")
                }
            })

            XposedBridge.hookAllMethods(instrumentation, "callActivityOnResume", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendBroadcast(act, BROADCAST_ACTIVITY_RESUME, "onResume")
                }
            })

            XposedBridge.hookAllMethods(instrumentation, "callActivityOnPause", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendBroadcast(act, BROADCAST_ACTIVITY_PAUSE, "onPause")
                }
            })

            XposedBridge.hookAllMethods(instrumentation, "callActivityOnStop", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendBroadcast(act, BROADCAST_ACTIVITY_STOP, "onStop")
                }
            })

        } catch (e: Throwable) {
            Log.e(TAG, "Activity Hook Error", e)
        }
    }

    private fun sendBroadcast(activity: Activity, action: String, event: String) {
        val intent = Intent(action).apply {
            putExtra("pkg_name", activity.packageName)
            putExtra("act_name", activity.javaClass.name)
            putExtra("event_type", event)
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        }
        activity.sendBroadcast(intent)
    }
}

