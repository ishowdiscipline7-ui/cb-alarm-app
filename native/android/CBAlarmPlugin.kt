package com.clearbefore.cb

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Base64
import java.io.File
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.PermissionState
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback
import org.json.JSONArray
import org.json.JSONObject

@CapacitorPlugin(name = "CBAlarm", permissions = [Permission(alias = "notifications", strings = [Manifest.permission.POST_NOTIFICATIONS])])
class CBAlarmPlugin : Plugin() {
    @PluginMethod
    fun syncAlarms(call: PluginCall) {
        val alarms = call.getArray("alarms") ?: JSArray()
        val json = alarms.toString()
        val prefs = context.getSharedPreferences(CBAlarmScheduler.PREFS, Context.MODE_PRIVATE)
        val uploadedSoundData = call.getString("uploadedSoundData")
        if (!uploadedSoundData.isNullOrBlank()) {
            try {
                val encoded = uploadedSoundData.substringAfter(',', uploadedSoundData)
                val target = File(context.filesDir, "cb-alarm-custom-sound")
                target.writeBytes(Base64.decode(encoded, Base64.DEFAULT))
                prefs.edit().putString(CBAlarmScheduler.CUSTOM_SOUND_PATH, target.absolutePath).apply()
            } catch (_: Exception) { }
        } else {
            val oldPath = prefs.getString(CBAlarmScheduler.CUSTOM_SOUND_PATH, null)
            if (!oldPath.isNullOrBlank()) File(oldPath).delete()
            prefs.edit().remove(CBAlarmScheduler.CUSTOM_SOUND_PATH).apply()
        }
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
    fun getStoredAlarms(call: PluginCall) {
        val raw = context.getSharedPreferences(CBAlarmScheduler.PREFS, Context.MODE_PRIVATE)
            .getString(CBAlarmScheduler.ALARMS_KEY, "[]") ?: "[]"
        val alarms = try { JSONArray(raw) } catch (_: Exception) { JSONArray() }
        call.resolve(JSObject().put("alarms", alarms))
    }

    @PluginMethod
    fun requestAlarmPermissions(call: PluginCall) {
        if (Build.VERSION.SDK_INT >= 33 && getPermissionState("notifications") != PermissionState.GRANTED) {
            requestPermissionForAlias("notifications", call, "notificationPermissionsCallback")
            return
        }
        continueExactAlarmPermission(call)
    }

    @PermissionCallback
    private fun notificationPermissionsCallback(call: PluginCall) {
        continueExactAlarmPermission(call)
    }

    private fun continueExactAlarmPermission(call: PluginCall) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!manager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .setData(Uri.parse("package:" + context.packageName))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
                call.resolve(JSObject().put("exactAlarmPermission", false).put("openedSettings", true)
                    .put("notificationsGranted", Build.VERSION.SDK_INT < 33 || getPermissionState("notifications") == PermissionState.GRANTED))
                return
            }
        }
        call.resolve(JSObject().put("exactAlarmPermission", true)
            .put("notificationsGranted", Build.VERSION.SDK_INT < 33 || getPermissionState("notifications") == PermissionState.GRANTED))
    }

    @PluginMethod
    fun stopNativeAlarm(call: PluginCall) {
        context.stopService(Intent(context, CBAlarmService::class.java))
        call.resolve()
    }
}
