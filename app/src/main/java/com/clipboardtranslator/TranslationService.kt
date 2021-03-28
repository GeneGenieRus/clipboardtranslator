package com.clipboardtranslator

import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.ClipboardManager.OnPrimaryClipChangedListener
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.clipboardtranslator.Constants.CHARSET_WIN_1251
import com.clipboardtranslator.Constants.DEFAULT_DICTIONARY_PATH
import com.google.common.base.Strings
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*

class TranslationService : Service(), OnPrimaryClipChangedListener {

    private var mClipboardManager: ClipboardManager? = null
    private var mPreviousText: String? = null
    private var mNotificationIdCounter = 1
    private var mTranslateInNotifications = false

    override fun onCreate() {
        super.onCreate()
        startForegroundService()

        mClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        mClipboardManager?.addPrimaryClipChangedListener(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        mTranslateInNotifications = intent.getBooleanExtra(Constants.PARAM_TRANSLATE_INTO_NOTIFICATIONS, false)
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        mClipboardManager?.removePrimaryClipChangedListener(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }



    override fun onPrimaryClipChanged() {
        val clipboardText = if (mClipboardManager?.primaryClip?.itemCount ?: 0 > 0)
            mClipboardManager?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()  else null

        if (Strings.isNullOrEmpty(clipboardText) || clipboardText == mPreviousText) {
            return
        }

        mPreviousText = clipboardText
        translateAndPublish(clipboardText)
    }

    private fun translateAndPublish(clipboardText: String?) {
        Handler(mainLooper).post {
            try {
                val reader = BufferedReader(InputStreamReader(
                        assets.open(DEFAULT_DICTIONARY_PATH), Charset.forName(CHARSET_WIN_1251)))
                while (reader.ready()) {
                    val line: String? = reader.readLine()
                    when {
                        line?.split("=")?.get(0) == clipboardText!!.toLowerCase(Locale.ROOT)  -> {
                            publishTranslated(line)
                            return@post
                        }
                    }
                }
                Toast.makeText(applicationContext, getString(R.string.toast_not_found), Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(applicationContext, getString(R.string.toast_error_message, e.localizedMessage),
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun publishTranslated(line: String) {
        when {
            mTranslateInNotifications -> publishNotification(line)
            else -> Toast.makeText(applicationContext, line.split("=")[1], Toast.LENGTH_SHORT).show()
        }
    }

    private fun publishNotification(line: String) {
        val builder = NotificationCompat.Builder(applicationContext, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(line.split("=")[0])
                .setContentText(line.split("=")[1])
                .setSmallIcon(R.drawable.ic_clipboard_translator_notification_name)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(mNotificationIdCounter++, builder.build())
    }

    private fun startForegroundService() {
        val notificationIntent = Intent(this, TranslationService::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.notif_foreground_title))
                .setSmallIcon(R.drawable.ic_clipboard_translator_notification_name)
                .setContentIntent(pendingIntent)
                .setTicker(getString(R.string.notif_foreground_title))
                .build()    //todo stop service by click notification
        startForeground(mNotificationIdCounter++, notification)
    }
}