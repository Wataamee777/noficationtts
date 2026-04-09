package me.wata.noficationtts

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val padding = (24 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val title = TextView(this).apply {
            text = "通知読み上げくん"
            textSize = 22f
            gravity = Gravity.CENTER
        }

        statusText = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
        }

        val openSettingsButton = Button(this).apply {
            text = "通知アクセス設定を開く"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        root.addView(title)
        root.addView(statusText)
        root.addView(openSettingsButton)

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        statusText.text = if (isNotificationListenerEnabled()) {
            "状態: 有効 (通知を読み上げます)"
        } else {
            "状態: 無効 (設定画面から有効化してください)"
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val cn = ComponentName(this, MyNotificationService::class.java)
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabled?.contains(cn.flattenToString()) == true
    }
}
