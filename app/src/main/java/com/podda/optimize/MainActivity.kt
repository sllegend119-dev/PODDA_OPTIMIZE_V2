package com.podda.optimize

import android.app.Activity
import android.app.ActivityManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {

    private val password = "PODDA"
    private val license = "PODDA-CUST01-X7K9-2026"

    private lateinit var fpsView: TextView
    private lateinit var ramView: TextView
    private lateinit var batteryView: TextView
    private lateinit var displayView: TextView

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        window.statusBarColor = Color.rgb(5, 7, 16)
        window.navigationBarColor = Color.rgb(5, 7, 16)

        showLogin()
    }

    private fun showLogin() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 40, 28, 30)
            setBackgroundColor(Color.rgb(5, 7, 16))
        }

        root.addView(label("⚡ PODDA OPTIMIZE ⚡", 30f))
        root.addView(label("V2 • GAMING PERFORMANCE ENGINE", 13f))

        val input = EditText(this).apply {
            hint = "ENTER PASSWORD"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            inputType = 0x81
        }

        root.addView(input)

        root.addView(button("🔓 UNLOCK") {
            val value = input.text.toString()

            if (value == password || value == license) {
                showDashboard()
            } else {
                input.error = "Invalid password"
            }
        })

        root.addView(label("LICENSE: $license", 10f))

        setContentView(root)
    }

    private fun showDashboard() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 25, 20, 25)
            setBackgroundColor(Color.rgb(5, 7, 16))
        }

        root.addView(label("⚡ PODDA V2 ⚡", 29f))
        root.addView(label("ADVANCED PERFORMANCE DASHBOARD", 12f))

        root.addView(label("LIVE STATUS", 18f))

        fpsView = card("FPS MONITOR: READY")
        ramView = card("RAM: --")
        batteryView = card("BATTERY: --")
        displayView = card("DISPLAY: --")

        root.addView(fpsView)
        root.addView(ramView)
        root.addView(batteryView)
        root.addView(displayView)

        root.addView(label("FUNCTIONS", 18f))

        root.addView(button("🎮 GAME MODE") {
            toast("Game mode selected")
        })

        root.addView(button("⚡ PERFORMANCE") {
            toast("Performance profile selected")
        })

        root.addView(button("🎯 INPUT MONITOR") {
            toast("Input monitor enabled")
        })

        root.addView(button("📡 NETWORK MONITOR") {
            toast("Network monitor enabled")
        })

        root.addView(button("🌡 THERMAL MONITOR") {
            toast("Thermal monitor enabled")
        })

        root.addView(button("🖥 START OVERLAY") {
            startOverlay()
        })

        root.addView(button("⛔ STOP OVERLAY") {
            stopOverlay()
        })

        root.addView(button("🔄 REFRESH") {
            updateStats()
        })

        root.addView(label("DEVICE INFORMATION", 18f))

        root.addView(card(
            "MANUFACTURER: ${Build.MANUFACTURER}\n" +
            "MODEL: ${Build.MODEL}\n" +
            "ANDROID: ${Build.VERSION.RELEASE}\n" +
            "SDK: ${Build.VERSION.SDK_INT}\n" +
            "CPU CORES: ${Runtime.getRuntime().availableProcessors()}"
        ))

        scroll.addView(root)
        setContentView(scroll)

        updateStats()
    }

    private fun updateStats() {
        if (!::ramView.isInitialized) return

        val manager =
            getSystemService(ACTIVITY_SERVICE) as ActivityManager

        val info = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(info)

        val ramUsed =
            ((info.totalMem - info.availMem).toDouble() /
                    info.totalMem.toDouble()) * 100.0

        ramView.text = String.format(
            Locale.US,
            "RAM: %.0f%% USED",
            ramUsed
        )

        val battery =
            (getSystemService(BATTERY_SERVICE) as BatteryManager)
                .getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY
                )

        batteryView.text = "BATTERY: $battery%"

        val refresh =
            if (Build.VERSION.SDK_INT >= 30) {
                display?.refreshRate ?: 0f
            } else {
                0f
            }

        displayView.text = String.format(
            Locale.US,
            "DISPLAY: %.0f Hz",
            refresh
        )

        fpsView.text =
            String.format(Locale.US,
                "FPS MONITOR: DISPLAY %.0f Hz",
                refresh
            )

        handler.postDelayed(
            { updateStats() },
            1000
        )
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
            return
        }

        val intent = Intent(this, OverlayService::class.java)

        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        toast("Overlay started")
    }

    private fun stopOverlay() {
        stopService(Intent(this, OverlayService::class.java))
        toast("Overlay stopped")
    }

    private fun label(text: String, size: Float): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(8, 12, 8, 12)
        }
    }

    private fun card(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(20, 18, 20, 18)
            setBackgroundColor(Color.rgb(18, 23, 40))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 5, 0, 5)
            }
        }
    }

    private fun button(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 5, 0, 5)
            }
        }
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
