package com.clearbefore.cb

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import org.json.JSONArray
import org.json.JSONObject

@CapacitorPlugin(name = "CBAlarm")
class CBAlarmPlugin : Plugin() {
    @PluginMethod
    fun syncAlarms(call: PluginCall) {
        val alarms = call.getArray("alarms") ?: JSArray()
        val json = alarms.toString()
        val prefs = context.getSharedPreferences(CBAlarmScheduler.PREFS, Context.MODE_PRIVATE)
        CBAlarmScheduler.cancelAll(context)
        prefs.edit().putString(CBAlarmScheduler.ALARMS_KEY, json).apply()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val a = arr.optJSONObject(i) ?: continue
                if (a.optBoolean("enabled", false)) CBAlarmScheduler.scheduleNext(context, a)
            }
        } catch (_: Exception) { }
        call.resolve(JSObject().put("scheduled", alarms.length()))
    }

    @PluginMethod
    fun requestAlarmPermissions(call: PluginCall) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!manager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .setData(Uri.parse("package:" + context.packageName))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
                call.resolve(JSObject().put("exactAlarmPermission", false).put("openedSettings", true))
                return
            }
        }
        if (Build.VERSION.SDK_INT >= 33) {
            activity.requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 743)
        }
        call.resolve(JSObject().put("exactAlarmPermission", true))
    }

    @PluginMethod
    fun stopNativeAlarm(call: PluginCall) {
        context.stopService(Intent(context, CBAlarmService::class.java))
        call.resolve()
    }
}
