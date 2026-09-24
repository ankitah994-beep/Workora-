package com.workora.app

import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

class WorkoraApplication : Application() {

    // attachBaseContext runs BEFORE any ContentProvider (for example Firebase's auto-init provider)
    // is created and before Application.onCreate(), so the handler is in place as early as possible.
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // The crash-screen process (":crash") must not install the handler, otherwise a failure
        // there could start another crash screen forever.
        if (!CrashStore.isCrashProcess()) {
            CrashStore.installHandler(this)
        }
    }
}

/**
 * Saves, reads and clears the last crash report, and installs the global crash handler.
 * Used by WorkoraApplication, CrashActivity and MainActivity.
 */
object CrashStore {
    private const val PREFS_NAME: String = "workora_crash_prefs"
    private const val KEY_REPORT: String = "last_crash_report"
    private const val MAX_REPORT_CHARS: Int = 20000

    fun isCrashProcess(): Boolean {
        return currentProcessName().endsWith(":crash")
    }

    private fun currentProcessName(): String {
        return try {
            File("/proc/self/cmdline").readText().trim { it == '\u0000' || it.isWhitespace() }
        } catch (t: Throwable) {
            ""
        }
    }

    fun installHandler(context: Context) {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            var report: String? = null

            // 1) Build and save the report (never let this step throw).
            try {
                val built: String = buildReport(thread, throwable)
                report = built
                save(context, built)
            } catch (t: Throwable) {
                // Nothing else we can do here.
            }

            // 2) Show the report in CrashActivity, which runs in its own process (":crash").
            if (report != null) {
                try {
                    val intent = Intent(context, CrashActivity::class.java)
                    intent.putExtra(CrashActivity.EXTRA_REPORT, report)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                } catch (t: Throwable) {
                    // If this fails, the saved report is shown by MainActivity on the next launch.
                }
            }

            // 3) End the crashed process without the system "app keeps stopping" dialog.
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }

    fun buildReport(thread: Thread, throwable: Throwable): String {
        var root: Throwable = throwable
        var depth = 0
        while (root.cause != null && root.cause !== root && depth < 20) {
            root = root.cause ?: break
            depth++
        }

        val stackTrace = StringWriter()
        throwable.printStackTrace(PrintWriter(stackTrace))

        val time: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

        val report: String = buildString {
            append("Message: ").append(throwable.localizedMessage ?: "(no message)").append("\n")
            append("Type: ").append(throwable.javaClass.name).append("\n")
            if (root !== throwable) {
                append("Root cause: ").append(root.javaClass.name)
                    .append(": ").append(root.localizedMessage ?: "(no message)").append("\n")
            }
            append("Thread: ").append(thread.name).append("\n")
            append("Process: ").append(currentProcessName()).append("\n")
            append("Time: ").append(time).append("\n")
            append("Android: ").append(Build.VERSION.RELEASE)
                .append(" (API ").append(Build.VERSION.SDK_INT).append(")\n")
            append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n\n")
            append(stackTrace.toString())
        }
        return report.take(MAX_REPORT_CHARS)
    }

    fun save(context: Context, report: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_REPORT, report)
            .commit()
    }

    fun read(context: Context): String? {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_REPORT, null)
        } catch (t: Throwable) {
            null
        }
    }

    fun clear(context: Context) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_REPORT)
                .commit()
        } catch (t: Throwable) {
            // Nothing else to do.
        }
    }
}

/**
 * Shows the crash report on a plain white screen.
 *
 * It runs in its own process (android:process=":crash" in AndroidManifest.xml) and is built with
 * plain Android views only, so it still works when Compose, your theme, Firebase or the rest of
 * the app is what crashed.
 */
class CrashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val report: String = intent?.getStringExtra(EXTRA_REPORT)
            ?: CrashStore.read(this)
            ?: "No crash details were received."

        val density: Float = resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.WHITE)
        root.setPadding(dp(16), dp(48), dp(16), dp(24))

        val title = TextView(this)
        title.text = "Workora crashed"
        title.textSize = 22f
        title.typeface = Typeface.DEFAULT_BOLD
        title.setTextColor(Color.parseColor("#B00020"))
        root.addView(title)

        val hint = TextView(this)
        hint.text = "This is the error. Tap Copy or Share and send the whole text."
        hint.textSize = 14f
        hint.setTextColor(Color.DKGRAY)
        hint.setPadding(0, dp(4), 0, dp(12))
        root.addView(hint)

        val buttonRow = LinearLayout(this)
        buttonRow.orientation = LinearLayout.HORIZONTAL

        buttonRow.addView(
            makeButton("Copy") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Workora crash report", report))
                Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
            },
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        buttonRow.addView(
            makeButton("Share") {
                val sendIntent = Intent(Intent.ACTION_SEND)
                sendIntent.type = "text/plain"
                sendIntent.putExtra(Intent.EXTRA_TEXT, report)
                startActivity(Intent.createChooser(sendIntent, "Share crash report"))
            },
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        buttonRow.addView(
            makeButton("Close") {
                CrashStore.clear(this)
                finishAndRemoveTask()
            },
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        root.addView(buttonRow)

        val body = TextView(this)
        body.text = report
        body.textSize = 12f
        body.typeface = Typeface.MONOSPACE
        body.setTextColor(Color.BLACK)
        body.setTextIsSelectable(true)
        body.setPadding(0, dp(12), 0, 0)

        val scroll = ScrollView(this)
        scroll.addView(body)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        )

        setContentView(root)
    }

    private fun makeButton(label: String, onClick: () -> Unit): Button {
        val button = Button(this)
        button.text = label
        button.setOnClickListener { onClick() }
        return button
    }

    companion object {
        const val EXTRA_REPORT: String = "extra_crash_report"
    }
}
