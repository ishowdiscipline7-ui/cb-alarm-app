package com.clearbefore.cb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CBBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) CBAlarmScheduler.scheduleAll(context)
    }
}
