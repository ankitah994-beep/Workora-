// WorkoraApplication.kt
// NOTE: change the package line below if your project uses a different package name.
package com.workora.app

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
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
