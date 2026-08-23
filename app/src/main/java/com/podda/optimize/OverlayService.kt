package com.podda.optimize

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import java.util.Locale

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlay: TextView

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()

        createChannel()

        val notification = Notification.Builder(
            this,
            "podda_overlay"
        )
            .setContentTitle("PODDA OPTIMIZE V2")
            .setContentText("Performance overlay is running")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()

        startForeground(1001, notification)

        windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        overlay = TextView(this).apply {
            text = "⚡ PODDA V2\nFPS: --\nRAM: --"
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(210, 5, 7, 16))
            setPadding(18, 12, 18, 12)
        }

        val type =
            if (Build.VERSION.SDK_INT >= 26)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 20
        params.y = 100

        windowManager.addView(overlay, params)

        update()
    }

    private fun update() {
        val manager =
            getSystemService(ACTIVITY_SERVICE)
                    as android.app.ActivityManager

        val info =
            android.app.ActivityManager.MemoryInfo()

        manager.getMemoryInfo(info)

        val ram =
            ((info.totalMem - info.availMem).toDouble() /
                    info.totalMem.toDouble()) * 100.0

        val refresh =
            if (Build.VERSION.SDK_INT >= 30) {
                display?.refreshRate ?: 0f
            } else {
                0f
            }

        overlay.text = String.format(
            Locale.US,
            "⚡ PODDA V2\nDISPLAY: %.0f Hz\nRAM: %.0f%%",
            refresh,
            ram
        )

        handler.postDelayed(
            { update() },
            1000
        )
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                "podda_overlay",
                "PODDA Overlay",
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(
                NotificationManager::class.java
            ).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)

        if (::overlay.isInitialized) {
            try {
                windowManager.removeView(overlay)
            } catch (_: Exception) {
            }
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
