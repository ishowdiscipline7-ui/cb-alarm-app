package com.clearbefore.cb

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.json.JSONObject
import java.util.Calendar

object CBAlarmScheduler {
    const val PREFS = "cb_native_alarms"
    const val ALARMS_KEY = "alarms"
    const val CUSTOM_SOUND_PATH = "custom_sound_path"

    fun pending(context: Context, id: String): PendingIntent {
        val intent = Intent(context, CBAlarmReceiver::class.java).setAction("com.clearbefore.cb.FIRE").putExtra("alarmId", id)
        return PendingIntent.getBroadcast(context, id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun cancelAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(ALARMS_KEY, "[]") ?: "[]"
        try {
            val arr = org.json.JSONArray(raw)
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            for (i in 0 until arr.length()) {
                val a = arr.optJSONObject(i) ?: continue
                val id = a.optString("id")
                if (id.isNotBlank()) manager.cancel(pending(context, id))
            }
        } catch (_: Exception) { }
    }

    fun scheduleNext(context: Context, alarm: JSONObject) {
        val id = alarm.optString("id")
        val time = alarm.optString("time")
        if (id.isBlank() || !time.contains(":")) return
        val parts = time.split(":")
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return
        val days = alarm.optJSONArray("days")
        val now = Calendar.getInstance()
        for (offset in 0..7) {
            val candidate = Calendar.getInstance().apply {
                timeInMillis = now.timeInMillis
                add(Calendar.DAY_OF_YEAR, offset)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (candidate.timeInMillis <= now.timeInMillis) continue
            if (days != null && days.length() > 0) {
                var matches = false
                for (i in 0 until days.length()) {
                    if (days.optInt(i, -1) == candidate.get(Calendar.DAY_OF_WEEK) - 1) matches = true
                }
                if (!matches) continue
            } else if (offset > 0) continue
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = pending(context, id)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, candidate.timeInMillis, pi)
                else manager.setExact(AlarmManager.RTC_WAKEUP, candidate.timeInMillis, pi)
            } catch (_: SecurityException) {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, candidate.timeInMillis, pi)
            }
            return
        }
    }

    fun scheduleAll(context: Context) {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ALARMS_KEY, "[]") ?: "[]"
        try {
            val arr = org.json.JSONArray(raw)
            for (i in 0 until arr.length()) {
                val a = arr.optJSONObject(i) ?: continue
                if (a.optBoolean("enabled", false)) scheduleNext(context, a)
            }
        } catch (_: Exception) { }
    }
}
