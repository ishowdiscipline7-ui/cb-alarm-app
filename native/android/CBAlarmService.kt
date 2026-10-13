package com.clearbefore.cb

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder

class CBAlarmService : Service() {
    private var player: MediaPlayer? = null
    private val channelId = "cb_alarm_ring"
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "CB alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alarm ringing notifications"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val label = intent?.getStringExtra("label") ?: "Time to wake up"
        val id = intent?.getStringExtra("alarmId") ?: "cb-alarm"
        val stopIntent = Intent(this, CBAlarmReceiver::class.java).setAction("com.clearbefore.cb.STOP").putExtra("alarmId", id)
        val stopPi = PendingIntent.getBroadcast(this, id.hashCode() + 11, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val snoozeIntent = Intent(this, CBAlarmReceiver::class.java).setAction("com.clearbefore.cb.SNOOZE").putExtra("alarmId", id)
        val snoozePi = PendingIntent.getBroadcast(this, id.hashCode() + 12, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, channelId) else Notification.Builder(this)
        notification.setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(label).setContentText("CB Exam Alarm is ringing").setCategory(Notification.CATEGORY_ALARM).setVisibility(Notification.VISIBILITY_PUBLIC).setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", stopPi)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 5 min", snoozePi)
        startForeground(2001, notification.build())
        if (player == null) {
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                player = MediaPlayer().apply {
                    setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                    setDataSource(this@CBAlarmService, uri)
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (_: Exception) { }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        try { player?.stop() } catch (_: Exception) { }
        player?.release()
        player = null
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
