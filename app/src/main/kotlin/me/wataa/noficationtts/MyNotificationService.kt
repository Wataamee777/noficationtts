package me.wata.noficationtts

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.tts.TextToSpeech
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

class MyNotificationService : NotificationListenerService(), TextToSpeech.OnInitListener {
    companion object {
        const val CHANNEL_ID = "tts_foreground_channel"
        const val NOTIFICATION_ID = 1
    }

    private var tts: TextToSpeech? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
        startForeground(NOTIFICATION_ID, buildPersistentNotification(this))
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {}

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val text = sbn.notification?.extras?.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()
        if (!text.isNullOrBlank()) {
            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, sbn.key)
        }
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}

fun buildPersistentNotification(context: Context): Notification {
    val openIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    val pending = PendingIntent.getActivity(
        context, 0, openIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    return NotificationCompat.Builder(context, MyNotificationService.CHANNEL_ID)
        .setContentTitle("通知読み上げくん")
        .setContentText("通知内容を自動で読み上げます")
        .setSmallIcon(android.R.drawable.ic_lock_silent_mode)
        .setContentIntent(pending)
        .setOngoing(true)
        .build()
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "TTS Foreground Service"
        val desc = "通知読み上げサービスの常駐通知"
        val channel = NotificationChannel(
            MyNotificationService.CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_MIN
        )
        channel.description = desc
        channel.setSound(null, null)
        channel.lockscreenVisibility = android.app.Notification.VISIBILITY_SECRET
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}
