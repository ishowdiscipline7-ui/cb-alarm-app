package com.clearbefore.cb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.AlarmManager

class CBBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {
                CBAlarmScheduler.scheduleAll(context)
            }
        }
    }
}
