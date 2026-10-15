package com.clearbefore.cb

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class CBAlarmActivity : Activity() {
    private var alarmId: String = ""
    private var snoozeMinutes: Int = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyLockScreenFlags()
        renderAlarmScreen(intent)
    }

    override fun onResume() {
        super.onResume()
        activeActivity = this
    }

    override fun onPause() {
        if (activeActivity === this) activeActivity = null
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        renderAlarmScreen(intent)
    }

    private fun applyLockScreenFlags() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }

    private fun renderAlarmScreen(source: Intent?) {
        alarmId = source?.getStringExtra("alarmId") ?: ""
        val label = source?.getStringExtra("label") ?: "CB Exam Alarm"
        snoozeMinutes = source?.getIntExtra("snooze", 5) ?: 5
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(28), dp(32), dp(28), dp(32))
            setBackgroundColor(Color.BLACK)
        }
        val mark = text("CB", 18f, Color.BLACK, true).apply {
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding(dp(14), dp(10), dp(14), dp(10))
        }
        root.addView(mark, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        val eyebrow = text("ALARM RINGING", 13f, Color.LTGRAY, true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.16f
        }
        root.addView(eyebrow, marginParams(top = 34))
        val title = text(label, 32f, Color.WHITE, true).apply { gravity = Gravity.CENTER }
        root.addView(title, marginParams(top = 14))
        val subtitle = text("It's time to wake up. You've got this.", 16f, Color.LTGRAY, false).apply {
            gravity = Gravity.CENTER
        }
        root.addView(subtitle, marginParams(top = 10))
        val dismiss = Button(this).apply {
            text = "STOP ALARM"
            textSize = 17f
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.WHITE)
            setOnClickListener { finishAlarm("com.clearbefore.cb.STOP") }
        }
        root.addView(dismiss, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(58)).apply { topMargin = dp(38) })
        if (snoozeMinutes > 0) {
            val snooze = Button(this).apply {
                text = "SNOOZE  ·  $snoozeMinutes MIN"
                textSize = 16f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.rgb(35, 35, 35))
                setOnClickListener { finishAlarm("com.clearbefore.cb.SNOOZE") }
            }
            root.addView(snooze, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(58)).apply { topMargin = dp(12) })
        }
        setContentView(root)
    }

    private fun finishAlarm(action: String) {
        val actionIntent = Intent(this, CBAlarmReceiver::class.java)
            .setAction(action)
            .putExtra("alarmId", alarmId)
            .putExtra("snoozeMinutes", snoozeMinutes)
        sendBroadcast(actionIntent)
        finishAndRemoveTask()
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

    private fun marginParams(top: Int = 0): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = top
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private var activeActivity: CBAlarmActivity? = null

        fun finishIfOpen() {
            activeActivity?.runOnUiThread { activeActivity?.finishAndRemoveTask() }
        }
    }
}
