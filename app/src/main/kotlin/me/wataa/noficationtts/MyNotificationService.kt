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
import java.util.Locale

class MyNotificationService : NotificationListenerService(), TextToSpeech.OnInitListener {
    companion object {
        const val CHANNEL_ID = "tts_foreground_channel"
        const val NOTIFICATION_ID = 1
    }

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
        startForeground(NOTIFICATION_ID, buildPersistentNotification(this))
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.language = Locale.JAPAN
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName || !isTtsReady) {
            return
        }

        val extras = sbn.notification.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        val speakText = listOf(title, text).filter { it.isNotBlank() }.joinToString("。")

        if (speakText.isNotBlank()) {
            tts?.speak(speakText, TextToSpeech.QUEUE_ADD, null, sbn.key)
        }
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}

fun buildPersistentNotification(context: Context): Notification {
    val openIntent = Intent(context, MainActivity::class.java)
    val pending = PendingIntent.getActivity(
        context,
        0,
        openIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
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
        val channel = NotificationChannel(
            MyNotificationService.CHANNEL_ID,
            "TTS Foreground Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "通知読み上げサービスの常駐通知"
            setSound(null, null)
            lockscreenVisibility = Notification.VISIBILITY_SECRET
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}
