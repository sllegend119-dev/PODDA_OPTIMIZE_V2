package com.podda.optimize

import android.app.Activity
import android.app.ActivityManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : Activity(), Choreographer.FrameCallback {

    private val password = "PODDA"
    private val license = "PODDA-CUST01-X7K9-2026"

    private lateinit var root: LinearLayout
    private lateinit var status: TextView
    private lateinit var fpsView: TextView

    private var frames = 0
    private var fps = 0.0
    private var windowStart = 0L
    private var lastFrame = 0L

    private val handler = Handler(Looper.getMainLooper())

    private var beforeFps = 0.0
    private var beforeRam = 0
    private var beforeBattery = 0

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        window.statusBarColor = 0xFF050710.toInt()
        window.navigationBarColor = 0xFF050710.toInt()

        showLogin()
    }

    private fun showLogin() {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.gravity = Gravity.CENTER
        box.setPadding(32, 50, 32, 32)
        box.setBackgroundColor(0xFF050710.toInt())

        val title = label("PODDA OPTIMIZE", 32f)
        val version = label("V2 • GAMING PERFORMANCE ENGINE", 13f)

        val pw = EditText(this)
        pw.hint = "PASSWORD"
        pw.inputType = 0x81
        styleInput(pw)

        val key = EditText(this)
        key.hint = "LICENSE KEY"
        styleInput(key)

        val login = button("UNLOCK ENGINE")

        val info = label(
            "SECURE LOCAL ACCESS\n\n" +
            "Password + License required",
            13f
        )

        box.addView(title)
        box.addView(version)
        space(box, 30)
        box.addView(pw)
        box.addView(key)
        space(box, 15)
        box.addView(login)
        space(box, 15)
        box.addView(info)

        setContentView(box)

        login.setOnClickListener {
            if (pw.text.toString() == password &&
                key.text.toString() == license
            ) {
                showDashboard()
            } else {
                Toast.makeText(
                    this,
                    "Invalid password or license",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showDashboard() {

        val scroll = ScrollView(this)

        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(18, 24, 18, 30)
        root.setBackgroundColor(0xFF050710.toInt())

        scroll.addView(root)
        setContentView(scroll)

        addTitle("PODDA OPTIMIZE V2")
        addSubtitle("ADVANCED GAMING PERFORMANCE CENTER")

        status = card("● ENGINE READY")

        section("DEVICE ANALYSIS")

        card(
            "DEVICE\n" +
            "${Build.MANUFACTURER} ${Build.MODEL}\n\n" +
            "ANDROID       ${Build.VERSION.RELEASE}\n" +
            "SDK            ${Build.VERSION.SDK_INT}\n" +
            "CPU CORES      ${Runtime.getRuntime().availableProcessors()}"
        )

        val ram = getRamPercent()
        val battery = getBattery()

        card(
            "INITIAL PERFORMANCE\n\n" +
            "RAM USAGE      $ram%\n" +
            "BATTERY        $battery%\n" +
            "DISPLAY        ${getRefreshRate().roundToInt()} Hz\n" +
            "THERMAL        ${thermalState()}"
        )

        section("PRE-RUN ANALYSIS")

        val table = card(
            "METRIC                 VALUE\n" +
            "──────────────────────────────\n" +
            "RAM                     $ram%\n" +
            "BATTERY                 $battery%\n" +
            "DISPLAY                 ${getRefreshRate().roundToInt()} Hz\n" +
            "THERMAL                 ${thermalState()}\n" +
            "CPU CORES               ${Runtime.getRuntime().availableProcessors()}"
        )

        val scan = button("RUN PERFORMANCE SCAN")

        scan.setOnClickListener {
            beforeRam = getRamPercent()
            beforeBattery = getBattery()

            status.text = "● PERFORMANCE SCAN COMPLETE"

            table.text =
                "PRE-RUN PERFORMANCE\n\n" +
                "RAM USAGE       $beforeRam%\n" +
                "BATTERY         $beforeBattery%\n" +
                "DISPLAY         ${getRefreshRate().roundToInt()} Hz\n" +
                "THERMAL         ${thermalState()}\n" +
                "CPU CORES       ${Runtime.getRuntime().availableProcessors()}"
        }

        section("GAMING PROFILE")

        val profile = Spinner(this)
        val profiles = arrayOf(
            "BALANCED GAMING",
            "PERFORMANCE",
            "LOW LATENCY",
            "BATTERY FRIENDLY"
        )

        profile.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            profiles
        )

        root.addView(profile)

        val run = button("⚡ RUN SELECTED PROFILE")

        run.setOnClickListener {
            status.text =
                "● PROFILE ACTIVE: ${profiles[profile.selectedItemPosition]}"

            Toast.makeText(
                this,
                "Profile applied for this app session",
                Toast.LENGTH_SHORT
            ).show()
        }

        section("FREE FIRE RECOMMENDATION")

        card(
            "DEVICE PROFILE\n\n" +
            "MODEL: ${Build.MANUFACTURER} ${Build.MODEL}\n\n" +
            "Recommended approach:\n" +
            "• Start with stable sensitivity\n" +
            "• Keep graphics consistent\n" +
            "• Monitor frame stability\n" +
            "• Adjust controls gradually\n\n" +
            "Note: recommendations do not modify\n" +
            "game files or bypass game security."
        )

        section("LIVE PERFORMANCE")

        fpsView = card("FPS: waiting for monitor…")

        val start = button("START LIVE MONITOR")
        val stop = button("STOP LIVE MONITOR")

        start.setOnClickListener {
            status.text = "● LIVE MONITOR ACTIVE"
            startFpsMonitor()
        }

        stop.setOnClickListener {
            status.text = "● MONITOR STOPPED"
            stopFpsMonitor()
        }

        section("RUN RESULT")

        val result = card(
            "BEFORE → AFTER\n\n" +
            "FPS             -- → --\n" +
            "RAM             -- → --\n" +
            "BATTERY         -- → --\n" +
            "DISPLAY         -- → --"
        )

        val compare = button("UPDATE RESULT")

        compare.setOnClickListener {
            result.text =
                "BEFORE → CURRENT\n\n" +
                String.format(
                    Locale.US,
                    "FPS             %.1f → %.1f\n",
                    beforeFps,
                    fps
                ) +
                "RAM             $beforeRam% → ${getRamPercent()}%\n" +
                "BATTERY         $beforeBattery% → ${getBattery()}%\n" +
                "DISPLAY         ${getRefreshRate().roundToInt()} Hz"
        }
    }

    private fun startFpsMonitor() {
        frames = 0
        fps = 0.0
        lastFrame = 0L
        windowStart = System.nanoTime()

        Choreographer.getInstance().removeFrameCallback(this)
        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun stopFpsMonitor() {
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(time: Long) {

        if (lastFrame != 0L) {
            frames++

            val elapsed =
                (time - windowStart) / 1_000_000_000.0

            if (elapsed >= 1.0) {

                fps = frames / elapsed

                if (beforeFps == 0.0) {
                    beforeFps = fps
                }

                frames = 0
                windowStart = time

                runOnUiThread {
                    fpsView.text =
                        String.format(
                            Locale.US,
                            "FPS: %.1f\nFRAME TIME: %.2f ms",
                            fps,
                            if (fps > 0) 1000.0 / fps else 0.0
                        )
                }
            }
        }

        lastFrame = time

        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun getRamPercent(): Int {

        val manager =
            getSystemService(ACTIVITY_SERVICE) as ActivityManager

        val info = ActivityManager.MemoryInfo()

        manager.getMemoryInfo(info)

        return (
            ((info.totalMem - info.availMem).toDouble() /
                info.totalMem.toDouble()) * 100
            ).roundToInt()
    }

    private fun getBattery(): Int {

        val bm =
            getSystemService(BATTERY_SERVICE) as BatteryManager

        return bm.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY
        )
    }

    private fun getRefreshRate(): Float {

        return if (Build.VERSION.SDK_INT >= 30) {
            display?.refreshRate ?: 60f
        } else {
            60f
        }
    }

    private fun thermalState(): String {

        if (Build.VERSION.SDK_INT >= 29) {

            val manager =
                getSystemService(POWER_SERVICE) as android.os.PowerManager

            return when (
                manager.currentThermalStatus
            ) {
                android.os.PowerManager.THERMAL_STATUS_NONE ->
                    "NORMAL"

                android.os.PowerManager.THERMAL_STATUS_LIGHT ->
                    "LIGHT"

                android.os.PowerManager.THERMAL_STATUS_MODERATE ->
                    "MODERATE"

                android.os.PowerManager.THERMAL_STATUS_SEVERE ->
                    "SEVERE"

                else ->
                    "CRITICAL"
            }
        }

        return "UNAVAILABLE"
    }

    private fun addTitle(text: String) {
        val t = label(text, 29f)
        t.gravity = Gravity.CENTER
        root.addView(t)
    }

    private fun addSubtitle(text: String) {
        val t = label(text, 12f)
        t.gravity = Gravity.CENTER
        root.addView(t)
    }

    private fun section(text: String) {
        val t = label(text, 17f)
        t.setPadding(4, 25, 4, 8)
        root.addView(t)
    }

    private fun card(text: String): TextView {

        val t = label(text, 15f)

        t.setPadding(18, 18, 18, 18)
        t.setBackgroundColor(0xFF111423.toInt())

        root.addView(
            t,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 5, 0, 5)
            }
        )

        return t
    }

    private fun label(text: String, size: Float): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(0xFFFFFFFF.toInt())
        }
    }

    private fun button(text: String): Button {

        return Button(this).apply {
            this.text = text
            setOnClickListener { }
        }
    }

    private fun styleInput(edit: EditText) {

        edit.setTextColor(0xFFFFFFFF.toInt())
        edit.setHintTextColor(0xFF888888.toInt())
        edit.setPadding(18, 14, 18, 14)

        rootPadding(edit)
    }

    private fun rootPadding(view: View) {
        view.layoutParams = LinearLayout.LayoutParams(
            -1,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 7, 0, 7)
        }
    }

    private fun space(box: LinearLayout, dp: Int) {

        box.addView(
            Space(this),
            LinearLayout.LayoutParams(1, dp)
        )
    }

    override fun onDestroy() {
        Choreographer.getInstance().removeFrameCallback(this)
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
