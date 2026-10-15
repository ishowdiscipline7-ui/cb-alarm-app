package com.clearbefore.cb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CBAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.clearbefore.cb.STOP") {
            context.stopService(Intent(context, CBAlarmService::class.java))
            CBAlarmActivity.finishIfOpen()
            return
        }
        if (intent.action == "com.clearbefore.cb.SNOOZE") {
            val minutes = intent.getIntExtra("snoozeMinutes", 5)
            if (minutes <= 0) return
            context.stopService(Intent(context, CBAlarmService::class.java))
            CBAlarmActivity.finishIfOpen()
            val id = intent.getStringExtra("alarmId") ?: return
            val fire = Intent(context, CBAlarmReceiver::class.java).setAction("com.clearbefore.cb.FIRE").putExtra("alarmId", id)
            val pi = android.app.PendingIntent.getBroadcast(context, id.hashCode(), fire, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
            val manager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val triggerAt = System.currentTimeMillis() + minutes * 60 * 1000L
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    manager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAt, pi)
                } catch (_: SecurityException) {
                    manager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAt, pi)
                }
            } else {
                manager.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
            return
        }
        val id = intent.getStringExtra("alarmId") ?: return
        val prefs = context.getSharedPreferences(CBAlarmScheduler.PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(CBAlarmScheduler.ALARMS_KEY, "[]") ?: "[]"
        try {
            val alarms = JSONArray(raw)
            for (i in 0 until alarms.length()) {
                val a = alarms.optJSONObject(i) ?: continue
                if (a.optString("id") != id) continue
                if ((a.optJSONArray("days")?.length() ?: 0) == 0) {
                    val firedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    a.put("enabled", false)
                    a.put("lastFiredDate", firedDate)
                    a.put("nativeFiredDate", firedDate)
                } else CBAlarmScheduler.scheduleNext(context, a)
                prefs.edit().putString(CBAlarmScheduler.ALARMS_KEY, alarms.toString()).apply()
                val service = Intent(context, CBAlarmService::class.java).putExtra("label", a.optString("label", "CB Alarm")).putExtra("alarmId", id).putExtra("snooze", a.optInt("snooze", 5)).putExtra("sound", a.optString("sound", "gentle"))
                try { if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service) else context.startService(service) } catch (_: Exception) { }
                break
            }
        } catch (_: Exception) { }
    }
}
