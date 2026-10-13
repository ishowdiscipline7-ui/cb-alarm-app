package com.clearbefore.cb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import org.json.JSONArray

class CBAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.clearbefore.cb.STOP") {
            context.stopService(Intent(context, CBAlarmService::class.java))
            return
        }
        if (intent.action == "com.clearbefore.cb.SNOOZE") {
            context.stopService(Intent(context, CBAlarmService::class.java))
            val id = intent.getStringExtra("alarmId") ?: return
            val fire = Intent(context, CBAlarmReceiver::class.java).setAction("com.clearbefore.cb.FIRE").putExtra("alarmId", id)
            val pi = android.app.PendingIntent.getBroadcast(context, id.hashCode(), fire, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
            val manager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            manager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 60 * 1000L, pi)
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
                if (a.optJSONArray("days")?.length() ?: 0 == 0) a.put("enabled", false)
                else CBAlarmScheduler.scheduleNext(context, a)
                prefs.edit().putString(CBAlarmScheduler.ALARMS_KEY, alarms.toString()).apply()
                val service = Intent(context, CBAlarmService::class.java).putExtra("label", a.optString("label", "CB Alarm")).putExtra("alarmId", id).putExtra("snooze", a.optInt("snooze", 5))
                try { if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service) else context.startService(service) } catch (_: Exception) { }
                break
            }
        } catch (_: Exception) { }
    }
}
