here// CrashActivity.kt
// NOTE: change the package line below if your project uses a different package name.
package com.workora.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

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
